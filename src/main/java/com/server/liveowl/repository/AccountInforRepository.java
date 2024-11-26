package com.server.liveowl.repository;

import com.server.liveowl.entity.AccountInfor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Blob;
import java.time.LocalDate;
import java.util.UUID;

public interface AccountInforRepository extends JpaRepository<AccountInfor, String> {
    AccountInfor findByAccountId(String accountId);

    @Modifying
    @Transactional
    @Query("UPDATE AccountInfor a SET a.profile = :profile WHERE a.accountId = :accountId")
    int updateProfile(@Param("profile") Blob profile, @Param("accountId") String accountId);

    @Modifying
    @Transactional
    @Query("UPDATE AccountInfor ai SET ai.fullName = :fullName, ai.dateOfBirth = :dateOfBirth, " +
            "ai.gender = :gender " + "WHERE ai.accountId = :accountId")
    int updateAccountInfor(@Param("accountId") String accountId, @Param("fullName") String fullName, @Param("dateOfBirth") LocalDate dateOfBirth, @Param("gender") Boolean gender);
}
