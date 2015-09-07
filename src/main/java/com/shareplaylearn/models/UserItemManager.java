package com.shareplaylearn.models;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.shareplaylearn.InternalErrorException;
import com.shareplaylearn.services.ImagePreprocessorPlugin;
import com.shareplaylearn.services.SecretsService;
import com.shareplaylearn.services.UploadPreprocessor;
import com.shareplaylearn.services.UploadPreprocessorPlugin;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

/**
 * Created by stu on 9/6/15.
 * Metadata about the items the user has, and possibly
 * cached values for the items.
 * Location, type, names, etc.
 * Data here should be safe to cache in Redis (userid but no auth tokens, etc)
 */
public class UserItemManager {
    private static final String ROOT_DIR = "/root/";

    private int totalItemQuota = Limits.DEFAULT_ITEM_QUOTA;
    private HashMap<String,Integer> itemQuota;

    private String userId;
    private String userDir;

    public UserItemManager(String userId) {
        this.userId = userId;
        this.userDir = this.getUserDir();
        this.itemQuota = new HashMap<>();
        this.itemQuota.put(ItemSchema.IMAGE_TYPE, Limits.DEFAULT_ITEM_QUOTA);
        this.itemQuota.put(ItemSchema.UNKNOWN_TYPE, Limits.DEFAULT_ITEM_QUOTA / 2);
    }

    public Response addItem( String name, byte[] item ) throws InternalErrorException {
        Response quotaCheck = this.checkQuota();
        if( quotaCheck.getStatus() != 200 ) {
            return quotaCheck;
        }

        List<UploadPreprocessorPlugin> uploadPreprocessorPlugins = new ArrayList<>();
        uploadPreprocessorPlugins.add(new ImagePreprocessorPlugin());
        UploadPreprocessor uploadPreprocessor = new UploadPreprocessor( uploadPreprocessorPlugins );
        Map<String,byte[]> uploads = uploadPreprocessor.process(item);

        if( uploads.size() == 0 ) {
            throw new InternalErrorException("Upload processor returned empty upload set");
        } else if( !uploads.containsKey(uploadPreprocessor.getPreferredTag()) ) {
            throw new InternalErrorException("Upload processor had no preferred tag! Not sure what to do.");
        }
        String type = ItemSchema.UNKNOWN_TYPE;

        //TODO: move this decision into the processors themselves ?
        //TODO: will likely become much more important as we add types
        if( uploadPreprocessor.getLastUsedProcessor() instanceof  ImagePreprocessorPlugin ) {
            type = ItemSchema.IMAGE_TYPE;
        }

        if(  uploads.containsKey(ImagePreprocessorPlugin.PREVIEW_TAG) ) {
            byte[] preview = uploads.get(ImagePreprocessorPlugin.PREVIEW_TAG);
            this.saveItem( name, preview, ItemSchema.PREVIEW_IMAGE_TYPE );
        }
        //the preferred tag could equal "original", but if it is not, we save off the original
        //somewhere else, and note that fact in the metadata
        boolean beenResized = false;
        if( !uploadPreprocessor.getPreferredTag().equals(ImagePreprocessorPlugin.ORIGINAL_TAG) ) {
            byte[] original = uploads.get(ImagePreprocessorPlugin.ORIGINAL_TAG);
            this.saveItem( name, original, ItemSchema.ORIGINAL_IMAGE_TYPE );
            beenResized = true;
        }

        //if we resized, then it's a jpg now.
        if( !name.endsWith(".jpg") && beenResized ) {
            name += ".jpg";
        }

        byte[] preferredUpload = uploads.get(uploadPreprocessor.getPreferredTag());
        this.saveItem(name, preferredUpload, type);
        return Response.status(200).build();
    }


    /**
     * Writes items to S3, and item metadata to Redis
     */
    private void saveItem( String name, byte[] itemData, String type ) throws InternalErrorException {

        String itemLocation = this.getItemLocation(name, type);
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId, SecretsService.amazonClientSecret)
        );
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(itemData);
        ObjectMetadata metadata = this.makeBasicMetadata(itemData.length, false);
        metadata.addUserMetadata(UploadMetadataFields.OBJECT_TYPE, type);
        //TODO: save this metadata, along with location, to local Redis
        s3Client.putObject(ItemSchema.S3_BUCKET, itemLocation, byteArrayInputStream, metadata);
    }

    private ObjectMetadata makeBasicMetadata( int bufferLength, boolean isPublic ) {
        ObjectMetadata fileMetadata = new ObjectMetadata();
        fileMetadata.setContentEncoding(MediaType.APPLICATION_OCTET_STREAM);
        if (isPublic) {
            fileMetadata.addUserMetadata(UploadMetadataFields.PUBLIC, UploadMetadataFields.TRUE_VALUE);
        } else {
            fileMetadata.addUserMetadata(UploadMetadataFields.PUBLIC, UploadMetadataFields.FALSE_VALUE);
        }
        fileMetadata.setContentLength(bufferLength);
        return fileMetadata;
    }

    /**
     * TODO: UNIT TEST THIS!! Then integrate with new template - getting closer! :D
     * @return
     */
    public List<UserItem> getItemList() {
        HashMap<String,HashSet<String>> itemLocations = getItemLocations();
        List<UserItem> itemList = new ArrayList<>();
        for( String location : itemLocations.get(ItemSchema.IMAGE_TYPE) ) {
            String[] path = location.split("/");
            String name = path[path.length-1];
            //Another approach would be to embed the preview path in some metadata associated with the
            //preferred itemLocation name. However, metadata lookups in S3 are slow (but we are looking at
            //redis), and this approach guarantees that the preview will exist (although we could just
            //add a check after looking up the metadata...). In other words, works for now, may change
            //once we get a redis local metadata cache up and going.
            String previewPath = this.getItemLocation(name, ItemSchema.PREVIEW_IMAGE_TYPE);
            UserItem userItem;
            if( itemLocations.get(ItemSchema.PREVIEW_IMAGE_TYPE).contains(previewPath) ) {
                userItem  = new UserItem( location, previewPath, null, ItemSchema.IMAGE_TYPE);
            } else {
                userItem = new UserItem(location, null, null, ItemSchema.IMAGE_TYPE);
            }
            itemList.add(userItem);
        }
        return itemList;
    }

    public HashMap<String,HashSet<String>> getItemLocations() {
        HashMap<String,HashSet<String>> itemLocations = new HashMap<>();

        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId, SecretsService.amazonClientSecret));
        String imageDir = this.getUserDir() + ItemSchema.IMAGE_TYPE;
        String previewDir = this.getUserDir() + ItemSchema.PREVIEW_IMAGE_TYPE;
        String originalDir = this.getUserDir() + ItemSchema.ORIGINAL_IMAGE_TYPE;
        String unknownDir = this.getUserDir() + ItemSchema.UNKNOWN_TYPE;

        ObjectListing imageListing = s3Client.listObjects(ItemSchema.S3_BUCKET, imageDir);
        ObjectListing previewListing = s3Client.listObjects(ItemSchema.S3_BUCKET, previewDir);

        HashSet<String> imageLocations = new HashSet<>();
        for( S3ObjectSummary obj : imageListing.getObjectSummaries() ) {
            imageLocations.add(obj.getKey());
        }
        itemLocations.put(ItemSchema.IMAGE_TYPE, imageLocations);

        HashSet<String> previewLocations = new HashSet<>();
        for( S3ObjectSummary obj : previewListing.getObjectSummaries() ) {
            previewLocations.add( obj.getKey() );
        }
        itemLocations.put(ItemSchema.PREVIEW_IMAGE_TYPE, previewLocations);
        return itemLocations;
    }

    public String getUserDir() {
        return ROOT_DIR + this.userId + "/";
    }

    public String getItemLocation( String name, String type ) {
        return this.userDir + type + "/" + name;
    }

    /**
     * This is not good enough. It slows things down, and still costs money.
     * Eventually, we should have an async task that updates a local cache of
     * used storage. If the cache says your below X of the limit (think atms),
     * you're good. Once you get up close, ping Amazon every time.
     * @param objectListing
     * @param maxSize
     * @return
     */
    private Response checkObjectListingSize( ObjectListing objectListing, int maxSize )
    {
        if( objectListing.isTruncated() && objectListing.getMaxKeys() >= maxSize ) {
            System.out.println("Error, too many uploads");
            return Response.status(418).entity("I'm a teapot! j/k - not enough space " + maxSize).build();
        }
        if( objectListing.getObjectSummaries().size() >= maxSize ) {
            System.out.println("Error, too many uploads");
            return Response.status(418).entity("I'm a teapot! Er, well, at least I can't hold " + maxSize + " stuff.").build();
        }
        return Response.status(Response.Status.OK).entity("OK").build();
    }

    private Response checkQuota() {
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId, SecretsService.amazonClientSecret));
        ObjectListing curList = s3Client.listObjects(ItemSchema.S3_BUCKET, this.getUserDir());
        Response listCheck;
        if ((listCheck = this.checkObjectListingSize(curList, Limits.MAX_NUM_FILES_PER_USER)).getStatus()
                != Response.Status.OK.getStatusCode()) {
            return listCheck;
        }
        ObjectListing userList = s3Client.listObjects(ItemSchema.S3_BUCKET, "/");
        if ((listCheck = this.checkObjectListingSize(userList, Limits.MAX_TOTAL_FILES)).getStatus()
                != Response.Status.OK.getStatusCode()) {
            return listCheck;
        }
        return Response.status(Response.Status.OK).build();
    }
}
