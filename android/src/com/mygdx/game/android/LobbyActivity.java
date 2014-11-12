package com.mygdx.game.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LobbyActivity extends FragmentActivity implements LocationListener {

    private final String TAG = "Lobby";

    ConnectToServerTask mConnectToServerTask = null;
    ServerConnection mServerConnection;
    LoginSession mLoginSession;

    String locationProvider = LocationManager.NETWORK_PROVIDER;
    //String locationProvider = LocationManager.GPS_PROVIDER;

    LocationManager mLocationManager;
    boolean mIsLocationProviderEnabled;
    boolean mIsGpsUpdaterEnabled;

    Location mCurrentBestLocation;

    GoogleMap mGoogleMap;
    MarkerOptions mYouMarker;
    List<MarkerOptions> mPlayerMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // Get the extra login data from LoginActivity.java
        //
        String username = getIntent().getStringExtra("username");
        String sessionId = getIntent().getStringExtra("sessionId");
        if(username == null || sessionId == null) {
            Log.e(TAG, "No username or sessionId found.");
            mLoginSession = new LoginSession("test", "testSessionId"); //TODO: Remove this test line
        } else {
            mLoginSession = new LoginSession(username, sessionId);
        }

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

        // Set up Google Maps
        //
        setupMapIfNeeded();
        mPlayerMarkers = new ArrayList<MarkerOptions>();
        mYouMarker = new MarkerOptions();

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
    protected void onResume () {
        super.onResume();

        // Start updating the location
        mIsLocationProviderEnabled = mLocationManager.isProviderEnabled(locationProvider);
        if(!mIsGpsUpdaterEnabled && mIsLocationProviderEnabled) {

            mLocationManager.requestLocationUpdates(
                    locationProvider, // Find location by WiFi or Cell Network
                    0, // minimum time interval between notifications, milliseconds
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
    protected void onPause () {
        super.onPause();
        /**
         * If an activity has lost focus but is still visible
         */

        // Don't look for new locations while app is in the background
        if(mIsGpsUpdaterEnabled) {
            Log.d(TAG, "Gps disabled");
            mLocationManager.removeUpdates(this);
            mIsGpsUpdaterEnabled = false;
        }
    }

    @Override
    protected void onStart () {
        super.onStart();

        if(mServerConnection == null) {
            if(mConnectToServerTask == null) {
                mConnectToServerTask = new ConnectToServerTask();
                mConnectToServerTask.execute((Void) null);
            }
        }
    }

    @Override
    protected void onStop () {
        /**
         * If an activity is completely obscured by another activity, it is stopped.
         * It still retains all state and member information, however, it is
         * no longer visible to the user so its window is hidden and it
         * will often be killed by the system when memory is needed elsewhere.
         */
        super.onStop();

        if(mServerConnection != null) {
            try {
                mServerConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerConnection = null;
        }
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

                mCurrentBestLocation = location;
                if(mServerConnection != null)
                    mServerConnection.sendLocationUpdate(location);
                updateMapPosition(location);

                Log.d(TAG, "Updating location");
            }
        }
    }

    /**
     * Called when GPS Provider is changed. NETWORK_PROVIDER / GPS_PROVIDER
     */
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

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

    private void setupMapIfNeeded() {
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


    /**
     * Represents an asynchronous login/registration task used to authenticate the user.
     */
    class ConnectToServerTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Log.d(TAG, "Trying to connect to server");
                mServerConnection = new ServerConnection(mLoginSession);
                Log.d(TAG, "Connected to server!!!!!");
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "Failed to connect to server!!!!!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mConnectToServerTask = null;
        }

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

    String locationToString(Location location) {
        return "Location(lat: " + location.getLatitude() + ",long: " + location.getLongitude() + ") altitude: " + location.getAltitude() + " - accuracy: " + location.getAccuracy();
    }

}
