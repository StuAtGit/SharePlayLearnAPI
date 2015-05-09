package com.shareplaylearn.resources.test;

import com.google.gson.Gson;
import com.shareplaylearn.resources.AccessToken;
import com.shareplaylearn.utilities.OauthPasswordFlow;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by stu on 5/9/15.
 */
public class AccessTokenTest {
    private String username;
    private String password;
    private static String ACCESS_TOKEN_RESOURCE = BackendTest.TEST_BASE_URL + "/" + AccessToken.RELATIVE_RESOURCE_PATH;
    public AccessTokenTest( String username, String password ) {
        this.username = username;
        this.password = password;
    }

    public OauthPasswordFlow.LoginInfo testPost() throws IOException {
        HttpPost httpPost = new HttpPost(ACCESS_TOKEN_RESOURCE);
        String credentialsString = username + ":" + password;
        httpPost.addHeader("Authorization",
                Base64.encodeBase64String(credentialsString.getBytes(StandardCharsets.UTF_8))
        );

        try( CloseableHttpResponse response = BackendTest.httpClient.execute(httpPost) ) {
            BackendTest.ProcessedHttpResponse processedHttpResponse = new BackendTest.ProcessedHttpResponse(response);
            if( response.getStatusLine().getStatusCode() != 200 ) {
                throw new RuntimeException("Error testing access token endpoint: " +
                        processedHttpResponse.completeMessage );
            }
            return new Gson().fromJson(processedHttpResponse.entity, OauthPasswordFlow.LoginInfo.class);
        }
    }
}
