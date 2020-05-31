package com.threadteam.thread.models;

import android.net.Uri;
import android.widget.ImageView;

public class User {


    // Instance-specific variables
    private Integer _id;
    private String _username;
    private String _profileImage;
    private String _aboutUsMessage;
    private String _statusMessage;
    // Template User class placeholder
    public User(){

    }

    public User(Integer _id, String _username, String _profileImage, String _aboutUsMessage, String _statusMessage) {
        this._id = _id;
        this._username = _username;
        this._profileImage = _profileImage;
        this._aboutUsMessage = _aboutUsMessage;
        this._statusMessage = _statusMessage;
    }
    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public String get_profileImage() {
        return _profileImage;
    }

    public void set_profileImage(String _profileImage) {
        this._profileImage = _profileImage;
    }

    public String get_aboutUsMessage() {
        return _aboutUsMessage;
    }

    public void set_aboutUsMessage(String _aboutUsMessage) {
        this._aboutUsMessage = _aboutUsMessage;
    }

    public String get_statusMessage() {
        return _statusMessage;
    }

    public void set_statusMessage(String _statusMessage) {
        this._statusMessage = _statusMessage;
    }




}
