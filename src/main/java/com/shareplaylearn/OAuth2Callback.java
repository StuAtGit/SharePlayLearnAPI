/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shareplaylearn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.POST;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
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

    private static final String CLIENT_ID = "726837865357-tqs20u6luqc9oav1bp3vb8ndgavjnrkf.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "PIjRUpf9JsmoBiugQPfQdfHo";
    
    /**
     * Creates a new instance of OAuth2Callback
     */
    public OAuth2Callback() {
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
            @QueryParam("state") String stateToken,
            @QueryParam("code") String authCode,
            @QueryParam("session_state") String sessionState 
    ) throws URISyntaxException 
    {
        HttpPost tokenPost = new HttpPost("https://accounts.google.com/o/oauth2/token");
        List<NameValuePair> authArgs = new ArrayList<NameValuePair>();
        authArgs.add( new BasicNameValuePair("code",authCode) );
        authArgs.add( new BasicNameValuePair("client_id",CLIENT_ID) );
        authArgs.add( new BasicNameValuePair("client_secret",CLIENT_SECRET) );
        authArgs.add( new BasicNameValuePair("redirect_uri",
                "http://www.shareplaylearn.com/SharePlayLearn2/api/oauth2callback") );
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
                    String loginStatus = "Login failed, google called us back with state token " + stateToken; 
                    loginStatus += "and auth code " + authCode;
                    loginStatus += ", but then we made a token request and got: " + 
                            response.getStatusLine().getReasonPhrase() + "/" + response.getStatusLine().getStatusCode();
                    ResponseBuilder responseBuilder = Response.serverError();
                    responseBuilder.entity(loginStatus);
                    responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
                    return responseBuilder.build();
                }
                String authJson = EntityUtils.toString(response.getEntity());
                //ResponseBuilder responseBuilder = Response.ok(authJson);
                String authTokenUri = "http://www.shareplaylearn.com/SharePlayLearn2/#/login_callback?session_state=" + sessionState;
                ResponseBuilder responseBuilder = Response.seeOther( URI.create(authTokenUri) );
                responseBuilder.entity(authJson);
                return responseBuilder.build();
            }
        } catch( Exception e ) {
            ResponseBuilder responseBuilder = Response.serverError();
            responseBuilder.entity(e.getMessage());
            responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
            return responseBuilder.build();
        }
        //return Response.seeOther( new URI("http://www.shareplaylearn.com/SharePlayLearn2/#/login_callback?state=" + stateToken + "&code=" + authCode + "&session_state=" + sessionState ) ).build();
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
        return "up!";
    }
}
