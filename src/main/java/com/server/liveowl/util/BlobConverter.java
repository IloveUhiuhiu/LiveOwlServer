package com.server.liveowl.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class BlobConverter {

public static String blobToBase64(Blob blob) throws Exception
{
    if (blob == null)
    {
        return null;
    }
    InputStream inputStream = blob.getBinaryStream();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1)
    {
        outputStream.write(buffer, 0, bytesRead);
    }
    byte[] blobBytes = outputStream.toByteArray();
    return Base64.getEncoder().encodeToString(blobBytes);
}

public static Blob bytesToBlob(byte[] data, Connection connection) throws Exception {
    if (data == null || connection == null) {
        throw new IllegalArgumentException("Data or Connection cannot be null");
    }
    Blob blob = connection.createBlob();
    blob.setBytes(1, data);
    return blob;
}

}
