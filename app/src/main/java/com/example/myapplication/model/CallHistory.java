package com.example.myapplication.model;


import java.io.Serializable;

public class CallHistory implements Serializable {
    private String number;
    private String type;
    private long timestamp;
    private int duration;

    public CallHistory() {}

    public CallHistory(String number, String type, long timestamp, int duration) {
        this.number = number;
        this.type = type;
        this.timestamp = timestamp;
        this.duration = duration;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
