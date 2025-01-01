package com.server.liveowl.service.imp;

import com.server.liveowl.dto.AccountDetailDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.AccountInfor;
import com.server.liveowl.payload.request.SingupRequest;
import com.server.liveowl.payload.request.UploadAvtRequest;

import java.time.LocalDate;
import java.util.List;

public interface UserServiceImp {

    List<String> getAllAccountId();
    Boolean checkLogin(String email, String password);
    Boolean  addUser(SingupRequest singupRequest);
    int getUserRole(String email);
    Account getAccountByEmail(String email);
    AccountDetailDTO getAccountDetail(String email);
    boolean uploadAVT(String email, String imageBase64);
    Account getAccountById(String accountId);
    AccountDetailDTO getAccountInforById(String accountId);
    boolean updateInfo(String emailTonken, String name, String emailnew, LocalDate dateofbirth, Boolean gender);
}
