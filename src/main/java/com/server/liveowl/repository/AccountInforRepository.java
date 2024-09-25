package com.server.liveowl.repository;

import com.server.liveowl.entity.AccountInfor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountInforRepository extends JpaRepository<AccountInfor, UUID> {

}
