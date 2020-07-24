package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Represents a single post.
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 */

@IgnoreExtraProperties
public class Post {

    // DATA STORE

    /**
     * Unique identifier.
     * Excluded from Firebase automatic child values; this should be used as a key.
     */

    @Exclude private String _id;

    /** The image URL for the post. */
    private String _imageLink;

    /** The title of the post. */
    private String _title;

    /** The message content of the post. */
    private String _message;

    /** The user id of the sender. */
    private String _senderUID;

    /** The username of the sender. */
    private String _senderUsername;

    /**
     * The timestamp of the post when it was sent. Should be synced with Firebase.
     * There is no auto-generated timestamp cloud function for post timestamps!
     */

    @Exclude private Long timestampMillis;

    // CONSTRUCTORS

    // FIREBASE REQUIRED BLANK CONSTRUCTOR
    public Post() {}

    public Post(String imageLink, String title, String message, String senderUID, String senderUsername) {
        this._imageLink = imageLink;
        this._title = title;
        this._message = message;
        this._senderUID = senderUID;
        this._senderUsername = senderUsername;

        // LEAVE TIMESTAMP EMPTY FOR SOLE-SERVER SIDE GENERATION
    }

    public Post(String imageLink, String title, String message, String senderID, String senderUsername, Long timestampMillis) {
        this._imageLink = imageLink;
        this._title = title;
        this._message = message;
        this._senderUID = senderID;
        this._senderUsername = senderUsername;

        this.timestampMillis = timestampMillis;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_imageLink() {
        return _imageLink;
    }

    public void set_imageLink(String _imageLink) {
        this._imageLink = _imageLink;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }

    public String get_senderID() {
        return _senderUID;
    }

    public void set_senderID(String _senderUID) {
        this._senderUID = _senderUID;
    }

    public String get_senderUsername() {
        return _senderUsername;
    }

    public void set_senderUsername(String _senderUsername) {
        this._senderUsername = _senderUsername;
    }

    public Long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(Long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    // METHOD OVERRIDES

    @Override
    public String toString() {
        return "Post{" +
                "_id='" + _id + '\'' +
                ", _imageLink='" + _imageLink + '\'' +
                ", _title='" + _title + '\'' +
                ", _message='" + _message + '\'' +
                ", _senderID='" + _senderUID + '\'' +
                ", _senderUsername='" + _senderUsername + '\'' +
                ", timestampMillis=" + timestampMillis +
                '}';
    }
}
