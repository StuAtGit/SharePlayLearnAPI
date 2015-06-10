package com.shareplaylearn.models;

/**
 * Created by stu on 6/10/15.
 */
public class UploadMetadata {

    /**
     * This needs some work
     * Eventually we may need to support multiple previews,
     * and multiple "current" statuses - for presenting on
     * mobile & desktop devices, etc.
     */
    public boolean isPreview;
    public boolean isOriginal;
    public boolean hasPreview;
    public boolean hasOriginal;
    public String type;
    public String link;
    public String previewLink;
    public String originalLink;

    /**
     * This two are currently used to identify if there
     * is a particular upload entry that is preferred for use.
     * For example, when we resize, we'll prefer the entry with the
     * "resize" tag.
     */
    public String tag;
    public String preferredTag;


    public UploadMetadata(String link, String type) {
        this.link = link;
        this.type = type;
    }

    public UploadMetadata setIsPreview( boolean isPreview ) {
        this.isPreview = isPreview;
        return this;
    }

    public UploadMetadata setHasPreview( boolean hasPreview ) {
        this.hasPreview = hasPreview;
        return this;
    }

    public UploadMetadata setIsOriginal(boolean isOriginal) {
        this.isOriginal = isOriginal;
        return this;
    }

    public UploadMetadata setHasOriginal(boolean hasOriginal) {
        this.hasOriginal = hasOriginal;
        return this;
    }

    public UploadMetadata setType(String type) {
        this.type = type;
        return this;
    }

    public UploadMetadata setLink(String link) {
        this.link = link;
        return this;
    }

    public UploadMetadata setPreviewLink(String previewLink) {
        this.previewLink = previewLink;
        return this;
    }

    public UploadMetadata setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
        return this;
    }

    public UploadMetadata setTag( String tag ) {
        this.tag = tag;
        return this;
    }

    public UploadMetadata setPreferredTag( String tag ) {
        this.preferredTag = tag;
        return this;
    }
}