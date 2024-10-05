package com.server.liveowl.payload.request;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

public class AddExamRequest {

    private String nameOfExam;

    private String subjectOfExam;

    private LocalDateTime startTimeOfExam;

    private int durationOfExam;

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

}
