package com.example.myapplication.model;


import java.io.Serializable;

public class CallHistory implements Serializable {
    private String id;
    private String number;
    private String type;
    private String timestamp;
    private String duration;

    public CallHistory() {}

    public CallHistory(String id, String number, String type, String timestamp, String duration) {
        this.id = id;
        this.number = number;
        this.type = type;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "CallHistory{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", type='" + type + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
