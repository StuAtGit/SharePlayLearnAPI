package com.shareplaylearn.resources.test;

import com.shareplaylearn.services.SecretsService;
import com.shareplaylearn.utilities.Exceptions;
import com.shareplaylearn.utilities.OauthPasswordFlow;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by stu on 5/5/15.
 */
public class TestClient
    implements Runnable {

    private boolean testsPassed;

    public TestClient( String host, int port ) {
        this.testsPassed = false;
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
            AccessTokenTest accessTokenTest = new AccessTokenTest(SecretsService.testOauthUsername,
                    SecretsService.testOauthPassword);
            OauthPasswordFlow.LoginInfo loginInfo = accessTokenTest.testPost();
            FileResourceTest testFileResource = new FileResourceTest(loginInfo.id,
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
