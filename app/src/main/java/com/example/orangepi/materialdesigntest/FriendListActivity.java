package com.example.orangepi.materialdesigntest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    RecyclerView FriendListView;
    MaterialToolbar FriendListAppBar;
    BottomNavigationView FriendListBottomBar;
    List<Friends> users=new ArrayList<>();
    SearchFriendAdapter adapter;
    private MyServiceConn conn;
    private ConnectReceiver myReceiver;
    MyService myService;
    String Uid;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        if(myReceiver!=null){
            unregisterReceiver(myReceiver);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        FriendListView=findViewById(R.id.FriendListView);
        FriendListAppBar=findViewById(R.id.FriendListAppBar);
        FriendListBottomBar=findViewById(R.id.FriendListBottomBar);
        FriendListBottomBar.getMenu().getItem(1).setChecked(true);//设置通讯录被选中
        FriendListBottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.chat:
                        finish();
                        break;
                }
                return true;
            }
        });

        Bundle bundle= this.getIntent().getExtras();
        Uid=bundle.getString("UID");

        adapter =new SearchFriendAdapter(users,getApplicationContext(),Uid,this);
        adapter.setAllFriend(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        FriendListView.setLayoutManager(linearLayoutManager);
        FriendListView.setAdapter(adapter);

        //绑定服务
        conn=new MyServiceConn();
        bindService(new Intent(this,MyService.class),conn,BIND_AUTO_CREATE);
        doRegisterReceiver();

    }


    private void doRegisterReceiver() {
        myReceiver= new ConnectReceiver();
        IntentFilter filter = new IntentFilter("com.example.orangepi.content");
        registerReceiver(myReceiver,filter);
    }


    private class MyServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService=((MyService.MyBinder)service).getService();
            String onlineUid=myService.getOnlineUsers();
            //建立好友列表
            if(myService.userFriends!=null){
                for(int i=0;i<myService.userFriends.size();i++){
                    Bundle bundle=myService.userFriends.get(i);
                    Friends fri=new Friends(bundle.getString("uname"),bundle.getString("desp"),bundle.getString("sex"),
                            bundle.getString("uid"),true,false);
                    if(onlineUid.contains(bundle.getString("uid"))){
                        fri.setUserDescription("[在线]"+fri.getUserDescription());
                    }
                    else {
                        fri.setUserDescription("[离线]"+fri.getUserDescription());
                    }
                    users.add(fri);
                    Log.i("Friend", "onServiceConnected: add a friend: uid="+bundle.getString("uid")+"uname="+bundle.getString("uname"));
                    adapter.notifyDataSetChanged();
                }

            }
            //建立群组
            if(myService.userGroups!=null){
                for(int i=0;i<myService.userGroups.size();i++){
                    Bundle bundle=myService.userGroups.get(i);
                    users.add(new Friends(bundle.getString("gname"),bundle.getString("desp"),"group",
                            bundle.getString("gid"),true,true));
                    Log.i("Friend", "onServiceConnected: add a group: gid="+bundle.getString("gid")+"gname="+bundle.getString("gname"));
                    adapter.notifyDataSetChanged();
                }

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class ConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            String str=intent.getStringExtra("return");
//            if(str!=null){
//                Log.i("STR", "onReceive: return"+str);
//                Toast.makeText(getApplicationContext(),"SearchFriend "+str,Toast.LENGTH_SHORT).show();
//            }


        }
    }
}
