package com.server.liveowl.repository;

import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {

    List<Exam> findByAccount(Account account);
}
