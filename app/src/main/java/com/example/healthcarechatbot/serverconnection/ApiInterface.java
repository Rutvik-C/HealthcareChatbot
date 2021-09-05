package com.example.healthcarechatbot.serverconnection;

import com.example.healthcarechatbot.classes.Response;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiInterface {
    // String BASE_URL = "http://10.0.2.2:5000/";
    String BASE_URL = "http://192.168.0.100:5000/";


    @POST("chat")
    Call<Response> sendMessage(@Body RequestBody body);
}
