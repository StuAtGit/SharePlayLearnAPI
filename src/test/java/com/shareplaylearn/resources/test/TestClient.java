package com.shareplaylearn.resources.test;

import com.amazonaws.services.simpleworkflow.model.Run;
import com.shareplaylearn.utilities.Exceptions;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;

/**
 * Created by stu on 5/5/15.
 */
public class TestClient
    implements Runnable {

    private String testHost = "localhost";
    private int testPort = 8080;
    //I looked through the httpclient source code, and the finalizer does cleanup the connection pools
    private static CloseableHttpClient httpClient = HttpClients.custom().build();
    private boolean testsPassed;

    public TestClient( String host, int port ) {
        this.testHost = host;
        this.testPort = port;
        this.testsPassed = false;
    }

    public boolean passed() {
        return this.testsPassed;
    }

    @Override
    public void run() {
        String testBaseUrl = "http://" + testHost + ":" + testPort + "/api";
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
        this.testsPassed = true;
    }
}
