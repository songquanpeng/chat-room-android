package com.song.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.emitter.Emitter;

public class HomeFragment extends Fragment {
    private EditText messageInput;
    private Connection connection;
    private String username;
    private List<Message> messageList = new ArrayList<>();
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize web socket connection
        connection = Connection.getInstance();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        String serverAddress = sharedPreferences.getString("serverAddress", "https://chat.iamazing.cn/");
        username = sharedPreferences.getString("username", "Default Username");
        String roomID = sharedPreferences.getString("roomID", "/");
        connection.init(serverAddress, username, roomID,
                onConnect, onDisconnect,
                onConnectError, onMessage,
                onConflictUsername, onRegisterSuccess);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        messageInput = root.findViewById(R.id.message_input);
        Button sendBtn = root.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // Initialize recycle view
        messageRecyclerView = root.findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        messageRecyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messageList);
        messageRecyclerView.setAdapter(messageAdapter);
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connection.destroy();
    }

    private void sendBtnClicked(boolean isTextMessage) {
        if (isTextMessage) {
            String messageContent = messageInput.getText().toString().trim();
            if (messageContent.equals("")) return;
            Log.i("send message", messageContent);
            connection.sendMessage(messageContent, "TEXT");
            messageInput.setText("");
        }
        // TODO: select image from phone, upload & then send image url.
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            R.string.connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            R.string.disconnect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            R.string.connect_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String content;
                    String sender;
                    String type;
                    try {
                        content = data.getString("content");
                        sender = data.getString("sender");
                        type = data.getString("type");
                        Log.i("receive message", data.toString());
                        // TODO: Update UI
                        Message message = new Message(content, sender, type, username.equals(sender));
                        messageList.add(message);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        messageRecyclerView.scrollToPosition(messageList.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onConflictUsername = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            R.string.conflict_username, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onRegisterSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            R.string.register_success, Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}
