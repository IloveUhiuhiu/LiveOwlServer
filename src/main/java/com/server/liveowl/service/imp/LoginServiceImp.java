package com.server.liveowl.service.imp;



import com.server.liveowl.dto.UserDTO;
import com.server.liveowl.payload.request.SingupRequest;
import com.server.liveowl.service.LoginService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LoginServiceImp {

    //    List<UserDTO> getAllUser();
    Boolean checkLogin(String email, String password);
    Boolean  addUser(SingupRequest singupRequest);
    int getUserRole(String email);
}
