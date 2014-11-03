package com.mygdx.game.android;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class LoginActivity extends Activity {
    private final String TAG = "LoginActivity";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AsyncTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;

    Switch mGuestLoginSwitch;

    boolean TEMP_DEBUG_ON = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        ImageButton mLoginButton = (ImageButton) findViewById(R.id.buttonLogin);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TEMP_DEBUG_ON) {
                    openLobby();
                }else {
                    onLogin();
                }
            }
        });

        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    onLogin();
                    return true;
                }
                return false;
            }
        });

        mGuestLoginSwitch = (Switch) findViewById(R.id.switchLoginAsGuest);
        mGuestLoginSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isOn = ((Switch)view).isChecked();

                if(isOn) {
                    mUsernameView.setEnabled(false);
                    mPasswordView.setEnabled(false);
                } else {
                    mUsernameView.setEnabled(true);
                    mPasswordView.setEnabled(true);
                }
            }
        });

        Button mDebugButton_skipLogin = (Button)findViewById(R.id.debugButton_skipLogin);
        mDebugButton_skipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLobby();
            }
        });

        if(TEMP_DEBUG_ON) {
            mDebugButton_skipLogin.setVisibility(View.INVISIBLE);
        }
    }

    public void openLobby() {
        startActivity(new Intent(LoginActivity.this, LobbyActivity.class));
    }

    /**
     * Will log in as GUEST or as USER
     */
    public void onLogin() {
        if(mGuestLoginSwitch.isChecked()) {
            attemptGuestLogin();
        } else {
            attemptLogin();
        }
    }

    public void attemptGuestLogin() {
        if (mAuthTask != null) {
            return;
        }

        //TODO: showProgress(true);
        GuestLoginTask guestLoginTask = new GuestLoginTask();
        mAuthTask = guestLoginTask;
        guestLoginTask.execute((Void) null);
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //TODO: showProgress(true);
            UserLoginTask userLoginTask = new UserLoginTask(username, password);
            mAuthTask = userLoginTask;
            userLoginTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                SecureServerConnection conn = new SecureServerConnection(LoginActivity.this);
                return conn.login(mUsername, mPassword);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            //TODO: showProgress(false);

            if (success) {
                Toast.makeText(getApplicationContext(), "Logged in as: " + mUsername, Toast.LENGTH_LONG).show();
                openLobby();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //TODO: showProgress(false);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate the guest.
     */
    public class GuestLoginTask extends AsyncTask<Void, Void, String> {

        GuestLoginTask() {}

        @Override
        protected String doInBackground(Void... params) {

            // Will return the new name of the guest (ex. guest1234) if successful.
            try {
                SecureServerConnection conn = new SecureServerConnection(LoginActivity.this);
                return conn.guestLogin();

            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String guestName) {
            mAuthTask = null;
            //TODO: showProgress(false);

            Log.d(LoginActivity.this.TAG, "Guest logged in as: " + guestName);

            if (!guestName.equals("")) { // if logging in was successful

                Toast.makeText(getApplicationContext(), "Logged in as: " + guestName, Toast.LENGTH_LONG).show();
                openLobby();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //TODO: showProgress(false);
        }
    }

}
