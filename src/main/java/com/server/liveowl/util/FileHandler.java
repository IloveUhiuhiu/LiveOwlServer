package com.server.liveowl.util;

import java.io.File;

public class FileHandler {
    public static boolean checkAndCreateFolder(String folderPath) {
        File folder = new File(folderPath);

        if (!folder.exists()) {

            boolean wasCreated = folder.mkdirs();
            if (wasCreated) {
                System.out.println("Thư mục đã được tạo: " + folderPath);
                return true;
            } else {
                System.err.println("Không thể tạo thư mục: " + folderPath);
                return false;
            }
        } else {
            System.out.println("Thư mục đã tồn tại: " + folderPath);
            return true;
        }
    }

}
