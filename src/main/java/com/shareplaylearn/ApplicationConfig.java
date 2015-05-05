/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shareplaylearn;

import com.shareplaylearn.resources.File;
import com.shareplaylearn.resources.OAuth2Callback;
import com.shareplaylearn.resources.Status;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.io.IOException;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Application;

/**
 *
 * @author stu
 */
@javax.ws.rs.ApplicationPath("/api/")
public class ApplicationConfig extends ResourceConfig {

    public static CloseableHttpClient httpClient = HttpClients.custom().build();

    public ApplicationConfig() {
        try {
            System.out.println("****Share,Play,Learn loading resources.****");
            packages("com.shareplaylearn.resources");
            register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
            register(org.glassfish.jersey.filter.LoggingFilter.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println( "********* Share,Play,Learn application started**** ");
        property(ServerProperties.TRACING, "ALL");
        System.out.println("*******Tracing enabled************");
    }

    @PreDestroy
    public static void cleanupApplication() {
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
