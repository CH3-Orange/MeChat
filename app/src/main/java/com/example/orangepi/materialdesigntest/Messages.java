package com.example.orangepi.materialdesigntest;

public class Messages {
    private  String friendName,textMessage;
    private  MessageType type;
    public enum MessageType{
        SEND,RECEIVE
    }


    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public MessageType getType() {
        return type;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public Messages(String friendName, String textMessage, MessageType type) {
        this.friendName = friendName;
        this.textMessage = textMessage;
        this.type = type;
    }

}
