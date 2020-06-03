package com.song.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    private EditText serverEditText;
    private EditText usernameEditText;
    private EditText roomIDEditText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        serverEditText = this.findViewById(R.id.serverEditText);
        usernameEditText = this.findViewById(R.id.usernameEditText);
        roomIDEditText = this.findViewById(R.id.roomIDEditText);
        Button saveBtn = this.findViewById(R.id.save_setting_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("user", "click save setting btn");
                save();
            }
        });
        load();
    }

    private void save() {
        String serverAddress = serverEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String roomID = roomIDEditText.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Please notice username is empty", Toast.LENGTH_LONG).show();
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
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
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
