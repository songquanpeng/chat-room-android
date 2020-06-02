package com.song.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> chatMessageList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        LinearLayout leftContentLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView senderName;
        ImageView leftPic;
        ImageView rightPic;

        ViewHolder(View view) {
            super(view);
            leftLayout = view.findViewById(R.id.left_layout);
            rightLayout = view.findViewById(R.id.right_layout);
            leftMsg = view.findViewById(R.id.tv_left_msg);
            rightMsg = view.findViewById(R.id.tv_right_msg);
            leftContentLayout = view.findViewById(R.id.left_content_layout);
            senderName = view.findViewById(R.id.tv_sender_name);
            leftPic = view.findViewById(R.id.iv_left_msg);
            rightPic = view.findViewById(R.id.iv_right_msg);
        }
    }

    MessageAdapter(List<Message> msgList) {
        chatMessageList = msgList;
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
                    holder.rightMsg.setText(chatMessage.content);
                    holder.rightPic.setVisibility(View.GONE);
                } else {
                    holder.leftMsg.setText(chatMessage.content);
                    holder.senderName.setText(chatMessage.sender);
                    holder.leftPic.setVisibility(View.GONE);
                }
                break;
            case Message.TYPE_IMAGE:
                // TODO: Message.TYPE_IMAGE
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

