package com.server.liveowl.controller;

import com.server.liveowl.payload.Responsetdata;
import com.server.liveowl.payload.request.SingupRequest;
import com.server.liveowl.service.imp.LoginServiceImp;
import com.server.liveowl.ustil.JwtUstilHelper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.concurrent.CompletableFuture;

@CrossOrigin("*")
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    LoginServiceImp loginServiceImp;

    @Autowired
    JwtUstilHelper jwtUstilHelper;

    @PostMapping("/singin")
    public ResponseEntity<?> singin(@RequestParam String email, @RequestParam String password) {
        Responsetdata responsetdata = new Responsetdata();

        // tạo key cho api
//        SecretKey key = Jwts.SIG.HS256.key().build(); //or HS384.key() or HS512.key()
//        String secretString = Encoders.BASE64.encode(key.getEncoded());
//        System.out.println(secretString);

        if(loginServiceImp.checkLogin(email, password))
        {
            int role = loginServiceImp.getUserRole(email);
            String token = jwtUstilHelper.generateToken(email, role);
            responsetdata.setData(token);
        }
        else
        {
            responsetdata.setData("");
            responsetdata.setIssucess(false);
        }

        return new ResponseEntity<>(responsetdata, HttpStatus.OK);
    }

    @PostMapping("/singup")
    public ResponseEntity<?> singup(@RequestBody SingupRequest singupRequest) {
        Responsetdata responsetdata = new Responsetdata();
        responsetdata.setData(loginServiceImp.addUser(singupRequest));
        return new ResponseEntity<>(responsetdata, HttpStatus.OK);
    }

//    @PostMapping("/getAllUser")
//    public ResponseEntity<?> getAllUser() {
//        Responsetdata responsedata = new Responsetdata();
//        responsedata.setData(loginServiceImp.getAllUser());
//        return new ResponseEntity<>(responsedata, HttpStatus.OK);
//    }
}
