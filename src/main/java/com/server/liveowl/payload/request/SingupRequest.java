package com.server.liveowl.payload.request;

import java.util.Date;

public class SingupRequest {

    private String email;
    private String password;
    private int role;
    private String fullName;
    private Date dateOfBirth;
    private Boolean gender;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getFullname() {
        return fullName;
    }

    public void setFullname(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public Date getDateofbirth() {
        return dateOfBirth;
    }

    public void setDateofbirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
