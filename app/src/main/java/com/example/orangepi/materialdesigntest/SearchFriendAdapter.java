package com.example.orangepi.materialdesigntest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orangepi.me.User;
import com.example.orangepi.me.UserHelper;
import com.google.android.material.button.MaterialButton;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class SearchFriendAdapter extends RecyclerView.Adapter<SearchFriendAdapter.MyHolder> {

    private List<Friends> Users;
    private Context context;
    private String myUid;
    private MyServiceConn conn;
    private ConnectReceiver myReceiver;
    MyService myService;
    Activity Act;

    public void setAllFriend(boolean allFriend) {
        isAllFriend = allFriend;
    }

    private boolean isAllFriend = false;

    public SearchFriendAdapter(List<Friends> users, Context context, String uid, Activity act) {
        Users = users;
        this.context = context;
        myUid = uid;
        Act=act;
        //绑定服务
        conn=new MyServiceConn();
        Act.bindService(new Intent(Act.getApplicationContext(),SearchFriendAdapter.class),conn,context.BIND_AUTO_CREATE);
        doRegisterReceiver();
    }

    public void clearData() {
        Users.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false);
        return new MyHolder(view);
    }
    private void doRegisterReceiver() {
        myReceiver= new ConnectReceiver();
        IntentFilter filter = new IntentFilter("com.example.orangepi.content");
        Act.registerReceiver(myReceiver,filter);
    }
    private class MyServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService=((MyService.MyBinder)service).getService();
            if(myService!=null){
                Log.i("SearchFriendAdapter", "onServiceConnected: get myService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class ConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        try{
            holder.SetUserDescription(Users.get(position).getUserDescription());
            holder.SetUserName(Users.get(position).getUserName());
            holder.SetSexIcon(Users.get(position).getSex());
            holder.SetUid(Users.get(position).getUid());
            holder.SetPos(position);
        }catch (Exception ee){
            Log.e("SearchFriendAdapter", "onBindViewHolder: "+ee.getMessage() );
        }
        if (!Users.get(position).isGroup()&&Users.get(position).isFriend()) {//如果不是群 但是是有好友关系 表明是好友
            holder.SetButtonChat();
        } else if (Users.get(position).isGroup()&&Users.get(position).isFriend()){//如果是群组，并且有好友关系 表示是已加入的群
            holder.SetButtonGroup();

        }  else if (Users.get(position).isGroup()&&!Users.get(position).isFriend()){//如果是群组，但是没有好友关系 表示未加入的群
            holder.SetButtonAddGroup();

        } else {//不满足上述条件 最后就是陌生人
            holder.SetButtonAdd();
        }
    }

    @Override
    public int getItemCount() {
        return Users == null ? 0 : Users.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView userHead, sexIcon;
        TextView userName, userDescription, Uid;
        MaterialButton addFriendBtn, delFriendBtn;
        MyHandler handler = new MyHandler();
        boolean ifAddBtn = false, ifDelBtn = false;
        int pos;//记录在Users中的位置

        private final int MAKE_FRIEND_OK = 1, MAKE_FRIEND_FAIL = -1, DEL_FRIEND_OK = 2, DEL_FRIEND_FAIL = -2,
                JOIN_GROUP_OK=3,JOIN_GROUP_FAIL=-3,EXIT_GROUP_OK=4,EXIT_GROUP_FEIL=-4;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            userHead = itemView.findViewById(R.id.UserHead);
            sexIcon = itemView.findViewById(R.id.SexIcon);
            userName = itemView.findViewById(R.id.UserName);
            userDescription = itemView.findViewById(R.id.UserDescription);
            addFriendBtn = itemView.findViewById(R.id.AddFriend);
            Uid = itemView.findViewById(R.id.Uid);
            delFriendBtn = itemView.findViewById(R.id.DelFriend);
            if (isAllFriend) {//判断当前是全好友的列表，即在通讯录界面打开的
                SetButtonChat();
            } else//是在搜索用户的界面打开的
            {
                delFriendBtn.setVisibility(View.GONE);
            }
            delFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ifDelBtn) return;//防止重复点击
                    ifDelBtn = true;
                    if(addFriendBtn.getTag().equals("group")){//退出群
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bundle bundle = UserHelper.ExitGroupInDatabase(myUid, Uid.getText().toString());
                                if (bundle.getString("code").equals("1")) {
                                    Log.i("DelFriend", "退出群成功: " + userName.getText());
                                    Message msg = Message.obtain();
                                    msg.what = EXIT_GROUP_OK;
                                    handler.sendMessage(msg);

                                } else {
                                    Log.i("DelFriend", "退出群失败: " + userName.getText());
                                    Message msg = Message.obtain();
                                    msg.what = EXIT_GROUP_FEIL;
                                    handler.sendMessage(msg);
                                }

                                ifDelBtn = false;
                            }
                        }).start();
                    }else {//删除好友
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                UserHelper.DelFriendInDatabase(Uid.getText().toString(), myUid);
                                Bundle bundle = UserHelper.DelFriendInDatabase(myUid, Uid.getText().toString());
                                if (bundle.getString("code").equals("1")) {
                                    Log.i("DelFriend", "删除好友成功: " + userName.getText());
                                    Message msg = Message.obtain();
                                    msg.what = DEL_FRIEND_OK;
                                    handler.sendMessage(msg);

                                } else {
                                    Log.i("DelFriend", "删除好友失败: " + userName.getText());
                                    Message msg = Message.obtain();
                                    msg.what = DEL_FRIEND_FAIL;
                                    handler.sendMessage(msg);
                                }

                                ifDelBtn = false;
                            }
                        }).start();
                    }
                }
            });
            addFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ifAddBtn) return;//防止多次点击
                    ifAddBtn = true;
                    if (addFriendBtn.getTag().equals("add")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                UserHelper.MakeFriendInDatabase(myUid, Uid.getText().toString());
                                Bundle bundle = UserHelper.MakeFriendInDatabase(Uid.getText().toString(), myUid);
                                if (bundle.getString("code").equals("1")) {
                                    Message msg = Message.obtain();
                                    msg.what = MAKE_FRIEND_OK;
                                    handler.sendMessage(msg);
                                    Log.i("AddFriend", "添加好友成功: " + userName.getText());

                                } else {
                                    Message msg = Message.obtain();
                                    msg.what = MAKE_FRIEND_FAIL;
                                    handler.sendMessage(msg);
                                    Log.i("AddFriend", "添加好友失败: " + userName.getText());
                                }
                                ifAddBtn = false;
                            }
                        }).start();
                    }
                    else if (addFriendBtn.getTag().equals("addGroup")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //待写
                                Bundle bundle = UserHelper.JoinGroupInDatabase(myUid, Uid.getText().toString());
                                if (bundle.getString("code").equals("1")) {
                                    Message msg = Message.obtain();
                                    msg.what = JOIN_GROUP_OK;
                                    handler.sendMessage(msg);
                                    Log.i("AddFriend", "加入群成功: " + userName.getText());

                                } else {
                                    Message msg = Message.obtain();
                                    msg.what = JOIN_GROUP_FAIL;
                                    handler.sendMessage(msg);
                                    Log.i("AddFriend", "加入群失败: " + userName.getText());
                                }
                                ifAddBtn = false;
                            }
                        }).start();
                    }else if (addFriendBtn.getTag().equals("chat")||addFriendBtn.getTag().equals("group")) {

                        Bundle bundle = new Bundle();
                        bundle.putString("ChatName", userName.getText().toString());
                        bundle.putString("UID", Uid.getText().toString());
                        if(addFriendBtn.getTag().equals("group")){//添加群聊标记
                            bundle.putInt("ChatTag", 2);
                            Log.i("AddFriend", "进入群聊: " + userName.getText());
                        }else {//添加单聊标记
                            bundle.putInt("ChatTag", 1);
                            Log.i("AddFriend", "发起聊天: " + userName.getText());
                        }
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//在非Activity中打开其他activity要加上flag
                        context.startActivity(intent);
                        ifAddBtn = false;
                    }
                }
            });
        }

        public void SetUserName(String name) throws UnsupportedEncodingException {
            if (name.getBytes("utf-8").length >= 8) {
                userName.setText(name.substring(0, 4) + " ...");
            } else {
                userName.setText(name);
            }
        }

        public void SetUserDescription(String desp) throws UnsupportedEncodingException {
            if (desp.getBytes("utf-8").length>= 10) {
                userDescription.setText(desp.substring(0, 5) + " ...");
            } else {
                userDescription.setText(desp);
            }
        }

        public void SetSexIcon(String sex) {
            if (sex.equals("male")) {
                sexIcon.setImageResource(R.drawable.ic_sex_boy);
            } else if (sex.equals("female")) {
                sexIcon.setImageResource(R.drawable.ic_sex_girl);
            }else if (sex.equals("group")) {
                sexIcon.setImageResource(R.drawable.ic_group);
            } else {
                sexIcon.setImageResource(R.drawable.ic_action_head_icon_default);

            }
        }

        public void SetButtonChat() {
            addFriendBtn.setText("聊天");
            addFriendBtn.setTag("chat");
            delFriendBtn.setVisibility(View.VISIBLE);
        }

        public void SetButtonAdd() {
            addFriendBtn.setText("加好友");
            addFriendBtn.setTag("add");
        }

        public void SetDisappear() {
            Users.remove(pos);//删掉这个数据
            notifyDataSetChanged();
        }

        public void SetUid(String uid) {
            Uid.setText(uid);
        }

        public void SetPos(int position) {
            pos=position;
        }

        public void SetButtonGroup() {
            addFriendBtn.setText("聊天");
            addFriendBtn.setTag("group");
            delFriendBtn.setVisibility(View.VISIBLE);
            delFriendBtn.setText("退群");
            Log.i("SetButtonGroup", "设置按钮为群聊 "+addFriendBtn.getTag());
        }

        public void SetButtonAddGroup() {
            addFriendBtn.setText("加入群");
            addFriendBtn.setTag("addGroup");
        }

        private class MyHandler extends Handler {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MAKE_FRIEND_OK:
                        Toast.makeText(context, "添加好友: " + userName.getText(), Toast.LENGTH_SHORT).show();
                        SetButtonChat();
                        if(myService!=null){
                            myService.AddFriend(Users.get(pos));
                        }else{
                            Log.e("SearchFriendAdapter", "handleMessage: myService is null !!!" );
                        }
                        break;
                    case MAKE_FRIEND_FAIL:
                        Toast.makeText(context, "添加失败 ", Toast.LENGTH_SHORT).show();
                        break;
                    case JOIN_GROUP_OK:
                        Toast.makeText(context, "加入群: " + userName.getText(), Toast.LENGTH_SHORT).show();
                        SetButtonGroup();
                        if(myService!=null){
                            myService.JoinGroup(Users.get(pos));
                        }else{
                            Log.e("SearchFriendAdapter", "handleMessage: myService is null !!!" );
                        }
                        break;
                    case JOIN_GROUP_FAIL:
                        Toast.makeText(context, "加入群失败 ", Toast.LENGTH_SHORT).show();
                        break;
                    case DEL_FRIEND_OK:
                        Toast.makeText(context, "删除成功: " + userName.getText(), Toast.LENGTH_SHORT).show();
                        if(myService!=null){
                            myService.DelFriend(Users.get(pos).getUid());
                        }else{
                            Log.e("SearchFriendAdapter", "handleMessage: myService is null !!!" );
                        }
                        SetDisappear();
                        break;
                    case DEL_FRIEND_FAIL:
                        Toast.makeText(context, "删除失败 ", Toast.LENGTH_SHORT).show();
                        break;
                    case EXIT_GROUP_OK:
                        Toast.makeText(context, "退群成功: " + userName.getText(), Toast.LENGTH_SHORT).show();
                        if(myService!=null){
                            myService.ExitGroup(Users.get(pos).getUid());
                        }else{
                            Log.e("SearchFriendAdapter", "handleMessage: myService is null !!!" );
                        }
                        SetDisappear();
                        break;
                    case EXIT_GROUP_FEIL:
                        Toast.makeText(context, "退群失败 ", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

    }

}
