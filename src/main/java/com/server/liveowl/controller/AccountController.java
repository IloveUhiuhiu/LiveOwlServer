package com.server.liveowl.controller;

import com.server.liveowl.dto.AccountDetailDTO;
import com.server.liveowl.payload.Responsetdata;
import com.server.liveowl.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    AccountService accountService;

    @PreAuthorize("hasAuthority('ROLE_GIAO_VIEN')")
    @PostMapping("/detail")
    public ResponseEntity<?> getDetailUser() {
        Responsetdata responsetdata = new Responsetdata();

        try {
            // Lấy email từ SecurityContext (đã được xác thực bởi JWT)
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("email controller " + email);

            // Lấy thông tin chi tiết tài khoản
            AccountDetailDTO accountDetail = accountService.getAccountDetail(email);

            if (accountDetail != null) {
                responsetdata.setData(accountDetail);
                responsetdata.setIssucess(true);
                return new ResponseEntity<>(responsetdata, HttpStatus.OK);
            } else {
                responsetdata.setData("Account not found");
                responsetdata.setIssucess(false);
                return new ResponseEntity<>(responsetdata, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            responsetdata.setData("Error: " + e.getMessage());
            responsetdata.setIssucess(false);
            return new ResponseEntity<>(responsetdata, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
