package com.omnitech.cryptune.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAADyZNYNA:APA91bEF-bDox8eIvyHnaBrx7C2-iCkTpCmqX2BnlxxQGrmb4fj8mRqeP6VvWOwZpHUZ9HHCXlk6oHz9yo7mBLFKcnQ7FXd_ZpdEY1ntQMmBwMYqeMLIQQ14U7brJqF8ynmkgJYUBXS9" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

