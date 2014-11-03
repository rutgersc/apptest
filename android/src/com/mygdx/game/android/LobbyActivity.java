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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class LobbyActivity extends FragmentActivity implements LocationListener {

    private final String TAG = "Lobby";

    String locationProvider = LocationManager.NETWORK_PROVIDER;
    //String locationProvider = LocationManager.GPS_PROVIDER;

    LocationManager mLocationManager;
    boolean mIsLocationProviderEnabled;
    boolean mIsGpsUpdaterEnabled;

    Location mCurrentBestLocation;

    GoogleMap mGoogleMap;
    MarkerOptions mYouMarker;

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
                Intent intent = new Intent(LobbyActivity.this, AndroidLauncher.class);
                startActivity(intent);
            }
        });

        setUpMapIfNeeded();

        Location lastKnownLocation = mLocationManager.getLastKnownLocation(locationProvider);
        if(lastKnownLocation != null) {
            Log.d(TAG, "Last Known: " + locationToString(lastKnownLocation));


            updateMapPosition(lastKnownLocation);

            mYouMarker = new MarkerOptions()
                    .position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                    .title("You");
            mGoogleMap.addMarker(mYouMarker);
        }
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

            mLocationManager.requestLocationUpdates(
                        locationProvider, // Find location by WiFi or Cell Network
                        0, // minimum time interval between notifications
                        5, // minimum distance in meters between notifications
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


    private void updateMapPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.location_map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mGoogleMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                //TODO:mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        }
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
