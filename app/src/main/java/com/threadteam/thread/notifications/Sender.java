package com.threadteam.thread.notifications;

import com.google.gson.annotations.SerializedName;

/**
 * The Sender class
 * @author Mohamed Thabith
 * @version 2.0
 * @since 2.0
 */

public class Sender {
   @SerializedName("to")
    private String to;

   @SerializedName("notification")
    private NotificationModel notification;

    /**
     * Sender class constructor
     * @param to
     * @param notification
     */
   public Sender(String to,NotificationModel notification){
       this.to = to;
       this.notification = notification;
   }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public NotificationModel getNotification() {
        return notification;
    }

    public void setNotification(NotificationModel notification) {
        this.notification = notification;
    }
}
