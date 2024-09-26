package com.server.liveowl.service;

import com.server.liveowl.dto.UserDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.AccountInfor;
import com.server.liveowl.entity.Roles;
import com.server.liveowl.entity.Users;
import com.server.liveowl.payload.request.SingupRequest;
import com.server.liveowl.repository.AccountInforRepository;
import com.server.liveowl.repository.AccountInforRepository;
import com.server.liveowl.repository.AccountReposiroty;
import com.server.liveowl.repository.AccountReposiroty;
import com.server.liveowl.service.imp.LoginServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class LoginService implements LoginServiceImp {
    @Autowired
    AccountReposiroty accountReposiroty;

    @Autowired
    AccountInforRepository accountInforRepository;

    @Override
    public Boolean checkLogin(String email, String rawPassword) {
        List<Account> listaccount = accountReposiroty.findByEmail(email);
        if (!listaccount.isEmpty()) {
            Account account = listaccount.get(0);
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(rawPassword, account.getPassword())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean addUser(SingupRequest singupRequest) {
        String password = singupRequest.getPassword();
        String email = singupRequest.getEmail();
        String fullname = singupRequest.getFullname();
        Date dateofbirth = singupRequest.getDateofbirth();
        int role = singupRequest.getRole();
        Boolean gender = singupRequest.getGender();

        List<Account> listaccount = accountReposiroty.findByEmail(email);

        if (listaccount.size() == 0) {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String hashedPassword = passwordEncoder.encode(password);
                UUID accountid = UUID.randomUUID();

                Account account = new Account();
                account.setAccountId(accountid);
                account.setEmail(email);
                account.setPassword(hashedPassword);
                account.setRole(role);

                AccountInfor accountInfor = new AccountInfor();
                accountInfor.setAccountId(accountid);
                accountInfor.setFullName(fullname);
                accountInfor.setDateOfBirth(dateofbirth);
                accountInfor.setGender(gender);
                accountInfor.setAccount(account);  // Liên kết AccountInfor với Account

                account.setAccountInfor(accountInfor);  // Liên kết Account với AccountInfor

                accountReposiroty.save(account);  // Lưu account vào cơ sở dữ liệu
                return true;
        }
        return false;
    }

    @Override
    public int getUserRole(String email) {
        int role = accountReposiroty.findByEmail(email).get(0).getRole();
        return role;
    }
}
