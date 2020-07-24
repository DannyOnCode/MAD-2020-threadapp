package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;

/**
 * Represents a single post.
 *
 * @author Danny Chan Yu Tian
 * @version 2.0
 * @since 2.0
 */
public class PostMessage {

    /**
     * Unique identifier.
     * Excluded from Firebase automatic child values; this should be used as a key.
     */
    @Exclude
    private String _id;

    /** The user id of the sender. */
    private String _senderID;

    /** The username of the sender. */
    private String _senderUsername;

    /** The comment content of the post message. */
    private String _comment;

    /**
     * TimeStamp for Local Use. Should not be synced with firebase.
     * Fetch Firebase's timestamp Child Value for a more accurate Timestamp
     */
    @Exclude
    private Long timestampMillis;

    /** The Title of the user who sent the comment */
    @Exclude
    private String _title;

    /** The Level of the user who sent the comment */
    @Exclude
    private String _level;

    /** The Display Colour of the user who sent the comment */
    @Exclude
    private Integer _displayColour;

    // CONSTRUCTORS

    // FIREBASE REQUIRED BLANK CONSTRUCTOR
    public PostMessage(){}

    public PostMessage(String _senderID, String _senderUsername, String _comment) {
        this._senderID = _senderID;
        this._senderUsername = _senderUsername;
        this._comment = _comment;

        // LEAVE TIMESTAMP EMPTY FOR SOLE-SERVER SIDE GENERATION
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
