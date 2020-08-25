package com.omnitech.cryptune.ChatsPackage;

public class ChatObject {
    String Name;
    String FCM;
    String PUK;
    String Phone;

    public ChatObject(String name, String FCM, String PUK, String phone) {
        Name = name;
        this.FCM = FCM;
        this.PUK = PUK;
        this.Phone = phone;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getFCM() {
        return FCM;
    }

    public void setFCM(String FCM) {
        this.FCM = FCM;
    }

    public String getPUK() {
        return PUK;
    }

    public void setPUK(String PUK) {
        this.PUK = PUK;
    }
}
