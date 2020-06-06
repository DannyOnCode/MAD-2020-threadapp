package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

// CHAT MESSAGE CLASS
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

    private String _senderID;
    private String _senderUsername;
    private String _message;

    // TIMESTAMP FOR LOCAL USE. SHOULD NOT BE SYNCED WITH FIREBASE.
    // FETCH FIREBASE'S timestamp CHILD VALUE FOR A MORE ACCURATE TIMESTAMP.
    @Exclude
    private Long timestampMillis;

    // CONSTRUCTORS

    // FIREBASE REQUIRED BLANK CONSTRUCTOR
    public ChatMessage() {}

    public ChatMessage(String senderUID, String sender, String message) {
        this._senderID = senderUID;
        this._senderUsername = sender;
        this._message = message;

        // LEAVE TIMESTAMP EMPTY FOR SOLE-SERVER SIDE GENERATION
    }

    public ChatMessage(String senderUID, String sender, String message, Long timestampMillis) {
        this._senderID = senderUID;
        this._senderUsername = sender;
        this._message = message;
        this.timestampMillis = timestampMillis;
    }

    // GET/SET METHODS

    public String get_id() { return this._id; }

    public void set_id(String _id) { this._id = _id; }

    public String get_message() { return this._message; }

    public void set_message(String _message) { this._message = _message; }

    public String get_senderUsername() { return this._senderUsername; }

    public void set_senderUsername(String _senderUsername) { this._senderUsername = _senderUsername; }

    public String get_senderID() { return _senderID; }

    public void set_senderID(String _senderID) { this._senderID = _senderID; }

    public Long getTimestampMillis() { return this.timestampMillis; }

    public void setTimestampMillis(Long timestampMillis) { this.timestampMillis = timestampMillis; }

    // METHOD OVERRIDES

    @Override
    public String toString() {
        return "ChatMessage{" +
                "_id='" + _id + '\'' +
                ", _senderUID='" + _senderID + '\'' +
                ", _sender='" + _senderUsername + '\'' +
                ", _message='" + _message + '\'' +
                ", timestampMillis=" + timestampMillis +
                '}';
    }
}
