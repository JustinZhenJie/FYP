package com.example.fyp_ilikethatcoffee;

public class Friend {
    String Status;
    String receiverId;
    String senderId;

    public Friend() {
    }

    public Friend(String status, String receiverId, String senderId) {
        Status = status;
        this.receiverId = receiverId;
        this.senderId = senderId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
