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
    private String serverAddress;
    private String username;
    private String roomID;
    private Socket socket;
    private Emitter.Listener onConnect;
    private Emitter.Listener onDisconnect;
    private Emitter.Listener onConnectError;
    private Emitter.Listener onMessage;
    private Emitter.Listener onConflictUsername;
    private Emitter.Listener onRegisterSuccess;

    private Connection() {
    }

    void init(Emitter.Listener onConnect, Emitter.Listener onDisconnect,
              Emitter.Listener onConnectError, Emitter.Listener onMessage,
              Emitter.Listener onConflictUsername, Emitter.Listener onRegisterSuccess) {
        this.onConnect = onConnect;
        this.onDisconnect = onDisconnect;
        this.onConnectError = onConnectError;
        this.onMessage = onMessage;
        this.onConflictUsername = onConflictUsername;
        this.onRegisterSuccess = onRegisterSuccess;
    }

    void connect(String serverAddress, String username, String roomID) {
        if (!serverAddress.equals(this.serverAddress) || !username.equals(this.username) || !roomID.equals(this.roomID)) {
            disconnect();
            this.serverAddress = serverAddress;
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
            if (socket != null) {
                socket.emit("register", username, roomID);
            }
        }
    }

    void disconnect() {
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
