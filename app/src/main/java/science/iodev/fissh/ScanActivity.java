package science.iodev.fissh;

import android.Manifest;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.cert.CertificateException;

public class ScanActivity extends AppCompatActivity {

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private static final String KEY_NAME = "FiSSH";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintHandler fingerprintHelper;


    private Computer computer;

    public Boolean ScanRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Load received data
        computer = (Computer) getIntent().getSerializableExtra("computer");

        // Scan Finger
        ScanFinger();
    }




    // Scan the Fingerprint!
    public void ScanFinger()
    {
        if (ScanRunning) return;

        // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
        // or higher before executing any fingerprint-related code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            //Check whether the device has a fingerprint sensor//
            if (!fingerprintManager.isHardwareDetected()) {
                // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
                Snackbar.make(this.findViewById(android.R.id.content), "Your device doesn't support fingerprint authentication", Snackbar.LENGTH_LONG).show();
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                Snackbar.make(this.findViewById(android.R.id.content), "Please enable the fingerprint permission", Snackbar.LENGTH_LONG).show();
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
                Snackbar.make(this.findViewById(android.R.id.content), "No fingerprint configured. Please register at least one fingerprint in your device's Settings", Snackbar.LENGTH_LONG).show();
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                Snackbar.make(this.findViewById(android.R.id.content), "Please enable lockscreen security in your device's Settings", Snackbar.LENGTH_LONG).show();
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                    fingerprintHelper = new FingerprintHandler(this);
                    Snackbar.make(this.findViewById(android.R.id.content), "Ready to scan your finger!", Snackbar.LENGTH_SHORT).show();
                    fingerprintHelper.startAuth(fingerprintManager, cryptoObject, computer);
                    ScanRunning = true;
                }
            }
        }

    }

    //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    public void FinishScanning() {
        ScanRunning = false;

        // Display an error message
        AlertDialog.Builder bld = new AlertDialog.Builder(this);

        bld.setTitle("Success!");
        bld.setMessage("Authorization sent to " + computer.Nickname + " (" + computer.ComputerIP + ")");

        bld.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        AlertDialog successMsg = bld.create();
        successMsg.show();
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu); //your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_retry:
                undoFatalError();
                ScanFinger();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void reportFatalError()
    {
        ScanRunning = false;

        // Display an error message
        AlertDialog.Builder bld = new AlertDialog.Builder(this);

        bld.setTitle("Error");
        bld.setMessage("Fingerprint authentication failed! Try again later!");

        bld.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog errorMsg = bld.create();
        errorMsg.show();

        // Time to change the UI a little
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainLayout.setBackgroundResource(R.color.colorBackgroundRed);

        TextView fingerPrinttext = (TextView) findViewById(R.id.fingerprint_text);
        fingerPrinttext.setText("Scanning failed! Click retry!");
    }

    public void reportFatalNetworkError()
    {
        ScanRunning = false;

        // Display an error message
        AlertDialog.Builder bld = new AlertDialog.Builder(this);

        bld.setTitle("Error");
        bld.setMessage("Network Error! Check your connection and Try again!");

        bld.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog errorMsg = bld.create();
        errorMsg.show();

        // Time to change the UI a little
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainLayout.setBackgroundResource(R.color.colorBackgroundRed);

        TextView fingerPrinttext = (TextView) findViewById(R.id.fingerprint_text);
        fingerPrinttext.setText("Network failed! Click retry!");
    }

    public void reportCertificateError(String oldFingerprint, String newFingerprint, final byte[] toSave)
    {
        ScanRunning = false;

        // Display an error message
        AlertDialog.Builder bld = new AlertDialog.Builder(this);

        bld.setTitle("Connection Aborted");

        if (oldFingerprint.equals("NONE"))
            bld.setMessage("FiSSH now supports self-signed certificate validation to prevent Man-In-The-Middle attacks\n\nPlease confirm that your certificate's fingerprint is:\n" + newFingerprint + "\n\nIf you confirm this certificate, it will be stored and trusted from now on.");
        else
            bld.setMessage("Unknown Certificate!\n\nWarning: This could be a Man-In-The-Middle attack\n\nThe fingerprint of the NEW certificate is:\n" + newFingerprint + "\n\nAnd the fingerprint of the trusted (stored) certificate is:\n" + oldFingerprint + "\n\nShould I trust the new one from now on?");

        bld.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                Snackbar.make(findViewById(android.R.id.content), "New certificate REJECTED by user!", Snackbar.LENGTH_LONG).show();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ScanFinger();
                    }
                }, 2000);
            }
        });

        bld.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                try {
                    computer.Certificate = toSave;
                    Selfish.selfish.DB.updateComputer(computer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Snackbar.make(findViewById(android.R.id.content), "New certificate APPROVED by user!", Snackbar.LENGTH_LONG).show();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ScanFinger();
                    }
                }, 2000);
            }
        });



        AlertDialog errorMsg = bld.create();
        errorMsg.show();

    }


    private void undoFatalError()
    {
        // Time to change the UI a little
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainLayout.setBackgroundResource(R.color.colorBackground);

        TextView fingerPrinttext = (TextView) findViewById(R.id.fingerprint_text);
        fingerPrinttext.setText("Please scan your fingerprint!");
    }

    @Override
    protected void onPause()
    {
        if (ScanRunning)
            fingerprintHelper.cancelAuth();

        super.onPause();
    }

}
