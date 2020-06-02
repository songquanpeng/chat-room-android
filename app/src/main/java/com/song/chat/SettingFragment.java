package com.song.chat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.song.chat.R;

public class SettingFragment extends Fragment {
    private EditText serverEditText;
    private EditText usernameEditText;
    private EditText roomIDEditText;
    private SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Activity activity = this.getActivity();
        if (activity != null) {
            sharedPreferences = activity.getSharedPreferences("data", Context.MODE_PRIVATE);
        } else {
            Log.e("fatal", "activity is null!");
        }
        View root = inflater.inflate(R.layout.fragment_setting, container, false);
        serverEditText = root.findViewById(R.id.serverEditText);
        usernameEditText = root.findViewById(R.id.usernameEditText);
        roomIDEditText = root.findViewById(R.id.roomIDEditText);
        Button saveBtn = root.findViewById(R.id.save_setting_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("user", "click save setting btn");
                save();
            }
        });
        load();
        return root;
    }

    private void save() {
        String serverAddress = serverEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String roomID = roomIDEditText.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this.getActivity(), "Please notice username is empty", Toast.LENGTH_LONG).show();
            return;
        }
        if (roomID.isEmpty() || !roomID.startsWith("/")) {
            roomID = "/" + roomID;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("serverAddress", serverAddress);
        editor.putString("username", username);
        editor.putString("roomID", roomID);
        editor.apply();
        Toast.makeText(this.getActivity(), "Saved", Toast.LENGTH_SHORT).show();
    }

    private void load() {
        String server_address = sharedPreferences.getString("serverAddress", "https://chat.iamazing.cn/");
        String username = sharedPreferences.getString("username", "Default Username");
        String roomID = sharedPreferences.getString("roomID", "/");
        serverEditText.setText(server_address);
        usernameEditText.setText(username);
        roomIDEditText.setText(roomID);
    }
}
