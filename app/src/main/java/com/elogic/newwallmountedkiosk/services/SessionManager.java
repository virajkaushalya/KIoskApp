package com.elogic.newwallmountedkiosk.services;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "AppSessionPref";
    private static final String KEY_TERMINAL_ID = "terminal_id";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_TIMER_STATUS = "timer_status";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveTerminalId(String terminalId) {
        editor.putString(KEY_TERMINAL_ID, terminalId);
        editor.apply();
    }

    public String getTerminalId() {
        return prefs.getString(KEY_TERMINAL_ID, null);
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public void saveTimerStatus(String timerStatus) {
        editor.putString(KEY_TIMER_STATUS, timerStatus);
        editor.apply();
    }

    public String getTimerStatus() {
        return prefs.getString(KEY_TIMER_STATUS, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

}
