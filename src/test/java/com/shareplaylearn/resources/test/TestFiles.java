package com.shareplaylearn.resources.test;

import java.util.HashMap;

/**
 * Created by stu on 9/13/15.
 */
public class TestFiles {
    public static final String TEST_UPLOAD_TEXT_FILE = "TestUpload.txt";
    //private static final String TEST_UPLOAD_JPG_FILE = "Disneyland.jpg";
    //might just want to *.jpg fill this out, so we can have all the things!! :D
    public static final String TEST_UPLOAD_JPG_FILE = "pctechsupportcat.jpg";
    public static final String TEST_UPLOAD_FILE_PATH = "/home/stu/Projects/SharePlayLearnMaven/";
    public static final HashMap<String,String> testUploads;
    static {
        testUploads = new HashMap<>();
        testUploads.put(TEST_UPLOAD_TEXT_FILE, TEST_UPLOAD_FILE_PATH + TEST_UPLOAD_TEXT_FILE);
        testUploads.put(TEST_UPLOAD_JPG_FILE, TEST_UPLOAD_FILE_PATH + TEST_UPLOAD_JPG_FILE);
    }
}
