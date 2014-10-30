package com.mygdx.game.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.MyGdxGame;

public class AndroidLauncher extends AndroidApplication {

    AndroidImplementation implementation;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hiermee kunnen we vanuit de game android code aanroepen (zoals bv teruggaan naar de lobby etc.)
        implementation = new AndroidImplementation(this);

        // Gegenereerde ligGDX code
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(new MyGdxGame(implementation), config);
    }
}
