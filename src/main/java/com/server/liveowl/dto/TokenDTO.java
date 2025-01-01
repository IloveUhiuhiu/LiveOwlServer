package com.server.liveowl.dto;

public class TokenDTO {
    private int role;
    private String token;
    private String userId;

    public TokenDTO() {

    }

    public TokenDTO(int role, String token,String userId) {
        this.role = role;
        this.token = token;
        this.userId = userId;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
