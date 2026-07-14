package com.elogic.newwallmountedkiosk.responses;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    // The JSON array is returned as an escaped String, so we map it as a String
    @SerializedName("data")
    private String data;

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getData() { return data; }
}
