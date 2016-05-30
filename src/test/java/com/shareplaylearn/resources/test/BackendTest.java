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
package com.shareplaylearn.resources.test;

import com.google.gson.Gson;
import com.shareplaylearn.services.SecretsService;
import com.shareplaylearn.utilities.Exceptions;
import com.shareplaylearn.utilities.OauthPasswordFlow;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by stu on 4/25/15.
 */
public class BackendTest{

    public static String TEST_HOST = "localhost";
    public static final int TEST_PORT = 8081;
    public static final String TEST_BASE_URL = "http://" + TEST_HOST + ":" + TEST_PORT + "/api";
    //I looked through the httpclient source code, and the finalizer does cleanup the connection pools
    public static CloseableHttpClient httpClient = HttpClients.custom().build();

    public static class ProcessedHttpResponse {
        public int code;
        public String reason;
        public String entity;
        public String completeMessage;

        public ProcessedHttpResponse( CloseableHttpResponse response ) throws IOException {
            this.code = response.getStatusLine().getStatusCode();
            this.reason = response.getStatusLine().getReasonPhrase();
            this.entity = "";
            if( response.getEntity() != null ) {
                this.entity += EntityUtils.toString(response.getEntity());
            }
            this.completeMessage = code + "/" + reason + " " + entity;
        }
    }

    @Test
    public void RunBackendTests() throws Exception {
        int timeout = 10000;
        int port = TEST_PORT;

        StandaloneServer standaloneServer = new StandaloneServer(port);
        Thread serverThread = new Thread(standaloneServer);
        serverThread.start();
        Thread.sleep(3000);

        try {
            TestClient testClient = new TestClient("localhost", port);
            Thread clientThread = new Thread(testClient);
            clientThread.start();
            clientThread.join();
            assertTrue(testClient.passed());
        } catch( RuntimeException e ) {
            System.out.println("Error in client tests " + Exceptions.asString(e) );
        }

        serverThread.join(timeout);
        standaloneServer.stop();
    }
}
