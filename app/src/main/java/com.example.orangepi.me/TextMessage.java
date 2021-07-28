package com.example.orangepi.me;

public class TextMessage extends UserMessage {
    //User user;
    public TextMessage(String SourceId,String  TargetId,MsgType msgType){
        super(SourceId,TargetId,msgType);
        Message=null;
    }

    @Override
    public UserMessage ChangeDirection() {
        return new TextMessage(TargetId,SourceId,(String) Message,super.Type);
    }

    public TextMessage(String SourceId,String  TargetId,String message,MsgType msgType){
        super(SourceId,TargetId,msgType);
        Message=message;
    }
    public TextMessage(String SourceId,String  TargetId,String message){
        super(SourceId,TargetId,MsgType.SINGLE);
        Message=message;
    }

    @Override
    public String toString(){
        return ("[From "+SourceId+" to "+TargetId+"]"+(Type==MsgType.GROUP?"GROUP":"SINGLE")+":"+Message);
    }
}

