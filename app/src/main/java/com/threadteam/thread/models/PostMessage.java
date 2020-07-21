package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;

public class PostMessage {

    @Exclude
    private String _id;

    private String _senderID;
    private String _senderUsername;
    private String _comment;

    // TIMESTAMP FOR LOCAL USE. SHOULD NOT BE SYNCED WITH FIREBASE.
    // FETCH FIREBASE'S timestamp CHILD VALUE FOR A MORE ACCURATE TIMESTAMP.
    @Exclude
    private Long timestampMillis;
    @Exclude
    private String _title;
    @Exclude
    private String _level;



    @Exclude
    private Integer _displayColour;

    public PostMessage(){}

    public PostMessage(String _senderID, String _senderUsername, String _comment) {
        this._senderID = _senderID;
        this._senderUsername = _senderUsername;
        this._comment = _comment;
    }

    public PostMessage(String _senderID, String _senderUsername, String _comment, Long timestampMillis) {
        this._senderID = _senderID;
        this._senderUsername = _senderUsername;
        this._comment = _comment;
        this.timestampMillis = timestampMillis;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_level() {
        return _level;
    }

    public void set_level(String _level) {
        this._level = _level;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_senderID() {
        return _senderID;
    }

    public void set_senderID(String _senderID) {
        this._senderID = _senderID;
    }

    public String get_senderUsername() {
        return _senderUsername;
    }

    public void set_senderUsername(String _senderUsername) {
        this._senderUsername = _senderUsername;
    }

    public String get_comment() {
        return _comment;
    }

    public void set_comment(String _comment) {
        this._comment = _comment;
    }

    public Long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(Long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    public Integer get_displayColour() {
        return _displayColour;
    }

    public void set_displayColour(Integer _displayColour) {
        this._displayColour = _displayColour;
    }


}
