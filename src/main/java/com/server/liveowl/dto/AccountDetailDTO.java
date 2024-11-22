package com.server.liveowl.dto;

import java.sql.Blob;
import java.time.LocalDate;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountDetailDTO {

    private String accountId;
    private String email;
    private int role;
    private String fullName;
    private LocalDate dateOfBirth;
    private Boolean gender;
  //  private Blob profile;
    private String profile;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    // Constructor, getters, setters

    public AccountDetailDTO(String accountId, String email, int role, String fullName, LocalDate dateOfBirth,
                            Boolean gender, String profile, LocalDateTime createAt, LocalDateTime updateAt) {
        this.accountId = accountId;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.profile = profile;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

//    public Blob getProfile() {
//        return profile;
//    }
//
//    public void setProfile(Blob profile) {
//        this.profile = profile;
//    }
    public String getProfile() {
        return profile;
    }
    public void setProfile(String profile) {
        this.profile = profile;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}
