package com.server.liveowl.repository;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ResultRepository extends JpaRepository<Result, String> {
    List<Result> getResultsByExamExamId(String examId);
    List<Result> getResultsByExamAccount(Account account);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO result (result_id, link_video, link_key_board, account_id, exam_id) VALUES (?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    void saveToResultTable(String resultId, String linkVideo, String linkKeyBoard, String accountId, String examId);
}
