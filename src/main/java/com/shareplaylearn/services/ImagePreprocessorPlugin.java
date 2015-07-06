package com.shareplaylearn.services;

import com.shareplaylearn.utilities.Exceptions;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stu on 6/10/15.
 */
public class ImagePreprocessorPlugin
    implements UploadPreprocessorPlugin {

    private byte[] imageBuffer;
    public static final int PREVIEW_WIDTH = 200;
    public static final int RESIZE_LIMIT = 1024;
    public static final String RESIZED_TAG = "resized";
    private String preferredTag;
    //set when we calculate a preview
    private int lastPreviewHeight;
    //always set when we adjust the height,
    //just used for the methods to talk internally
    private int lastHeight;

    public ImagePreprocessorPlugin() {
        this.imageBuffer = null;
        this.lastPreviewHeight = -1;
        this.lastHeight = -1;
        this.preferredTag = ORIGINAL_TAG;
    }

    @Override
    public String getPreferredTag() {
        return this.preferredTag;
    }

    @Override
    public boolean canProcess(byte[] fileBuffer) {
        //this is a simple, but possibly slow method
        //first - to detect the file, it just checks if we have any readers for it
        //next - ImageIO.getScaledInstance is supposed to be a bit slow (but this info may be outdated?)
        //https://stackoverflow.com/questions/4220612/scaling-images-with-java-jai
        //https://github.com/thebuzzmedia/imgscalr/blob/master/src/main/java/org/imgscalr/Scalr.java
        try {
            return ImageIO.read(toImageInputStream(fileBuffer)) != null;
        } catch (IOException e) {
            System.out.println("Cannot processing filebuffer in Image Plugin because of exception " + e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, byte[]> process(byte[] fileBuffer) {
        HashMap<String,byte[]> uploadList = new HashMap<>();

        uploadList.put(ORIGINAL_TAG, fileBuffer);

        int originalWidth = -1;
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(this.toImageInputStream(fileBuffer));
            originalWidth = bufferedImage.getWidth();
            byte[] previewBuffer = shrinkImageToWidth(bufferedImage, PREVIEW_WIDTH);
            uploadList.put(PREVIEW_TAG, previewBuffer);
            this.lastPreviewHeight = this.lastHeight;
        } catch( IOException e ) {
            System.out.println(Exceptions.asString(e));
        }

        if( bufferedImage != null && originalWidth > 0
                && originalWidth > RESIZE_LIMIT ) {
            try {
                byte[] modifiedBuffer = shrinkImageToWidth(bufferedImage, RESIZE_LIMIT);
                uploadList.put(RESIZED_TAG,modifiedBuffer);
                this.preferredTag = RESIZED_TAG;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uploadList;
    }

    private ImageInputStream toImageInputStream( byte[] fileBuffer ) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBuffer);
        try {
            return ImageIO.createImageInputStream(byteArrayInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] shrinkImageToWidth( BufferedImage bufferedImage, int targetWidth) throws IOException {
        double previewRatio = (double)targetWidth / (double)bufferedImage.getWidth();
        System.out.println("preview ratio: " + previewRatio);
        System.out.println("Target width: " + targetWidth);
        System.out.println("Original height: " + bufferedImage.getHeight());
        System.out.println("original width: " + bufferedImage.getWidth());
        int previewHeight = (int)(previewRatio*bufferedImage.getHeight());
        this.lastHeight = previewHeight;
        System.out.println("preview height: " + previewHeight);
        Image scaledImage = bufferedImage.getScaledInstance(targetWidth, previewHeight, BufferedImage.SCALE_SMOOTH);
        BufferedImage preview = new BufferedImage(targetWidth, previewHeight, BufferedImage.TYPE_INT_RGB);
        preview.createGraphics().drawImage(scaledImage, 0, 0, null);
        ByteArrayOutputStream previewOutputStream = new ByteArrayOutputStream();
        ImageIO.write(preview, "jpg", previewOutputStream);
        return previewOutputStream.toByteArray();
    }

    public int getLastPreviewHeight() {
        System.out.println("Returning a preview height of " + this.lastPreviewHeight);
        return this.lastPreviewHeight;
    }

 /*   private void processImageUpload( byte[] fileBuffer, BufferedImage bufferedImage, String filename,
                                     String userId, ObjectMetadata fileMetadata,
                                     AmazonS3Client s3Client ) throws IOException {
        ObjectMetadata previewMetadata = new ObjectMetadata();
        previewMetadata.addUserMetadata("IsPreview", "true");
        previewMetadata.addUserMetadata("IsPreviewOf", filename);
        String publicField = fileMetadata.getUserMetaDataOf(FileMetadata.PUBLIC_FIELD);
        if( publicField != null ) {
            previewMetadata.addUserMetadata(FileMetadata.PUBLIC_FIELD, fileMetadata.getUserMetaDataOf(FileMetadata.PUBLIC_FIELD));
        } else {
            System.out.println("Public field was null?");
        }
        previewMetadata.setContentEncoding(fileMetadata.getContentEncoding());
        previewMetadata.setContentLength(previewContentLength.get());
        String previewKey = "/" + userId + "/" + "Preview-" + filename;
        s3Client.putObject(S3_BUCKET, previewKey, previewInputStream, previewMetadata);
        previewInputStream.close();

        *//**
         * TODO: pull this out into an ImageProcessing class (factor out keys like "HasOriginal" while we're at it).
         * (perhaps even an tryFile() method that returns an Image if it is one, and null otherwise)
         * Also, have it create a FileList full of FileListEntries (see below).
         * We'll need to wipe the S3 repo to rebuild it - since we're doing that, might as well create
         * /image /[ type ] subcategory handling code, and start with that.
         *
         *THEN we can update the ShareMyStuff template to process the new itemlist entries, and
         *     create previews w/ popup links, etc.
         *
         *NEXT - NOTES! Markdown! HTML! Might be a nice way to transition to the Learn page
         *       (by creating tools for tutorial creation)
         *//*
        if( width > resizeWidth ) {
            String originalKey = "/" + userId + "/" + "Original-" + filename;
            AtomicInteger resizedContentLenth = new AtomicInteger(0);
            ByteArrayInputStream resizedInputStream = shrinkImageToWidth(bufferedImage, resizeWidth, resizedContentLenth);
            ObjectMetadata resizedMetadata = new ObjectMetadata();
            resizedMetadata.addUserMetadata("HasOriginal", "true");
            resizedMetadata.addUserMetadata("HasPreview", "true");
            resizedMetadata.addUserMetadata("PreviewKey", previewKey);
            resizedMetadata.addUserMetadata("OriginalKey",originalKey );
            resizedMetadata.addUserMetadata(FileMetadata.PUBLIC_FIELD, fileMetadata.getUserMetaDataOf(FileMetadata.PUBLIC_FIELD));
            resizedMetadata.setContentEncoding(fileMetadata.getContentEncoding());
            resizedMetadata.setContentLength(resizedContentLenth.get());
            s3Client.putObject(S3_BUCKET, "/" + userId + "/" + filename, resizedInputStream, resizedMetadata);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBuffer);
            fileMetadata.addUserMetadata("IsOriginal", "true");
            s3Client.putObject(S3_BUCKET, originalKey, byteArrayInputStream, fileMetadata);
        } else {
            //basically, we just redeclare so the stream is reset
            fileMetadata.addUserMetadata("HasOriginal", "false");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileBuffer);
            s3Client.putObject(S3_BUCKET, "/" + userId + "/" + filename, byteArrayInputStream, fileMetadata);
        }
    }
*/
}
