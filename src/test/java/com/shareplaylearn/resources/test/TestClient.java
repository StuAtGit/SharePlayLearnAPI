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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by stu on 5/5/15.
 */
public class TestClient
    implements Runnable {

    private OauthPasswordFlow.LoginInfo loginInfo;
    private boolean testsPassed;

    public TestClient( String host, int port, OauthPasswordFlow.LoginInfo loginInfo ) {
        this.testsPassed = false;
        this.loginInfo = loginInfo;
    }

    public boolean passed() {
        return this.testsPassed;
    }

    private void testStatusEndpoint() {
        String testStatusUrl = BackendTest.TEST_BASE_URL + "/status";
        HttpGet statusGet = new HttpGet( testStatusUrl );
        try( CloseableHttpResponse statusResponse = BackendTest.httpClient.execute(statusGet) ) {
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

    @Override
    public void run() {
        try {
            this.testStatusEndpoint();
            TestFileResource testFileResource = new TestFileResource(loginInfo.id,
                    loginInfo.accessToken, BackendTest.httpClient);
            testFileResource.testPost();
            testFileResource.testGet();
            testFileResource.testGetPathAuthorization();
            testFileResource.testGetFileList();
        }
        catch( Throwable t ) {
            throw new RuntimeException(Exceptions.asString(t));
        }
        this.testsPassed = true;
    }
}
