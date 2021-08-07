package ro.ioanm.fissh.core;

/**
 * Created by Ioan Moldovan on 10/29/2017.
 */

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;


import java.io.*;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ro.ioanm.fissh.MainFragment;

public class TCPMessenger {

    private Computer computerDetails;
    private MainFragment CONTEXT;

    public final String TAG = "FiSSH";

    public TCPMessenger(MainFragment context, Computer details)
    {
        CONTEXT = context;
        computerDetails = details;
    }

    public void run() {
        TCPMessageSendTask sender = new TCPMessageSendTask(CONTEXT);
        sender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    // Check self signed certificates
    TrustManager[] selfSignedTrust = new TrustManager[] { new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // Not implemented
            // Client doesn't have any certificate
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // Server certificate is always first in chain
            byte[] serverCert = arg0[0].getEncoded();

            try {
                byte[] oldCert = computerDetails.Certificate;

                if (oldCert == null)
                    throw new Exception("Stored certificate not found");

                if (Arrays.equals(serverCert, oldCert))
                    return; // All is fine, certificate checks out
                CONTEXT.requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CONTEXT.reportUnknownCertificate(Selfish.getX509Fingerprint(oldCert), Selfish.getX509Fingerprint(serverCert), serverCert);
                    }
                });

            }
            catch (Exception e)
            {
                CONTEXT.requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CONTEXT.reportUnknownCertificate("NONE", Selfish.getX509Fingerprint(serverCert), serverCert);
                    }
                });

            }

            throw new CertificateException();
        }
    } };

    /**
     * A simple task for sending messages across the network.
     */
    @SuppressLint("StaticFieldLeak")
    public class TCPMessageSendTask extends AsyncTask<Void, Void, Void> {

        private PrintWriter out;
        private SSLSocket socket = null;
        private MainFragment context = null;


        public TCPMessageSendTask(MainFragment context){
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... arg0){

            try {
                Log.d(TAG, "Connecting...");

                //create a socket to make the connection with the server
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, selfSignedTrust, new java.security.SecureRandom());

                socket = (SSLSocket) sc.getSocketFactory().createSocket();
                socket.connect(new InetSocketAddress(computerDetails.ComputerIP, 2222), 4000);
                socket.startHandshake();

                try {

                    // Create the message sender
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    Log.d(TAG, "Connected!");

                } catch (Exception e) {
                    context.requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.reportNetworkError();
                        }
                    });
                    Log.e(TAG, "Server Error", e);

                } finally {

                    if (out != null && !out.checkError()) {
                        try{
                            out.println(computerDetails.Password);
                            out.flush();
                        }
                        catch (Exception e){
                            context.requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    context.reportNetworkError();
                                }
                            });

                            Log.e(TAG, "Some Error happened!");
                        }
                    }

                    // Tell Fingerprint Handler all is fine

                    context.requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.authorizationSent();
                        }
                    });

                    // Close the socket after stopClient is called
                    out.close();
                    socket.close();
                }

            }
            catch (SSLHandshakeException he)
            {
                Log.e(TAG, "Certificate Error", he);
            }
            catch (Exception e) {
                context.requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.reportNetworkError();
                    }
                });

                Log.e(TAG, "Error", e);

            }

            return null;
        }
    }
}