package com.shareplaylearn.resources.test;

import com.google.gson.Gson;
import com.shareplaylearn.InternalErrorException;
import com.shareplaylearn.models.UserItem;
import com.shareplaylearn.models.UserItemManager;
import com.shareplaylearn.services.SecretsService;
import com.shareplaylearn.utilities.OauthPasswordFlow;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Created by stu on 9/13/15.
 */
public class UserItemManagerTest {

    @Test
    public void testGetFileList(OauthPasswordFlow.LoginInfo loginInfo) throws IOException, InternalErrorException {
        UserItemManager userItemManager = new UserItemManager( loginInfo.userName, loginInfo.id );
        for(Map.Entry<String,String> testUpload : TestFiles.testUploads.entrySet() ) {
            Path testPath = FileSystems.getDefault().getPath(testUpload.getValue());
            byte[] fileBuffer = Files.readAllBytes(testPath);
            userItemManager.addItem(testUpload.getKey(),fileBuffer);
        }

        List<UserItem> itemList = userItemManager.getItemList();

        Gson gson = new Gson();
        System.out.println("UserItemManagerTest, retrieved items: ");
        for( UserItem userItem : itemList ) {
            System.out.println( gson.toJson(userItem) );
        }
    }
}
