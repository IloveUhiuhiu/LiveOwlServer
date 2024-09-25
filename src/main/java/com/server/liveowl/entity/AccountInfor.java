package com.server.liveowl.entity;

import jakarta.persistence.*;

import java.sql.Blob;
import java.util.Date;
import java.util.UUID;

@Entity
public class AccountInfor {

    //`accountInforId`, `fullName`, `dateOfBirth`, `gender`, `profile`, `createAt`, `updateAt`
    @Id
    @Column(name = "account_infor_id")
    private UUID accountId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "gender")
    private Boolean gender;

    @Lob
    @Column(name = "profile")
    private Blob profile;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "update_at")
    private Date updateAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_infor_id", referencedColumnName = "accountId")
    private Account account;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDateOfBirth() {
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

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
