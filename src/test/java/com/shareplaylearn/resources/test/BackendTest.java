package com.shareplaylearn.resources.test;

import com.google.gson.Gson;
import com.shareplaylearn.services.SecretsService;
import com.shareplaylearn.utilities.Exceptions;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

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

    @Test
    public void RunBackendTests() throws Exception {

        OauthPasswordFlow.LoginInfo loginInfo;
        try {
            loginInfo = OauthPasswordFlow.googleLogin(SecretsService.testOauthUsername,
                    SecretsService.testOauthPassword,
                    "726837865357-tqs20u6luqc9oav1bp3vb8ndgavjnrkf.apps.googleusercontent.com"
                    , "https://www.shareplaylearn.com/api/oauth2callback");

            Gson gson = new Gson();
            System.out.println(gson.toJson(loginInfo));
        } catch (SocketTimeoutException e ) {
            e.printStackTrace();
            System.out.println("failed to connect to oauth provider " + e.getMessage());
            assertTrue( false );
            throw e;
        }

        int timeout = 10000;
        int port = TEST_PORT;

        StandaloneServer standaloneServer = new StandaloneServer(port);
        Thread serverThread = new Thread(standaloneServer);
        serverThread.start();
        Thread.sleep(1500);

        try {
            TestClient testClient = new TestClient("localhost", port, loginInfo);
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
