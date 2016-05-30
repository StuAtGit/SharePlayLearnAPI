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
package com.shareplaylearn.services;

import com.shareplaylearn.models.ItemSchema;

import java.util.Map;

/**
 * Created by stu on 6/10/15.
 */
public interface UploadPreprocessorPlugin {
    boolean canProcess( byte[] fileBuffer );
    //this returns a map of the presentation type (from the ItemSchema)
    //to the actual bytes. The presentations types are used to indicate
    //various transforms done on the original data customized for how it will be presented
    //(as a preview, as the actual thing, but adjusted (like resizing, but not for previewing),
    //or just the original bytes
    Map<ItemSchema.PresentationType,byte[]> process(byte[] fileBuffer );
    //this returns the file extension to use with any preferred presentation type transformation,
    //returns an empty string if the preferred is the original (no transformation)
    String getPreferredFileExtension();
    //this is the ItemSchema content type, not an HTTP content type
    String getContentType();
}
