package com.server.liveowl.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Exam {
    @Id
    @Column(name = "exam_id")
    private UUID examId;

    @Column(name ="name_of_exam" )
    private String nameOfExam;

    @Column(name ="subject_of_exam" )
    private String subjectOfExam;

    @Column(name ="start_time_of_exam" )
    private LocalDateTime startTimeOfExam;

    @Column(name ="duration_of_exam" )
    private int durationOfExam;

    @Column(name ="code_of_exam" )
    private String codeOfExam;

    public void setExamId(UUID examId) {
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
    public void setCodeOfExam(String codeOfExam) {
        this.codeOfExam = codeOfExam;
    }
    public UUID getExamId() {
        return examId;
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

}
