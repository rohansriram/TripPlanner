package com.example.hw07_group1_9;

public class Message {

    String senderName;
    String time;
    String Date;
    String image;
    String msgContent;
    String senderID;
    String messageID;

    public Message(String senderName, String time, String image, String msgContent,String Date,String senderID,String messageID) {
        this.senderName = senderName;
        this.time = time;
        this.image = image;
        this.msgContent = msgContent;
        this.Date = Date;
        this.senderID = senderID;
        this.messageID =messageID;
    }

    public Message() {
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }


    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }
}
