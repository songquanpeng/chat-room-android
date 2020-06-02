package com.song.chat;

class Message {
    static final String TYPE_TEXT = "TEXT";
    static final String TYPE_IMAGE = "IMAGE";
    static final String TYPE_FILE = "FILE";
    String content;
    String sender;
    String type;
    boolean sendByMyself;

    Message(String content, String sender, String type, boolean sendByMyself) {
        this.content = content;
        this.type = type;
        this.sender = sender;
        this.sendByMyself = sendByMyself;
    }
}
