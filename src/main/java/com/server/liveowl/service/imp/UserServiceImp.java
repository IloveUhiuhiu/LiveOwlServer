package com.server.liveowl.service.imp;



import com.server.liveowl.dto.AccountDetailDTO;
import com.server.liveowl.payload.request.SingupRequest;

public interface UserServiceImp {

    //    List<UserDTO> getAllUser();
    Boolean checkLogin(String email, String password);
    Boolean  addUser(SingupRequest singupRequest);
    int getUserRole(String email);
    AccountDetailDTO getAccountDetail(String email);
}
