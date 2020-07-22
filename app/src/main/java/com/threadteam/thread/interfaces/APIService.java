package com.threadteam.thread.interfaces;

import com.threadteam.thread.notifications.Sender;
import com.threadteam.thread.notifications.ThreadResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAADHjmeZk:APA91bH4d33dBMJj5kBlrDXRZg8uYOgIMWRVCeVjP9TY9lgCMvCoERhsI4oVtiChgZV2kcil68moeMvMtfVYZwLCf4xmmH7ERQekC1xzvVU492K8TH4qShlw70H96ypvd-4HcrcEqYpz"
            }
    )

    @POST("fcm/send")
    Call<ThreadResponse> sendNotification(@Body Sender sender);
}
