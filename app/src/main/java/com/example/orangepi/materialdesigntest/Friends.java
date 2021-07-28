package com.example.orangepi.materialdesigntest;

import androidx.annotation.NonNull;

import com.example.orangepi.me.User;

public class Friends {
    private String UserName,UserDescription,Sex,Uid;
    private boolean isFriend ;
    private boolean isGroup;

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserDescription() {
        return UserDescription;
    }

    public void setUserDescription(String userDescription) {
        UserDescription = userDescription;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String sex) {
        Sex = sex;
    }

    public Friends(String userName, String userDescription, String sex,String uid) {
        UserName = userName;
        UserDescription = userDescription;
        Sex = sex;
        Uid = uid;
    }
    public Friends(String userName, String userDescription, String sex,String uid, boolean isFriend ,boolean isGroup) {
        UserName = userName;
        UserDescription = userDescription;
        Sex = sex;
        Uid = uid;
        this.isFriend=isFriend;
        this.isGroup=isGroup;
    }
    public Friends(User user){
        UserName = user.UserName;
        UserDescription = user.UserDescription;
        Sex = user.UserSex;
        Uid=user.Id;
    }

    @NonNull
    @Override
    public String toString() {
        return Uid+" "+UserName;
    }

    public void setOnline() {//设置用户在线
    }
}
