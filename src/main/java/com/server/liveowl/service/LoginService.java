package com.server.liveowl.service;

import com.server.liveowl.dto.UserDTO;
import com.server.liveowl.entity.Roles;
import com.server.liveowl.entity.Users;
import com.server.liveowl.payload.request.SingupRequest;
import com.server.liveowl.repository.UserReposiroty;
import com.server.liveowl.service.imp.LoginServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class LoginService implements LoginServiceImp {
    @Autowired
    UserReposiroty userReposiroty;

    @Override
    public List<UserDTO> getAllUser() {
        List<Users> listuser = userReposiroty.findAll();
        List<UserDTO> listuserDTO = new ArrayList<>();
        for (Users user : listuser) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setUserName(user.getUserName());
            userDTO.setFullName(user.getFullName());
            userDTO.setPassWord(user.getPassWord());
            listuserDTO.add(userDTO);
        }
        return listuserDTO;
    }

    @Override
    @Async
    public CompletableFuture<Boolean> checkLogin(String username, String password) {
        List<Users> listuser = userReposiroty.findByUserNameAndPassWord(username, password);
        return CompletableFuture.completedFuture(!listuser.isEmpty());
    }

    @Override
    public Boolean addUser(SingupRequest singupRequest) {
        String username = singupRequest.getUsername();
        String password = singupRequest.getPassword();
        String email = singupRequest.getEmail();
        String confirmpassword = singupRequest.getConfirmpassword();
        List<Users> listuser = userReposiroty.findByUserNameOrEmail(username, email);
        System.out.println(listuser.size());
        if(listuser.size() == 0){
            if(password.equals(confirmpassword)){
                Users user = new Users();
                Roles roles = new Roles();
                roles.setId(singupRequest.getRole_id());
                user.setUserName(singupRequest.getUsername());
                user.setPassWord(singupRequest.getPassword());
                user.setEmail(singupRequest.getEmail());
                user.setRoles(roles);
                userReposiroty.save(user);
                return true;
            }
        }else{
            return false;
        }
        return false;
    }
}
