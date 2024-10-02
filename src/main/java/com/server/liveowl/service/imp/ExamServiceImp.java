package com.server.liveowl.service.imp;

import com.server.liveowl.dto.ExamDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Exam;
import com.server.liveowl.payload.request.AddExamRequest;
import com.server.liveowl.payload.request.UpdateExamRequest;

import java.util.List;

import java.util.UUID;

public interface ExamServiceImp {
    List<Exam> getExamsByAccount(Account account);
    Exam getExamById(String examId);
    Exam addExam(AddExamRequest request, Account account);
    Exam updateExam(UpdateExamRequest request, String examId);
    void deleteExamById(String examId);
    List<ExamDTO> getConvertedExams(List<Exam> exams);
    ExamDTO convertToDto(Exam exam);
}
