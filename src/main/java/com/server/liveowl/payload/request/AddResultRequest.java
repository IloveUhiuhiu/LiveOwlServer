package com.server.liveowl.payload.request;

import java.util.List;

public class AddResultRequest {
        private List<String> linkVideo;
        private List<String> linkKeyBoard;
        private List<String> studentId;
        private String examId;

        public AddResultRequest(List<String> linkVideo, List<String> linkKeyBoard, List<String> studentId, String examId) {
            this.linkVideo = linkVideo;
            this.linkKeyBoard = linkKeyBoard;
            this.studentId = studentId;
            this.examId = examId;
        }

        public List<String> getLinkVideo() {
            return linkVideo;
        }
        public void setLinkVideo(List<String>  linkVideo) {
            this.linkVideo = linkVideo;
        }
        public List<String>  getLinkKeyBoard() {
            return linkKeyBoard;
        }
        public void setLinkKeyBoard(List<String>  linkKeyBoard) {
            this.linkKeyBoard = linkKeyBoard;
        }
        public List<String> getStudentId() {
            return studentId;
        }
        public void setStudentId(List<String> studentId) {
            this.studentId = studentId;
        }
        public String getExamId() {
            return examId;
        }
        public void setExamId(String examId) {
            this.examId = examId;
        }


    }

