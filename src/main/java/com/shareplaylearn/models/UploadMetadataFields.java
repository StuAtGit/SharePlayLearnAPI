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
    public static String IS_PREVIEW = "is_preview";
    public static String IS_ORIGINAL = "is_original";
    public static String HAS_ORIGINAL = "has_original";
    public static String HAS_PREVIEW = "has_preview";
    public static String TRUE_VALUE = "true";
    public static String FALSE_VALUE = "false";
    public static String HAS_ON_CLICK = "has_on_click";
    public static String ON_CLICK = "on_click";
    public static String OBJECT_TYPE = "type";
}