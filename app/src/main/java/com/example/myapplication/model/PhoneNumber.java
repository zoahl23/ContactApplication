package com.example.myapplication.model;

import java.io.Serializable;

public class PhoneNumber implements Serializable {
    private String key, ten, sdt, avt, mail;

    public PhoneNumber() {

    }

    public PhoneNumber(String key, String ten, String sdt, String avt, String mail) {
        this.key = key;
        this.ten = ten;
        this.sdt = sdt;
        this.avt = avt;
        this.mail = mail;
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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
