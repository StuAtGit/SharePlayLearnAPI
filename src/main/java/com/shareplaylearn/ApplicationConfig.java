/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shareplaylearn;

import com.shareplaylearn.resources.File;
import com.shareplaylearn.resources.OAuth2Callback;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.io.IOException;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Application;

/**
 *
 * @author stu
 */
@javax.ws.rs.ApplicationPath("/api/")
public class ApplicationConfig extends Application {

    public static CloseableHttpClient httpClient = HttpClients.custom().build();

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(OAuth2Callback.class);
        resources.add(File.class);
        resources.add(MultiPartFeature.class);
        resources.add(LoggingFilter.class) ;
        return resources;
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
