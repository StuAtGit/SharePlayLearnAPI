package com.shareplaylearn;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by stu on 4/14/15.
 */
public class File {

    @POST
    @Consumes( MediaType.APPLICATION_OCTET_STREAM )
    public void postFile( )
    {

    }

    @GET
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    public void getFile()
    {

    }
}
