package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

// SERVER CLASS
//
// PROGRAMMER-IN-CHARGE:
// EUGENE LONG, S10193060J
//
// DESCRIPTION
// REPRESENTS A SINGLE CHAT MESSAGE, CONTAINING
// ITS OWN UNIQUE ID, SENDER ID, SENDER NAME AND
// MESSAGE TEXT.

@IgnoreExtraProperties
public class ChatMessage {

    // DATA STORE

    // UNIQUE IDENTIFIER, EXCLUDE FROM FIREBASE CHILD VALUES.
    // SHOULD BE USED AS A KEY
    @Exclude
    private String _id;

    private String _senderUID;
    private String _sender;
    private String _message;

    // TIMESTAMP FOR LOCAL USE. SHOULD NOT BE SYNCED WITH FIREBASE.
    // FETCH FIREBASE'S timestamp CHILD VALUE FOR A MORE ACCURATE TIMESTAMP.
    @Exclude
    private Long timestampMillis;

    // CONSTRUCTORS

    // FIREBASE REQUIRED BLANK CONSTRUCTOR
    public ChatMessage() {}

    public ChatMessage(String senderUID, String sender, String message) {
        this._senderUID = senderUID;
        this._sender = sender;
        this._message = message;

        // LEAVE TIMESTAMP EMPTY FOR SOLE-SERVER SIDE GENERATION
    }

    public ChatMessage(String senderUID, String sender, String message, Long timestampMillis) {
        this._senderUID = senderUID;
        this._sender = sender;
        this._message = message;
        this.timestampMillis = timestampMillis;
    }

    // GET/SET METHODS

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

    // METHOD OVERRIDES

    @Override
    public String toString() {
        return "ChatMessage{" +
                "_id='" + _id + '\'' +
                ", _senderUID='" + _senderUID + '\'' +
                ", _sender='" + _sender + '\'' +
                ", _message='" + _message + '\'' +
                ", timestampMillis=" + timestampMillis +
                '}';
    }
}
