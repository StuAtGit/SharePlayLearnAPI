package com.shareplaylearn.models;

/**
 * Created by stu on 6/10/15.
 */
public class UploadMetadataFields {
    /**
     * We should convert all of these to lower case, since
     * it looks like amazon does this anyways.
     * But prolly best to reset the metadata when we do.
     */
    public static final String PUBLIC = "public";
    public static String DISPLAY_HTML = "display_html";
    public static String IS_PREVIEW = "IS_PREVIEW";
    public static String IS_ORIGINAL = "IS_ORIGINAL";
    public static String HAS_ORIGINAL = "HAS_ORIGINAL";
    public static String HAS_PREVIEW = "HAS_PREVIEW";
    public static String TRUE_VALUE = "TRUE";
    public static String FALSE_VALUE = "FALSE";
    public static String HAS_ON_CLICK = "has_on_click";
    public static String ON_CLICK = "on_click";
}