package com.server.liveowl.controller;

import com.server.liveowl.payload.Responsedata;
import com.server.liveowl.payload.request.SingupRequest;
import com.server.liveowl.service.imp.LoginServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@CrossOrigin("*")
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    LoginServiceImp loginServiceImp;

    @PostMapping("/singin")
    public CompletableFuture<ResponseEntity<?>> singin(@RequestParam String username, @RequestParam String password) {
        Responsedata responsedata = new Responsedata();
        return loginServiceImp.checkLogin(username, password)
                .thenApply(result -> {
                    responsedata.setData(result);
                    return new ResponseEntity<>(responsedata, HttpStatus.OK);
                });
    }

    @PostMapping("/singup")
    public ResponseEntity<?> singup(@RequestBody SingupRequest singupRequest) {
        Responsedata responsedata = new Responsedata();
        responsedata.setData(loginServiceImp.addUser(singupRequest));
        return new ResponseEntity<>(responsedata, HttpStatus.OK);
    }

    @PostMapping("/getAllUser")
    public ResponseEntity<?> getAllUser() {
        Responsedata responsedata = new Responsedata();
        responsedata.setData(loginServiceImp.getAllUser());
        return new ResponseEntity<>(responsedata, HttpStatus.OK);
    }
}
