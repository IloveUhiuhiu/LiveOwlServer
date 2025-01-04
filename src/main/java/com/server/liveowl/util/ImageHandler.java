package com.server.liveowl.util;

import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;

public class ImageHandler {

    public static boolean canConvertToImage(byte[] imageBytes) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            Image image = new Image(byteArrayInputStream);
            if (image.getWidth() > 0 && image.getHeight() > 0) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

}
