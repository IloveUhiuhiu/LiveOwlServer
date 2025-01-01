package com.server.liveowl.service;
import com.server.liveowl.dto.ResultDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Exam;
import com.server.liveowl.entity.Result;
import com.server.liveowl.payload.request.AddResultRequest;
import com.server.liveowl.repository.ResultRepository;
import com.server.liveowl.service.imp.ExamServiceImp;
import com.server.liveowl.service.imp.ResultServiceImp;
import com.server.liveowl.service.imp.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResultService implements ResultServiceImp {

    private final ResultRepository resultRepository;

    private final UserServiceImp userService;
    private final ExamServiceImp examService;


    @Autowired
    public ResultService(ResultRepository resultRepository, UserServiceImp userService, ExamServiceImp examService) {
        this.resultRepository = resultRepository;
        this.userService = userService;
        this.examService = examService;

    }


    @Override
    public Result getResultById(String resultId) {
        Result result =  resultRepository.findById(resultId).orElseThrow(() -> new RuntimeException("Result not found."));
        return  result;
    }

    @Override
    public ResultDTO convertToDto(Result result) {
        return new ResultDTO(result.getResultId(),result.getAccount().getAccountId(),result.getAccount().getAccountInfor().getFullName(),result.getExam().getExamId());
    }

    @Override
    public List<Result> getResultsByExam(String examId) {
        return resultRepository.getResultsByExamExamId(examId);
    }

    @Override
    public List<ResultDTO> getConvertedResults(List<Result> results) {
        return  results.stream().map(this::convertToDto).collect(Collectors.toList());
    }

//    @Override
//    public Result addResult(AddResultRequest request, Account account) {
//        Account student = userService.getAccountById(request.getStudentId());
//        Exam exam = examService.getExamById(request.getExamId());
//        Result result = new Result();
//        result.setAccount(student);
//        result.setExam(exam);
//        result.setLinkKeyBoard(request.getLinkKeyBoard());
//        result.setLinkVideo(request.getLinkVideo());
//        result.setResultId(UUID.randomUUID().toString().substring(0, 8));
//        return  resultRepository.save(result);
//    }

    @Override
    @Transactional
    public void addResult(AddResultRequest request, Account account) {
        List<String> studentId = request.getStudentId();
        List<String> linkVideo = request.getLinkVideo();
        List<String> linkKeyBoard = request.getLinkKeyBoard();
        String examId = request.getExamId();
        int length = request.getStudentId().size();
        for (int i = 0; i < length; i++) {
            Result checkResult = getResultByExamIdAndAccountId(examId, studentId.get(i));
            if (checkResult != null) continue;
            String resultId = UUID.randomUUID().toString().substring(0, 8);
            resultRepository.insertResult(
                    resultId,
                    linkVideo.get(i),
                    linkKeyBoard.get(i),
                    studentId.get(i),
                    examId
            );

        }
    }

    @Override
    public List<Result> getResultsByAccount(Account account) {
        return resultRepository.getResultsByExamAccount(account);
    }


    @Override
    public Result getResultByExamIdAndAccountId(String examId, String accountId) {
        Exam exam = examService.getExamById(examId);
        Account account = userService.getAccountById(accountId);
        return resultRepository.getByExamAndAccount(exam, account);
    }
}
