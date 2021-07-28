package com.example.orangepi.me;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class User implements Serializable {
    private transient final Lock lock = new ReentrantLock();
    private transient final Condition condition = lock.newCondition();
    public String Id;
    public String UserName;
    public String Name;
    public String UserSex;
    public String UserDescription;
    private transient Queue<UserMessage> MessageQueue;//不需要序列化

    public User(String id, String name, String userName, String userSex, String userDescription) {
        Id = id;
        Name=name;
        UserName = userName;
        UserSex = userSex;
        UserDescription = userDescription;
    }
    public User(User user) {
        Id = user.Id;
        Name=user.Name;
        UserName = user.UserName;
        UserSex = user.UserSex;
        UserDescription = user.UserDescription;
    }

    public User(String Id,String Name){
        this.Id=Id;
        this.Name=Name;
        MessageQueue= new LinkedList<UserMessage>();
    }

    public boolean reNewMessageQueue(){//重建消息队列，以应对服务器通过socket获取用户对象时没有消息队列的情况
        if(MessageQueue!=null){
            return false;
        }
        MessageQueue= new LinkedList<>();
        return true;
    }

    public UserMessage GetMessage(){
        lock.lock();
        try{
            //System.out.println(super.toString()+" 当前的消息队列有(GET):"+MessageQueue.size());
            if(!MessageQueue.isEmpty()){
                return MessageQueue.remove();
            }
            return null;
        }catch (Exception ee){
            System.out.println(ee.getMessage());
        }finally {
            lock.unlock();
        }
        return null;
    }

    public boolean isEmpty(){
        return MessageQueue.isEmpty();
    }
    public boolean PushMessage(UserMessage message){
        lock.lock();
        try{
            //System.out.println(super.toString()+" 当前的消息队列有(PUSH):"+MessageQueue.size());
            return MessageQueue.offer(message);
        }catch (Exception ee){
            System.out.println(ee.getMessage());
        }finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public String toString(){
        return "id="+Id+",name="+Name+",userName="+UserName+",sex="+UserSex+",desp="+UserDescription;
    }
}
