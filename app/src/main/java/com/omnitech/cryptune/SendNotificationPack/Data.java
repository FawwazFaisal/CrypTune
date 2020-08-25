package com.omnitech.cryptune.SendNotificationPack;

public class Data {
    private String Name;
    private String Message;
    private String FCMToken;
    private String PublicKey;

    public Data(String name, String message, String publicKey, String phone, String FCMToken) {
        Name = name;
        Message = message;
        PublicKey = publicKey;
        this.FCMToken = FCMToken;
    }

    public String getFCMToken() {
        return FCMToken;
    }

    public void setFCMToken(String FCMToken) {
        this.FCMToken = FCMToken;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getPublicKey() {
        return PublicKey;
    }

    public void setPublicKey(String publicKey) {
        this.PublicKey = publicKey;
    }
}
