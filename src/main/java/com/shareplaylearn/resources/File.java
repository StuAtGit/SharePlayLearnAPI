package com.shareplaylearn.resources;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.shareplaylearn.services.SecretsService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by stu on 4/14/15.
 */
@Path("/file")
public class File {

    private static final String S3_BUCKET = "shareplaylearn";
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

    @POST
    @Path("/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postFileForm( @NotNull @FormDataParam("file") InputStream filestream,
                                  @NotNull @FormDataParam("file") FormDataContentDisposition contentDisposition,
                                  @NotNull @FormDataParam("filename") String filename,
                                  @NotNull @FormDataParam("user_id") String userId,
                                  @NotNull @FormDataParam("access_token") String accessToken )
    {
        if( userId == null || userId.trim().length() == 0 )
        {
           return Response.status(Response.Status.BAD_REQUEST).entity("No user id given.").build();
        }
        if( accessToken == null || accessToken.trim().length() == 0 )
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("No access token given.").build();
        }

        Response tokenResponse = OAuth2Callback.validateToken(accessToken);
        if( tokenResponse.getStatus() != Response.Status.OK.getStatusCode() )
        {
            return tokenResponse;
        }
        //you could possible parse out the token response username & id and check if they match
        //but maybe we want to override that sometimes?
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId,SecretsService.amazonClientSecret) );
        ObjectMetadata fileMetadata = new ObjectMetadata();
        fileMetadata.setContentEncoding(MediaType.APPLICATION_OCTET_STREAM);
        fileMetadata.addUserMetadata(FileMetadata.PUBLIC_FIELD, FileMetadata.NOT_PUBLIC);
        //odd, but we can still hit this, despite annotation ?
        //this happens if the upload element is not named 'file'
        if( contentDisposition == null ) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Content Disposition not supplied! (did you forget to name your file input field?").build();
        }
        fileMetadata.setContentLength(contentDisposition.getSize());
        s3Client.putObject(S3_BUCKET, userId + "/" + filename, filestream, new ObjectMetadata());
        return Response.status(Response.Status.CREATED).entity(filename + " stored").build();
    }

    private Response getS3Object( AmazonS3Client s3Client, ObjectMetadata objectMetadata, String filename ) {
        S3Object object = s3Client.getObject(S3_BUCKET, filename);
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
            while( (bytesRead = inputStream.read(buffer)) > 0 ) {
                outputStream.write(buffer,0,bytesRead);
            }
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
    @Path("{filename}")
    public Response getFile( @NotNull @PathParam("filename") String filename,
                             @PathParam("access_token") String access_token)
    {
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId, SecretsService.amazonClientSecret)
        );
        ObjectMetadata objectMetadata = s3Client.getObjectMetadata(S3_BUCKET, filename);
        if( access_token == null  || access_token.length() == 0 ) {
            if(objectMetadata.getUserMetaDataOf(FileMetadata.PUBLIC_FIELD) == FileMetadata.IS_PUBLIC) {
                return getS3Object(s3Client,objectMetadata,filename);
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized").build();
            }
        }
        else
        {
            Response tokenResponse = OAuth2Callback.validateToken(access_token);
            if( tokenResponse.getStatus() == Response.Status.OK.getStatusCode() ) {
                return getS3Object(s3Client,objectMetadata,filename);
            }
            return tokenResponse;
        }
    }
}
