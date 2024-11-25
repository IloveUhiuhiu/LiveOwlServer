package com.server.liveowl.repository;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResultRepository extends JpaRepository<Result, String> {
    List<Result> getResultsByExamExamId(String examId);
    List<Result> getResultsByExamAccount(Account account);
}
