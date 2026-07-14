package com.elogic.newwallmountedkiosk.services;

import com.elogic.newwallmountedkiosk.responses.ApiResponse;
import com.elogic.newwallmountedkiosk.responses.LoginRequest;
import com.elogic.newwallmountedkiosk.responses.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api.php")
    Call<ApiResponse> getTerminals();  //call

    @POST("api.php")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

}

