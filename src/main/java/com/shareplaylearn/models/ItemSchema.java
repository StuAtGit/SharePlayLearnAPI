package com.shareplaylearn.models;

/**
 * Created by stu on 9/7/15.
 */
public class ItemSchema {
    public static final String PREVIEW_PRESENTATION_TYPE = "preview";
    public static final String ORIGINAL_PRESENTATION_TYPE = "original";
    public static final String PREFERRED_PRESENTATION_TYPE = "preferred";
    public static final String[] PRESENTATION_TYPES = {
            PREVIEW_PRESENTATION_TYPE,
            ORIGINAL_PRESENTATION_TYPE,
            PREFERRED_PRESENTATION_TYPE
    };

    public static final String UNKNOWN_CONTENT_TYPE = "unknown";
    public static final String IMAGE_CONTENT_TYPE = "image";
    //while it would be nice to map these to RFC HTTP types
    //they wouldn't be as directory friendly (we could do it, but the directory structure might be a bit odd)
    //think about it though
    public static final String[] CONTENT_TYPES = {
            IMAGE_CONTENT_TYPE,
            UNKNOWN_CONTENT_TYPE
    };

    public static final String S3_BUCKET = "shareplaylearn";
}
