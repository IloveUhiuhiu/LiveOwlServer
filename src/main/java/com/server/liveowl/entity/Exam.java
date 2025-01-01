package com.server.liveowl.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
public class Exam {
    @Id
    @Column(name = "exam_id")
    private String examId;

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

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = true, updatable = true)
    private Account account;
    public Exam () {
    }
    public Exam(String nameOfExam,String subjectOfExam, LocalDateTime startTimeOfExam, int durationOfExam, Account account) {
        this.nameOfExam = nameOfExam;
        this.subjectOfExam = subjectOfExam;
        this.startTimeOfExam = startTimeOfExam;
        this.durationOfExam = durationOfExam;
        this.account = account;
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

    public void setCodeOfExam(String codeOfExam) {
        this.codeOfExam = codeOfExam;
    }

    public String getExamId() {
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}
