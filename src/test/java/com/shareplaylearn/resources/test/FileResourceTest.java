/**
 * Copyright 2015-2016 Stuart Smith
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Created by stu on 5/7/15.
 */
public class FileResourceTest {

    private String accessToken;
    private String userId;
    private String userName;
    private HttpClient httpClient;
    private Logger log;

    public FileResourceTest(String userName, String userId, String accessToken, HttpClient httpClient) {
        this.accessToken = accessToken;
        this.httpClient = httpClient;
        this.userId = userId;
        this.userName = userName;
        this.log = LoggerFactory.getLogger(FileResourceTest.class);
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

            log.debug("Testing upload of: " + uploadTest.toString());
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
                                boolean addHeader, String encode,
                                boolean verifyContentMatch, String testItemName ) throws IOException {
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
            if ( verifyContentMatch && !Arrays.equals(entity, testFileBuffer)) {
                String message = "File resource: " + fileResource + " did not have bytes matching: " + testItemName + " !";
                if (entity == null) {
                    message += " entity was null!?";
                } else {
                    if( entity.length < 20 ) {
                        message += " returned buffer  was: " + new String(entity, StandardCharsets.UTF_8);
                    }
                    String[] paths = fileResource.split("/");
                    String name = paths[paths.length-1];
                    Path path = FileSystems.getDefault().getPath(name + "_failed");
                    Files.write( path, entity, StandardOpenOption.CREATE );
                }
                throw new RuntimeException(message);
            }
            log.debug("Successfully retrieved the test file - and the bytes matched! :)");
        }
    }


    public void testGet( String itemLocation, Path testItemName, boolean verifyContentMatch ) throws IOException {
        byte[] testFileBuffer = Files.readAllBytes(testItemName);
        String fileResource = BackendTest.TEST_BASE_URL + File.RESOURCE_BASE + itemLocation;
        testGetGeneric(fileResource, testFileBuffer, true, null, verifyContentMatch, testItemName.toString());
        testGetGeneric(fileResource, testFileBuffer, true, "base64", verifyContentMatch, testItemName.toString());
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
                    String message = "Error retrieving file list for user: " + this.userName + " " +
                            processedHttpResponse.completeMessage;
                    throw new RuntimeException(message);
                }
                List<UserItem> filelist;
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<UserItem>>(){}.getType();
                    filelist = gson.fromJson(processedHttpResponse.entity, listType);
                } catch (Throwable t) {
                    String message = "Failed to parse response entity into json: " + processedHttpResponse.entity +
                            "\n" + Exceptions.asString(t);
                    log.error( message );
                    throw new RuntimeException( message );
                }
                log.debug("Got filelist: " + processedHttpResponse.entity);
                boolean found = false;
                //this basically is just doing a linear search of the file list to see if
                //we've found the test upload in the filelist (we should!)
                //and validates that file, if possible
                for( UserItem item : filelist ) {
                    if( item.getOriginalLocation() == null ) {
                        String message = "Original location was null, this should not happen.";
                        log.error(message);
                        throw new RuntimeException(message);
                    }
                    if( item.getOriginalLocation().endsWith(uploadEntry.getKey()) ) {
                        Path itemPath = FileSystems.getDefault().getPath( uploadEntry.getValue() );
                        //the final argument indicates whether we want to validate the file contents or not
                        //If the preferred location is not equal to the original location, then
                        //it has likely been transformed, and should not have the same contents
                        testGet( item.getPreferredLocation(), itemPath, item.getPreferredLocation().equals(
                                item.getOriginalLocation()
                        ) );
                        found = true;
                    }
                }
                if (!found) {
                    String message = "Error: file list " + processedHttpResponse.entity + " did not contain test filename " +
                            uploadEntry.getKey();
                    log.error(message);
                    throw new RuntimeException(message);
                }
                log.debug("Successfully retrieved file list: " + processedHttpResponse.entity);
            }
        }
    }
}
