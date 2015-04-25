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
        URI oAuthUrl = new URI("https", null, "accounts.google.com", 443, "/o/oauth2/auth", oAuthQuery, null);
        HttpGet oAuthGet = new HttpGet(oAuthUrl.toString());
        try( CloseableHttpResponse response = httpClient.execute(oAuthGet)) {
            if(response.getEntity() != null) {
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
            /**
             * Check for OK, and post like this form would (grabbing test account & password from Secret Service):
             *
             <form novalidate method="post" action="https://accounts.google.com/ServiceLoginAuth" id="gaia_loginform">
             <input name="GALX" type="hidden"
             value="InQr98KQoo4">
             <input name="continue" type="hidden" value="https://accounts.google.com/o/oauth2/auth?scope=openid+email&amp;response_type=code&amp;redirect_uri=https://www.shareplaylearn.com/api/oauth2callback&amp;client_id=726837865357-tqs20u6luqc9oav1bp3vb8ndgavjnrkf.apps.googleusercontent.com&amp;hl=en-US&amp;from_login=1&amp;as=509bb8481cb219d5">
             <input name="service" type="hidden" value="lso">
             <input name="ltmpl" type="hidden" value="popup">
             <input name="shdf" type="hidden" value="Cp4BCxIRdGhpcmRQYXJ0eUxvZ29VcmwaAAwLEhV0aGlyZFBhcnR5RGlzcGxheU5hbWUaH1Byb2plY3QgRGVmYXVsdCBTZXJ2aWNlIEFjY291bnQMCxIGZG9tYWluGh9Qcm9qZWN0IERlZmF1bHQgU2VydmljZSBBY2NvdW50DAsSFXRoaXJkUGFydHlEaXNwbGF5VHlwZRoHREVGQVVMVAwSA2xzbyIUO-y6s874AtymHX1FyAZlDywoVRooATIUcNWlS1x_xRwwT3GXOu6zouQBYUk">
             <input name="scc" type="hidden" value="1">
             <input name="sarp" type="hidden" value="1">
             <input type="hidden" id="_utf8" name="_utf8" value="&#9731;"/>
             <input type="hidden" name="bgresponse" id="bgresponse" value="js_disabled">
             <label class="hidden-label" for="Email">Email</label>
             <input id="Email" name="Email" type="email"
             placeholder="Email"
             value=""
             spellcheck="false"
             class="">
             <label class="hidden-label" for="Passwd">Password</label>
             <input id="Passwd" name="Passwd" type="password"
             placeholder="Password"
             class="">
             <input id="signIn" name="signIn" class="rc-button rc-button-submit" type="submit" value="Sign in">
             <input type="hidden" name="PersistentCookie" value="yes">
             <a id="link-forgot-passwd" href="https://accounts.google.com/RecoverAccount?service=lso&amp;continue=https%3A%2F%2Faccounts.google.com%2Fo%2Foauth2%2Fauth%3Fscope%3Dopenid%2Bemail%26response_type%3Dcode%26redirect_uri%3Dhttps%3A%2F%2Fwww.shareplaylearn.com%2Fapi%2Foauth2callback%26client_id%3D726837865357-tqs20u6luqc9oav1bp3vb8ndgavjnrkf.apps.googleusercontent.com%26hl%3Den-US%26from_login%3D1%26as%3D509bb8481cb219d5"

             >
             Need help?
             </a>
             </form>
             */
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue( false );
        }

    }
}
