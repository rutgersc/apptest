package com.mygdx.game.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.GameLauncher;
import com.mygdx.game.MyGdxGame1;


public class AndroidLauncher extends AndroidApplication {

    AndroidImplementation implementation;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Hiermee kunnen we vanuit de game android code aanroepen (zoals bv teruggaan naar de lobby etc.)
        implementation = new AndroidImplementation(this);


        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream

        MatchmakingConnection mmc = LobbyActivity.mMatchmakingConnection;
        if(mmc == null) {
            implementation.openLobby();
        }

        // Gegenereerde ligGDX code
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(new MyGdxGame1(implementation), config);

        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream
        //TODO:   ---- ADD network input/output stream



    }
}
