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
        byte[] decodedCredentials = Base64.decodeBase64(credentials.trim());
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
