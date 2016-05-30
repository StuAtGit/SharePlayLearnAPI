/**
 * Copyright 2015-2016 Stuart Smith
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
