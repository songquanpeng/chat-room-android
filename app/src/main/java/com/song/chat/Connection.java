package com.song.chat;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

class Connection {
    private static Connection instance = new Connection();
    private String username;
    private String roomID;
    private Socket socket;
    private boolean initialized;

    private Connection() {
        initialized = false;
    }

    void init(String serverAddress, String username, String roomID,
              Emitter.Listener onConnect, Emitter.Listener onDisconnect,
              Emitter.Listener onConnectError, Emitter.Listener onMessage,
              Emitter.Listener onConflictUsername, Emitter.Listener onRegisterSuccess) {
        //if(initialized) return;
        initialized = true;
        this.username = username;
        this.roomID = roomID;
        try {
            socket = IO.socket(serverAddress);
        } catch (URISyntaxException e) {
            Log.e("cannot initialize socket", e.toString());
            e.printStackTrace();
            return;
        }
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on("message", onMessage);
        socket.on("conflict username", onConflictUsername);
        socket.on("register success", onRegisterSuccess);
        socket.connect();
        registerUsername();
    }

    private void registerUsername() {
        if (socket != null) {
            socket.emit("register", username, roomID);
        }
    }

    void destroy() {
        if (socket != null) socket.disconnect();
    }

    void sendMessage(String content, String type) {
        if (socket == null) return;
        try {
            JSONObject data = new JSONObject();
            data.put("content", content);
            data.put("type", type);
            socket.emit("message", data, roomID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static Connection getInstance() {
        return instance;
    }
}
