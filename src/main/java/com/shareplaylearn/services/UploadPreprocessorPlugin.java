package com.shareplaylearn.services;

import java.util.Map;

/**
 * Created by stu on 6/10/15.
 */
public interface UploadPreprocessorPlugin {

    public static final String ORIGINAL_TAG = "original";
    public static final String PREVIEW_TAG = "preview";
    public abstract boolean canProcess( byte[] fileBuffer );
    public abstract Map<String,byte[]> process( byte[] fileBuffer );
    public abstract String getPreferredTag();
}
