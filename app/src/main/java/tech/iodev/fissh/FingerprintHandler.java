package tech.iodev.fissh;

/**
 * Created by Ioan Moldovan on 10/29/2017.
 */

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.Manifest;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    // You should use the CancellationSignal method whenever your app can no longer process user input, for example when your app goes
    // into the background. If you don’t use this method, then other apps will be unable to access the touch sensor, including the lockscreen!//

    private CancellationSignal cancellationSignal;
    private ScanActivity context;

    // Passed by ScanActivity
    private Computer computerDetails;

    public FingerprintHandler(ScanActivity mContext) {
        context = mContext;
    }

    //Implement the startAuth method, which is responsible for starting the fingerprint authentication process//

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject, Computer details) {

        computerDetails = details;

        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    public void cancelAuth()
    {
        // Send out the cancel!
        cancellationSignal.cancel();
        context.ScanRunning = false;
    }


    @Override
    //onAuthenticationError is called when a fatal error has occurred. It provides the error code and error message as its parameters//
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (errMsgId != FingerprintManager.FINGERPRINT_ERROR_CANCELED)
            context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                context.reportFatalError();
            }
        });
    }

    @Override
    //onAuthenticationFailed is called when the fingerprint doesn’t match with any of the fingerprints registered on the device//
    public void onAuthenticationFailed() {
        Snackbar.make(context.findViewById(android.R.id.content), "Authentication failed!", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    //onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional information about the error,
    //so to provide the user with as much feedback as possible I’m incorporating this information into my snackbar//
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Snackbar.make(context.findViewById(android.R.id.content), "Authentication help\n" + helpString, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    //onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints stored on the user’s device//
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {

            TCPMessenger msg = new TCPMessenger(this, computerDetails);
            msg.run();
    }

    private void reportScanningFinished()
    {
        context.ScanRunning = false;
        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                context.FinishScanning();
            }
        });
    }


    public void reportNetworkError()
    {
        context.runOnUiThread(new Runnable() {

                                  @Override
                                  public void run() {
                                      context.reportFatalNetworkError();
                                  }
                              });


    }

    public void reportUnknownCertificate(final String oldFingerprint, final String newFingerprint, final byte[] toSave)
    {
        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                context.reportCertificateError(oldFingerprint, newFingerprint, toSave);
            }
        });


    }

    // Runs after the SSH key phrase has been sent through network
    public void onAuthorizationFinished()
    {
        // Notify user of success
        reportScanningFinished();
    }


}