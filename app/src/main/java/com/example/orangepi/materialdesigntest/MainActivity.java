package com.example.orangepi.materialdesigntest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.view.View;
import android.widget.Toast;

import com.example.orangepi.me.User;
import com.example.orangepi.me.UserHelper;
import com.example.orangepi.me.UserMessage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    TextInputEditText NameInput;
    TextInputEditText PswInput;
    MaterialButton Login,Sign;
    private MyHandler myHandler;
    private final int ACCEPT_LOGIN=1,REJECT_LOGIN=-1;
    boolean isQuery=false;
    private MyServiceConn conn;
    private ConnectReceiver myReceiver;
    MyService myService;
    User nowUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        NameInput= findViewById(R.id.UserInput);
        PswInput= findViewById(R.id.PswInput);
        Login = findViewById(R.id.Login);
        Sign = findViewById(R.id.Sign);
        myHandler=new MyHandler();
        Login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isQuery)return;//防止查询中多次点击按钮，产生多个查询
                isQuery=true;
                new Thread(()->{
                    Bundle result= UserHelper.CheckUserInDatabase(NameInput.getText().toString(),PswInput.getText().toString());
                    String code=result.getString("code");
                    if(code.equals("1")){
                        String uname=result.getString("uname");
                        String uid =result.getString("uid");
                        nowUser= new User(uid,uname);

                        Message mes=Message.obtain();
                        mes.what=ACCEPT_LOGIN;
                        mes.obj=uname;
                        myHandler.sendMessage(mes);
                        Log.i("Login","已发送登录");
                    }
                    else {
                        Message mes=Message.obtain();
                        mes.what=REJECT_LOGIN;
                        mes.obj=result.getString("ERROR");
                        myHandler.sendMessage(mes);
                        Log.i("Login","已拒绝登录");
                    }
                }).start();
            }
        });
        Sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignInActiviy.class));

            }
        });
        BindService();
    }
    public void  BindService(){
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

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class ConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String name=intent.getStringExtra("return");
            if(name!=null&&name.equals("登录成功")){
                Log.i("STR", "onReceive: return List"+name);
                Toast.makeText(getApplicationContext()," 登录成功",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),ChatListActivity.class));
            }
            if(name!=null&&name.contains("online_users")){
                Log.i("STR", "onReceive: return List"+name);
                Toast.makeText(getApplicationContext()," 已获取在线用户",Toast.LENGTH_SHORT).show();
                myService.setOnlineUsers(name);
            }


        }
    }

    private class MyHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ACCEPT_LOGIN:
                    Toast.makeText(getApplicationContext(),"Hello "+msg.obj.toString()+"uid="+nowUser.Id,Toast.LENGTH_SHORT).show();
                    Log.i("Login","已响应登录");
                    myService.SetNowUser(nowUser);
                    isQuery=false;//还原查询结束的状态

                    break;
                case REJECT_LOGIN:
                    Toast.makeText(getApplicationContext(),"错误: "+msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    Log.i("Login","登录被拒绝,");
                    isQuery=false;//还原查询结束的状态
                    break;
                default:
                    isQuery=false;//还原查询结束的状态
            }
        }
    }

}
