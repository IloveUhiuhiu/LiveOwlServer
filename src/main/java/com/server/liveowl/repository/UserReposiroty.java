package com.server.liveowl.repository;

import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.AccountInfor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserReposiroty extends JpaRepository<Account, String> {
    List<Account> findByEmail(String email);
    Account findByAccountId(String accountId);
    List<Account> findAll();
}


