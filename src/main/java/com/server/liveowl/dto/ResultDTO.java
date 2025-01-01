package com.server.liveowl.dto;


public class ResultDTO {
    private String resultId;
    private String studentId;
    private String name;
    private String examId;

    public ResultDTO(String resultId, String studentId,String name, String examId) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.name = name;
        this.examId = examId;
    }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
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
