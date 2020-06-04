package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.sql.Timestamp;

@IgnoreExtraProperties
public class ChatMessage {

    @Exclude
    private String _id;

    // Instance-specific variables
    private String _senderUID;
    private String _sender;
    private String _message;

    @Exclude
    private Long timestampMillis;

    // Empty constructor for Firebase
    public ChatMessage() {}

    public ChatMessage(String senderUID, String sender, String message) {
        this._senderUID = senderUID;
        this._sender = sender;
        this._message = message;

        // Leave timestamp empty for server side generation
    }

    public ChatMessage(String senderUID, String sender, String message, Long timestampMillis) {
        this._senderUID = senderUID;
        this._sender = sender;
        this._message = message;
        this.timestampMillis = timestampMillis;
    }

    public String get_id() { return this._id; }

    public void set_id(String _id) { this._id = _id; }

    public String get_message() { return this._message; }

    public void set_message(String _message) { this._message = _message; }

    public String get_sender() { return this._sender; }

    public void set_sender(String _sender) { this._sender = _sender; }

    public String get_senderUID() { return _senderUID; }

    public void set_senderUID(String _senderUID) { this._senderUID = _senderUID; }

    public Long getTimestampMillis() { return this.timestampMillis; }

    public void setTimestampMillis(Long timestampMillis) { this.timestampMillis = timestampMillis; }
}
