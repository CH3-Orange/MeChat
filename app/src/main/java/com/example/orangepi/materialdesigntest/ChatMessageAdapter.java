package com.example.orangepi.materialdesigntest;

import android.app.Notification;
import android.content.Context;
import android.drm.DrmStore;
import android.media.Image;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MyHolder> {

    private List<Messages> messages;
    private Context context;

    public ChatMessageAdapter(List<Messages> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Messages message = messages.get(position);
        switch (message.getType()) {
            case SEND:
                holder.SetTextMessage(message.getTextMessage());
                holder.SetFriendEnable(false);
                holder.SetMyHead(true);
                holder.SetMessageRight(true);
                break;
            case RECEIVE:
                holder.SetTextMessage(message.getTextMessage());
                holder.SetFriendName(message.getFriendName());
                holder.SetFriendEnable(true);
                holder.SetMyHead(false);
                holder.SetMessageRight(false);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageButton friendHead, myHead;
        TextView friendName, textMessage;
        ImageView imageMessage;
        LinearLayout messageBox;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            friendHead = itemView.findViewById(R.id.friendHead);
            friendName = itemView.findViewById(R.id.friendName);
            myHead = itemView.findViewById(R.id.myHead);
            textMessage = itemView.findViewById(R.id.textMessage);
            imageMessage = itemView.findViewById(R.id.imageMessage);
            messageBox = itemView.findViewById(R.id.messageBox);
        }

        public void SetFriendName(String name) {
            friendName.setText(name);
        }

        public void SetTextMessage(String text) {
            textMessage.setText(text);
        }

        public void SetFriendEnable(boolean ifshow) {
            friendHead.setVisibility(ifshow ? View.VISIBLE : View.INVISIBLE);//不可视朋友头像
            if (!ifshow) {
//                friendName.setVisibility(View.GONE);//不显示朋友名称
                friendName.setText("");
            }
        }

        public void SetMyHead(boolean ifshow) {
            myHead.setVisibility(ifshow ? View.VISIBLE : View.INVISIBLE);
        }

        public void SetMessageRight(boolean ifRight) {
            //textMessage.setGravity(ifRight? Gravity.RIGHT:Gravity.LEFT);
//            messageBox.setForegroundGravity(ifRight? Gravity.RIGHT:Gravity.LEFT);
            messageBox.setGravity(ifRight ? Gravity.RIGHT : Gravity.LEFT);//设置聊天框向右或向左


            // textMessage.setLayoutParams();
        }
    }
}
