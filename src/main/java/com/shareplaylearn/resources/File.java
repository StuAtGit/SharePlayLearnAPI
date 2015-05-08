package com.shareplaylearn.resources;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.google.gson.Gson;
import com.shareplaylearn.services.SecretsService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by stu on 4/14/15.
 */
@Path(File.RESOURCE_BASE)
public class File {

    private static final String S3_BUCKET = "shareplaylearn";
    public static final String RESOURCE_BASE = "/file";
    /**
     * With 2MB default upload limit on tomcat, this comes to about:
     * 1000*2MB ~ 2GB of data stored at any given time.
     * 1 req/sec ~ 3x10^6 requests a month.
     * At 10 GB / Month out, and 1 TB / Month in (kyup limits it to 1 TB, so that should throttle that).
     * https://calculator.s3.amazonaws.com/index.html says we should be spending around $24 max.
     */
    //per user
    private static final int MAX_NUM_FILES = 100;
    private static final int MAX_NUM_USERS = 10;
    //limit retrieves to 0.5 GB for now (Tomcat should limit uploads to 2MB).
    //raise to 1 GB when we buy more memory (if needed)
    private static final int MAX_RETRIEVE_SIZE = (1024*1024*1024)/2;
    static class FileMetadata {
        public static final String PUBLIC_FIELD = "public";
        public static final String IS_PUBLIC = "true";
        public static final String NOT_PUBLIC = "false";
    }

    @POST
    @Consumes( MediaType.APPLICATION_OCTET_STREAM )
    public Response postFile( )
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Not implemented yet").build();
    }

    /**
     * This is not good enough. It slows things down, and still costs money.
     * Eventually, we should have an async task that updates a local cache of
     * used storage. If the cache says your below X of the limit (think atms),
     * you're good. Once you get up close, ping Amazon every time.
     * @param objectListing
     * @param maxSize
     * @return
     */
    private Response checkObjectListingSize( ObjectListing objectListing, int maxSize )
    {
        if( objectListing.isTruncated() && objectListing.getMaxKeys() >= maxSize ) {
            System.out.println("Error, too many uploads");
            return Response.status(418).entity("I'm a teapot! j/k - not enough space " + maxSize).build();
        }
        if( objectListing.getObjectSummaries().size() >= maxSize ) {
            System.out.println("Error, too many uploads");
            return Response.status(418).entity("I'm a teapot! Er, well, at least I can't hold " + maxSize + " stuff.").build();
        }
        return Response.status(Response.Status.OK).entity("OK").build();
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
                                  @FormDataParam("access_token") String accessToken )
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
            System.out.println("file submitted, and user authenticated, checking quota");
            //you could possible parse out the token response username & id and check if they match
            //but maybe we want to override that sometimes?
            AmazonS3Client s3Client = new AmazonS3Client(
                    new BasicAWSCredentials(SecretsService.amazonClientId, SecretsService.amazonClientSecret));
            ObjectListing curList = s3Client.listObjects(S3_BUCKET, "/" + userId + "/");
            Response listCheck;
            if ((listCheck = this.checkObjectListingSize(curList, MAX_NUM_FILES)).getStatus() != Response.Status.OK.getStatusCode()) {
                return listCheck;
            }
            ObjectListing userList = s3Client.listObjects(S3_BUCKET, "/");
            if ((listCheck = this.checkObjectListingSize(userList, MAX_NUM_USERS)).getStatus() != Response.Status.OK.getStatusCode()) {
                return listCheck;
            }

            ObjectMetadata fileMetadata = new ObjectMetadata();
            fileMetadata.setContentEncoding(MediaType.APPLICATION_OCTET_STREAM);
            fileMetadata.addUserMetadata(FileMetadata.PUBLIC_FIELD, FileMetadata.NOT_PUBLIC);
            //amazon bitches if you don't supply this,
            //yet it throws 501/not implemented due to supplied header if you do
            //morons.
            ///fileMetadata.setContentLength(contentDisposition.getSize());
            s3Client.putObject(S3_BUCKET, "/" + userId + "/" + filename, filestream, fileMetadata);
            return Response.status(Response.Status.CREATED).entity(filename + " stored under user id " + userId).build();
        }
        catch( RuntimeException r )
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            r.printStackTrace(pw);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sw.toString()).build();
        }
        catch( Throwable t )
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sw.toString()).build();
        }
    }

    private Response getS3Object( AmazonS3Client s3Client, String itemPath ) {
        if( !itemPath.startsWith("/") ) {
            itemPath = "/" + itemPath;
        }
        S3Object object = s3Client.getObject(S3_BUCKET, itemPath);
        try( S3ObjectInputStream inputStream = object.getObjectContent() ) {
            long contentLength = object.getObjectMetadata().getContentLength();
            if(contentLength > MAX_RETRIEVE_SIZE)
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

    @GET
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    @Path("/{userId}/{filename}")
    public Response getFile( @NotNull @PathParam("userId") String userId,
                             @NotNull @PathParam("filename") String filename,
                             @HeaderParam("Authorization") String access_token)
    {
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
        if( access_token == null  || access_token.length() == 0 ) {
            if(objectMetadata.getUserMetaDataOf(FileMetadata.PUBLIC_FIELD) == FileMetadata.IS_PUBLIC) {
                return getS3Object(s3Client,filename);
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized").build();
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
        ObjectListing objectListing = s3Client.listObjects(S3_BUCKET, "/" + userId + "/");
        Gson gson = new Gson();
        List<String> objectNames = new ArrayList<>();
        List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        int prefixLength = ("/" + userId + "/").length();
        for( S3ObjectSummary objectSummary : objectSummaries ) {
            if( objectSummary.getKey().length() <= prefixLength ) {
                continue;
            }
            objectNames.add( objectSummary.getKey().substring( prefixLength ) );
        }
        return Response.status(Response.Status.OK).entity(gson.toJson(objectNames)).build();
    }

}
