package com.mygdx.game.android;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mygdx.game.NativeCode;

/**
 * Created by Rutger on 13-10-2014.
 */
public class AndroidImplementation implements NativeCode {

    private final Context context;

    AndroidImplementation(Context context) {
        this.context = context;
    }

    @Override
    public void log(String message) {
        Log.d("libGDX Game", message);
    }

    @Override
    public void doeIets() {
        Log.d("test", "Doet iets vanuit de game!");
    }

    @Override
    public void openLobby() {
        Intent intent = new Intent(context, LobbyActivity.class);
        context.startActivity(intent);
    }
}
