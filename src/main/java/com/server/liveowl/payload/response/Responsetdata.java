package com.server.liveowl.payload.response;

import com.google.gson.JsonObject;

public class Responsetdata {
    private String message;
    private Object data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
