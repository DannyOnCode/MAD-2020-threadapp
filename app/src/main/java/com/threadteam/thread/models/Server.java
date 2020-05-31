package com.threadteam.thread.models;

import java.util.List;

public class Server {

    /*
    DATA MODEL FOR THREAD APP ~ with reference from https://firebase.google.com/docs/database/android/structure-data
    updated as of 3:07AM, 28 May 2020
    - users
        - userId -> view profile
            - user_data
            - subscribed_servers
    - servers -> view servers
        - serverId <- use serverId for loading information when user goes into a server-related activity
            - name
            - owner
    - members -> view members when in server
        - serverId
            - memberId
    - messages -> view chat when in server
        - serverId
            - messageId
                - sender
                - messageText
                - timestamp
     */

    // Instance-specific variables
    private Integer _id;
    private Integer _ownerID;
    private String _name;
    private String _desc;

    public Server(Integer id, Integer ownerID, String serverName, String serverDesc) {
        this._id = id;
        this._ownerID = ownerID;
        this._name = serverName;
        this._desc = serverDesc;
    }

    public Integer get_id() { return _id; }

    public String get_name() {
        return _name;
    }

    public String get_desc() { return _desc; }
}
