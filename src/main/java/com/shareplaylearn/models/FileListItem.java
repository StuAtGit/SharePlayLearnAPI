package com.shareplaylearn.models;

/**
 * Created by stu on 6/29/15.
 */
public class FileListItem {

    private String displayHtml;
    private String name;

    public FileListItem( String name, String displayHtml ) {
        this.name = name;
        this.displayHtml = displayHtml;
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
}
