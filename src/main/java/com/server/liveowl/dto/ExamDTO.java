package com.server.liveowl.dto;

import java.time.LocalDateTime;

public class ExamDTO {
    private String examId;
    private String nameOfExam;

    private String subjectOfExam;

    private LocalDateTime startTimeOfExam;

    private int durationOfExam;

    private String codeOfExam;

    public ExamDTO(String examId, String nameOfExam, String subjectOfExam, LocalDateTime startTimeOfExam,int durationOfExam, String codeOfExam) {
        this.examId = examId;
        this.nameOfExam = nameOfExam;
        this.subjectOfExam = subjectOfExam;
        this.startTimeOfExam = startTimeOfExam;
        this.durationOfExam = durationOfExam;
        this.codeOfExam = codeOfExam;

    }
    public String getExamId() {
        return examId;
    }
    public void setExamId(String examId) {
        this.examId = examId;
    }

    public void setNameOfExam(String nameOfExam) {
        this.nameOfExam = nameOfExam;
    }
    public void setSubjectOfExam(String subjectOfExam) {
        this.subjectOfExam = subjectOfExam;
    }
    public void setStartTimeOfExam(LocalDateTime startTimeOfExam) {
        this.startTimeOfExam = startTimeOfExam;
    }
    public void setDurationOfExam(int durationOfExam) {
        this.durationOfExam = durationOfExam;
    }
    public String getNameOfExam() {
        return nameOfExam;
    }
    public String getSubjectOfExam() {
        return subjectOfExam;
    }
    public LocalDateTime getStartTimeOfExam() {
        return startTimeOfExam;
    }
    public int getDurationOfExam() {
        return durationOfExam;
    }
    public String getCodeOfExam() {
        return codeOfExam;
    }
    public void setCodeOfExam(String codeOfExam) {
        this.codeOfExam = codeOfExam;
    }

}
