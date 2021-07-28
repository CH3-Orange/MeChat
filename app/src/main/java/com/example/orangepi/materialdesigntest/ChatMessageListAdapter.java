package com.example.orangepi.materialdesigntest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatMessageListAdapter extends RecyclerView.Adapter<ChatMessageListAdapter.MyHolder> {

    private List<ChatMessage> chatMessagelist;
    private Context context;

    public ChatMessageListAdapter(List<ChatMessage> chatMessagelist, Context context) {
        this.chatMessagelist = chatMessagelist;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_item,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.SetUname(chatMessagelist.get(position).getChatMessageUname());
        holder.SetDescription(chatMessagelist.get(position).getGetChatMessageDescription());
        holder.SetDate(chatMessagelist.get(position).getGetChatMessageDate());
    }

    @Override
    public int getItemCount() {
        return chatMessagelist ==null?0: chatMessagelist.size();
    }



    public class MyHolder extends RecyclerView.ViewHolder {

        private ImageView headIcon,tip;
        private TextView uname,description,date;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            headIcon=itemView.findViewById(R.id.chatMessageHeadIcon);
            tip=itemView.findViewById(R.id.chatMessageTip);
            uname=itemView.findViewById(R.id.chatMessageUname);
            description=itemView.findViewById(R.id.chatMessageDescription);
            date=itemView.findViewById(R.id.chatMessageDate);

        }
        public void SetUname(String Uname){
            uname.setText(Uname);
        }
        public void SetDescription(String Desp){
            if(Desp.length()>=24){
                description.setText(Desp.substring(0,23)+" ......");
            }
            else
            {
                description.setText(Desp);
            }
        }
        public void SetDate(String Date){
            date.setText(Date);
        }
    }
}
