/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shareplaylearn;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author stu
 */
@javax.ws.rs.ApplicationPath("/api/")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(com.shareplaylearn.OAuth2Callback.class);
        resources.add(com.shareplaylearn.File.class);
        resources.add(MultiPartFeature.class);
        resources.add(LoggingFilter.class) ;
        return resources;
    }
}
