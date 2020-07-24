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

    /**
     * NotificationModel class constructor
     * @param title
     * @param body
     */
    public NotificationModel(String title, String body) {
        this.title = title;
        this.body = body;
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
}
