package com.threadteam.thread.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Represents a server.
 *
 * @author Eugene Long
 * @version 1.0
 * @since 1.0
 */

@IgnoreExtraProperties
public class Server implements Comparable<Server> {

    // DATA STORE

    /**
     * Unique identifier.
     * Excluded from Firebase automatic child values; this should be used as a key.
     */

    @Exclude private String _id;

    /** The id of the user who created this server. */
    private String _ownerID;

    /** The name of the server. */
    private String _name;

    /** The description of the server. */
    private String _desc;

    // CONSTRUCTORS

    // FIREBASE REQUIRED BLANK CONSTRUCTOR
    public Server() {}

    public Server(String ownerID, String serverName, String serverDesc) {
        this._ownerID = ownerID;
        this._name = serverName;
        this._desc = serverDesc;
    }

    // GET/SET METHODS

    public String get_ownerID() { return _ownerID; }

    public void set_ownerID(String _ownerID) { this._ownerID = _ownerID; }

    public String get_id() { return _id; }

    public void set_id(String _id) { this._id = _id; }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) { this._name = _name; }

    public String get_desc() { return _desc; }

    public void set_desc(String _desc) { this._desc = _desc; }

    // METHOD OVERRIDES

    @Override
    public int compareTo(Server o) {
        if(o.get_name().compareTo(this._name) > 0) {
            return -1;
        } else if (o.get_name().compareTo(this._name) < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Server{" +
                "_id='" + _id + '\'' +
                ", _ownerID='" + _ownerID + '\'' +
                ", _name='" + _name + '\'' +
                ", _desc='" + _desc + '\'' +
                '}';
    }
}
