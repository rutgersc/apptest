package com.mygdx.game.android;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Rutger on 24-10-2014.
 */
public class ServerConnection {
    private final String TAG = "ConnectionService";

    public static final String HOSTNAME = "192.168.1.127"; //TODO: static ip from raspberry/server
    public static final int PORT = 8881;

    LoginSession mLoginSession;

    SocketAddress socketAddress;
    Socket socket = new Socket();

    InputStreamReader inputStreamRaw;
    OutputStreamWriter outputStreamRaw;

    BufferedReader in;
    PrintWriter out;

    ServerConnection(LoginSession session) throws IOException {
        mLoginSession = session;

        reconnect();
    }

    public void reconnect() throws IOException {
        socketAddress = new InetSocketAddress(HOSTNAME, PORT);
        socket.connect(socketAddress);

        inputStreamRaw = new InputStreamReader(socket.getInputStream());
        outputStreamRaw = new OutputStreamWriter(socket.getOutputStream());
        in = new BufferedReader(inputStreamRaw);
        out = new PrintWriter(outputStreamRaw);
    }

    public void disconnect() throws IOException {
        out.close();
        in.close();
        socket.close();
    }

    public void sendLocationUpdate(Location location)  {

        out.println("updateLocation");

        out.println(mLoginSession.getSessionId());

        out.println(location.getLatitude());
        out.println(location.getLongitude());
        out.flush();

        if (out.checkError()) {
            System.out.println("SocketClient: java.io.PrintWriter error");
        }

        /*
        try {
            String response = in.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Log.d(TAG, "sendLocationUpdate done");
    }

    public void sendAppIsInBackground() {
        //TODO: Optional??????
    }

    public void sendSarchingForGame() {
        //TODO: User clicked play button, send search request to server
    }
}

