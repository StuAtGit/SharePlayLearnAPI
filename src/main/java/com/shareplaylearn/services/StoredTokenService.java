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

import com.shareplaylearn.resources.OAuth2Callback;

/**
 * Created by stu on 7/5/15.
 * TODO: the plan is for this to validate an ephermal token, validate it matches the identity given,
 *       and generate and store at token that will be permanently associated with the identity.
 *       Basically for caching tokens so we don't have to hit external providers.
 *       Also, could serve to enable test tokens as well.
 *
 *       We'll likely start with stormpath as a backend for this, (and hopefully leverage this code
 *       when we start associating devices with users). We do need to figure out how to properly scope
 *       these tokens (associate the given access token wih only one object or resource path)
 */
public class StoredTokenService {

    public StoredTokenService() {

    }

    public String createStoredToken( String userId, String userEmail, String userAccessToken ) {
        //TODO: add a validateToken call to OAuth2Callback that also verifies the ID matches the accessToken.
        if(OAuth2Callback.validateToken(userAccessToken).getStatus() == 200) {
            if( userAccessToken.startsWith("Bearer ") ) {
                userAccessToken = userAccessToken.split(" ")[1];
            }
            return userAccessToken;
        }
        return null;
    }

    public String getStoredToken( String userId, String userEmail, String userAccessToken ) {
        if(OAuth2Callback.validateToken(userAccessToken).getStatus() == 200) {
            if( userAccessToken.startsWith("Bearer ") ) {
                userAccessToken = userAccessToken.split(" ")[1];
            }
            return userAccessToken;
        }
        return null;
    }
}
