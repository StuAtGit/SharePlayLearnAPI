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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by stu on 9/7/15.
 */
public class ItemSchema {
    /**
     * These really shouldn't much change over time (if at all)
     * And represent the context in which this instance of an item should be presented.
     * Something like "mobile" or "crappy_network :D" might be a good addition, though.
     * The Content Types will hopefully grow and grown! We'll see ;)
     */
    public enum PresentationType {
        PREVIEW_PRESENTATION_TYPE("preview"),
        ORIGINAL_PRESENTATION_TYPE("original"),
        PREFERRED_PRESENTATION_TYPE("preferred");
        private final String type;

        PresentationType(String type ) {
            this.type = type;
        }

        public String toString() {
            return this.type;
        }

        public static PresentationType fromString( String type ) {
            if( type.equals("preview") ) {
                return PREVIEW_PRESENTATION_TYPE;
            } else if( type.equals("original") ) {
                return ORIGINAL_PRESENTATION_TYPE;
            } else if( type.equals("preferred") ) {
                return PREFERRED_PRESENTATION_TYPE;
            } else {
                throw new IllegalArgumentException("Invalid presentation type: " + type);
            }
        }

    }

    public static final PresentationType[] PRESENTATION_TYPES = {
            PresentationType.PREVIEW_PRESENTATION_TYPE,
            PresentationType.ORIGINAL_PRESENTATION_TYPE,
            PresentationType.PREFERRED_PRESENTATION_TYPE
    };

    public static final String UNKNOWN_CONTENT_TYPE = "unknown";
    public static final String IMAGE_CONTENT_TYPE = "image";
    //while it would be nice to map these to RFC HTTP types
    //they wouldn't be as directory friendly (we could do it, but the directory structure might be a bit odd)
    //think about it though
    public static final String[] CONTENT_TYPES = {
            IMAGE_CONTENT_TYPE,
            UNKNOWN_CONTENT_TYPE
    };

    public static final String S3_BUCKET = "shareplaylearn";
}
