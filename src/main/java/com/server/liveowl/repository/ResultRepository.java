package com.server.liveowl.repository;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Exam;
import com.server.liveowl.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, String> {
    List<Result> getResultsByExamExamId(String examId);
    List<Result> getResultsByExamAccount(Account account);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO result (result_id, link_video, link_key_board, account_id, exam_id) " +
            "VALUES (:resultId, :linkVideo, :linkKeyBoard, :accountId, :examId)", nativeQuery = true)
    void insertResult(@Param("resultId") String resultId,
                      @Param("linkVideo") String linkVideo,
                      @Param("linkKeyBoard") String linkKeyBoard,
                      @Param("accountId") String accountId,
                      @Param("examId") String examId);

    Result getByExamAndAccount(Exam exam, Account account);
}
