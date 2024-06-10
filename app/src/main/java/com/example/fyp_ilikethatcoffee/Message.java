package com.example.fyp_ilikethatcoffee;

public class Message {
    private String senderId;
    private String senderName;
    private String messageContent;
    private String messageType;
    private String timestamp;

    // Default constructor (required for Firebase)
    public Message() {
    }

    // Constructor with all fields
    public Message(String senderId, String senderName, String messageContent, String messageType, String timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.timestamp = timestamp;
    }

    // Getter methods for all fields
    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Setter methods for all fields
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
