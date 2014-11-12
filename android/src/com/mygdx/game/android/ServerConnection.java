package com.mygdx.game.android;

import android.location.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

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
    }

    public List<PlayerData> requestNearbyPlayers() throws IOException {

        out.println("requestNearbyPlayers");
        out.println(mLoginSession.getSessionId());
        out.flush();

        String nearbyPlayersString = in.readLine();

        List<PlayerData> playerList = new ArrayList<PlayerData>();

        if(!nearbyPlayersString.equals("Empty")) {
            String[] players = nearbyPlayersString.split(";");

            for(String player : players) {
                String[] pInfo = player.split(",");

                Location loc = new Location("");
                loc.setLatitude(Double.parseDouble(pInfo[1]));
                loc.setLongitude(Double.parseDouble(pInfo[2]));

                PlayerData pd = new PlayerData(pInfo[0], loc);
                playerList.add(pd);
            }
        }

        return playerList;
    }

    public void sendAppIsInBackground() {
        //TODO: Optional??????
    }

    public void sendSarchingForGame() {
        //TODO: User clicked play button, send search request to server
    }
}

