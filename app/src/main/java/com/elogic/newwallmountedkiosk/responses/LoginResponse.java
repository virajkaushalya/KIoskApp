package com.elogic.newwallmountedkiosk.responses;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private LoginData data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LoginData getData() {
        return data;
    }
}
