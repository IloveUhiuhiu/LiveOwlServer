package com.server.liveowl.service;

import com.server.liveowl.dto.AccountDetailDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.AccountInfor;
import com.server.liveowl.repository.AccountInforRepository;
import com.server.liveowl.repository.AccountReposiroty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    AccountReposiroty accountReposiroty;

    @Autowired
    AccountInforRepository accountInforRepository;

    public AccountDetailDTO getAccountDetail(String email) {
        List<Account> account = accountReposiroty.findByEmail(email);
        if (account != null && !account.isEmpty()) {
            AccountInfor accountInfor = accountInforRepository.findByAccountId(account.get(0).getAccountId());
            return new AccountDetailDTO(
                    account.get(0).getAccountId(),
                    account.get(0).getEmail(),
                    account.get(0).getRole(),
                    accountInfor.getFullName(),
                    accountInfor.getDateOfBirth() != null ? new Date(accountInfor.getDateOfBirth().getTime()) : null,  // Handle date conversion
                    accountInfor.getGender(),
                    accountInfor.getProfile(),
                    accountInfor.getCreateAt() != null ? new Date(accountInfor.getCreateAt().getTime()) : null,  // Handle date conversion
                    accountInfor.getUpdateAt() != null ? new Date(accountInfor.getUpdateAt().getTime()) : null   // Handle date conversion
            );
        } else {
            return null;
        }
    }
}
