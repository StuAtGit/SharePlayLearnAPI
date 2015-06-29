package com.shareplaylearn.services;

import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;

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
    private UploadPreprocessorPlugin lastUsedProcessor;

    public UploadPreprocessor( List<UploadPreprocessorPlugin> preprocessorPluginList ) {
        this.uploadPreprocessorPluginList = preprocessorPluginList;
        this.preferredTag = "original";
        this.lastUsedProcessor = null;
    }

    @Override
    public boolean canProcess(byte[] fileBuffer) {
        return true;
    }

    public UploadPreprocessorPlugin getLastUsedProcessor() {
        return lastUsedProcessor;
    }

    @Override
    public Map<String, byte[]> process(byte[] fileBuffer) {
        for( UploadPreprocessorPlugin p : this.uploadPreprocessorPluginList ) {
            if( p.canProcess(fileBuffer) ) {
                Map<String,byte[]> uploadList = p.process(fileBuffer);
                this.preferredTag = p.getPreferredTag();
                this.lastUsedProcessor = p;
                return uploadList;
            }
        }
        Map<String,byte[]> defaultList = new HashMap<>();
        defaultList.put(this.preferredTag, fileBuffer);
        this.lastUsedProcessor = this;
        return defaultList;
    }

    @Override
    public String getPreferredTag() {
        return this.preferredTag;
    }
}
