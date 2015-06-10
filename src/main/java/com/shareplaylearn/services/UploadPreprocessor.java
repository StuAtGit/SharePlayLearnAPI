package com.shareplaylearn.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stu on 6/10/15.
 */
public class UploadPreprocessor
    implements  UploadPreprocessorPlugin {

    List<UploadPreprocessorPlugin> uploadPreprocessorPluginList;
    private String preferredTag;

    public UploadPreprocessor( List<UploadPreprocessorPlugin> preprocessorPluginList ) {
        this.uploadPreprocessorPluginList = uploadPreprocessorPluginList;
        this.preferredTag = "original";
    }

    @Override
    public boolean canProcess(byte[] fileBuffer) {
        return true;
    }

    @Override
    public Map<String, byte[]> process(byte[] fileBuffer) {
        for( UploadPreprocessorPlugin p : this.uploadPreprocessorPluginList ) {
            if( p.canProcess(fileBuffer) ) {
                Map<String,byte[]> uploadList = p.process(fileBuffer);
                this.preferredTag = p.getPreferredTag();
                return uploadList;
            }
        }
        Map<String,byte[]> defaultList = new HashMap<>();
        defaultList.put(this.preferredTag, fileBuffer);
        return defaultList;
    }

    @Override
    public String getPreferredTag() {
        return this.preferredTag;
    }
}
