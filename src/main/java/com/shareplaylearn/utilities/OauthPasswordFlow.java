package com.shareplaylearn.utilities;

import com.google.gson.Gson;
import com.shareplaylearn.services.SecretsService;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by stu on 5/8/15.
 */
public class OauthPasswordFlow {
    /**
     *   First we post like the form in our site would to redirect to google:
     *
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

     Then we end up with a post to googles oauth endpoint which we then parse the form:
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

     out of the page (to get all the hidden values to submit & the cookie), post to that form, and grab the
     access token out of the redirect to the live site.

     */
    public static class OauthJwt {
        public String iss;
        public String sub;
        public String azp;
        public String email;
        public String at_hash;
        public String email_verified;
        public String aud;
        public String iat;
        public String exp;
    }

    public static class LoginInfo {
        public String accessToken;
        public String expiry;
        public String idToken;
        public OauthJwt idTokenBody;
        public String id;
    }


    public static class AuthorizationException extends Exception {
        public AuthorizationException( String message ) {
            super( message );
        }
    }

    public static class UnauthorizedException extends Exception {
        public UnauthorizedException( String message ) {
            super( message );
        }
    }

    public static LoginInfo googleLogin(String username, String password,
                                        String clientId, String callbackUri)
            throws URISyntaxException, IOException, AuthorizationException, UnauthorizedException {

        CloseableHttpClient httpClient = HttpClients.custom().build();
        String oAuthQuery = "client_id=" + clientId + "&";
        oAuthQuery += "response_type=code&";
        oAuthQuery += "scope=openid email&";
        oAuthQuery += "redirect_uri=" + callbackUri;
        URI oAuthUrl = new URI("https", null, "accounts.google.com", 443, "/o/oauth2/auth", oAuthQuery, null);
        Connection oauthGetCoonnection = Jsoup.connect(oAuthUrl.toString());
        Connection.Response oauthResponse = oauthGetCoonnection.method(Connection.Method.GET).execute();
        if( oauthResponse.statusCode() != 200 ) {
            String errorMessage = "Error contacting Google's oauth endpoint: "
                    + oauthResponse.statusCode() + " / " + oauthResponse.statusMessage();
            if( oauthResponse.body() != null ) {
                errorMessage += oauthResponse.body();
            }
            throw new AuthorizationException( errorMessage );
        }
        Map<String,String> oauthCookies = oauthResponse.cookies();
        Document oauthPage = oauthResponse.parse();
        Element oauthForm = oauthPage.getElementById("gaia_loginform");
        System.out.println(oauthForm.toString());
        Connection oauthPostConnection  = Jsoup.connect("https://accounts.google.com/ServiceLoginAuth");
        HashMap<String,String> formParams = new HashMap<>();
        for( Element child : oauthForm.children() ) {
            if( child.tagName().equals("input")
                    && child.hasAttr("name") ) {

                String keyName = child.attr("name");
                String keyValue = null;

                if( keyName.equals("Email") ) {
                    keyValue = username;
                } else if( keyName.equals("Passwd")) {
                    keyValue = password;
                } else if( child.hasAttr("value") ) {
                    keyValue = child.attr("value");
                }

                if( keyValue != null ) {
                    oauthPostConnection.data(keyName, keyValue);
                    formParams.put(keyName,keyValue);
                }
            }
        }
        oauthPostConnection.cookies(oauthCookies);
        //oauthPostConnection.followRedirects(false);
        System.out.println("form post params were: ");
        for( Map.Entry<String,String> kvp : formParams.entrySet() ) {
            System.out.println( kvp.getKey() + "," + kvp.getValue() );
        }
        System.out.println("form cookies were: ");
        for( Map.Entry<String,String> cookie : oauthCookies.entrySet() ) {
            System.out.println(cookie.getKey() + "," + cookie.getValue());
        }
        Connection.Response postResponse = oauthPostConnection.method(Connection.Method.POST).timeout(5000).execute();
        if( postResponse.statusCode() != 200 ) {
            String errorMessage = "Failed to validate credentials: "
                    + oauthResponse.statusCode() + " / " + oauthResponse.statusMessage();
            if( oauthResponse.body() != null ) {
                errorMessage += oauthResponse.body();
            }
            throw new UnauthorizedException( errorMessage );
        }
        System.out.println("Response headers (after post to google form & following redirect):");
        for( Map.Entry<String,String> header : postResponse.headers().entrySet() ) {
            System.out.println(header.getKey() + "," + header.getValue() );
        }
        System.out.println( "Final response url was: " + postResponse.url().toString() );
        String[] args = postResponse.url().toString().split("&");
        LoginInfo loginInfo = new LoginInfo();
        for( String arg : args ) {
            if( arg.startsWith("access_token") ) {
                loginInfo.accessToken = arg.split("=")[1].trim();
            }
            else if( arg.startsWith("id_token") ) {
                loginInfo.idToken = arg.split("=")[1].trim();
            }
            else if( arg.startsWith("expires_in") ) {
                loginInfo.expiry = arg.split("=")[1].trim();
            }
        }

        assertNotNull(loginInfo.accessToken);
        assertNotNull(loginInfo.idToken);
        String[] idTokenFields = loginInfo.idToken.split("\\.");
        if(idTokenFields.length < 3){
            throw new AuthorizationException("Error parsing id token " + loginInfo.idToken + "\n" + "it only had " + idTokenFields.length + " field!");
        }
        String jwtBody = new String(Base64.decodeBase64(idTokenFields[1]), StandardCharsets.UTF_8);
        loginInfo.idTokenBody = new Gson().fromJson(jwtBody,OauthJwt.class);
        loginInfo.id = loginInfo.idTokenBody.sub;
        return loginInfo;
        /**
         Document oauthPostResponse = postResponse.parse();
         System.out.println("*** Oauth response from google *** ");
         System.out.println(oauthPostResponse.toString());
         **/
    }
}
