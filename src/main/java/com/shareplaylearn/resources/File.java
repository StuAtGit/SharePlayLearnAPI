package com.shareplaylearn.resources;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.shareplaylearn.services.SecretsService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Created by stu on 4/14/15.
 */
@Path("/file")
public class File {

    private static final String S3_BUCKET = "shareplaylearn";

    @POST
    @Consumes( MediaType.APPLICATION_OCTET_STREAM )
    public Response postFile( )
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Not implemented yet").build();
    }

    @POST
    @Path("/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postFileForm( @FormDataParam("file") InputStream filestream,
                                  @FormDataParam("file") FormDataContentDisposition contentDisposition,
                                  @FormDataParam("filename") String filename )
    {
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials(SecretsService.amazonClientId,SecretsService.amazonClientSecret) );
        ObjectMetadata fileMetadata = new ObjectMetadata();
        fileMetadata.setContentEncoding(MediaType.APPLICATION_OCTET_STREAM);
        fileMetadata.setContentLength(contentDisposition.getSize());
        s3Client.putObject(S3_BUCKET, filename, filestream, new ObjectMetadata());
        return Response.status(Response.Status.CREATED).entity(filename + " stored").build();
    }

    @GET
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    @Path("{filename}")
    public Response getFile( @NotNull @PathParam("filename") String filename )
    {
        return Response.status(Response.Status.NOT_FOUND ).entity(filename + " not found, because not implemented yet!").build();
    }
}
