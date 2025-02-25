package com.server.liveowl.service.imp;

import com.server.liveowl.dto.ResultDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Result;
import com.server.liveowl.payload.request.AddResultRequest;

import java.util.List;

public interface ResultServiceImp {
    Result getResultById(String resultId);
    ResultDTO convertToDto(Result result);
    List<Result> getResultsByExam(String examId);
    List<ResultDTO> getConvertedResults(List<Result> results);
    void addResult(AddResultRequest request, Account account);
    List<Result> getResultsByAccount(Account account);
    Result getResultByExamIdAndAccountId(String examId, String accountId);
}
