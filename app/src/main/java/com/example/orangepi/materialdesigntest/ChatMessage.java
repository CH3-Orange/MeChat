package com.example.orangepi.materialdesigntest;

import android.util.Log;

import com.example.orangepi.me.UserMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatMessage {
    private String chatMessageUname;
    private String getChatMessageDescription;
    private String getChatMessageDate;
    private String uid;
    int chatTag=0;//标明该聊天是什么类型的聊天 1为单聊 2为群聊
    private List<UserMessage> MessageQueue;
    private transient final Lock lock = new ReentrantLock();
    private transient final Condition condition = lock.newCondition();


    public ChatMessage(String chatMessageUname, String getChatMessageDescription, String getChatMessageDate, String uid,int ChatTag) {
        this.chatMessageUname = chatMessageUname;
        this.getChatMessageDescription = getChatMessageDescription;
        this.getChatMessageDate = getChatMessageDate;
        this.uid = uid;
        chatTag=ChatTag;
        MessageQueue=new ArrayList<>();
    }

    public String getChatMessageUname() {
        return chatMessageUname;
    }

    public int getChatTag() {
        return chatTag;
    }

    public void setChatMessageUname(String chatMessageUname) {
        this.chatMessageUname = chatMessageUname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGetChatMessageDescription() {
        return getChatMessageDescription;
    }

    public void setGetChatMessageDescription(String getChatMessageDescription) {
        this.getChatMessageDescription = getChatMessageDescription;
    }

    public String getGetChatMessageDate() {
        return getChatMessageDate;
    }

    public void setGetChatMessageDate(String getChatMessageDate) {
        this.getChatMessageDate = getChatMessageDate;
    }


//    public UserMessage GetMessage(){
//        lock.lock();
//        try{
//            //System.out.println(super.toString()+" 当前的消息队列有(GET):"+MessageQueue.size());
//            if(!MessageQueue.isEmpty()){
//                return MessageQueue.remove();
//            }
//            return null;
//        }catch (Exception ee){
//            System.out.println(ee.getMessage());
//        }finally {
//            lock.unlock();
//        }
//        return null;
//    }
    public List<UserMessage>GetUserMessages(){
        return MessageQueue;
    }

    public boolean isEmpty(){
        return MessageQueue.isEmpty();
    }
    public boolean PushMessage(UserMessage message){
        lock.lock();
        try{
            //System.out.println(super.toString()+" 当前的消息队列有(PUSH):"+MessageQueue.size());
            return MessageQueue.add(message);
        }catch (Exception ee){
            Log.e("ChatMessage", "PushMessage: "+ee.getMessage());
        }finally {
            lock.unlock();
        }
        return false;
    }

}
