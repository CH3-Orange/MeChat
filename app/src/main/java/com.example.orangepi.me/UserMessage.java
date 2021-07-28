package com.example.orangepi.me;

import java.io.Serializable;

public abstract class UserMessage implements Serializable,Cloneable {
    public String SourceId;
    public String TargetId;
    public Object Message;
    public String GroupId;
    public MsgType Type;// Type=TEXT&SINGLE
    public enum MsgType{
        SINGLE,GROUP//单向消息，群组消息
    }
    private static final long serialVersionUID=10000L;
    public UserMessage(String  SourceId,String  TargetId,MsgType msgType){
        this.SourceId=SourceId;
        this.TargetId=TargetId;
        Type=msgType;
        switch (Type){
            case GROUP://初始化为群组消息时 群组id为目标id
                GroupId=TargetId;
                break;
            default:
                GroupId="-1";
        }
    }
    public UserMessage(String  SourceId,String  TargetId){
        this.SourceId=SourceId;
        this.TargetId=TargetId;
        Type=MsgType.SINGLE;//默认为单聊消息
        GroupId="-1";
    }
    @Override
    public Object clone(){
        Object copied=null;
        try{
            copied=(UserMessage)super.clone();
        }catch (CloneNotSupportedException ee){
            System.out.println("(Error)UserMessage:"+ee.getMessage());
        }
        return copied;
    }
    //abstract public void ReadInMessage() throws IOException;
    abstract public UserMessage ChangeDirection();
}

