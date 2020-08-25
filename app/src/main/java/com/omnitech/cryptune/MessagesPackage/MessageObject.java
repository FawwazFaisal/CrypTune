package com.omnitech.cryptune.MessagesPackage;

public class MessageObject {
    String Phone;
    String Text;
    String Timestamp;

    public MessageObject(String phone, String text, String timestamp) {
        Phone = phone;
        Text = text;
        Timestamp = timestamp;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }
}
