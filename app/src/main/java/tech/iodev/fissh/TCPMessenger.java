package tech.iodev.fissh;

/**
 * Created by Ioan Moldovan on 10/29/2017.
 */

import android.os.AsyncTask;
import android.util.Log;


import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TCPMessenger {


    private static String SERVER_IP;
    private static String PASSWORD;
    private static FingerprintHandler CONTEXT;


    public static final String TAG = "FiSSH";


    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPMessenger(FingerprintHandler context, String ip, String password)
    {
        SERVER_IP = ip;
        PASSWORD = password;
        CONTEXT = context;
    }




    public void run() {

        TCPMessageSendTask sender = new TCPMessageSendTask(CONTEXT, PASSWORD);
        sender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);



    }


    // Don't bother checking certificates
    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // Not implemented
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // Not implemented
        }
    } };

    /**
     * A simple task for sending messages across the network.
     */
    public class TCPMessageSendTask extends AsyncTask<Void, Void, Void> {



        private PrintWriter out;
        private String message;
        private SSLSocket socket = null;
        private FingerprintHandler context = null;


        public TCPMessageSendTask(FingerprintHandler context, String message){
            this.message = message;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... arg0){

            try {
                Log.d(TAG, "Connecting...");

                //create a socket to make the connection with the server
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                socket = (SSLSocket) sc.getSocketFactory().createSocket(new Socket(SERVER_IP, 2222), SERVER_IP, 2222, false);

                socket.startHandshake();

                try {

                    // Create the message sender
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    Log.d(TAG, "Connected!");



                } catch (Exception e) {
                    context.reportNetworkError();
                    Log.e(TAG, "Server Error", e);

                } finally {

                    if (out != null && !out.checkError()) {
                        try{
                            out.println(message);
                            out.flush();
                        }
                        catch (Exception e){
                            context.reportNetworkError();
                            Log.e(TAG, "Some Error happened!");
                        }
                    }

                    // Tell Fingerprint Handler all is fine
                    context.onAuthorizationFinished();

                    // Close the socket after stopClient is called
                    out.close();
                    socket.close();
                }

            } catch (Exception e) {
                context.reportNetworkError();
                Log.e(TAG, "Error", e);

            }


            return null;
        }
    }
}