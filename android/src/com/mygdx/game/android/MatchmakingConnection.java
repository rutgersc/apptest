package com.mygdx.game.android;

import android.location.Location;
import android.util.Log;

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
 * Created by Rutger on 25-11-2014.
 */
public class MatchmakingConnection {
    private final String TAG = "MatchmakingConnection";

    LoginSession mLoginSession;

    SocketAddress socketAddress;
    Socket socket = new Socket();

    InputStreamReader inputStreamRaw;
    OutputStreamWriter outputStreamRaw;
    BufferedReader in;
    PrintWriter out;

    String currentGameId = null;

    MatchmakingConnection(LoginSession session) throws IOException {
        mLoginSession = session;
    }

    private void connect() throws IOException {
        socketAddress = new InetSocketAddress(ServerConnection.HOSTNAME, ServerConnection.PORT);
        socket.connect(socketAddress);

        inputStreamRaw = new InputStreamReader(socket.getInputStream());
        outputStreamRaw = new OutputStreamWriter(socket.getOutputStream());
        in = new BufferedReader(inputStreamRaw);
        out = new PrintWriter(outputStreamRaw);

        out.println("searchGame");
        out.println(mLoginSession.getSessionId());
        out.flush();
    }

    public boolean startSearching(String gameType) throws IOException {
        connect();
        out.println("startSearch");
        out.println(gameType);
        out.flush();

        String response = in.readLine();

        // Wait until a player is found
        if (response != null && response.equals("waitingForPlayer")) {
            Log.d(TAG, "Waiting for a player");
            response = in.readLine();
        }

        if(response != null && response.equals("gameFound")) {

            currentGameId = in.readLine();
            Log.d(TAG, "Game id = " + currentGameId );

            return true;
        }

        return false;
    }

    /**
     * !!! Call from a different thread !!!! Not the one which called startSearching()
     */
    public void stopSearchingForGame() throws IOException {
        out.close();
        in.close();
        socket.close();
    }

    public boolean sendAcceptGameCommand(boolean accepted) throws IOException {

        if (currentGameId != null) {

            out.println("acceptGameChoice");
            out.println(currentGameId);
            out.println(accepted);
            out.flush();

            String confirmation = in.readLine();

            if (confirmation != null && confirmation.equals("startGame")) {
                return true;
            }
        }

        return false;
    }
}
