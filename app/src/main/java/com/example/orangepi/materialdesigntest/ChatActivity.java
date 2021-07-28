package com.example.orangepi.materialdesigntest;

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
import android.os.IBinder;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orangepi.me.TextMessage;
import com.example.orangepi.me.UserMessage;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ChatActivity extends AppCompatActivity {

    MaterialToolbar appBar, bottomBar;
    TextView chatName;
    Bundle bundle;
    RecyclerView messagesView;
    List<Messages> messages;
    EditText inputText;
    ChatMessageAdapter adapter;
    private MyServiceConn conn;
    private ConnectReceiver myReceiver;
    MyService myService;
    String UID;
    int chatTag=0;//标明该聊天是什么类型的聊天 1为单聊 2为群聊

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        bundle = this.getIntent().getExtras();
        UID=bundle.getString("UID");
        chatTag=bundle.getInt("ChatTag");
        appBar = findViewById(R.id.ChatAppBar);
        bottomBar = findViewById(R.id.ChatBottomBar);
        chatName = findViewById(R.id.ChatName);
        chatName.setText(bundle.getString("ChatName"));
        List<UserMessage> msgQueue=(List<UserMessage>)bundle.getSerializable("msg");
        inputText=findViewById(R.id.InputText);
        appBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        messages=new ArrayList<>();
        if(msgQueue!=null){
            for(UserMessage msg:msgQueue){
                messages.add(new Messages(msg.SourceId,(String)msg.Message, Messages.MessageType.RECEIVE));
            }
        }

        messagesView=findViewById(R.id.chatMessageListView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messagesView.setLayoutManager(linearLayoutManager);
        adapter = new ChatMessageAdapter(messages,this);
        messagesView.setAdapter(adapter);
        messagesView.scrollToPosition(adapter.getItemCount()-1);

        bottomBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.send:
                        String text=inputText.getText().toString();
                        if(text.length()<=0){
                            inputText.setHint("写点什么叭");
                            break;
                        }
                        Messages message=new Messages(bundle.getString("ChatName"),text, Messages.MessageType.SEND);
                        messages.add(message);
                        adapter.notifyItemInserted(messages.size());
                        messagesView.scrollToPosition(adapter.getItemCount()-1);
                        inputText.setText("");
                        inputText.setHint("");
                        switch (chatTag){
                            case 2://群聊
                                myService.nowUser.PushMessage(new TextMessage(myService.nowUser.Id,UID,text, UserMessage.MsgType.GROUP));//推送消息
                                break;
                            default://默认单聊
                                myService.nowUser.PushMessage(new TextMessage(myService.nowUser.Id,UID,text, UserMessage.MsgType.SINGLE));//推送消息
                        }

                        Log.i("SendMsg", "onMenuItemClick: my_id="+myService.nowUser.Id+" tar_id="+UID+" text="+text);

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
            Bundle bundle =intent.getBundleExtra("MSG");
            if(bundle==null)return;
            Log.i("MSG", "onReceive: MSG");
            UserMessage msg=(UserMessage)bundle.getSerializable("MSG");
            if(msg!=null){
                switch (msg.Type){
                    case GROUP:
                        if(msg.TargetId.equals(UID)){
                            messages.add(new Messages(msg.SourceId,(String)msg.Message, Messages.MessageType.RECEIVE));
                            adapter.notifyItemInserted(messages.size());
                            messagesView.scrollToPosition(adapter.getItemCount()-1);
                        }
                        break;

                    default:
                        messages.add(new Messages(msg.SourceId,(String)msg.Message, Messages.MessageType.RECEIVE));
                        adapter.notifyItemInserted(messages.size());
                        messagesView.scrollToPosition(adapter.getItemCount()-1);
                }
            }



        }
    }
}
