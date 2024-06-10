package com.example.fyp_ilikethatcoffee;

import java.util.List;

public class Conversations {
    private String id; // Add a field to store the conversation ID
    private List<String> UserIDs;
    private String lastMessageContent;
    private String lastMessageTimeStamp;
    // Add more fields as needed for your use case, e.g., subcollection for messages

    public Conversations() {
        // Default constructor required for Firestore
    }

    public Conversations(String id, List<String> UserIDs, String lastMessageContent, String lastMessageTimeStamp) {
        this.id = id;
        this.UserIDs = UserIDs;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageTimeStamp = lastMessageTimeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getUserIDs() {
        return UserIDs;
    }

    public void setUserIDs(List<String> UserIDs) {
        this.UserIDs = UserIDs;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public String getLastMessageTimeStamp() {
        return lastMessageTimeStamp;
    }

    public void setLastMessageTimeStamp(String lastMessageTimeStamp) {
        this.lastMessageTimeStamp = lastMessageTimeStamp;
    }
}
