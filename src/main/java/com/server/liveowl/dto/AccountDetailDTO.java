package com.server.liveowl.dto;

import java.sql.Blob;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountDetailDTO {

    private UUID accountId;
    private String email;
    private int role;
    private String fullName;
    private Date dateOfBirth;
    private Boolean gender;
    private Blob profile;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    // Constructor, getters, setters

    public AccountDetailDTO(UUID accountId, String email, int role, String fullName, Date dateOfBirth,
                            Boolean gender, Blob profile, LocalDateTime createAt, LocalDateTime updateAt) {
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

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
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

    public java.util.Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public Blob getProfile() {
        return profile;
    }

    public void setProfile(Blob profile) {
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
