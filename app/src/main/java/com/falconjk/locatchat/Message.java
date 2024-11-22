package com.falconjk.LocalChat;

// Message.java
public class Message {
    private String senderId;
    private String senderAlias;
    private String content;
    private long timestamp;
    private MessageType type;

    public enum MessageType {
        SENT,
        RECEIVED
    }

    public Message(String senderId, String senderAlias, String content, MessageType type) {
        this.senderId = senderId;
        this.senderAlias = senderAlias;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.type = type;
    }

    // Getters and Setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderAlias() {
        return senderAlias;
    }

    public void setSenderAlias(String senderAlias) {
        this.senderAlias = senderAlias;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}


