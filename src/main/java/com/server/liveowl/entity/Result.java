package com.server.liveowl.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Result {
    @Id
    @Column(name = "result_id")
    private String resultId;

    @Column(name = "link_video")
    private String linkVideo;

    @Column(name = "link_key_board")
    private String linkKeyBoard;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id", insertable = false, updatable = false)
    private Exam exam;

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
    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
    }
    public Exam getExam() {
        return exam;
    }
    public void setExam(Exam exam) {
        this.exam = exam;
    }

}
