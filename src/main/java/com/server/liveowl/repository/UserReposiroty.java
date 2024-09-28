package com.server.liveowl.repository;

import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.AccountInfor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserReposiroty extends JpaRepository<Account, UUID> {
//    List<Users> findByUserNameAndPassWord(String username, String password);
//
//    List<Users> findByUserNameAndEmail(String username, String email);
//
//    List<Users> findByUserNameOrEmail(String username, String email);

    List<Account> findByEmail(String email);
    AccountInfor findByAccountId(UUID accountId);
}
