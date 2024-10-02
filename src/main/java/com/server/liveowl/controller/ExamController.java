package com.server.liveowl.controller;

import com.server.liveowl.dto.ExamDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Exam;
import com.server.liveowl.payload.request.AddExamRequest;
import com.server.liveowl.payload.request.UpdateExamRequest;
import com.server.liveowl.payload.response.Responsedata;
import com.server.liveowl.service.imp.ExamServiceImp;
import com.server.liveowl.service.imp.UserServiceImp;
import com.server.liveowl.util.JwtUtilHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@RestController
@RequestMapping("/exams")
public class ExamController {
    private final UserServiceImp userServiceImp;
    private final ExamServiceImp examServiceImp;
    private final JwtUtilHelper jwtUtilHelper;
    private Account account;
    @Autowired
    public ExamController(UserServiceImp userServiceImp, ExamServiceImp examServiceImp, JwtUtilHelper jwtUtilHelper) {
        this.userServiceImp = userServiceImp;
        this.examServiceImp = examServiceImp;
        this.jwtUtilHelper = jwtUtilHelper;
    }
    @ModelAttribute
    public void setUserInfo(HttpServletRequest request) {
        String jwtToken = jwtUtilHelper.getTokenFromHeader(request);
        String email = jwtUtilHelper.getEmailFromToken(jwtToken);
        account = userServiceImp.getAccountByEmail(email);

    }

    @GetMapping("/all")
    public ResponseEntity<Responsedata> getAllExams () {
        System.out.println(account.getEmail());
        List<Exam> exams = examServiceImp.getExamsByAccount(account);
        List<ExamDTO> examDTOS = examServiceImp.getConvertedExams(exams);
        return ResponseEntity.ok(new Responsedata("Lấy danh sách bài thi thành công!",examDTOS));
    }
    @GetMapping("/{examId}")
    public ResponseEntity<Responsedata> getExamById (@PathVariable String examId) {
        try {
            Exam exam = examServiceImp.getExamById(examId);
            ExamDTO examDTO = examServiceImp.convertToDto(exam);
            return ResponseEntity.ok(new Responsedata("Lấy bài thi thành công!",examDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new Responsedata(e.getMessage(),null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Responsedata> addExam (@RequestBody AddExamRequest request) {
        try {
            Exam exam = examServiceImp.addExam(request,account);
            return ResponseEntity.ok(new Responsedata("Thêm bài thi thành công!",exam));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new Responsedata(e.getMessage(),null));
        }

    }

    @PutMapping("/update/{examId}")
    public ResponseEntity<Responsedata> updateExam (@RequestBody UpdateExamRequest request, @PathVariable String examId) {
        try {
            Exam exam = examServiceImp.updateExam(request,examId);
            return ResponseEntity.ok(new Responsedata("Cập nhật bài thi thành công!",exam));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new Responsedata(e.getMessage(),null));
        }

    }

    @DeleteMapping("/delete/{examId}")
    public ResponseEntity<Responsedata> deleteExam (@PathVariable String examId) {
        try {
            examServiceImp.deleteExamById(examId);
            return ResponseEntity.ok(new Responsedata("Xóa bài thi thành công!",null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new Responsedata(e.getMessage(),null));
        }

    }
}
