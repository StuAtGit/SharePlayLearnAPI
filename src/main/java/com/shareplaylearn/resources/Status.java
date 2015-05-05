package com.shareplaylearn.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by stu on 5/5/15.
 */
@Path("status")
public class Status {
    @GET
    public String status()
    {
        return "up! version 0.1";
    }
}
