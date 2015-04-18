package com.shareplaylearn;

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

    @POST
    @Consumes( MediaType.APPLICATION_OCTET_STREAM )
    public Response postFile( )
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Not implemented yet").build();
    }

    @POST
    @Path("/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postFileForm( @FormDataParam("file") InputStream filestream, @FormDataParam("filename") String filename )
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Not implemented yet").build();
    }

    @GET
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    @Path("{filename}")
    public Response getFile( @NotNull @PathParam("filename") String filename )
    {
        return Response.status(Response.Status.NOT_FOUND ).entity(filename + " not found, because not implemented yet!").build();
    }
}
