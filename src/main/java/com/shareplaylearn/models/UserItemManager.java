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

    private String userName;
    private String userId;
    private String userDir;

    public UserItemManager(String userName, String userId) {
        this.userName = userName;
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

    public Response getItem( String type, String name ) {
        if( !name.startsWith("/") ) {
            name = "/" + name;
        }
        name = this.getUserDir() + type + name;
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId, SecretsService.amazonClientSecret)
        );
        try
        {
            S3Object object = s3Client.getObject(ItemSchema.S3_BUCKET, name);
            try( S3ObjectInputStream inputStream = object.getObjectContent() ) {
                long contentLength = object.getObjectMetadata().getContentLength();
                if (contentLength > Limits.MAX_RETRIEVE_SIZE) {
                    throw new IOException("Object is to large: " + contentLength + " bytes.");
                }
                int bufferSize = Math.min((int) contentLength, 10 * 8192);
                byte[] buffer = new byte[bufferSize];
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                int bytesRead = 0;
                int totalBytesRead = 0;
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                System.out.println("GET in file resource read: " + totalBytesRead + " bytes.");
                return Response.status(Response.Status.OK).entity(outputStream.toByteArray()).build();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("\nFailed to retrieve: " + name);
            e.printStackTrace(pw);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sw.toString()).build();
        }
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
     * @return
     */
    public List<UserItem> getItemList() {
        HashMap<String,HashSet<String>> itemLocations = getItemLocations();
        List<UserItem> itemList = new ArrayList<>();
        for( String location : itemLocations.get(ItemSchema.IMAGE_TYPE) ) {
            String[] path = location.split("/");
            String name = path[path.length-1];
            String previewPath = this.makeExternalLocation(
                    this.getItemLocation(name, ItemSchema.PREVIEW_IMAGE_TYPE) );
            UserItem userItem;
            if( itemLocations.get(ItemSchema.PREVIEW_IMAGE_TYPE).contains(previewPath) ) {
                userItem  = new UserItem( location, previewPath, null, ItemSchema.IMAGE_TYPE);
                userItem.addAttr("altText", "Preview of " + name);
                //for now, leave it to the browser to figure out height & width of preview..
                //(until we have a metadata store for that stuff)
            } else {
                userItem = new UserItem(location, null, null, ItemSchema.IMAGE_TYPE);
            }
            itemList.add(userItem);
        }
        for( String location : itemLocations.get(ItemSchema.UNKNOWN_TYPE) ) {
            UserItem userItem = new UserItem(location, null, null, ItemSchema.UNKNOWN_TYPE);
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
        ObjectListing unknownItemListing = s3Client.listObjects(ItemSchema.S3_BUCKET, unknownDir);

        itemLocations.put(ItemSchema.IMAGE_TYPE, getExternalItemListing(imageListing));
        itemLocations.put(ItemSchema.UNKNOWN_TYPE, getExternalItemListing(unknownItemListing));
        itemLocations.put(ItemSchema.PREVIEW_IMAGE_TYPE, getExternalItemListing(previewListing));
        return itemLocations;
    }

    private HashSet<String> getExternalItemListing( ObjectListing objectListing ) {
        HashSet<String> itemLocations = new HashSet<>();
        for( S3ObjectSummary obj : objectListing.getObjectSummaries() ) {
            String externalPath = makeExternalLocation(obj.getKey());
            if( externalPath != null ) {
                itemLocations.add(externalPath);
                System.out.println("External path was " + externalPath);
            } else {
                System.out.println("External path for object list was null?");
            }
        }
        return itemLocations;
    }
    /**
     * Translates an internal S3 path to the path used in the external API
     * @param internalPath
     * @return
     */
    private String makeExternalLocation( String internalPath ) {
        //"/root/" is not used in the external API, strip it off
        String[] itemPath = internalPath.split("/");
        if( itemPath.length > 2 ) {
            String externalPath = "";
            for (int i = 0; i < itemPath.length; ++i) {
                if( itemPath[i].equals("root")  && i < 2 ) {
                    continue;
                }
                if( itemPath[i].trim().length() == 0 ) {
                    continue;
                }
                externalPath += "/";
                externalPath += itemPath[i];
            }
            return externalPath;
        }
        return null;
    }

    public String getUserDir() {
        return ROOT_DIR + this.userName + "/" + this.userId + "/";
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
