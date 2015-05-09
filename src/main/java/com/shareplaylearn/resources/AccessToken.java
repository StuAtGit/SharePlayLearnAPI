package com.shareplaylearn.resources;

import com.amazonaws.services.ec2.model.StatusType;
import com.google.gson.Gson;
import com.shareplaylearn.services.SecretsService;
import com.shareplaylearn.utilities.Exceptions;
import com.shareplaylearn.utilities.OauthPasswordFlow;
import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * Created by stu on 5/9/15.
 */
@Path(AccessToken.RELATIVE_RESOURCE_PATH)
public class AccessToken {
    public static final String RELATIVE_RESOURCE_PATH = "access_token";
    @POST
    public Response createToken( @HeaderParam("Authorization") String credentials ) {
        if( credentials == null ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No credentials provided.").build();
        }
        //TODO: fuzz this to see what happens with bad strings
        byte[] decodedCredentials = Base64.decodeBase64(credentials);
        if( decodedCredentials == null ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid credentials provided.").build();
        }
        String credentialsString = new String( decodedCredentials, StandardCharsets.UTF_8 );
        String[] usernamePassword = credentialsString.split(":");
        if( usernamePassword.length != 2 ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid username:password format.").build();
        }
        try {
            OauthPasswordFlow.LoginInfo loginInfo = OauthPasswordFlow.googleLogin(usernamePassword[0],
                    usernamePassword[1],
                    SecretsService.googleClientId
                    , "https://www.shareplaylearn.com/api/oauth2callback");
            return Response.status(Response.Status.OK)
                    .entity((new Gson()).toJson(loginInfo))
                    .build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Exceptions.asString(e)).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Exceptions.asString(e)).build();
        } catch (OauthPasswordFlow.AuthorizationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Exceptions.asString(e)).build();
        } catch (OauthPasswordFlow.UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage()).build();
        }
    }
}
