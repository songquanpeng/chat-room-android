package com.song.chat.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.song.chat.R;

public class HomeFragment extends Fragment {
    private EditText messageInput;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        messageInput = root.findViewById(R.id.message_input);
        Button sendBtn = root.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i("user", "click send btn");
                sendBtnClicked(true);
            }
        });
        sendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i("user", "long click send btn");
                sendBtnClicked(false);
                return false;
            }
        });
        return root;
    }

    private void sendBtnClicked(boolean isTextMessage){
        if(isTextMessage){
            String messageContent = messageInput.getText().toString().trim();
            if(messageContent.equals("")) return;
            Log.i("user send message", messageContent);
            // TODO: use socket to send message here.
            messageInput.setText("");
        }
        // TODO: select image from phone, upload & then send image url.
    }
}
