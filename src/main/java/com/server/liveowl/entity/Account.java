package com.server.liveowl.entity;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Account {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(name ="email" )
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private int role;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AccountInfor accountInfor;

    public Account() {

    }
    public Account(String accountId,String email, String password, int role) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public AccountInfor getAccountInfor() {
        return accountInfor;
    }

    public void setAccountInfor(AccountInfor accountInfor) {
        this.accountInfor = accountInfor;
    }
}
