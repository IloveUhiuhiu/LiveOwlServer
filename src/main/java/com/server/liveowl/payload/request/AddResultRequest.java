package com.server.liveowl.payload.request;

public class AddResultRequest {
        private String linkVideo;
        private String linkKeyBoard;
        private String studentId;
        private String examId;


        public AddResultRequest(String linkVideo, String linkKeyBoard, String studentId, String examId) {
            this.linkVideo = linkVideo;
            this.linkKeyBoard = linkKeyBoard;
            this.studentId = studentId;
            this.examId = examId;
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

