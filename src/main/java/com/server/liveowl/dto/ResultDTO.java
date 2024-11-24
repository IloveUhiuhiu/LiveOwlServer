package com.server.liveowl.dto;

import com.server.liveowl.entity.Account;
import com.server.liveowl.entity.Exam;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class ResultDTO {
    private String resultId;
    private String linkVideo;
    private String linkKeyBoard;
    private String studentId;
    private String examId;


    public ResultDTO(String resultId,String linkVideo, String linkKeyBoard, String studentId, String examId) {
        this.resultId = resultId;
        this.linkVideo = linkVideo;
        this.linkKeyBoard = linkKeyBoard;
        this.studentId = studentId;
        this.examId = examId;
    }

    public String getResultId() {
        return resultId;
    }
    public void setResultId(String resultId) {
        this.resultId = resultId;
    }
    public String getLinkVideo() {
        return linkVideo;
    }
    public void setLinkVideo(String linkVideo) {
        this.linkVideo = linkVideo;
    }
    public String getLinkKeyBoard() {
        return linkKeyBoard;
    }
    public void setLinkKeyBoard(String linkKeyBoard) {
        this.linkKeyBoard = linkKeyBoard;
    }
    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public String getExamId() {
        return examId;
    }
    public void setExamId(String examId) {
        this.examId = examId;
    }


}
