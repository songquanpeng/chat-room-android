package com.song.chat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.net.URL;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> chatMessageList;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        LinearLayout leftContentLayout;
        LinearLayout rightContentLayout;
        TextView leftTextMessage;
        TextView rightTextMessage;
        TextView leftName;
        TextView rightName;
        ImageView leftImageMessage;
        ImageView rightImageMessage;

        ViewHolder(View view) {
            super(view);
            leftLayout = view.findViewById(R.id.left_layout);
            rightLayout = view.findViewById(R.id.right_layout);
            leftTextMessage = view.findViewById(R.id.left_text_message);
            rightTextMessage = view.findViewById(R.id.right_text_message);
            leftContentLayout = view.findViewById(R.id.left_content_layout);
            rightContentLayout = view.findViewById(R.id.right_content_layout);
            leftName = view.findViewById(R.id.left_name);
            rightName = view.findViewById(R.id.right_name);
            leftImageMessage = view.findViewById(R.id.left_image_message);
            rightImageMessage = view.findViewById(R.id.right_image_message);
        }
    }

    MessageAdapter(List<Message> msgList, Context context) {
        chatMessageList = msgList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message chatMessage = chatMessageList.get(position);
        if (chatMessage.sendByMyself) {
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
        } else {
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
        }
        switch (chatMessage.type) {
            case Message.TYPE_TEXT:
                if (chatMessage.sendByMyself) {
                    holder.rightTextMessage.setText(chatMessage.content);
                    holder.rightName.setText(chatMessage.sender);
                    holder.rightImageMessage.setVisibility(View.GONE);
                } else {
                    holder.leftTextMessage.setText(chatMessage.content);
                    holder.leftName.setText(chatMessage.sender);
                    holder.leftImageMessage.setVisibility(View.GONE);
                }
                break;
            case Message.TYPE_IMAGE:
                if (chatMessage.sendByMyself) {
                    holder.rightName.setText(chatMessage.sender);
                    holder.rightTextMessage.setVisibility(View.GONE);
                    holder.rightLayout.setBackground(null);
                    try {
                        URL url = new URL(Connection.serverAddress + chatMessage.content);
                        Glide.with(context).load(url).placeholder(R.drawable.loading).into(holder.rightImageMessage);
                    } catch (Exception e) {
                        Log.d("test", e.toString());
                    }
                } else {
                    holder.leftName.setText(chatMessage.sender);
                    holder.leftTextMessage.setVisibility(View.GONE);
                    holder.leftLayout.setBackground(null);
                    try {
                        URL url = new URL(Connection.serverAddress + chatMessage.content);
                        Glide.with(context).load(url).placeholder(R.drawable.loading).into(holder.leftImageMessage);
                    } catch (Exception e) {
                        Log.d("test", e.toString());
                    }
                }
                break;
            case Message.TYPE_FILE:
                // TODO: Message.TYPE_FILE
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }
}

