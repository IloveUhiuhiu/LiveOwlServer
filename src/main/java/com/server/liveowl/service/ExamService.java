package com.server.liveowl.service;

import com.server.liveowl.dto.ExamDTO;
import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Exam;
import com.server.liveowl.payload.request.AddExamRequest;
import com.server.liveowl.payload.request.UpdateExamRequest;
import com.server.liveowl.repository.ExamRepository;
import com.server.liveowl.service.imp.ExamServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamService implements ExamServiceImp {
    private final ExamRepository examRepository;

    @Autowired
    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    public Exam addExam(AddExamRequest request, Account account) {

        Exam exam = new Exam(
                request.getNameOfExam(),
                request.getSubjectOfExam(),
                request.getStartTimeOfExam(),
                request.getDurationOfExam(),
                account
        );
        exam.setExamId(UUID.randomUUID().toString().substring(0, 8));
        exam.setCodeOfExam(UUID.randomUUID().toString().substring(0, 8));
        examRepository.save(exam);
        return exam;
    }


    @Override
    public List<Exam> getExamsByAccount(Account account) {
        return examRepository.findByAccount(account);
    }

    @Override
    public Exam getExamById(String examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found!"));
        return exam;
    }


    @Override
    public Exam updateExam(UpdateExamRequest request, String examId) {
        return examRepository.findById(examId)
                    .map(existingExam -> updateExistingExam(existingExam, request))
                    .map(examRepository::save)
                    .orElseThrow(() -> new RuntimeException("Exam not found!"));

    }
    public Exam updateExistingExam(Exam existingExam, UpdateExamRequest request) {
        existingExam.setNameOfExam(request.getNameOfExam());
        existingExam.setSubjectOfExam(request.getSubjectOfExam());
        existingExam.setStartTimeOfExam(request.getStartTimeOfExam());
        existingExam.setDurationOfExam(request.getDurationOfExam());
        return existingExam;
    };
    @Override
    public void deleteExamById(String examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found!"));
        examRepository.delete(exam);
    }
    @Override
    public List<ExamDTO> getConvertedExams(List<Exam> exams){
        return  exams.stream().map(this::convertToDto).toList();
    }
    @Override
    public ExamDTO convertToDto(Exam exam) {
        ExamDTO examDTO  = new ExamDTO(
                exam.getExamId(),
                exam.getNameOfExam(),
                exam.getSubjectOfExam(),
                exam.getStartTimeOfExam(),
                exam.getDurationOfExam(),
                exam.getCodeOfExam()
        );
        return examDTO;
    }

}
