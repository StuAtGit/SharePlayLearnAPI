package com.shareplaylearn.models;

/**
 * Created by stu on 6/29/15.
 */
public class FileListItem {

    private String displayHtml;
    private String name;
    private String accessToken;
    private boolean hasOnClick;
    private String onClick;

    public FileListItem( String name, String displayHtml ) {
        this.name = name;
        this.displayHtml = displayHtml;
        this.accessToken = "";
        this.onClick = "";
    }

    public String getDisplayHtml() {
        return displayHtml;
    }

    public FileListItem setDisplayHtml(String displayHtml) {
        this.displayHtml = displayHtml;
        return this;
    }

    public String getName() {
        return name;
    }

    public FileListItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getAccessToken( ) {
        return this.accessToken;
    }

    public FileListItem setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getOnClick() {
        return onClick;
    }

    public FileListItem setOnClick(String onClick) {
        this.onClick = onClick;
        return this;
    }

    public boolean getHasOnClick() {
        return hasOnClick;
    }

    public FileListItem setHasOnClick(boolean hasOnClick) {
        this.hasOnClick = hasOnClick;
        return this;
    }
}
