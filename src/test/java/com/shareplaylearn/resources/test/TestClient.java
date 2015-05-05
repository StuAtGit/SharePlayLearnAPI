package com.shareplaylearn.resources.test;

import com.shareplaylearn.utilities.Exceptions;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

/**
 * Created by stu on 5/5/15.
 */
public class TestClient
    implements Runnable {

    private String testHost = "localhost";
    private int testPort = 8080;
    private String accessToken;
    private String testBaseUrl;
    //I looked through the httpclient source code, and the finalizer does cleanup the connection pools
    private static CloseableHttpClient httpClient = HttpClients.custom().build();
    private boolean testsPassed;

    public TestClient( String host, int port, String accessToken ) {
        this.testHost = host;
        this.testPort = port;
        this.testsPassed = false;
        this.accessToken = accessToken;
        testBaseUrl = "http://" + testHost + ":" + testPort + "/api";
    }

    public boolean passed() {
        return this.testsPassed;
    }

    private void testStatusEndpoint() {
        String testStatusUrl = testBaseUrl + "/status";
        HttpGet statusGet = new HttpGet( testStatusUrl );
        try( CloseableHttpResponse statusResponse = httpClient.execute(statusGet) ) {
            if( statusResponse.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode() ) {
                String errorMessage = "Status endpoint at: " + testStatusUrl + "  did not return OK";
                if( statusResponse.getEntity() != null ) {
                    errorMessage += EntityUtils.toString(statusResponse.getEntity());
                }
                throw new RuntimeException(errorMessage);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            throw new RuntimeException(Exceptions.asString(e));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(Exceptions.asString(e));
        }
    }

    private void testUploadFileEndpoint() {
        //TODO: implement upload file test, using this.accessToken
    }

    public static class GpioClient extends Endpoint {

        @Override
        public void onOpen(Session session, EndpointConfig endpointConfig) {
            session.addMessageHandler(
                    new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage( String message ) {
                            System.out.println( "Received " + message + " from server");
                        }
                    }
            );
            try {
                session.getBasicRemote().sendText("Hello! from Gpio Test Client");
            } catch (IOException e) {
                throw new RuntimeException(Exceptions.asString(e));
            }
        }
    }

    private void testGpioEndpoint() throws Exception {
        String gpioEndpoint = this.testBaseUrl + "/gpio";
        gpioEndpoint = "ws://localhost:8080/gpio";
        gpioEndpoint = gpioEndpoint.replace("http", "ws");
        System.out.println("Websocket uri is: " + gpioEndpoint);
        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().build();
        ClientManager clientManager = ClientManager.createClient();
        clientManager.connectToServer(new GpioClient(),clientEndpointConfig,URI.create(gpioEndpoint));
    }

    @Override
    public void run() {
        this.testStatusEndpoint();
        this.testUploadFileEndpoint();
        try {
            this.testGpioEndpoint();
        } catch (Exception e) {
            throw new RuntimeException(Exceptions.asString(e));
        }
        this.testsPassed = true;
    }
}
