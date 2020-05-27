package com.threadteam.thread.models;

import java.sql.Timestamp;

public class ChatMessage {

    // Instance-specific variables
    private String _sender;
    private String _message;
    private Timestamp _timestamp;

    public ChatMessage(String sender, String _message) {
        this._sender = sender;
        this._message = _message;

        // Leave timestamp empty for server side generation
    }

    public ChatMessage(String sender, String message, Timestamp ts) {
        this._sender = sender;
        this._message = message;
        this._timestamp = ts;
    }

    public String get_message() { return this._message; }

    public String get_sender() { return this._sender; }

    public Timestamp get_timestamp() { return this._timestamp; }
}
