package com.threadteam.thread.models;

import android.net.Uri;
import android.widget.ImageView;

import com.google.firebase.database.Exclude;
import com.threadteam.thread.Utils;

import java.util.ArrayList;
import java.util.List;

// SERVER CLASS
//
// PROGRAMMER-IN-CHARGE:
// EUGENE LONG, DANNY CHAN, THABITH, S10193060J, S10196363F, S10196396B
//
// DESCRIPTION
// REPRESENTS A USER. CONTAINS METADATA SUCH AS
// ITS OWN UNIQUE ID, USER NAME, USER PROFILE IMAGE,
// USER ABOUT ME MESSAGE, USER STATUS/TITLE MESSAGE AND
// SUBSCRIBED SERVER.
public class User {
    //DATA STORE

    // UNIQUE IDENTIFIER, EXCLUDE FROM FIREBASE CHILD VALUES.
    // SHOULD BE USED AS A KEY
    @Exclude
    private String _id;

    private String _username;
    private String _profileImageURL;
    private String _aboutUsMessage;
    private String _statusMessage;

    @Exclude
    private List<String> _subscribedServers = new ArrayList<String>();

    @Exclude
    private List<Integer> _expList = new ArrayList<>();

    // CONSTRUCTORS

    // FIREBASE REQUIRED BLANK CONSTRUCTOR
    public User(){

    }

    public User(String _id, String _username, String _profileImageURL, String _aboutUsMessage, String _statusMessage,List<String> _subscribedServers,List<Integer> _expList) {
        if(_aboutUsMessage.trim().equals("")){
            _aboutUsMessage = "No Description";
        }
        if(_statusMessage.trim().equals("")){
            _statusMessage = "No Status";
        }

        this._id = _id;
        this._username = _username;
        this._profileImageURL = _profileImageURL;
        this._aboutUsMessage = _aboutUsMessage;
        this._statusMessage = _statusMessage;
        this._subscribedServers = _subscribedServers;
        this._expList = _expList;
    }

    public Integer GetUserExpForServer(String serverID) {
        int serverIndex = _subscribedServers.indexOf(serverID);
        return this._expList.get(serverIndex);
    }

    public Integer GetUserLevelForServer(String serverID) {
        return Utils.ConvertExpToLevel(GetUserExpForServer(serverID));
    }

    public Integer GetExpToNextLevelForServer(String serverID) {
        return Utils.GetExpToNextLevel(GetUserLevelForServer(serverID));
    }

    public Integer GetProgressToNextLevelForServer(String serverID) {
        int exp = GetUserExpForServer(serverID);
        int level = GetUserLevelForServer(serverID);
        return Utils.GetExpProgress(exp, level);
    }

    // GET/SET METHODS
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public String get_profileImageURL() {
        return _profileImageURL;
    }

    public void set_profileImageURL(String _profileImageURL) {
        this._profileImageURL = _profileImageURL;
    }

    public String get_aboutUsMessage() {
        return _aboutUsMessage;
    }

    public void set_aboutUsMessage(String _aboutUsMessage) {
        if(_aboutUsMessage.trim().equals("")){
            _aboutUsMessage = "No Description";
        }
        this._aboutUsMessage = _aboutUsMessage;
    }

    public String get_statusMessage() {
        return _statusMessage;
    }

    public void set_statusMessage(String _statusMessage) {
        if(_statusMessage.trim().equals("")){
            _statusMessage = "No Status";
        }
        this._statusMessage = _statusMessage;
    }

    public List<String> get_subscribedServers() {
        return _subscribedServers;
    }

    public void set_subscribedServers(List<String> _subscribedServers) {
        this._subscribedServers = _subscribedServers;
    }

    public List<Integer> get_expList() {
        return _expList;
    }

    public void set_expList(List<Integer> _expList) {
        this._expList = _expList;
    }
}
