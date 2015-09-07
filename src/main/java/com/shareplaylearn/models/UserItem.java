package com.shareplaylearn.models;

import java.util.HashMap;

/**
 * Created by stu on 6/29/15.
 */
public class UserItem {

    private String itemLocation;
    private String previewLocation;
    private String originalLocation;
    private String type;
    private HashMap<String,String> attr;

    public UserItem(String itemLocation, String previewLocation, String originalLocation, String type) {
        this.previewLocation = previewLocation;
        this.originalLocation = originalLocation;
        this.itemLocation = itemLocation;
        this.type = type;
        this.attr = new HashMap<>();
    }

    public String getPreviewLocation() {
        return previewLocation;
    }

    public UserItem setPreviewLocation(String previewLocation) {
        this.previewLocation = previewLocation;
        return this;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public UserItem setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
        return this;
    }

    public String getType() {
        return type;
    }

    public UserItem setType(String type) {
        this.type = type;
        return this;
    }

    public UserItem addMeta( String key, String value ) {
        this.attr.put(key, value);
        return this;
    }

    public String getOriginalLocation() {
        return originalLocation;
    }

    public UserItem setOriginalLocation(String originalLocation) {
        this.originalLocation = originalLocation;
        return this;
    }
}
