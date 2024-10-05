package com.server.liveowl.dto;

public class TokenDTO {
    private int role;
    private String token;
    public TokenDTO() {

    }
    public TokenDTO(int role, String token) {
        this.role = role;
        this.token = token;
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
}
