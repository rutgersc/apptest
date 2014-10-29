package com.mygdx.game.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class LobbyActivity extends Activity implements LocationListener {

    private final String TAG = "Lobby";

    String locationProvider = LocationManager.NETWORK_PROVIDER;
    //String locationProvider = LocationManager.GPS_PROVIDER;

    LocationManager mLocationManager;
    boolean mIsLocationProviderEnabled;
    boolean mIsGpsUpdaterEnabled;

    Location mCurrentBestLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // Set up GPS
        //
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Set up interface
        //
        Button mLoginButton = (Button) findViewById(R.id.buttonPlayGame);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LobbyActivity.this, AndroidGameLauncher.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onPause () {
        super.onPause();

        // Don't look for new locations while app is in the background
        if(mIsGpsUpdaterEnabled) {
            Log.d(TAG, "Gps disabled");
            mLocationManager.removeUpdates(this);
            mIsGpsUpdaterEnabled = false;
        }
    }

    @Override
    protected void onResume () {
        super.onResume();

        // Start updating the location
        mIsLocationProviderEnabled = mLocationManager.isProviderEnabled(locationProvider);

        if(!mIsGpsUpdaterEnabled && mIsLocationProviderEnabled) {

            Location lastKnownLocation = mLocationManager.getLastKnownLocation(locationProvider);

            if(lastKnownLocation != null) {
                Log.d(TAG, "Last Known: " + locationToString(lastKnownLocation));
            }

            mLocationManager.requestLocationUpdates(
                        locationProvider, // Find location by WiFi or Cell Network
                        0, // minimum time interval between notifications
                        0, // minimum distance between notifications
                        this);

            Log.d(TAG, "Gps enabled");
            mIsGpsUpdaterEnabled = true;
        }

        if(!mIsLocationProviderEnabled) {
            showActivateGpsAlert();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "OnStop" );
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {

        //TODO: Add logic
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            Log.d("Lobby", "New Location " + locationToString(location));
            //mLocationManager.removeUpdates(this);

            if (isBetterLocation(location, mCurrentBestLocation)) {
                //TODO: Send update to dinges
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "onProviderEnabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "onProviderDisabled: " + s);
    }

    String locationToString(Location location) {
        return "Location(lat: " + location.getLatitude() + ",long: " + location.getLongitude() + ") altitude: " + location.getAltitude() + " - accuracy: " + location.getAccuracy();
    }

    private void showActivateGpsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        LobbyActivity.this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog activateGpsAlertDialog = builder.create();
        activateGpsAlertDialog.show();
    }
}
