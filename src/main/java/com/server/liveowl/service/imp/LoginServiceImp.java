package com.server.liveowl.service.imp;



import com.server.liveowl.dto.UserDTO;
import com.server.liveowl.payload.request.SingupRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LoginServiceImp {
//    List<UserDTO> getAllUser();
    CompletableFuture<Boolean> checkLogin(String email, String password);
    Boolean  addUser(SingupRequest singupRequest);

}
