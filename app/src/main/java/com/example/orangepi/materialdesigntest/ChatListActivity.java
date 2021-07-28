package com.example.orangepi.materialdesigntest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.LocusId;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.orangepi.me.UserMessage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

    public class ChatListActivity extends AppCompatActivity {
    RecyclerView chatMessageListView;
    List<ChatMessage> chatMessageList;
    MaterialToolbar chatListAppBar;
    private MyServiceConn conn;
    private ConnectReceiver myReceiver;
    MyService myService;
    private MyHandler handler;
    private final int  GET_RESULT=1;
    ChatMessageListAdapter adapter;
    BottomNavigationView ChatListBottomBar;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        if(myReceiver!=null){
            myService.LetItOver();
            unregisterReceiver(myReceiver);//取消广播的注册
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        chatMessageList= new ArrayList<>();
        handler=new MyHandler();
        ChatListBottomBar=findViewById(R.id.ChatListBottomBar);
        chatMessageListView=findViewById(R.id.chatMessageListView);
        chatListAppBar=findViewById(R.id.ChatListAppBar);
        ChatListBottomBar.getMenu().getItem(0).setChecked(true);//设置聊天被选中
        ChatListBottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.friend:
                        Bundle bundle=new Bundle();
                        bundle.putString("UID",myService.nowUser.Id);
                        startActivity(new Intent(getApplicationContext(),FriendListActivity.class).putExtras(bundle));
                        ChatListBottomBar.getMenu().getItem(1).setChecked(false);//重新设置聊天被选中
                        ChatListBottomBar.getMenu().getItem(0).setChecked(true);//重新设置聊天被选中
                        break;
                }
                return true;
            }
        });
        chatListAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.add_friend:
                        Bundle bundle=new Bundle();
                        bundle.putString("UID",myService.nowUser.Id);
                        startActivity(new Intent(getApplicationContext(),SearchFriendActivity.class).putExtras(bundle));
                        break;
                    case R.id.search:

                        break;
                }
                return false;
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatMessageListView.setLayoutManager(linearLayoutManager);
        adapter = new ChatMessageListAdapter(chatMessageList,this);
        chatMessageListView.setAdapter(adapter);
        //绑定服务
        conn=new MyServiceConn();
        bindService(new Intent(this,MyService.class),conn,BIND_AUTO_CREATE);
        doRegisterReceiver();
        //myService.SetNowUser(new User("10","cz"));

    }

    private void doRegisterReceiver() {
        myReceiver= new ConnectReceiver();
        IntentFilter filter = new IntentFilter("com.example.orangepi.content");
        registerReceiver(myReceiver,filter);
    }

    public void ChatMessageItemClick (View view){
        int pos=chatMessageListView.getChildAdapterPosition(view);
        //Toast.makeText(this,"点击了 "+chatMessageList.get(pos).getChatMessageUname(),Toast.LENGTH_SHORT).show();
        Bundle bundle=new Bundle();
        bundle.putString("ChatName",chatMessageList.get(pos).getChatMessageUname());
        bundle.putString("UID",chatMessageList.get(pos).getUid());
        bundle.putInt("ChatTag",chatMessageList.get(pos).getChatTag());
        bundle.putSerializable("msg", (Serializable) chatMessageList.get(pos).GetUserMessages());
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtras(bundle);
        try {
            startActivity(intent);
        }catch (Exception ee){
            Log.e("Err", Objects.requireNonNull(ee.getMessage()));
        }
    }



    private class MyServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService=((MyService.MyBinder)service).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class ConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String name=intent.getStringExtra("return");
//            if(name!=null){
//                Log.i("STR", "onReceive: return List"+name);
//                Toast.makeText(getApplicationContext(),"ChatList "+name,Toast.LENGTH_SHORT).show();
//            }
            Bundle bundle =intent.getBundleExtra("MSG");
            if(bundle!=null){
                Log.i("MSG", "onReceive: MSG");
                UserMessage msg=(UserMessage)bundle.getSerializable("MSG");
                ChangeChatList(msg);
            }

        }
    }

    private void ChangeChatList(UserMessage msg){
        String uid=msg.SourceId;
        switch (msg.Type){
            case GROUP://群聊
                for(int i=0;i<chatMessageList.size();i++) {
                    if (chatMessageList.get(i).getUid().equals(msg.TargetId)) {//如果在当前聊天列表中匹配到了item
                        chatMessageList.get(i).setGetChatMessageDate("刚刚");//记得改
                        chatMessageList.get(i).setGetChatMessageDescription((String) msg.Message);
                        chatMessageList.get(i).PushMessage(msg);//把消息放到消息队列里
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
                if(myService.userGroups!=null){//查找群
                    for(int i=0;i<myService.userGroups.size();i++){
                        if(myService.userGroups.get(i).getString("gid").equals(msg.TargetId)){
                            Log.i("MSG", "ChangeChatList: added a Group chatmessage");
                            chatMessageList.add(new ChatMessage(myService.userGroups.get(i).getString("gname"),(String)msg.Message,"刚刚",msg.TargetId,2));
                            chatMessageList.get(chatMessageList.size()-1).PushMessage(msg);//把消息放到消息队列里
                            adapter.notifyDataSetChanged();
                            return;
                        }
                    }
                }
                break;

            default://单聊
                for(int i=0;i<chatMessageList.size();i++){
                    if(chatMessageList.get(i).getUid().equals(uid)){//如果在当前聊天列表中匹配到了item
                        chatMessageList.get(i).setGetChatMessageDate("刚刚");//记得改
                        chatMessageList.get(i).setGetChatMessageDescription((String)msg.Message);
                        chatMessageList.get(i).PushMessage(msg);//把消息放到消息队列里
                        adapter.notifyDataSetChanged();
                        return;
                    }

                }
                //以下是未能匹配到的情况 要先从好友列表里拉取我的好友，寻找uid对应的uname能信息，然后chatMessageList.add 之后在notify
                if(myService.userFriends!=null){//查找好友
                    for(int i=0;i<myService.userFriends.size();i++){
                        if(myService.userFriends.get(i).getString("uid").equals(uid)){
                            Log.i("MSG", "ChangeChatList: added a single chat message");
                            chatMessageList.add(new ChatMessage(myService.userFriends.get(i).getString("uname"),(String)msg.Message,"刚刚",uid,1));
                            chatMessageList.get(chatMessageList.size()-1).PushMessage(msg);//把消息放到消息队列里
                            adapter.notifyDataSetChanged();
                            return;
                        }
                    }
                }


        }



    }

    private class MyHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){

            }
        }
    }


}
