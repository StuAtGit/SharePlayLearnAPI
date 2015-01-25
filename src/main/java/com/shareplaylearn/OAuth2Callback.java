/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shareplaylearn;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * REST Web Service
 *
 * @author stu
 */
@Path("oauth2callback")
public class OAuth2Callback {

    @Context
    private UriInfo context;

    private String clientId;
    private String clientSecret;
    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String ID_TOKEN_FIELD = "id_token";
    private static final String TOKEN_EXPIRY_FIELD = "expires_in";

    /**
     * Creates a new instance of OAuth2Callback
     */
    public OAuth2Callback() {
        java.nio.file.Path secretsFile = FileSystems.getDefault().getPath("/etc/shareplaylearn.secrets");
        try {
            List<String> lines = Files.readAllLines(secretsFile, StandardCharsets.UTF_8);
            for( String line : lines ) {
                if( line.startsWith("GoogleId") ) {
                    clientId = this.getConfigValue(line);
                }
                if( line.startsWith("GoogleSecret") ) {
                    clientSecret = this.getConfigValue(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Warning, failed to read secrets file, will not be able to login users or access stored data." + e.getMessage());
        }
    }

    public String getConfigValue( String line )
    {
        String[] kv = line.split("=");
        if( kv.length != 2 ) {
            System.err.println("Warning: badly formatted line: " + line );
            return "";
        }
        return kv[1];
    }

    /**
     * Retrieves representation of an instance of net.shareplaylearn2.OAuth2Callback
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
        authArgs.add( new BasicNameValuePair("client_id", clientId) );
        authArgs.add( new BasicNameValuePair("client_secret", clientSecret) );
        authArgs.add( new BasicNameValuePair("redirect_uri",
                "https://www.shareplaylearn.com/api/oauth2callback") );
        authArgs.add( new BasicNameValuePair("grant_type","authorization_code") );
        UrlEncodedFormEntity tokenRequestEntity = new UrlEncodedFormEntity(authArgs, Consts.UTF_8);
        tokenPost.setEntity(tokenRequestEntity);
        
        try( CloseableHttpClient httpClient = HttpClients.createDefault() ) {
            try( CloseableHttpResponse response = httpClient.execute(tokenPost) ) {
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
            }
        } catch( Exception e ) {
            ResponseBuilder responseBuilder = Response.serverError();
            responseBuilder.entity(e.getMessage());
            responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
            return responseBuilder.build();
        }
    }

    /**
     * PUT method for updating or creating an instance of OAuth2Callback
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }
    
    @POST
    @Consumes( {MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN} )
    public Response postCredentials(String content) 
    {
        return Response.ok().build();
    }
    
    @GET
    @Path("/status")
    public String status()
    {
        return "up! version 0.1";
    }
}
