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
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.orangepi.me.User;
import com.example.orangepi.me.UserHelper;
import com.example.orangepi.me.UserMessage;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class SearchFriendActivity extends AppCompatActivity {

    MaterialToolbar searchAppBar;
    List<Friends> users=new ArrayList<>();
    RecyclerView resultView;
    EditText searchInput;
    SearchFriendAdapter adapter;
    private MyServiceConn conn;
    private ConnectReceiver myReceiver;
    MyService myService;
    private MyHandler handler;
    private final int  GET_RESULT=1,NO_RESULT=0,WAIT_RESULT=-1;
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
        setContentView(R.layout.activity_search_friend);
        handler=new MyHandler();
        Bundle bundle= this.getIntent().getExtras();
        Uid=bundle.getString("UID");
        searchAppBar=findViewById(R.id.SearchFriendAppBar);
        resultView=findViewById(R.id.SearchFriendView);
        searchInput= findViewById(R.id.SearchInput);
        searchAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        adapter =new SearchFriendAdapter(users,getApplicationContext(),Uid,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        resultView.setLayoutManager(linearLayoutManager);
        resultView.setAdapter(adapter);
        searchAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.search_friend:
                        users.clear();
                        Message msg=Message.obtain();
                        msg.what=WAIT_RESULT;
                        handler.sendMessage(msg);
                        String input=searchInput.getText().toString();
//                        if(input.length()==0)break; //限制不能搜索空用户
                        new Thread(() -> {
                            Bundle userResult=UserHelper.SearchUserInDatabase(input,input,Uid);
                            Bundle groupResult=UserHelper.SearchGroupInDatabase(input,input,Uid);
                            Message msg2=Message.obtain();
                            if(userResult.getString("code").equals("-1")&&groupResult.getString("code").equals("-1")){//未查到任何用户和群组
                                msg2.what=NO_RESULT;
                                msg2.obj=userResult.getString("ERROR");
                                handler.sendMessage(msg2);
                            }else {
                                List<Friends> Result1 = null,Result2=null;
                                if(userResult.getString("code").equals("1")) {
                                    Result1 = (List<Friends>) userResult.getSerializable("result");
                                }
                                if(groupResult.getString("code").equals("1")){
                                    Result2 = (List<Friends>) groupResult.getSerializable("result");
                                }
                                if(Result1!=null){
                                    for(Friends user : Result1){
                                        users.add(user);
                                    }
                                }
                                if(Result2!=null){
                                    for(Friends user : Result2){
                                        users.add(user);
                                    }
                                }

                                msg2.what=GET_RESULT;
                                handler.sendMessage(msg2);
                            }
                        }).start();
                        break;
                }
                return false;
            }
        });
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


    private class MyServiceConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService=((MyService.MyBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class ConnectReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
//            String str=intent.getStringExtra("return");
//            if(str!=null){
//                Log.i("STR", "onReceive: return"+str);
//                Toast.makeText(getApplicationContext(),"SearchFriend "+str,Toast.LENGTH_SHORT).show();
//            }


        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case WAIT_RESULT:
                    Toast.makeText(getApplicationContext(),"查询中，请等待",Toast.LENGTH_SHORT).show();
                    if(resultView.getChildCount()>0){
                        resultView.removeAllViews();
                        adapter.clearData();
                    }
                    break;
                case GET_RESULT:
                    adapter.notifyItemInserted(users.size());
                    break;
                case NO_RESULT:
                    Toast.makeText(getApplicationContext(),(String)msg.obj,Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }


}
