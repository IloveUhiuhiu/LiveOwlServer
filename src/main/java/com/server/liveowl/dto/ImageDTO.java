package com.server.liveowl.dto;


public class ImageDTO {
    private String clientId;
    private byte[] image;
    public ImageDTO(String clientId, byte[] image) {
        this.clientId = clientId;
        this.image = image;
    }
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;

    }
    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
}
