package com.example.orangepi.me;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerUser {
    UsersMap usersMap=new UsersMap();
    public enum UserStat{
        ONLINE,OUTLINE,ERR
    }
    public void Start() {
        System.out.println("started!");
        usersMap.pushUser(new User("10","cz"),UserStat.OUTLINE);
        try (ServerSocket ss = new ServerSocket(10083)) {

            while (true) {
                Socket socket = ss.accept();
                System.out.println("connected! " + socket.getRemoteSocketAddress());
                Thread newConnection = new SeverHandler(socket);
                newConnection.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ServerUser su=new ServerUser();
        su.Start();
    }

    private class UsersMap {
        private Map<String,User> users;
        private Map<String, UserStat> userStat;//用户状态
        public UsersMap(){
            this.users= new HashMap<String,User>();
            this.userStat= new HashMap<String,UserStat>();
        }
        public boolean pushUser(User user,UserStat stat){
            if(users.containsKey(user.Id)){
                return false;
            }
            users.put(user.Id, user);
            userStat.put(user.Id, stat);
            return true;
        }
        public User GetUser(String userId){
            if(users.containsKey(userId)){//如果map中有该用户则返回用户对象
                return users.get(userId);
            }
            return null;
        }
        public UserStat GetUserStat(String userId){
            if(userStat.containsKey(userId)){
                return userStat.get(userId);
            }
            return UserStat.ERR;
        }
        public boolean ChangeUserStat(String userId,UserStat stat){
            if(userStat.containsKey(userId)){
                userStat.put(userId,stat);
                return  true;
            }
            return false;
        }
        public boolean PushUserMessage(String userId,UserMessage message){
            UserStat stat=GetUserStat(userId);
            User user=GetUser(userId);
            if(user==null)return false;
            return user.PushMessage(message);
        }
        public UserMessage GetUserMessage(String userId){
            User user=GetUser(userId);
            if(user==null)return null;
            return user.GetMessage();
        }

    }
    private class SeverHandler extends Thread{
        Socket socket;
        User user;
        public SeverHandler(Socket s ){
            socket=s;
        }
        public void handleMessage(UserMessage msg){
            User targetUser=usersMap.GetUser(msg.TargetId);
            usersMap.PushUserMessage(targetUser.Id,msg);
            return;
        }
        @Override
        public void run(){
            try{
                handle(socket.getInputStream(),socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        private void handle(InputStream is, OutputStream os) throws IOException {
            ObjectInputStream reader = new ObjectInputStream(is);
            ObjectOutputStream writer = new ObjectOutputStream(os);

            Thread waitNewMessage = new Thread(()->{//处理接收消息的线程
                UserMessage acceptMessage;
                int cnt=0;
                while(user==null||usersMap.GetUserStat(user.Id)==UserStat.ONLINE){
                    try{
                        Mail mail= (Mail) reader.readObject();
                        if(mail==null)continue;
                        switch (mail.type){
                            case USER://如果发送的是用户信息
                                user=(User)mail.msg;
                                if(usersMap.GetUser(user.Id)==null){
                                    writer.writeObject(new Mail(Mail.TYPE.STR,"无此用户"));
                                }else {
                                    writer.writeObject(new Mail(Mail.TYPE.STR,"登录成功"));
                                }
                                break;
                            case MESSAGE:
                                acceptMessage=(UserMessage) mail.msg;
                                handleMessage(acceptMessage);//处理消息信息
                                break;
                            case BYE:
                                usersMap.ChangeUserStat(user.Id,UserStat.OUTLINE);
                                writer.writeObject(new Mail(Mail.TYPE.STR,"登出成功"));
                                reader.close();
                                writer.close();
                                socket.close();
                                return;
                        }

                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }catch (Exception ee){
                        System.out.println(ee.getMessage());
                    }
                }
            });
            waitNewMessage.start();
            while (true){//发送消息的线程
                try{
                    UserMessage sendMessage=usersMap.GetUserMessage(user.Id);//从当前用户的消息池里获取消息
                    if(sendMessage!=null){
                        writer.writeObject(sendMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
