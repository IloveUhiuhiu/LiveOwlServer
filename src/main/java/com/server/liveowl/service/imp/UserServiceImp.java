package com.server.liveowl.service.imp;

import com.server.liveowl.dto.AccountDetailDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.payload.request.SingupRequest;

import java.util.List;

public interface UserServiceImp {

    List<String> getAllAccountId();
    Boolean checkLogin(String email, String password);
    Boolean  addUser(SingupRequest singupRequest);
    int getUserRole(String email);
    Account getAccountByEmail(String email);
    AccountDetailDTO getAccountDetail(String email);
    Account getAccountById(String accountId);
}
