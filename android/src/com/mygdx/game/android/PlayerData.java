package com.mygdx.game.android;

import android.location.Location;

import java.util.Date;

/**
 * Created by Rutger on 12-11-2014.
 */
public class PlayerData {
    String databaseId;
    String username;

    Location location;
    Date updateDate;

    public PlayerData(String databaseId, String username, Location currentLocation, Date updateDate) {
        this.databaseId = databaseId;
        this.username = username;
        this.location = currentLocation;
        this.updateDate = updateDate;
    }

    public PlayerData(String username, Location currentLocation) {
        this.username = username;
        this.location = currentLocation;
    }
}

