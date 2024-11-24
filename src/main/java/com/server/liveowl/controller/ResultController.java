package com.server.liveowl.controller;

import com.server.liveowl.dto.ResultDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Result;
import com.server.liveowl.payload.request.AddResultRequest;
import com.server.liveowl.payload.response.Responsedata;
import com.server.liveowl.service.imp.ExamServiceImp;
import com.server.liveowl.service.imp.ResultServiceImp;
import com.server.liveowl.service.imp.UserServiceImp;
import com.server.liveowl.util.JwtUtilHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/results")
public class ResultController {
    private final UserServiceImp userServiceImp;
    private final ExamServiceImp examServiceImp;
    private final ResultServiceImp resultServiceImp;
    private final JwtUtilHelper jwtUtilHelper;
    private Account account;
    @Autowired
    public ResultController(UserServiceImp userServiceImp,ExamServiceImp examServiceImp, ResultServiceImp resultServiceImp, JwtUtilHelper jwtUtilHelper) {
        this.userServiceImp = userServiceImp;
        this.resultServiceImp = resultServiceImp;
        this.jwtUtilHelper = jwtUtilHelper;
        this.examServiceImp = examServiceImp;
    }
    @ModelAttribute
    public void setUserInfo(HttpServletRequest request) {
        String jwtToken = jwtUtilHelper.getTokenFromHeader(request);
        String email = jwtUtilHelper.getEmailFromToken(jwtToken);
        account = userServiceImp.getAccountByEmail(email);

    }
    @PreAuthorize("hasAuthority('ROLE_GIAO_VIEN')")
    @GetMapping("/all/{examId}")
    public ResponseEntity<Responsedata> getAllResultsByExam (@PathVariable String examId) {
        //System.out.println(account.getEmail());
        List<Result> results = resultServiceImp.getResultsByExam(examId);
        List<ResultDTO> resultDTOS = resultServiceImp.getConvertedResults(results);
        return ResponseEntity.ok(new Responsedata("Lấy danh sách bài thi thành công!",resultDTOS));
    }
//    @GetMapping("/all")
//    public ResponseEntity<Responsedata> getAllResults () {
//        //System.out.println(account.getEmail());
//        List<Result> results = resultServiceImp.getResultsByAccount(account);
//        List<ResultDTO> resultDTOS = resultServiceImp.getConvertedResults(results);
//        return ResponseEntity.ok(new Responsedata("Lấy danh sách bài thi thành công!",resultDTOS));
//    }
    @GetMapping("/{resultId}")
    public ResponseEntity<Responsedata> getResultById (@PathVariable String resultId) {
        try {
            Result result = resultServiceImp.getResultById(resultId);
            ResultDTO resultDTO = resultServiceImp.convertToDto(result);
            return ResponseEntity.ok(new Responsedata("Lấy kết quả thành công!",resultDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new Responsedata(e.getMessage(),null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Responsedata> addResult (@RequestBody AddResultRequest request) {
        try {
            Result result = resultServiceImp.addResult(request,account);
            return ResponseEntity.ok(new Responsedata("Thêm kết quả thành công!",result));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new Responsedata(e.getMessage(),null));
        }

    }

}