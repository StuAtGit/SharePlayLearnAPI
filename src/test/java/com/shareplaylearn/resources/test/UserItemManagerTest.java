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

import com.google.gson.Gson;
import com.shareplaylearn.InternalErrorException;
import com.shareplaylearn.models.UserItem;
import com.shareplaylearn.models.UserItemManager;
import com.shareplaylearn.utilities.OauthPasswordFlow;

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

    OauthPasswordFlow.LoginInfo loginInfo;

    public UserItemManagerTest( OauthPasswordFlow.LoginInfo loginInfo ) {
        this.loginInfo = loginInfo;
    }

    public void testGetFileList() throws IOException, InternalErrorException {
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
