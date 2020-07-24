package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;
import com.threadteam.thread.libraries.Progression;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user.
 *
 * @author Eugene Long
 * @author Thabith
 * @author Danny Chan
 * @version 2.0
 * @since 1.0
 */

public class User {
    //DATA STORE

    /**
     * Unique identifier.
     * Excluded from Firebase automatic child values; this should be used as a key.
     */

    @Exclude private String _id;

    /** The name of the user. */
    private String _username;

    /** The image URL of the profile picture of the user. */
    private String _profileImageURL;

    /** The about message of the user. */
    private String _aboutUsMessage;

    /** The status message of the user. */
    private String _statusMessage;

    /** The device token of the user. */
    private String _token;

    /** A list of server ids which the user is subscribed to. */
    @Exclude private List<String> _subscribedServers = new ArrayList<String>();

    /** A list of experience points, each corresponding to a server the user is subscribed to. */
    @Exclude private List<Integer> _expList = new ArrayList<>();

    // CONSTRUCTORS

    // FIREBASE REQUIRED BLANK CONSTRUCTOR
    public User(){

    }

    public User(String _id, String _username, String _profileImageURL, String _aboutUsMessage, String _statusMessage,String _token,List<String> _subscribedServers,List<Integer> _expList) {
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
        this._token = _token;
        this._subscribedServers = _subscribedServers;
        this._expList = _expList;
    }

    /**
     * Retrieves the experience points corresponding to a server id.
     * @param serverID The id of the server to get the current user's experience points for.
     * @return The experience points of the user in the server specified.
     */

    public Integer GetUserExpForServer(String serverID) {
        int serverIndex = _subscribedServers.indexOf(serverID);
        return this._expList.get(serverIndex);
    }

    /**
     * Convenience wrapper for the ConvertExpToLevel function in Progression.
     * @param serverID The id of the server to get the user's level for
     * @return The level of the user in the specified server.
     * @see Progression#ConvertExpToLevel(int) 
     */

    public Integer GetUserLevelForServer(String serverID) {
        return Progression.ConvertExpToLevel(GetUserExpForServer(serverID));
    }

    /**
     * Convenience wrapper for the GetExpToNextLevel function in Progression.
     * @param serverID The id of the server to get the experience required to progress to the next level for.
     * @return The experience required to progress to the next level for the specified server.
     * @see Progression#GetExpToNextLevel(int)
     */

    public Integer GetExpToNextLevelForServer(String serverID) {
        return Progression.GetExpToNextLevel(GetUserLevelForServer(serverID));
    }

    /**
     * Convenience wrapper for the GetExpProgress function in Progression.
     * @param serverID The id of the server.
     * @return The current percentage progress of the user to the next level for the specified server.
     * @see Progression#GetExpProgress(int, int)
     */

    public Integer GetProgressToNextLevelForServer(String serverID) {
        int exp = GetUserExpForServer(serverID);
        int level = GetUserLevelForServer(serverID);
        return Progression.GetExpProgress(exp, level);
    }

    /**
     * Gets the absolute level progress for the current user in a specified server.
     * @param serverID The id of the server.
     * @return The absolute level progress for the current user in the specified server.
     */

    public Integer GetAbsoluteLevelProgressForServer(String serverID) {
        return (int) ((double) GetUserExpForServer(serverID) / (double) GetExpToNextLevelForServer(serverID) * 100);
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

    public String get_token() {
        return _token;
    }

    public void set_token(String _token) {
        this._token = _token;
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
