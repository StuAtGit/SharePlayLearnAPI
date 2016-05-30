/**
 * Copyright 2015-2016 Stuart Smith
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
    public static final String TEST_UPLOAD_PNG_FILE = "pctechsupportcat2.png";
    public static final String TEST_UPLOAD_PNG_RESIZED_FILE = "oliviaAtThePier.png";
    public static final String TEST_UPLOAD_FILE_PATH = "/home/stu/Projects/SharePlayLearnMaven/";
    public static final HashMap<String,String> testUploads;
    static {
        testUploads = new HashMap<>();
        testUploads.put(TEST_UPLOAD_TEXT_FILE, TEST_UPLOAD_FILE_PATH + TEST_UPLOAD_TEXT_FILE);
        testUploads.put(TEST_UPLOAD_JPG_FILE, TEST_UPLOAD_FILE_PATH + TEST_UPLOAD_JPG_FILE);
        testUploads.put(TEST_UPLOAD_PNG_FILE, TEST_UPLOAD_FILE_PATH + TEST_UPLOAD_PNG_FILE);
        testUploads.put(TEST_UPLOAD_PNG_RESIZED_FILE, TEST_UPLOAD_FILE_PATH + TEST_UPLOAD_PNG_RESIZED_FILE);
    }
}
