package com.example.orangepi.me;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.orangepi.materialdesigntest.Friends;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserHelper {
    public static Bundle CheckUserInDatabase(String name, String psw){
        Bundle bundle = new Bundle();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("SELECT uid,uname,salt,psw FROM Users WHERE name=?");
        ){
            ps.setObject(1,name);
            try(ResultSet result =ps.executeQuery()){
                if(result.next()){
                    Long uid=result.getLong("uid");
                    String uname=result.getString("uname");
                    String salt=result.getString("salt");
                    String hash_psw=result.getString("psw");
                    Log.i("CheckUserInDatabase","uid="+uid+" name="+name+" uname="+uname+" salt="+salt+" sha="+hash_psw);
                    if(CheckUserPassword(hash_psw,salt,psw)){
                        bundle.putString("code","1");
                        bundle.putString("uid",uid.toString());
                        bundle.putString("uname",uname);
                    }
                    else {
                        bundle.putString("code","-1");
                        bundle.putString("ERROR","密码错误");
                    }
                }
                else {
                    bundle.putString("code","-1");
                    bundle.putString("ERROR","无此用户");
                }
            }
        }catch(Exception ee){
            Log.e("UserHelper","CheckUserInDatabase: (Connection) "+ee.getLocalizedMessage());
            bundle.putString("code","-1");
            bundle.putString("ERROR",ee.getLocalizedMessage());
        }
        return bundle;
    }

    public static Bundle GetUserFriendInDatabase(String Uid){
        Bundle bundle= new Bundle();
        List<Bundle> ans=new ArrayList<>();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("SELECT U.uid,U.uname,U.desp,U.sex FROM Users U INNER JOIN Friends F ON U.uid = F.friend_id WHERE F.user_id = ?")
        ){
            ps.setObject(1,Long.parseLong(Uid));
            try(ResultSet result = ps.executeQuery()){
                bundle.putString("code","1");
                while(result.next()){
                    Long uid=result.getLong("U.uid");
                    String uname=result.getString("U.uname");
                    String desp=result.getString("U.desp");
                    String sex=result.getString("U.sex");
                    Bundle b=new Bundle();
                    b.putString("uid",uid.toString());
                    b.putString("uname",uname);
                    b.putString("desp",desp);
                    b.putString("sex",sex);
                    ans.add(b);
                }
                bundle.putSerializable("ans",(Serializable)ans);
            }
        }catch(Exception ee) {
            Log.e("UserHelper", "SearchUserFriendInDatabase: (Connection) " + ee.getLocalizedMessage());
            bundle.putString("code", "-1");//标记出错
        }
        return  bundle;
    }

    public static Bundle GetUserGroupInDatabase(String Uid){
        Bundle bundle= new Bundle();
        List<Bundle> ans=new ArrayList<>();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("SELECT gid,gname,desp "+
                    "FROM jdbctest.Groups "+
                    "WHERE EXISTS( "+
                    "SELECT * "+
                    "FROM UserInGroup "+
                    "WHERE UserInGroup.uid=? AND Groups.gid=UserInGroup.gid)")
        ){
            ps.setObject(1,Long.parseLong(Uid));
            try(ResultSet result = ps.executeQuery()){
                bundle.putString("code","1");
                while(result.next()){
                    Long gid=result.getLong("gid");
                    String gname=result.getString("gname");
                    String desp=result.getString("desp");
                    Bundle b=new Bundle();
                    b.putString("gid",gid.toString());
                    b.putString("gname",gname);
                    b.putString("desp",desp);
                    ans.add(b);
                }
                bundle.putSerializable("ans",(Serializable)ans);
            }
        }catch(Exception ee) {
            Log.e("UserHelper", "SearchUserFriendInDatabase: (Connection) " + ee.getLocalizedMessage());
            bundle.putString("code", "-1");//标记出错
        }
        return  bundle;
    }

    public static  Bundle SearchUserFriendInDatabase(String Uid){
        Bundle bundle = new Bundle();
        List<Long> friends_uid = new  ArrayList<>();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("SELECT friend_id FROM Friends WHERE user_id=?")
        ){
            ps.setObject(1,Long.parseLong(Uid));
            try(ResultSet result = ps.executeQuery()){
                while (result.next()){
                    Long friend_id=result.getLong("friend_id");
                    friends_uid.add(friend_id);
                    Log.i("SearchUserFriendInDatabase",Uid+": "+friend_id.toString());
                }
            }
            if(friends_uid.size()>0){
                bundle.putString("code","1");//标记有好友
                bundle.putSerializable("result",(Serializable)friends_uid);
            }
            else {
                bundle.putString("code","0");
            }
        }
        catch(Exception ee){
            Log.e("UserHelper","SearchUserFriendInDatabase: (Connection) "+ee.getLocalizedMessage());
            bundle.putString("code","-1");//标记出错

    }
        return bundle;
    }

    public static Bundle CheckFriendsInBundle(Bundle src, Bundle friend) {
        if(friend.getString("code").equals("1")){
            List<Long> friends_uid = (List<Long>)friend.getSerializable("result");
            List<Friends> friends = (List<Friends>)src.getSerializable("result");
            List<Friends> friends_ans = new ArrayList<>();
            if(friends.size()>0){
             Bundle ans = new Bundle();
             ans.putString("code","1");
             for(Friends fri:friends ){
                 if( friends_uid.contains( Long.parseLong(fri.getUid()) ) ){
                     Log.i("CheckFriendsInBundle","matched "+fri.toString());
                     fri.setFriend(true);//并标记为好友
                 }else {
                     Log.i("CheckFriendsInBundle ","unmatched "+fri.toString());
                     fri.setFriend(false);//并标记为陌生人
                 }
                 friends_ans.add(fri);
             }
             ans.putSerializable("result",(Serializable)friends_ans);
             return ans;
            }
            return src;
        }
        return src;
    }

    public static Bundle SearchUserInDatabase(String Name,String UName,String Uid){
        Bundle bundle = new Bundle();
        List<Friends> users=new ArrayList<>();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("SELECT uid,name,uname,sex,desp FROM Users WHERE name like ? OR uname like ?");
        ){
            Name="%"+Name+"%";
            UName="%"+UName+"%";
            ps.setObject(1,Name);
            ps.setObject(2,UName);
            try(ResultSet result =ps.executeQuery()){
                while(result.next()){
                    Long uid=result.getLong("uid");
                    String uname=result.getString("uname");
                    String desp=result.getString("desp");
                    String name=result.getString("name");
                    String sex=result.getString("sex");
                    User user=new User(uid.toString(),name,uname,sex,desp);
                    Log.i("SearchUserInDatabase",user.toString());
                    users.add(new Friends(user));

                }
                if(users.size()==0) {
                    bundle.putString("code","-1");
                    bundle.putString("ERROR","无此用户");
                }
                else {
                    bundle.putString("code","1");
                    bundle.putSerializable("result",(Serializable)users);
                    return CheckFriendsInBundle(bundle,SearchUserFriendInDatabase(Uid));
                }
            }
        }catch(Exception ee){
            Log.e("UserHelper","SearchUserInDatabase: (Connection) "+ee.getLocalizedMessage());
            bundle.putString("code","-1");
            bundle.putString("ERROR",ee.getLocalizedMessage());
        }
        return bundle;
    }

    public static Bundle MakeFriendInDatabase(String User_id,String Friend_id){
        Bundle bundle= new Bundle();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Friends(user_id,friend_id) VALUES(?,?)");
        ){
            ps.setObject(1,Long.parseLong(User_id));
            ps.setObject(2,Long.parseLong(Friend_id));
            int result=ps.executeUpdate();
            bundle.putString("code",result>0? "1":"-1");

        }catch (Exception ee){
            Log.e("UserHelper","MakeFriendInDatabase: (Connection) "+ee.getMessage());
            bundle.putString("code","-1");
            bundle.putString("ERR",ee.getLocalizedMessage());
        }
        return bundle;
    }

    public static Bundle DelFriendInDatabase(String User_id,String Friend_id){
        Bundle bundle= new Bundle();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Friends WHERE user_id=? AND friend_id=?");
        ){
            ps.setObject(1,Long.parseLong(User_id));
            ps.setObject(2,Long.parseLong(Friend_id));
            int result=ps.executeUpdate();
            bundle.putString("code",result>0? "1":"-1");

        }catch (Exception ee){
            Log.e("UserHelper","MakeFriendInDatabase: (Connection) "+ee.getMessage());
            bundle.putString("code","-1");
            bundle.putString("ERR",ee.getLocalizedMessage());
        }
        return bundle;
    }

    public static void CheckNameLegal(String name)throws  Exception{
        if(name.length()<=1||name.length()>10) throw new Exception("用户名小于1或超过10");
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(name);
        if (m.find()) {
            throw new Exception("用户名不得包含汉字");
        }
    }
    public static boolean CheckNameRepeat(String name){
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("SELECT uid FROM Users WHERE name=?");
        ){
            ps.setObject(1,name);
            try(ResultSet result =ps.executeQuery()){
                if(result.next()){
                    Log.i("CheckNameRepeat","uid="+result.getLong("uid"));
                    return true;
                }
                return false;
            }
        }catch(Exception ee){
            Log.e("UserHelper","CheckNameRepeat: (Connection) "+ee.getLocalizedMessage());
        }
        return true;
    }
    public static void CheckPasswordLegal(String psw)throws Exception{
        boolean ifNum=false,ifLetter=false;
        for(int i=0;i<psw.length();i++){
            if(Character.isDigit(psw.charAt(i)))ifNum=true;
            else if(Character.isLetter(psw.charAt(i)))ifLetter=true;
        }
        if(!ifNum)throw new Exception("请至少包含一个数字");
        if(!ifLetter)throw new Exception("请至少包含一个字母");
    }
    public static long SignUserInDatabase(String name,String uname,String sex,String psw){
        String salt=Encrypt.GetNewSalt();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Users(name,uname,sex,salt,psw) VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
        ){
            ps.setObject(1,name);
            ps.setObject(2,uname);
            ps.setObject(3,sex);
            ps.setObject(4,salt);
            //密码加密加盐处理
            ps.setObject(5,Encrypt.SHA256(psw+salt));

            Log.i("SignUser","name="+name+" uname="+uname+" sex="+sex+" salt="+salt+" sha="+Encrypt.SHA256(psw+salt));
            ps.executeUpdate();
            try(ResultSet result = ps.getGeneratedKeys()){
                if(result.next()){
                    Log.i("SignUser","uid="+result.getLong(1));
                    return result.getLong(1);
                }
                return -1;
            }
        }catch(Exception ee){
            Log.e("UserHelper","CheckPasswordEqual: (Connection) "+ee.getLocalizedMessage());
        }
        return -1;
    }
    private static boolean CheckUserPassword(String hash_psw,String salt,String psw){
        String new_hash_psw;
        new_hash_psw=Encrypt.SHA256(psw+salt);
        return new_hash_psw.equals(hash_psw);
    }
    public static Bundle JoinGroupInDatabase(String uid, String gid) {
        Bundle bundle= new Bundle();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO UserInGroup(gid,uid) VALUES(?,?)");
        ){
            ps.setObject(1,Long.parseLong(gid));
            ps.setObject(2,Long.parseLong(uid));
            int result=ps.executeUpdate();
            bundle.putString("code",result>0? "1":"-1");

        }catch (Exception ee){
            Log.e("UserHelper","JoinGroupInDatabase: "+ee.getMessage());
            bundle.putString("code","-1");
            bundle.putString("ERR",ee.getLocalizedMessage());
        }
        return bundle;
    }

    public static Bundle ExitGroupInDatabase(String uid, String gid) {
        Bundle bundle= new Bundle();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM UserInGroup WHERE gid=? AND uid=?");
        ){
            ps.setObject(1,Long.parseLong(gid));
            ps.setObject(2,Long.parseLong(uid));
            int result=ps.executeUpdate();
            bundle.putString("code",result>0? "1":"-1");

        }catch (Exception ee){
            Log.e("UserHelper","ExitGroupInDatabase: "+ee.getMessage());
            bundle.putString("code","-1");
            bundle.putString("ERR",ee.getLocalizedMessage());
        }
        return bundle;
    }

    public static  Bundle SearchUserGroupInDatabase(String Uid){
        Bundle bundle = new Bundle();
        List<Long> group_gid = new  ArrayList<>();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("SELECT gid FROM UserInGroup WHERE uid=?")
        ){
            ps.setObject(1,Long.parseLong(Uid));
            try(ResultSet result = ps.executeQuery()){
                while (result.next()){
                    Long gid=result.getLong("gid");
                    group_gid.add(gid);
                    Log.i("SearchUserGroupInDatabase",Uid+": "+gid.toString());
                }
            }
            bundle.putString("code","1");//这里不管有没有群聊 都返回1
            bundle.putSerializable("result",(Serializable)group_gid);

        }
        catch(Exception ee){
            Log.e("UserHelper","SearchUserGroupInDatabase:"+ee.getLocalizedMessage());
            bundle.putString("code","-1");//标记出错

        }
        return bundle;
    }

    public static Bundle CheckGroupsInBundle(Bundle src, Bundle group) {
        if(src.getString("code").equals("1")){
            List<Long> group_gid = (List<Long>)group.getSerializable("result");
            List<Friends> groups = (List<Friends>)src.getSerializable("result");
            List<Friends> groups_ans = new ArrayList<>();
            if(groups.size()>0){//如果有已经加入的群 则筛选出加入的群并打上标记
                Bundle ans = new Bundle();
                ans.putString("code","1");
                for(Friends fri:groups ){
                    if( group_gid.contains( Long.parseLong(fri.getUid()) ) ){
                        Log.i("CheckGroupsInBundle","matched "+fri.toString());
                        fri.setFriend(true);//并标记已经在群,这里的friend表示是否是已加入的群的关系
                    }else {
                        Log.i("CheckGroupsInBundle ","unmatched "+fri.toString());
                        fri.setFriend(false);//并标记为未加入的群
                    }
                    fri.setGroup(true);//标记为群聊
                    groups_ans.add(fri);
                }
                ans.putSerializable("result",(Serializable)groups_ans);
                return ans;
            }
            //执行到这里表示没有已经加入的群，将所有群标记为未加入的群
            Bundle ans = new Bundle();
            ans.putString("code","1");
            for(Friends fri:groups ){
                fri.setFriend(false);//并标记为未加入的群
                fri.setGroup(true);//标记为群聊
                Log.i("CheckGroupsInBundle ","unmatched "+fri.toString());
                groups_ans.add(fri);
            }
            ans.putSerializable("result",(Serializable)groups_ans);
            return ans;
        }
        return src;//执行到这里表示没有找到群组
    }

    public static Bundle SearchGroupInDatabase(String Gid, String Gname, String uid) {
        Bundle bundle = new Bundle();
        List<Friends> users=new ArrayList<>();
        try(Connection conn = DriverManager.getConnection(JDBCHelper.JDBCUrl,JDBCHelper.JDBCUser,JDBCHelper.JDBCPassword);
            PreparedStatement ps = conn.prepareStatement("SELECT gid,gname,desp  FROM jdbctest.Groups WHERE gname like ?");
        ){
//            ps.setObject(1,Long.parseLong(Gid));
            Gname="%"+Gname+"%";//模糊查找
            ps.setObject(1,Gname);
            try(ResultSet result =ps.executeQuery()){
                while(result.next()){
                    Long gid=result.getLong("gid");
                    String gname=result.getString("gname");
                    String desp=result.getString("desp");
                    User user=new User(gid.toString(),"Group",gname,"group",desp);
                    Log.i("SearchGroupInDatabase",user.toString());
                    users.add(new Friends(user));

                }
                if(users.size()==0) {
                    bundle.putString("code","-1");
                    bundle.putString("ERROR","无此用户");
                }
                else {
                    bundle.putString("code","1");
                    bundle.putSerializable("result",(Serializable)users);
                    return CheckGroupsInBundle(bundle,SearchUserGroupInDatabase(uid));
                }
            }
        }catch(Exception ee){
            Log.e("UserHelper","SearchGroupInDatabase: (Connection) "+ee.getLocalizedMessage());
            bundle.putString("code","-1");
            bundle.putString("ERROR",ee.getLocalizedMessage());
        }
        return bundle;
    }
}
