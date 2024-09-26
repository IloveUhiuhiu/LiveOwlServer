package com.server.liveowl.payload;

public class Responsetdata {
    private int status = 200;
    private  Boolean issucess = true;
    private String desc;
    private  Object data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Boolean getIssucess() {
        return issucess;
    }

    public void setIssucess(Boolean issucess) {
        this.issucess = issucess;
    }
}
