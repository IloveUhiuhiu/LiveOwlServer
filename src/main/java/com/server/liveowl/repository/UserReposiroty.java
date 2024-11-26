package com.server.liveowl.repository;

import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.AccountInfor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserReposiroty extends JpaRepository<Account, String> {
    List<Account> findByEmail(String email);
    Account findByAccountId(String accountId);
    List<Account> findAll();

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.email = :email WHERE a.accountId = :accountId")
    int updateAccount(@Param("accountId") String accountId, @Param("email") String email);
}


