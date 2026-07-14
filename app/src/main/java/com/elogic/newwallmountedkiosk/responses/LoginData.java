package com.elogic.newwallmountedkiosk.responses;

import com.google.gson.annotations.SerializedName;

public class LoginData {
    @SerializedName("timerstatus")
    private String timerStatus;

    @SerializedName("token")
    private String token;

    public String getTimerStatus() {
        return timerStatus;
    }

    public String getToken() {
        return token;
    }
}
