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
    public static String DISPLAY_NAME = "display_name";
    public static String TRUE_VALUE = "true";
    public static String FALSE_VALUE = "false";
    public static String CONTENT_TYPE = "type";
}