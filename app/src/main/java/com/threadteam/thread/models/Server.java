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
        - serverId
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

    public Server(Integer id, Integer ownerID, String serverName) {
        this._id = id;
        this._ownerID = ownerID;
        this._name = serverName;
    }
}
