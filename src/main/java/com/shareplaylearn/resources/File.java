package com.shareplaylearn.resources;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareplaylearn.InternalErrorException;
import com.shareplaylearn.models.FileListItem;
import com.shareplaylearn.models.Limits;
import com.shareplaylearn.models.UploadMetadataFields;
import com.shareplaylearn.models.UserItemSet;
import com.shareplaylearn.services.*;
import com.shareplaylearn.utilities.Exceptions;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.List;

/**
 * Created by stu on 4/14/15.
 */
@Path(File.RESOURCE_BASE)
public class File {

    private static final String MODAL_DIV_ID = "OpenImageModal";
    private static final String MODAL_IMAGE_CLASS = "modalImagePopup";
    //we store this in the display html to indicate the token should be replaced
    //when sent to the user.
    private static final String ACCESS_TOKEN_MARKER = "{{ACCESS_TOKEN}}";
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

            Response tokenResponse = OAuth2Callback.validateToken(accessToken);
            if (tokenResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return tokenResponse;
            }

            byte[] fileBuffer = org.apache.commons.io.IOUtils.toByteArray(filestream);
            UserItemSet userItemSet = new UserItemSet( userId );
            userItemSet.addItem( filename, fileBuffer );

            System.out.println("Get localhost: " + InetAddress.getLocalHost());
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

    private String amazonBoolean( boolean val ) {
        if( val ) {
            return UploadMetadataFields.TRUE_VALUE;
        } else {
            return UploadMetadataFields.FALSE_VALUE;
        }
    }

    //TODO: migrate all this preview templating into Angular front-end template
    //TODO: then migrate S3 item extraction into the UserItemSet code, and test
    private String generatePreviewHtml(String userId, String previewFilename
            , String filename, int previewHeight) {
        StringBuilder previewTag = new StringBuilder();
        //"/api/file/{{user_info.user_id}}/{{user_info.access_token}}/{{item.name}}"
        previewTag.append( this.generateImageLink(userId, previewFilename, "preview of " + filename,
                ImagePreprocessorPlugin.PREVIEW_WIDTH, previewHeight));
        /**
         * We're really starting to tightly couple the presentation with the back-end now.
         * A clean (and generic!!) way of pulling this out into the template would be good.
         * Maybe just links to original and preview, preferred, and build out this logic with ng-if
         * , if possible. Yeah..
         */
        previewTag.append("<div id='" + MODAL_DIV_ID + "_" + filename + "' class='" + MODAL_IMAGE_CLASS + "'>");
        previewTag.append("<a href=\"\" ng-click=\"toggleOpacity(item.onClick, 0)\" title=\"Close\" class=\"close\">X</a>");
        previewTag.append( this.generateImageLink(userId, filename, "Picture of " + filename, -1, -1));
        previewTag.append("</div>");
        return previewTag.toString();
    }

    private String generateImageLink(String userId, String previewFilename, String altText, int imageWidth, int imageHeight) {
        String imageLink = "<img src=/api/file/" + userId + "/" + ACCESS_TOKEN_MARKER + "/" + previewFilename + " alt=" +
                "\"" + altText + "\" ";
        if( imageHeight > 0 ) {
            imageLink += " width=\"" + imageWidth + "\" " +
                        " height=\"" + imageHeight + "\" border=0 />";
        } else {
            imageLink += " border=0 />";
        }
        return imageLink;
    }

    private Response getS3Object( AmazonS3Client s3Client, String itemPath ) {
        if( !itemPath.startsWith("/") ) {
            itemPath = "/" + itemPath;
        }
        S3Object object = s3Client.getObject(S3_BUCKET, itemPath);
        try( S3ObjectInputStream inputStream = object.getObjectContent() ) {
            long contentLength = object.getObjectMetadata().getContentLength();
            if(contentLength > Limits.MAX_RETRIEVE_SIZE)
            {
                throw new IOException("Object is to large: " + contentLength + " bytes.");
            }
            int bufferSize = Math.min((int)contentLength,10*8192);
            byte[] buffer = new byte[bufferSize];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int bytesRead = 0;
            int totalBytesRead = 0;
            while( (bytesRead = inputStream.read(buffer)) > 0 ) {
                outputStream.write(buffer,0,bytesRead);
                totalBytesRead += bytesRead;
            }
            System.out.println("GET in file resource read: " + totalBytesRead + " bytes.");
            return Response.status(Response.Status.OK).entity(outputStream.toByteArray()).build();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sw.toString()).build();
        }
    }

    /**
     * Factored out this functionality so we can support access token in header, and in URL
     * @param userId
     * @param filename
     * @param access_token
     * @return
     */
    public Response getFileGeneric( String userId, String filename, String access_token )
    {
        if( userId == null || userId.trim().length() == 0  ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No user id").build();
        }
        if( filename == null || filename.trim().length() == 0  ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No file id given").build();
        }
        String itemPath = "/" + userId + "/" + filename;
        /**
         * Actually, don't need this for now, as java Path annotation won't let anything go too screwy
         * Eventually, we should regex the filename, though to allow / hiearchies (perhaps)
         try {
         URI userBucketPath = new URI(null,null,filename,null);
         } catch (URISyntaxException e) {

         **/
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId, SecretsService.amazonClientSecret)
        );
        ObjectMetadata objectMetadata = s3Client.getObjectMetadata(S3_BUCKET, itemPath);
        if( access_token == null  || access_token.length() == 0 || access_token.equals("public")) {
            String isPublic = objectMetadata.getUserMetaDataOf(UploadMetadataFields.PUBLIC);
            /**
             * TODO: (note we have an untested change above to not add jpg if the filename already ends with it)
             *       (e) path should not be using access token.
             *                - equivalent lifetime token
             *                - token to share with specific users??? (might use a different approach then this for sharing)
             *                - perma-token for shareable, but not fully public, links.
             *       (e) access token should be invalidated upon logout, if possible.
             *       (f) we need a in-line div "pop-up" of items that we know how to display
             *          (like images or markdown or text)
             *       (f) layout of previews should be a responsive grid .. or not?
             *       (g) try to figure out how to work with angular's SCE stuff.
             */
            if( isPublic != null && isPublic.equals(UploadMetadataFields.TRUE_VALUE)) {
                try {
                    return getS3Object(s3Client, itemPath);
                } catch (Throwable t) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Amazon has failed us, for key: " +
                            filename + "\n" + Exceptions.asString(t)).build();
                }
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized " + isPublic).build();
            }
        }
        else
        {
            Response tokenResponse = OAuth2Callback.validateToken(access_token);
            if( tokenResponse.getStatus() == Response.Status.OK.getStatusCode() ) {
                return getS3Object(s3Client,itemPath);
            }
            return tokenResponse;
        }
    }

    @GET
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    @Path("/{userId}/{filename}")
    public Response getFile( @PathParam("userId") String userId,
                             @PathParam("filename") String filename,
                             @HeaderParam("Authorization") String access_token)
    {
        return getFileGeneric(userId, filename, access_token );
    }

    /***
     * Might want to yank this back later (don't really want to encourage ppl to share their access token!!)
     * OTOH, we might want to look into a limited scope token that just works for this. Handy for sharing,
     * and will make the links expire.
     *
     * TODO: Deprecate this with angular click() intercept in template
     *
     * Note: we flip the order of access_token & filename so the browser will save the file with the logical
     * name
     * @param userId
     * @param filename
     * @param access_token
     * @return
     */
    @GET
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    @Path("/{userId}/{access_token}/{filename}")
    public Response getFilePathAuthorization( @PathParam("userId") String userId,
                                              @PathParam("filename") String filename,
                                              @PathParam("access_token") String access_token)
    {
        return getFileGeneric(userId, filename, access_token );
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userId}/filelist")
    public Response getFileList( @PathParam("userId") String userId,
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
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId, SecretsService.amazonClientSecret)
        );
        long listObjectStart = System.currentTimeMillis();
        ObjectListing objectListing = s3Client.listObjects(S3_BUCKET, "/" + userId + "/");
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        List<FileListItem> objectNames = new ArrayList<>();
        List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        int maxCalls = 10;
        int numCalls = 0;
        while( objectListing.isTruncated() && numCalls < maxCalls ) {
            objectListing = s3Client.listNextBatchOfObjects(objectListing);
            for( S3ObjectSummary objectSummary : objectListing.getObjectSummaries() ) {
                objectSummaries.add(objectSummary);
            }
            numCalls++;
        }
        long listObjectTime = System.currentTimeMillis() - listObjectStart;
        System.out.println("List object time is: " + listObjectTime);
        int prefixLength = ("/" + userId + "/").length();
        StoredTokenService storedTokenService = new StoredTokenService();
        /**
         * Maybe we can stream this out??? Sloww.. get some perf logs in here.
         * This REST call is taking >6 sec. Since we're going to pull the display html out into the
         * presentation layer, check to see if it's the getObjectMetadata or the listObjects call
         * that is so slow.
         * Soo... list call is ~500 ms, and get metadata calls are about 100 ms. but we make about 30 of them.
         * We are going to try to move all this Display HTML business into the presentation layer (angular template)
         * logic, but, still, 1/2 sec is slooww for one call to get a list.
         * We might need to start looking at:
         * http://redis.io/topics/lru-cache
         */
        long getObjectMetadataTime = 0;
        int numGetObjectCalls = 0;
        for( S3ObjectSummary objectSummary : objectSummaries ) {
            long getObjectMetadataStart = System.currentTimeMillis();
            String displayHtml = s3Client.getObjectMetadata(S3_BUCKET, objectSummary.getKey())
                    .getUserMetaDataOf(UploadMetadataFields.DISPLAY_HTML);
            getObjectMetadataTime += (System.currentTimeMillis() - getObjectMetadataStart);
            numGetObjectCalls++;

            //previews don't have display html - the display html of the objects points at the preview.
            if( displayHtml == null || displayHtml.trim().length() == 0) {
                continue;
            }
            if( objectSummary.getKey().length() <= prefixLength ) {
                System.out.println( "Item in S3 that appears only be a user directory: " + objectSummary.getKey() );
                continue;
            }

            System.out.println("Display html of: " + objectSummary.getKey() + ": " + displayHtml);
            if( displayHtml != null && displayHtml.contains(ACCESS_TOKEN_MARKER) ) {
                    displayHtml = displayHtml.replace(ACCESS_TOKEN_MARKER,
                            storedTokenService.getStoredToken(userId, "", authorization));
            }
            FileListItem fileListItem = new FileListItem( objectSummary.getKey().substring(prefixLength), displayHtml );
            getObjectMetadataStart = System.currentTimeMillis();
            String hasOnClickVal = s3Client.getObjectMetadata(S3_BUCKET, objectSummary.getKey())
                    .getUserMetaDataOf(UploadMetadataFields.HAS_ON_CLICK);
            getObjectMetadataTime += System.currentTimeMillis() - getObjectMetadataStart;
            numGetObjectCalls++;

            boolean hasOnClick = hasOnClickVal != null &&  hasOnClickVal.equals(UploadMetadataFields.TRUE_VALUE);
            String onClick = "";
            if( hasOnClick ) {
                getObjectMetadataStart = System.currentTimeMillis();
                onClick = s3Client.getObjectMetadata(S3_BUCKET, objectSummary.getKey())
                        .getUserMetaDataOf(UploadMetadataFields.ON_CLICK);
                getObjectMetadataTime += System.currentTimeMillis() - getObjectMetadataStart;
                numGetObjectCalls++;
                fileListItem.setOnClick(onClick);
            }
            fileListItem.setHasOnClick(hasOnClick);
            objectNames.add( fileListItem );
        }
        System.out.println("Get object metadata time: " + getObjectMetadataTime);
        System.out.println("Number of calls: " + numGetObjectCalls + " avg: " + getObjectMetadataTime
                / (double)numGetObjectCalls);
        return Response.status(Response.Status.OK).entity(gson.toJson(objectNames)).build();
    }

}
