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
    private String serverID;
    private String isOwner;
    private String activity;

    /**
     * NotificationModel class constructor
     * @param title
     * @param body
     */
    public NotificationModel(String title, String body, String serverID, String isOwner, String activity) {
        this.title = title;
        this.body = body;
        this.serverID = serverID;
        this.isOwner = isOwner;
        this.activity = activity;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getIsOwner() {
        return isOwner;
    }

    public void setIsOwner(String isOwner) {
        this.isOwner = isOwner;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
}
