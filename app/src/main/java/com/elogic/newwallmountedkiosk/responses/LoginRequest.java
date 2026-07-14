package com.elogic.newwallmountedkiosk.responses;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("request_type")
    private String request_type;
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    public LoginRequest(String request_type, String username, String password) {
        this.request_type = request_type;
        this.username = username;
        this.password = password;
    }
}
