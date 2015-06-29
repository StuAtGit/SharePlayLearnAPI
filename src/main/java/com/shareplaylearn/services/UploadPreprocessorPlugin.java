package com.shareplaylearn.services;

import java.util.Map;

/**
 * Created by stu on 6/10/15.
 */
public interface UploadPreprocessorPlugin {

    String ORIGINAL_TAG = "original";
    String PREVIEW_TAG = "preview";
    boolean canProcess( byte[] fileBuffer );
    Map<String,byte[]> process( byte[] fileBuffer );
    String getPreferredTag();
}
