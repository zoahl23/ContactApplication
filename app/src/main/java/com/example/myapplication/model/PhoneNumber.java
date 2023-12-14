package com.example.myapplication.model;

import java.io.Serializable;

public class PhoneNumber implements Serializable {
    String key;
    String ten;
    String sdt;
    String avt;


    public PhoneNumber() {

    }

    public PhoneNumber(String key, String ten, String sdt, String avt) {
        this.key = key;
        this.ten = ten;
        this.sdt = sdt;
        this.avt = avt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getAvt() {
        return avt;
    }

    public void setAvt(String avt) {
        this.avt = avt;
    }

}
