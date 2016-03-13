/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shareplaylearn;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 *
 * @author stu
 */
@javax.ws.rs.ApplicationPath("/api/")
public class SharePlayLearnApi extends ResourceConfig {

    public static CloseableHttpClient httpClient = HttpClients.custom().build();

    public SharePlayLearnApi() {
        try {
            System.out.println("****Share,Play,Learn loading resources.****");
            String basePackage = "com.shareplaylearn.";
            packages(basePackage + "resources");
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
