package com.elogic.newwallmountedkiosk.responses;

import com.google.gson.annotations.SerializedName;

public class Terminal {
    @SerializedName("id")
    private String id;

    @SerializedName("description")
    private String description;

    public String getId() { return id; }
    public String getDescription() { return description; }

    // Crucial: ArrayAdapter uses this to display the name in the dropdown
    @Override
    public String toString() {
        return description;
    }
}
