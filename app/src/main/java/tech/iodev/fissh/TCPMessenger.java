package tech.iodev.fissh;

/**
 * Created by Ioan Moldovan on 10/29/2017.
 */

import android.os.AsyncTask;
import android.util.Log;


import java.io.*;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TCPMessenger {


    private Computer computerDetails;
    private FingerprintHandler CONTEXT;


    public final String TAG = "FiSSH";


    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPMessenger(FingerprintHandler context, Computer details)
    {
        computerDetails = details;
        CONTEXT = context;
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

                CONTEXT.reportUnknownCertificate(Selfish.getX509Fingerprint(oldCert), Selfish.getX509Fingerprint(serverCert), serverCert);
            }
            catch (Exception e)
            {
                CONTEXT.reportUnknownCertificate("NONE", Selfish.getX509Fingerprint(serverCert), serverCert);
            }

            throw new CertificateException();


        }
    } };

    /**
     * A simple task for sending messages across the network.
     */
    public class TCPMessageSendTask extends AsyncTask<Void, Void, Void> {



        private PrintWriter out;
        private SSLSocket socket = null;
        private FingerprintHandler context = null;


        public TCPMessageSendTask(FingerprintHandler context){
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... arg0){

            try {
                Log.d(TAG, "Connecting...");

                //create a socket to make the connection with the server
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, selfSignedTrust, new java.security.SecureRandom());
                socket = (SSLSocket) sc.getSocketFactory().createSocket(new Socket(computerDetails.ComputerIP, 2222), computerDetails.ComputerIP, 2222, false);

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
                            out.println(computerDetails.Password);
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

            }
            catch (SSLHandshakeException he)
            {
                Log.e(TAG, "Certificate Error", he);
            }
            catch (Exception e) {
                context.reportNetworkError();
                Log.e(TAG, "Error", e);

            }


            return null;
        }
    }
}