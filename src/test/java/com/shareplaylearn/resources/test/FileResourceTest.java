package com.shareplaylearn.resources.test;

import com.amazonaws.util.Base64;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shareplaylearn.models.UserItem;
import com.shareplaylearn.models.UserItemManager;
import com.shareplaylearn.resources.File;
import com.shareplaylearn.utilities.Exceptions;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by stu on 5/7/15.
 */
public class FileResourceTest {

    private String accessToken;
    private String userId;
    private String userName;
    private HttpClient httpClient;

    public FileResourceTest(String userName, String userId, String accessToken, HttpClient httpClient) {
        this.accessToken = accessToken;
        this.httpClient = httpClient;
        this.userId = userId;
        this.userName = userName;
    }

    public void testPost() throws IOException {
        for(Map.Entry<String,String> uploadEntry : TestFiles.testUploads.entrySet()) {
            Path uploadTest = FileSystems.getDefault().getPath(uploadEntry.getValue());
            byte[] uploadBuffer = Files.readAllBytes(uploadTest);
            HttpEntity formEntity = MultipartEntityBuilder.create().
                    addBinaryBody("file", uploadBuffer,
                            ContentType.APPLICATION_OCTET_STREAM,
                            uploadEntry.getKey()).
                    addTextBody("user_id", userId).
                    addTextBody("user_name", userName).
                    addTextBody("access_token", accessToken).
                    addTextBody("filename",uploadEntry.getKey()).build();

            System.out.println("Testing upload of: " + uploadTest.toString());
            HttpPost httpPost = new HttpPost(BackendTest.TEST_BASE_URL + File.RESOURCE_BASE + "/form");
            httpPost.setEntity(formEntity);
            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost)) {
                String reply = "";
                if (response.getEntity() != null) {
                    reply += EntityUtils.toString(response.getEntity());
                }
                if (response.getStatusLine().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
                    String message = "Post to file resource returned " + response.getStatusLine().getReasonPhrase() +
                            response.getStatusLine().getStatusCode();
                    message += reply;
                    throw new RuntimeException(message);
                }
                System.out.println(reply);
            }
        }
    }

    public void testGetGeneric( String fileResource, byte[] testFileBuffer,
                                boolean addHeader, String encode ) throws IOException {
        if( encode != null ) {
            fileResource += "?encode=" + encode;
        }
        HttpGet fileGet = new HttpGet(fileResource);
        if( addHeader ) {
            fileGet.addHeader("Authorization", "Bearer " + this.accessToken);
        }
        try( CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(fileGet)) {
            int code = response.getStatusLine().getStatusCode();
            String reason = response.getStatusLine().getReasonPhrase();
            byte[] entity = null;
            if (response.getEntity() != null) {
                entity = EntityUtils.toByteArray(response.getEntity());
            }
            if (code != Response.Status.OK.getStatusCode()) {
                String message = "Get from file resource: " + fileResource + " failed ";
                message += code + "/" + reason;
                if (entity != null) {
                    message += new String(entity, StandardCharsets.UTF_8);
                }
                throw new RuntimeException(message);
            }

            if( encode != null && encode.toUpperCase().equals(UserItemManager.AvailableEncodings.BASE64) ) {
                entity = Base64.decode(entity);
            }
            if (!Arrays.equals(entity, testFileBuffer)) {
                String message = "File resource: " + fileResource + " did not have matching bytes!";
                if (entity == null) {
                    message += " entity was null!?";
                } else {
                    message += " returned buffer  was: " + new String(entity, StandardCharsets.UTF_8);
                }
                throw new RuntimeException(message);
            }
            System.out.println("Successfully retrieved the test file - and the bytes matched! :)");
        }
    }


    public void testGet( String itemLocation, Path testItemName ) throws IOException {
        byte[] testFileBuffer = Files.readAllBytes(testItemName);
        String fileResource = BackendTest.TEST_BASE_URL + File.RESOURCE_BASE + itemLocation;
        testGetGeneric(fileResource, testFileBuffer, true, null);
        testGetGeneric(fileResource, testFileBuffer, true, "base64");
    }

    public void testGetFileList() throws IOException {
        for( Map.Entry<String,String> uploadEntry : TestFiles.testUploads.entrySet() ) {
            String filelistResource = BackendTest.TEST_BASE_URL + File.RESOURCE_BASE + "/" + this.userName
            + "/" + this.userId + "/filelist";
            HttpGet filelistGet = new HttpGet(filelistResource);
            filelistGet.addHeader("Authorization", "Bearer " + this.accessToken);
            filelistGet.addHeader("UserId", this.userId);
            filelistGet.addHeader("UserName", this.userName);
            try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(filelistGet)) {
                BackendTest.ProcessedHttpResponse processedHttpResponse = new BackendTest.ProcessedHttpResponse(response);
                if (processedHttpResponse.code != Response.Status.OK.getStatusCode()) {
                    throw new RuntimeException("Error retrieving file list for user: " + this.userName + " " +
                            processedHttpResponse.completeMessage);
                }
                List<UserItem> filelist;
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<UserItem>>(){}.getType();
                    filelist = gson.fromJson(processedHttpResponse.entity, listType);
                } catch (Throwable t) {
                    throw new RuntimeException("Failed to parse response entity into json: " + processedHttpResponse.entity +
                            "\n" + Exceptions.asString(t));
                }
                System.out.println("Got filelist: " + processedHttpResponse.entity);
                boolean found = false;
                for( UserItem item : filelist ) {
                    if( item.getPreferredLocation().endsWith(uploadEntry.getKey()) ) {
                        Path itemPath = FileSystems.getDefault().getPath( uploadEntry.getValue() );
                        testGet( item.getPreferredLocation(), itemPath );
                        found = true;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Error: file list " + processedHttpResponse.entity + " did not contain test filename " +
                            uploadEntry.getKey());
                }
                System.out.println("Successfully retrieved file list: " + processedHttpResponse.entity);
            }
        }
    }
}
