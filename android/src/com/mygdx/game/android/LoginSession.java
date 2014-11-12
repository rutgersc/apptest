package com.mygdx.game.android;

/**
 * Created by Rutger on 11-11-2014.
 */
public class LoginSession {

    private String mUsername;
    //private String password;

    private String mSessionId;

    public String getUsername() {
        return mUsername;
    }
    public String getSessionId() {
        return mSessionId;
    }

    LoginSession(String username, String sessionId) {
        mUsername = username;
        mSessionId = sessionId;
    }
}
