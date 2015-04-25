package com.shareplaylearn.resources.test;

import junit.framework.TestCase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

/**
 * Created by stu on 4/25/15.
 */
public class BackendTest{

    /**
     *     <form method="GET" action="https://accounts.google.com/o/oauth2/auth" >
     <input type="hidden" name="client_id" value="726837865357-tqs20u6luqc9oav1bp3vb8ndgavjnrkf.apps.googleusercontent.com"/>
     <input type="hidden" name="response_type" value="code"/>
     <input type="hidden" name="scope" value="openid email"/>
     <input type="hidden" name="redirect_uri" value="https://www.shareplaylearn.com/api/oauth2callback"/>
     <!-- generate a random number for this session and save in session state - this can be recovered by the browser js
     code when the redirect comes back from the servlet-->
     <input type="hidden" name="state" value="insecure_test_token"/>
     <input type="submit" value="Log in with Google"/>
     </form>

     */
    @Test
    public void LoginTest() throws URISyntaxException {
        CloseableHttpClient httpClient = HttpClients.custom().build();
        String oAuthQuery = "client_id=726837865357-tqs20u6luqc9oav1bp3vb8ndgavjnrkf.apps.googleusercontent.com&";
        oAuthQuery += "response_type=code&";
        oAuthQuery += "scope=openid email&";
        oAuthQuery += "redirect_uri=https://www.shareplaylearn.com/api/oauth2callback";
        URI oAuthUrl = new URI("https", null, "accounts.google.com", 80, "/o/oauth2/auth", oAuthQuery, null);
        HttpGet oAuthGet = new HttpGet(oAuthUrl.toString());
        try( CloseableHttpResponse response = httpClient.execute(oAuthGet)) {
            if(response.getEntity() != null) {
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue( false );
        }

    }
}
