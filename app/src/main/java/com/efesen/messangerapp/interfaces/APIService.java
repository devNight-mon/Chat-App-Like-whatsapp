package com.efesen.messangerapp.interfaces;

import com.efesen.messangerapp.notifications.MyResponse;
import com.efesen.messangerapp.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;

import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Efe Şen on 20.09.2023.
 */
public interface  APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAU4PFTGE:APA91bFlu4DC5ljRXgsXVSmJK72ppoOZOH2TKnzKGmFwzDwx1ZKer3g-ibrIZ_Jz0YTyq6LUHkYpSHuzjhQr-xmzO-vw9H3wMR8jlywq9_CJsVTT3IU9Fw-PbOJI-qq8jlsvLU59u8-V"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
