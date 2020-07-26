package com.threadteam.thread.notifications;

/**
 * The NotificationModel class.
 *
 * @author Mohamed Thabith
 * @version 2.0
 * @since 2.0
 */

public class NotificationModel {
    private String title;
    private String body;
    private String profile;
    private String serverID;
    private String ownerID;
    private String activity;

    /**
     * NotificationModel class constructor
     * @param title
     * @param body
     */
    public NotificationModel(String title, String body,String profile, String serverID, String ownerID, String activity) {
        this.title = title;
        this.body = body;
        this.profile = profile;
        this.serverID = serverID;
        this.ownerID = ownerID;
        this.activity = activity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
}
