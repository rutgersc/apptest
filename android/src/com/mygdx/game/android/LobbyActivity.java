package com.mygdx.game.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mygdx.game.GameLauncher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LobbyActivity extends FragmentActivity implements LocationListener {

    private final String TAG = "Lobby";

    private static final int SETTING__GPS_MINIMAL_INTERVAL = 1000; // milliseconds
    private static final int SETTING__GPS_MINIMAL_DISTANCE = 0; // meters
    String locationProvider = LocationManager.NETWORK_PROVIDER;
    //String locationProvider = LocationManager.GPS_PROVIDER;

    LocationManager mLocationManager;
    boolean mIsLocationProviderEnabled;
    boolean mIsGpsUpdaterSetup;
    boolean mStartGpsUpdaterOnResume = true;

    Location mCurrentBestLocation;
    GoogleMap mGoogleMap;
    Marker mYouMarker;
    List<Marker> mPlayerMarkers;

    ConnectToServerTask mConnectToServerTask = null;
    RequestUpdatePlayersTask mRequestUpdatePlayersTask = null;
    ServerConnection mServerConnection;
    LoginSession mLoginSession;
    SearchGameTask mSearchGameTask = null;
    public static MatchmakingConnection mMatchmakingConnection;
    AcceptGameTask mAcceptGameTask = null;

    RelativeLayout topLayout;
    Button mPlayGameButton, mStopSearchingButton, mAcceptGameButton, mDeclineGameButton;
    NumberPicker mGamePicker;
    String[] mGamePickerValues = new String[3];
    TextView mUsernameTextView;
    TextView mConnectedToServerTextView;
    TextView mPlayerCountTextView, mPlayersSearchingTextView;

    RelativeLayout mSearchGameLayout, mAcceptGameLayout;
    int mAcceptGameLayoutHeight, mSearchGameLayoutHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);


        // Get the extra login data from LoginActivity.java
        //
        String username = getIntent().getStringExtra("username");
        String sessionId = getIntent().getStringExtra("sessionId");
        if (username == null || sessionId == null) {
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
        topLayout = (RelativeLayout) findViewById(R.id.topLayout);

        mPlayGameButton = (Button) findViewById(R.id.playGameButton);
        mPlayGameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchGameTask == null) {
                    mSearchGameTask = new SearchGameTask();
                    mSearchGameTask.execute((String) mGamePickerValues[mGamePicker.getValue()]);
                }
            }
        });
        mStopSearchingButton = (Button) findViewById(R.id.stopSearchingButton);
        mStopSearchingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchGameTask != null) {
                    mSearchGameTask.cancelSearch();
                }
            }
        });
        OnClickListener acceptGameOnclickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean gameAccepted = (view.getId() == mAcceptGameButton.getId());

                if (mAcceptGameTask == null) {
                    mAcceptGameTask = new AcceptGameTask();
                    mAcceptGameTask.execute(gameAccepted);
                }
            }
        };
        mAcceptGameButton = (Button) findViewById(R.id.acceptGameButton);
        mAcceptGameButton.setOnClickListener(acceptGameOnclickListener);
        mDeclineGameButton = (Button) findViewById(R.id.declineGameButton);
        mDeclineGameButton.setOnClickListener(acceptGameOnclickListener);

        mGamePicker = (NumberPicker) findViewById(R.id.gamePicker);
        mGamePickerValues[0] = " 5 min";
        mGamePickerValues[1] = "10 min";
        mGamePickerValues[2] = "10+ min";
        mGamePicker.setMinValue(0);
        mGamePicker.setMaxValue(mGamePickerValues.length - 1);
        mGamePicker.setDisplayedValues(mGamePickerValues);

        mSearchGameLayout = (RelativeLayout) findViewById(R.id.searchGameLayout);
        mAcceptGameLayout = (RelativeLayout) findViewById(R.id.acceptGameLayout);
        ViewTreeObserver vto = mAcceptGameLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAcceptGameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mAcceptGameLayoutHeight = mAcceptGameLayout.getMeasuredHeight();
                mSearchGameLayoutHeight =  mSearchGameLayout.getMeasuredHeight();

                mAcceptGameLayout.setY(-mAcceptGameLayoutHeight);
                mSearchGameLayout.setY(-mSearchGameLayoutHeight);
            }
        });



        mUsernameTextView = (TextView) findViewById(R.id.usernameTextView);
        if (mLoginSession != null) {
            mUsernameTextView.setText(mLoginSession.getUsername());
        }

        mPlayerCountTextView = (TextView) findViewById(R.id.playerCountTextView);

        mPlayersSearchingTextView = (TextView) findViewById(R.id.playersSearchingTextView);

        mConnectedToServerTextView = (TextView) findViewById(R.id.connectedToServertextView);
        mConnectedToServerTextView.setTextColor(Color.RED);

        // Set up Google Maps
        //
        mPlayerMarkers = new ArrayList<Marker>();

        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.location_map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mGoogleMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                //TODO:mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                Location lastKnownLocation = mLocationManager.getLastKnownLocation(locationProvider);
                if (lastKnownLocation != null) {
                    Log.d(TAG, "Last Known: " + locationToString(lastKnownLocation));
                    updateMapPositionAndMarker(lastKnownLocation);
                }
            }
        }
    }

    public void showSearchGameLayout() {
        // Stop other background tasks
        completelyStopUpdatingGpsLocation();
        mSearchGameLayout.animate().y(0);
    }

    public void hideSearchGameLayout() {
        mSearchGameLayout.animate().y(-mSearchGameLayoutHeight);
    }

    public void showAcceptGameLayout() {
        mAcceptGameLayout.animate().setDuration(1200);
        mAcceptGameLayout.animate().y(0);
    }

    public void hideAcceptGameLayout() {
        mAcceptGameLayout.animate().setDuration(500);
        mAcceptGameLayout.animate().y(-mAcceptGameLayoutHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // if TRUE -> Start looking for the GPS Location
        if (mStartGpsUpdaterOnResume) {
            registerGpsLocationUpdater();
        }

        if (mServerConnection == null || !mServerConnection.isConnected()) {
            if (mConnectToServerTask == null) { // If there isn't already a connect task running.
                mConnectToServerTask = new ConnectToServerTask();
                mConnectToServerTask.execute((Void) null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * If an activity has lost focus but is still visible
         */

        // Don't look for new locations while app is in the background
        unregisterGpsLocationUpdater();
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onStop() {
        /**
         * If an activity is completely obscured by another activity, it is stopped.
         * It still retains all state and member information, however, it is
         * no longer visible to the user so its window is hidden and it
         * will often be killed by the system when memory is needed elsewhere.
         */
        super.onStop();

        try {
            if (mServerConnection != null) {
                mServerConnection.disconnect();
            }

            if(mMatchmakingConnection != null) {
                mMatchmakingConnection.stopSearchingForGame();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerGpsLocationUpdater() {
        mIsLocationProviderEnabled = mLocationManager.isProviderEnabled(locationProvider);
        if (!mIsGpsUpdaterSetup && mIsLocationProviderEnabled) {

            mLocationManager.requestLocationUpdates(
                    locationProvider, // Find location by WiFi or Cell Network
                    SETTING__GPS_MINIMAL_INTERVAL, // minimum time interval between notifications, milliseconds
                    SETTING__GPS_MINIMAL_DISTANCE, // minimum distance in meters between notifications
                    this);

            Log.d(TAG, "Gps enabled");
            mIsGpsUpdaterSetup = true;
        }

        if (!mIsLocationProviderEnabled) {
            showActivateGpsAlert();
        }
    }

    public void unregisterGpsLocationUpdater() {
        if (mIsGpsUpdaterSetup) {
            Log.d(TAG, "Gps disabled");
            mLocationManager.removeUpdates(this);
            mIsGpsUpdaterSetup = false;
        }
    }

    public void startGpsUpdater() {
        mStartGpsUpdaterOnResume = true;
        registerGpsLocationUpdater();
    }

    /**
     * So that when the app comes back to the foreground (onResume) the GPS doesn't automatically
     * register again.
     */
    public void completelyStopUpdatingGpsLocation() {
        mStartGpsUpdaterOnResume = false;
        unregisterGpsLocationUpdater();
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {

        if (currentBestLocation == null) {
            return true;
        }

        // if location is the same
        if (location.getLatitude() == currentBestLocation.getLatitude() &&
                location.getLongitude() == currentBestLocation.getLatitude()) {
            return false;
        }

        //TODO: Add logic (check for accuracy etc)

        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("Lobby", "New Location " + locationToString(location));

            if (isBetterLocation(location, mCurrentBestLocation)) {

                mCurrentBestLocation = location;

                if (mServerConnection != null) {
                    if (mRequestUpdatePlayersTask == null) { // Only update players if the last request is done.
                        mRequestUpdatePlayersTask = new RequestUpdatePlayersTask();
                        mRequestUpdatePlayersTask.execute(location);
                        Log.d(TAG, "Updating players locations");
                    }
                }

                updateMapPositionAndMarker(location);
            }
        }
    }

    protected void updatePlayerMarkers(List<PlayerData> players) {

        // Remove ALL player markers
        if (mPlayerMarkers.size() > 0) {
            for (Marker marker : mPlayerMarkers) {
                marker.remove();
            }
        }

        mPlayerMarkers.clear();

        // Re-add all players
        for (PlayerData player : players) {
            Marker playerMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(player.location.getLatitude(), player.location.getLongitude()))
                    .title(player.username));

            mPlayerMarkers.add(playerMarker);
        }
    }

    /**
     * Called when GPS Provider is changed. NETWORK_PROVIDER / GPS_PROVIDER
     */
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


    private void updateMapPositionAndMarker(Location location) {
        if (mYouMarker != null)
            mYouMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("You");
        mYouMarker = mGoogleMap.addMarker(markerOptions);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    class ConnectToServerTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mConnectedToServerTextView.setTextColor(Color.CYAN);
            mConnectedToServerTextView.setText("Connecting...");
            Log.d(TAG, "Trying to connect to server");

            mPlayGameButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if (mServerConnection == null) {
                    mServerConnection = new ServerConnection(mLoginSession);
                } else {
                    mServerConnection.reconnect();
                }
            } catch (IOException e) {
                Log.d(TAG, "The d");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (mServerConnection == null || !mServerConnection.isConnected()) {
                mConnectedToServerTextView.setTextColor(Color.RED);
                mConnectedToServerTextView.setText("Failed connecting");
                Log.e(TAG, "Failed connecting to server!!!!!");
                completelyStopUpdatingGpsLocation();
            } else {
                mConnectedToServerTextView.setTextColor(Color.GREEN);
                mConnectedToServerTextView.setText("Connected");
                Log.d(TAG, "Connected to server!!!!!");
                startGpsUpdater();
                mPlayGameButton.setEnabled(true);
            }

            mConnectToServerTask = null;
        }
    }

    class RequestUpdatePlayersTask extends AsyncTask<Location, Void, Void> {

        private List<PlayerData> players;

        @Override
        protected Void doInBackground(Location... location) {

            try {
                mServerConnection.sendLocationUpdate(location[0]);
                players = mServerConnection.requestNearbyPlayers();
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "Failed to update player info!!!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            if (players != null) {
                updatePlayerMarkers(players);
                mPlayerCountTextView.setText(String.valueOf(players.size()));

                // (De)activate player button
                if (players.size() > 0) {
                    mPlayGameButton.setEnabled(true);
                } else {
                    mPlayGameButton.setEnabled(false);
                }
            }

            mRequestUpdatePlayersTask = null;
        }
    }

    class SearchGameTask extends AsyncTask<String, Void, Boolean> {

        public void cancelSearch()  {
            if(mMatchmakingConnection != null) {
                try {
                    mMatchmakingConnection.stopSearchingForGame();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPreExecute () {
            showSearchGameLayout();
        }

        @Override
        protected Boolean doInBackground(String... gameTypes) {

            Boolean foundGame = false;
            String gameType = gameTypes[0];
            try {
                mMatchmakingConnection = new MatchmakingConnection(mLoginSession);
                foundGame = mMatchmakingConnection.startSearching(gameType);

                if(!foundGame) {
                    Log.e(TAG, "Problem finding game");
                }

            } catch (IOException e) {
                // e.printStackTrace();
                Log.e(TAG, "Stopped searching");
            }

            return foundGame;
        }

        @Override
        protected void onPostExecute(Boolean foundGame) {

            hideSearchGameLayout();

            if(foundGame) {
                try { Thread.sleep(500); }
                catch (InterruptedException e) { e.printStackTrace();  }

                showAcceptGameLayout();
            }
            else {
                Toast.makeText(getApplicationContext(), "No game found", Toast.LENGTH_LONG).show();
            }

            mSearchGameTask = null;
        }
    }

    class AcceptGameTask extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... gameAccepted) {

            Boolean accepted = gameAccepted[0];

            boolean confirmation = false;
            try {
                confirmation = mMatchmakingConnection.sendAcceptGameCommand(accepted);
            } catch (IOException e) {
                Log.e(TAG, "Game declined by other player");
            }

            return confirmation;
        }

        @Override
        protected void onPostExecute(Boolean confirmation) {

            if(confirmation) {
                // Starting game!
                Toast.makeText(getApplicationContext(), "Start Game!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LobbyActivity.this, AndroidLauncher.class);
                startActivity(intent);
            }
            else {
                //TODO: Game not accepted by one of the players
                Toast.makeText(getApplicationContext(), "Other player cancelled game!", Toast.LENGTH_LONG).show();
            }

            hideAcceptGameLayout();
            mAcceptGameTask = null;
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
