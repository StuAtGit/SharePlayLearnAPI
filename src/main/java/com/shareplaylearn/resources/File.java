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
import com.google.gson.GsonBuilder;
import com.shareplaylearn.InternalErrorException;
import com.shareplaylearn.models.ItemSchema;
import com.shareplaylearn.models.UserItemManager;
import com.shareplaylearn.utilities.Exceptions;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.InetAddress;

/**
 * Created by stu on 4/14/15.
 */
@Path(File.RESOURCE_BASE)
public class File {

    public static final String RESOURCE_BASE = "/file";

    @POST
    @Consumes( MediaType.APPLICATION_OCTET_STREAM )
    public Response postFile( )
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Not implemented yet").build();
    }

    /**
     * TODO: Once we have an async form, send access token in header, not in form
     * @param filestream
     * @param contentDisposition
     * @param filename
     * @param userId
     * @param accessToken
     * @return
     */
    @POST
    @Path("/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postFileForm( @FormDataParam("file") InputStream filestream,
                                  @FormDataParam("file") FormDataContentDisposition contentDisposition,
                                  //we'll let this be optional, derive from the contentDisposition for now
                                  //and add this where we want/need to
                                  @FormDataParam("filename") String filename,
                                  @FormDataParam("user_name") String userName,
                                  @FormDataParam("user_id") String userId,
                                  @FormDataParam("access_token") String accessToken,
                                  @HeaderParam("Content-Length") String contentLength )
    {
        try {
            //these still show up as null, despite annotations
            if (filestream == null || contentDisposition == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Content Disposition or file not supplied " + filestream + "," + contentDisposition).build();

            }
            if (filename == null || filename.trim().length() == 0) {
                filename = contentDisposition.getFileName();
                if( filename == null || filename.trim().length() == 0 ) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Filename not specified and not preseent in the content disposition").build();
                }
            }
            if (userId == null || userId.trim().length() == 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No user id given.").build();
            }
            if (accessToken == null || accessToken.trim().length() == 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No access token given.").build();
            }
            if(userName == null || userId.trim().length() == 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No user name given.").build();
            }
            Response tokenResponse = OAuth2Callback.validateToken(accessToken);
            if (tokenResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return tokenResponse;
            }

            byte[] fileBuffer = org.apache.commons.io.IOUtils.toByteArray(filestream);
            UserItemManager userItemManager = new UserItemManager( userName, userId );
            userItemManager.addItem( filename, fileBuffer );

            //this had something to do with making it work locally for testing
            //possibly when I did a SEE OTHER return code.
            //TODO: confirm that we can rip this out at some point
            String[] host = InetAddress.getLocalHost().toString().split("/");
            if( host[0].trim().length() == 0 ) {
                return Response.status(Response.Status.CREATED).entity(filename + " stored under user id " + userId + " " + InetAddress.getLocalHost()).build();
            } else {
                String hostname = host[0].trim().toLowerCase();
                if( !hostname.equals("shareplaylearn.com") && !hostname.equals("shareplaylearn.net") ) {
                    hostname = "localhost";
                }
                return Response.status(Response.Status.CREATED).
                        entity(this.uploadSuccessEntity(filename)).build();
            }
        }
        catch( InternalErrorException ie ) {
            String error = Exceptions.asString(ie);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
        catch( RuntimeException r )
        {
            String error = Exceptions.asString(r);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
        catch( Throwable t )
        {
            String error = Exceptions.asString(t);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    /**
     * Messy. But returning seeOther() instead of 201 + the entity below from uploadForm POST:
     *  (a) invalidates the login (at least in Firefox). Looks like it clears the session cache due to redirect.
     *  (b) isn't really "correct" return value.
     *  (c) the real fix is to async the upload form.
     * @return
     */
    private String uploadSuccessEntity( String filename ) {
        StringBuffer backPage = new StringBuffer();
        backPage.append("<html>\n<head>");
        backPage.append("<link rel=\"stylesheet\" href=\"../../css/style-master.css\" type=\"text/css\">");
        backPage.append("</head><body>");
        backPage.append("        <div class=\"header\">\n" +
                "            <ul>\n" +
                "                <li class=\"header-section\">\n" +
                "                    <a href=\"../../#/share\">Share</a>\n" +
                "                </li>\n" +
                "                <li class=\"header-section\">\n" +
                "                    <a href=\"../../#/play\">Play</a>\n" +
                "                </li>\n" +
                "                <li class=\"header-section\">\n" +
                "                    <a href=\"../../#\">Learn</a>\n" +
                "                </li>\n" +
                "                <li class=\"login\">\n" +
                "                    <a href=\"../../#/login\" id=\"login-control\">Login</a>\n" +
                "                    <a href=\"../../#/logout\" id=\"logout-control\" style=\"display:none;\">Logout</a>\n" +
                "                </li>\n" +
                "            </ul>\n" +
                "            \n" +
                "        </div>");
        backPage.append("<div id='wrap'><div class='app-content'>" + filename + " saved!</div></div>");
        backPage.append("</body></html>");
        return backPage.toString();
    }

    /**
     * Factored out this functionality so we can support access token in header, and in URL
     * @param userId
     * @param filename
     * @param access_token
     * @return
     */
    public Response getFileGeneric(String userName, String userId, String contentType,
                                   ItemSchema.PresentationType presentationType, String filename,
                                   String access_token, String encoding )
    {
        if( userId == null || userId.trim().length() == 0  ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No user id").build();
        }
        if( filename == null || filename.trim().length() == 0  ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No file id given").build();
        }
        UserItemManager userItemManager = new UserItemManager( userName, userId );
        if( access_token == null  || access_token.length() == 0 ) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized").build();
        }
        else
        {
            Response tokenResponse = OAuth2Callback.validateToken(access_token);
            if( tokenResponse.getStatus() == Response.Status.OK.getStatusCode() ) {
                return userItemManager.getItem( contentType, presentationType,
                        filename, encoding );
            }
            return tokenResponse;
        }
    }

    @GET
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    @Path("/{userName}/{userId}/{filetype}/{presentationType}/{filename}")
    public Response getFile( @PathParam("userName") String userName,
                             @PathParam("userId") String userId,
                             @PathParam("filetype") String filetype,
                             @PathParam("presentationType") String presentationTypeArg,
                             @PathParam("filename") String filename,
                             @HeaderParam("Authorization") String access_token,
                             //Not entirely happy with this - Accept-Encoding would be more correct
                             //but this is easier in the UI, in some circumstances,
                             //and I don't want to interfere with the UA requesting compression, etc.
                             @QueryParam("encode") String encode )
    {
        if( encode != null && encode.length() > 0 ) {
            encode = encode.toUpperCase();
        }
        ItemSchema.PresentationType presentationType;
        try {
            presentationType = ItemSchema.PresentationType.fromString(presentationTypeArg);
        } catch (IllegalArgumentException e ) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return getFileGeneric( userName, userId, filetype, presentationType, filename, access_token, encode );

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}/{userId}/filelist")
    public Response getFileList( @PathParam("userName") String userName,
                                 @PathParam("userId") String userId,
                                 @HeaderParam("Authorization") String authorization )
    {
        if( userId == null ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No user id provided??").build();
        }
        if( authorization == null ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No user id provided??").build();
        }
        Response authResponse = OAuth2Callback.validateToken(authorization);
        if( authResponse.getStatus() != Response.Status.OK.getStatusCode() ) {
            return authResponse;
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        UserItemManager userItemManager = new UserItemManager( userName, userId );
        return Response.status(Response.Status.OK).entity(gson.toJson(userItemManager.getItemList())).build();
    }

}
