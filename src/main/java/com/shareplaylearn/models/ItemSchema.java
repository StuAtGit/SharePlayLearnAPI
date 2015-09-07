package com.shareplaylearn.models;

import com.shareplaylearn.services.ImagePreprocessorPlugin;

/**
 * Created by stu on 9/7/15.
 */
public class ItemSchema {
    public static final String IMAGE_TYPE = "image";
    public static final String PREVIEW_IMAGE_TYPE = ImagePreprocessorPlugin.PREVIEW_TAG + "_" + IMAGE_TYPE;
    public static final String ORIGINAL_IMAGE_TYPE = ImagePreprocessorPlugin.ORIGINAL_TAG + "_" + IMAGE_TYPE;
    public static final String UNKNOWN_TYPE = "unknown";

    public static final String S3_BUCKET = "shareplaylearn";
}
