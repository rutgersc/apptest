package com.mygdx.game.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SecureServerConnection {
    private final String TAG = "SecureLogin";

    public static final int SSL_PORT = 8882;

    private Context context;
    private SharedPreferences mPreferences;

    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;

    SecureServerConnection(Context context) throws IOException {

        this.context = context;
        this.mPreferences = context.getSharedPreferences("preferences", 0);

        try {
            Log.d(TAG, "SSL connecting to: " + ServerConnection.HOSTNAME + ":" + SecureServerConnection.SSL_PORT);

            char storePass[] = "aabb11".toCharArray();

            // Setup truststore
            KeyStore trustStore = KeyStore.getInstance("BKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.clienttruststore);
            trustStore.load(trustStoreStream, storePass);
            trustManagerFactory.init(trustStore);

            // Setup keystore
            KeyStore keyStore = KeyStore.getInstance("BKS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            InputStream keyStoreStream = context.getResources().openRawResource(R.raw.client);
            keyStore.load(keyStoreStream, storePass);
            keyManagerFactory.init(keyStore, storePass);

            // Setup the SSL context to use the truststore and keystore
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Set up the socket factory
            SocketFactory socketFactory = sslContext.getSocketFactory();
            this.socket = (SSLSocket) socketFactory.createSocket(ServerConnection.HOSTNAME, SecureServerConnection.SSL_PORT);
            this.socket.startHandshake();

            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }


    public String login(String userName, String password) throws IOException {

        String uuidFromServer = null;

        String existingUuid = "none";
        if(userName.equals(mPreferences.getString("uuid-username", ""))) {
            // only use existing UUID if the usernames are equal
            existingUuid = mPreferences.getString("uuid", "none");
        }

        out.println("login");
        out.println(userName);
        out.println(password);
        out.println(existingUuid); // send existing uuid if one exists.
        out.flush();

        if (out.checkError()) {
            System.out.println("SSLSocketClient: java.io.PrintWriter error");
        }

        String response = in.readLine();

        if (response != null) {
            Log.d(TAG, "Login Response: " + response);

            if (!response.equals("Fail")) {
                uuidFromServer = response;
            }
        }

        out.close();
        in.close();
        socket.close();

        // Save UUID in mPreferences
        if(uuidFromServer != null) {
            mPreferences.edit().putString("uuid", uuidFromServer)
                               .putString("uuid-username", userName).apply();
        }

        return uuidFromServer; // Returns null on failed login
    }


    public LoginSession guestLogin() throws IOException {

        LoginSession loginSession = null;

        out.println("guestLogin");
        out.flush();

        if (out.checkError()) {
            Log.e(TAG, "SSLSocketClient: java.io.PrintWriter error");
        }

        String response_GuestName = in.readLine();
        String response_uuid = in.readLine();

        if (response_GuestName != null && response_uuid != null) {
            loginSession = new LoginSession(response_GuestName, response_uuid);
        }

        out.close();
        in.close();
        socket.close();

        return loginSession; // Returns null on failed login
    }


    /**
     * DEBUG
     */
    private static void printSocketInfo(SSLSocket s) {
        System.out.println("Socket class: "+s.getClass());
        System.out.println("   Remote address = "
                +s.getInetAddress().toString());
        System.out.println("   Remote port = "+s.getPort());
        System.out.println("   Local socket address = "
                +s.getLocalSocketAddress().toString());
        System.out.println("   Local address = "
                +s.getLocalAddress().toString());
        System.out.println("   Local port = "+s.getLocalPort());
        System.out.println("   Need client authentication = "
                +s.getNeedClientAuth());
        SSLSession ss = s.getSession();
        System.out.println("   Cipher suite = "+ss.getCipherSuite());
        System.out.println("   Protocol = "+ss.getProtocol());
    }
}
