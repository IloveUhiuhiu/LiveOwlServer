package com.server.liveowl.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.util.Base64;

public class BlobConverter {
public static String blobToBase64(Blob blob) throws Exception {
    if (blob == null) {
        return null;
    }
    InputStream inputStream = blob.getBinaryStream();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
    }
    byte[] blobBytes = outputStream.toByteArray();
    return Base64.getEncoder().encodeToString(blobBytes);
}
}
