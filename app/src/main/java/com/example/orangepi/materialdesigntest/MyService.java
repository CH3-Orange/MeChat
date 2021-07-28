package com.example.orangepi.materialdesigntest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.orangepi.me.Mail;
import com.example.orangepi.me.User;
import com.example.orangepi.me.UserHelper;
import com.example.orangepi.me.UserMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class MyService extends Service {
    boolean isDestory=false;
    private  String name="111";
    public User nowUser;
    public List<Bundle> userFriends;
    public List<Bundle> userGroups;
    private boolean GetFriendsOver;
    private boolean GetGroupsOver;
    private String OnlineUsers="36";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Service","Created!");
    }

    public String getOnlineUsers() {
        return OnlineUsers;
    }

    public void setOnlineUsers(String onlineUsers) {
        OnlineUsers = onlineUsers;
    }

    public void LetItOver() {
        isDestory=true;
    }

    public void SetNowUser(User user){
        nowUser= new User(user);
        GetFriendsOver=false;
        isDestory=false;
        GetGroupsOver=false;
        GetUserFriends();
        GetUserGroups();
        nowUser.reNewMessageQueue();//重建消息队列
        Log.i("MyService", "SetNowUser: 已设置登入用户并重建消息队列");
        NetConnection();
    }

    public void AddFriend(Friends fri){
        Bundle b=new Bundle();
        b.putString("uid",fri.getUid());
        b.putString("uname",fri.getUserName());
        b.putString("desp",fri.getUserDescription());
        b.putString("sex",fri.getSex());
        userFriends.add(b);
    }
    public void JoinGroup(Friends fri){
        Bundle b=new Bundle();
        b.putString("gid",fri.getUid());
        b.putString("gname",fri.getUserName());
        b.putString("desp",fri.getUserDescription());
        userGroups.add(b);
    }
    public void DelFriend(String uid){
        for(int i=0;i<userFriends.size();i++){
            Bundle b=userFriends.get(i);
            if(b.getString("uid").equals(uid)){
                userFriends.remove(i);
            }
        }

    }
    public void ExitGroup(String gid){
        for(int i=0;i<userGroups.size();i++){
            Bundle b=userGroups.get(i);
            if(b.getString("gid").equals(gid)){
                userGroups.remove(i);
            }
        }

    }

    private void GetUserFriends(){
        new Thread(()->{
            if(nowUser==null){
                Log.e("MyService", "GetUserFriends: 用户为null");
                GetFriendsOver=true;
                return;
            }
            Bundle result = UserHelper.GetUserFriendInDatabase(nowUser.Id);
            if(result.getString("code").equals("1")){
                userFriends=(List<Bundle>)result.getSerializable("ans");
                Log.i("MyService", "GetUserFriends: 已获取到用户好友列表 cnt="+userFriends.size());
                GetFriendsOver=true;
                return ;
            }
        }).start();
    }

    private void GetUserGroups(){
        new Thread(()->{
            if(nowUser==null){
                Log.e("MyService", "GetUserFriends: 用户为null");
                GetGroupsOver=true;
                return;
            }
            Bundle result = UserHelper.GetUserGroupInDatabase(nowUser.Id);
            if(result.getString("code").equals("1")){
                userGroups=(List<Bundle>)result.getSerializable("ans");
                Log.i("MyService", "GetUserFriends: 已获取到用户群组列表 cnt="+userGroups.size());
                GetGroupsOver=true;
                return ;
            }
        }).start();
    }


    private void NetConnection(){
        //网络连接 建立线程
        new Thread(() -> {
            while(!GetFriendsOver);
            while (!GetGroupsOver);//只有获取到好友列表和群组信息后才能连接服务器，目的是为了卡住不进入主页面,主页面需要用户好友和群组列表的信息
            while(!isDestory){
                Log.i("ClierntUser", "NetConnection: 开始连接");
//                try(Socket socket = new Socket("192.168.142.1",10083))
                try(Socket socket = new Socket("***.***.***.***",00000))
                {
                    try(InputStream is= socket.getInputStream())
                    {
                        try(OutputStream os =socket.getOutputStream())
                        {
                            Log.i("ClientUser", "ConnectServer: success");
                            IOHandle(is,os);
                        }
                    }
                }catch (Exception ee)
                {
                    Log.e("ClientUser", "ConnectServer: "+ee.getMessage());
                    sendDisConnectBroadcast();
                }
//                    sendContentBroadcast(name);
//                    Log.i("SendBroadcast","NetConnection"+name);
            }
            Log.i("SendBroadcast","Destoryed!"+name);
        }).start();

    }

    private void IOHandle(InputStream is,OutputStream os)throws Exception{
        ObjectOutputStream writer = new ObjectOutputStream(os);
        ObjectInputStream reader = new ObjectInputStream(is);
        writer.writeObject(new Mail(Mail.TYPE.USER,nowUser));//先把自己的信息传过去
        //writer.writeObject(new Mail(Mail.TYPE.STR,"online_users"));//请求在线好友
        UserMessage sendMessage;
        Thread waitNewMessage = new Thread(()->{//接收消息线程
            Mail acceptMessage;
            while(!isDestory){
                try {
                    acceptMessage=(Mail) reader.readObject();
                    if(acceptMessage==null)continue;
                    switch (acceptMessage.type){
                        case MESSAGE:
                            UserMessage msg=(UserMessage)acceptMessage.msg;
                            String SrcUserId=msg.SourceId;
                            Bundle bundle = new Bundle();
                            bundle.putString("code","1");
                            bundle.putSerializable("MSG",msg);
                            BroadcastBundle(bundle);
                            Log.i("MSG", "IOHandle: 接收到一条消息"+msg.toString());
                            //接下来要保存消息
                            break;
                        case STR:
                            BroadcastStr((String)acceptMessage.msg);
                            Log.i("STR", "IOHandle: 接收一条字符串:"+acceptMessage.msg);
                            //接下来要保存消息
                            break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        waitNewMessage.start();
        while(!isDestory)//发送消息线程
        {
            try{
                sendMessage=nowUser.GetMessage();
                if(sendMessage!=null){
                    Mail mail = new Mail(Mail.TYPE.MESSAGE,sendMessage);
                    writer.writeObject(mail);//向socket流发送消息
                    Log.i("SendMessage", "IOHandle: 发送完毕"+ sendMessage.Message);
                }
                else{
                    //System.out.println("no input now");
                }


            }catch (Exception ee)
            {
                Log.e("SendMessage", "IOHandle: "+ee.getMessage());
                break;
            }
        }



    }

    protected void sendDisConnectBroadcast(){
        Intent intent = new Intent();
        intent.setAction("com.example.orangepi.content");
        intent.putExtra("服务器断开连接","");
        sendBroadcast(intent);
    }

    protected void BroadcastStr(String str){
        Intent intent = new Intent();
        intent.setAction("com.example.orangepi.content");
        intent.putExtra("return",str);
        sendBroadcast(intent);
    }
    protected void BroadcastBundle(Bundle bundle){
        Intent intent = new Intent();
        intent.setAction("com.example.orangepi.content");
        intent.putExtra("MSG",bundle);
        sendBroadcast(intent);
    }

    protected void sendContentBroadcast(String name) {
        Intent intent = new Intent();
        intent.setAction("com.example.orangepi.content");
        intent.putExtra("name",name);

        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestory=true;
        Log.i("Service","Destoryed!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }
    public class MyBinder extends Binder {
        public  MyService getService(){
            return MyService.this;
        }



    }
}
