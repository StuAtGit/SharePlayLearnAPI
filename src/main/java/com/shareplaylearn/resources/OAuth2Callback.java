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

package com.shareplaylearn.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shareplaylearn.SharePlayLearnApi;
import com.shareplaylearn.services.SecretsService;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Web Service
 * Sample response from googles /people/me:
 * Queries google and repackages as a Jax-Rs response (and also processes any server errors)
 * Sample valid response entity from google:
         {
         "kind": "plus#person",
         "etag": "\"\"",
         "gender": "",
         "emails": [
         {
         "value": "[email]",
         "type": "account"
         }
         ],
         "urls": [
         {
         "value": "https://profiles.google.com/[seriously? buzz?]/buzz",
         "type": "contributor",
         "label": "Buzz"
         }
         ],
         "objectType": "person",
         "id": "[id]",
         "displayName": "[first Last]",
         "name": {
         "familyName": "",
         "givenName": ""
         },
         "url": "https://plus.google.com/[a number]",
         "image": {
         "url": "",
         "isDefault": [boolean]
         },
         "isPlusUser": [boolean],
         "verified": [boolean]
         }

 Sample Unauthorized response:
         { "error": { "errors": [ { "domain": "global", "reason": "authError", "message": "Invalid Credentials", "locationType": "header", "location": "Authorization" } ], "code": 401, "message": "Invalid Credentials" } }
 *
 * @author stu
 */
@Path("oauth2callback")
public class OAuth2Callback {

    @Context
    private UriInfo context;
    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String ID_TOKEN_FIELD = "id_token";
    private static final String TOKEN_EXPIRY_FIELD = "expires_in";

    /**
     * Creates a new instance of OAuth2Callback
     */
    public OAuth2Callback() {
    }

    public static Response validateToken( String accessToken ) {
        if( accessToken == null ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Access token was null.").build();
        }
        accessToken = accessToken.trim();
        //allow for access tokens passed directly from header into this method
        //(that still have the Bearer prefix)
        if( !accessToken.startsWith("Bearer ") ) {
            accessToken = "Bearer " + accessToken;
        }
        HttpGet tokenGet = new HttpGet("https://www.googleapis.com/plus/v1/people/me");
        tokenGet.addHeader("Authorization", accessToken);
        try( CloseableHttpResponse response = SharePlayLearnApi.httpClient.execute(tokenGet) ) {
            if( response.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode() ) {
                System.out.println( "Access token: " + accessToken + " failed: " + response.getStatusLine().getReasonPhrase() );
                String errorMessage = "";
                if( response.getEntity() != null ) {
                    errorMessage = EntityUtils.toString(response.getEntity());
                    System.out.println( errorMessage );
                }
                return Response.status(Response.Status.UNAUTHORIZED).entity(errorMessage).build();
            }
            return Response.status(Response.Status.OK).entity(response.getEntity()).build();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Sample returned URL:
     * http://www.shareplaylearn.com/SharePlayLearn2/api/oauth2callback?
     * state=insecure_test_token
     * &code=4/1Oxqgx2PRd8y4YxC7ByfJOLNiN-2.4hTyImQWEVMREnp6UAPFm0EEMmr5kAI
     * &authuser=0&num_sessions=1
     * &prompt=consent
     * &session_state=3dd372aa714b1b2313a838f8c4a4145b928da51f..8b83
     * @return an instance of java.lang.String
     */
    @GET
    public Response getJson( 
            @QueryParam("state") String clientState,
            @QueryParam("code") String authCode,
            @QueryParam("session_state") String sessionState 
    ) throws URISyntaxException 
    {
        HttpPost tokenPost = new HttpPost("https://accounts.google.com/o/oauth2/token");
        List<NameValuePair> authArgs = new ArrayList<NameValuePair>();
        authArgs.add( new BasicNameValuePair("code",authCode) );
        authArgs.add( new BasicNameValuePair("client_id", SecretsService.googleClientId) );
        authArgs.add( new BasicNameValuePair("client_secret", SecretsService.googleClientSecret) );
        authArgs.add( new BasicNameValuePair("redirect_uri",
                "https://www.shareplaylearn.com/api/oauth2callback") );
        authArgs.add( new BasicNameValuePair("grant_type","authorization_code") );
        UrlEncodedFormEntity tokenRequestEntity = new UrlEncodedFormEntity(authArgs, Consts.UTF_8);
        tokenPost.setEntity(tokenRequestEntity);
        
        try( CloseableHttpResponse response = SharePlayLearnApi.httpClient.execute(tokenPost) ) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if( statusCode != Response.Status.OK.getStatusCode() )
            {
                String statusReason = statusLine.getReasonPhrase();
                String loginStatus = "Login failed, google called us back with state token " + clientState;
                loginStatus += "and auth code " + authCode;
                loginStatus += ", but then we made a token request and got: " +
                        response.getStatusLine().getReasonPhrase() + "/" + response.getStatusLine().getStatusCode();
                ResponseBuilder responseBuilder = Response.serverError();
                responseBuilder.entity(loginStatus);
                responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
                return responseBuilder.build();
            }
            String authJson = EntityUtils.toString(response.getEntity());
            /**
             * What google returns (as of 01/05/2014)
             * https://developers.google.com/accounts/docs/OAuth2Login
             * { "access_token" : "ya29.lQAIu_8j0WfvQrOT3ZCExMddengITNLFoBsioB63QN1zNiLMvcQ7wslG",
             *   "token_type" : "Bearer",
             *   "expires_in" : 3597,
             *   "id_token" : "eyJhbGciOiJSUzI1NiIsImtpZCI6IjljNjMxNDFjMzAzNjkyY2E3Y2Q4MDAxZTUxNmNhNDVhZDdlNTJiZTIifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwODMxNjM0MzU1MjI2MzY0OTQwIiwiYXpwIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJzdHUyNmNvZGVAZ21haWwuY29tIiwiYXRfaGFzaCI6Im12WnMzaXgtR2NHSVVETThuTW9TaWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaWF0IjoxNDEyNTM3NjE0LCJleHAiOjE0MTI1NDE1MTR9.Zc6zsQPkj5an-1XFJLCdOvMDw_rDAqIe3YzA8g2DzhbGqBbd6XiqjxeRl3XDfC1aqp0Vx15fGn5R1e9RIh8Nmp6xWPvCHrA0c4eY8SaIazJ6FBQyK-n3k1sxQNJpuYhVtctAKsmlxFZbilwL2OTqIf0RDx0BpcIgmnk_7gupGxs"
             * }
             *
             * TODO: parse JWT for token and store in DB ?
             *       or just do it in JS? JS will need to confirm anyways..
             */
            JsonParser jsonParser = new JsonParser();
            JsonElement authInfo = jsonParser.parse(authJson);
            JsonObject authObject = authInfo.getAsJsonObject();
            String accessToken = authObject.get(ACCESS_TOKEN_FIELD).getAsString();
            String accessExpires = authObject.get(TOKEN_EXPIRY_FIELD).getAsString();
            String accessId = authObject.get(ID_TOKEN_FIELD).getAsString();
            String loggedInEndpoint = "https://www.shareplaylearn.com/#/login_callback?client_state=" + clientState
                    + "&" + ACCESS_TOKEN_FIELD + "=" + accessToken + "&" + TOKEN_EXPIRY_FIELD +"=" + accessExpires + ""
                    + "&" + ID_TOKEN_FIELD + "=" + accessId;
            /**
             * ID token is a jws signed object.. we should verify this in the servlet, since we can't verify the client state "secret"
             * Note that this secret is not the same secret used in the jws
             */
            ResponseBuilder responseBuilder = Response.seeOther(URI.create(loggedInEndpoint) );
            responseBuilder.entity(authJson);
            return responseBuilder.build();
        } catch( Exception e ) {
            ResponseBuilder responseBuilder = Response.serverError();
            responseBuilder.entity(e.getMessage());
            responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
            return responseBuilder.build();
        }
    }
}
