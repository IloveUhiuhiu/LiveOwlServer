package com.server.liveowl.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Logger {
    @Id
    @Column(name ="logger_id")
    private UUID loggerId;

    @Column(name ="content_of_logger")
    private String contentOfLogger;

    @Column(name ="time_of_logger")
    private LocalDateTime timeOfLogger;

    @ManyToOne
    @JoinColumn(name = "result_id", referencedColumnName = "result_id", insertable = false, updatable = false)
    private Result result;

    public UUID getLoggerId() {
        return loggerId;
    }
    public void setLoggerId(UUID loggerId) {
        this.loggerId = loggerId;
    }
    public String getContentOfLogger() {
        return contentOfLogger;
    }
    public void setContentOfLogger(String contentOfLogger) {
        this.contentOfLogger = contentOfLogger;
    }
    public LocalDateTime getTimeOfLogger() {
        return timeOfLogger;
    }
    public void setTimeOfLogger(LocalDateTime timeOfLogger) {
        this.timeOfLogger = timeOfLogger;
    }
    public Result getResult() {
        return result;
    }
    public void setResult(Result result) {
        this.result = result;
    }

}
