package ro.ioanm.fissh.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import java.io.FileInputStream;


import java.security.MessageDigest;


/**
 * Created by ioan on 1/25/18.
 */

public class Selfish {
    private Context context;

    public static Selfish selfish;

    public ComputerDatabase DB;

    public Selfish(Context ctx) {
        this.selfish = this;
        this.selfish.context = ctx;

        DB = new ComputerDatabase(context);
    }

    // X509 fingerprint utils
    public static String getX509Fingerprint(byte[] cert) {
        String hexString = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            hexString = byte2HexFormatted(publicKey);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return hexString;
    }

    // Hashing utils
    public static String getSHA256Hash(String dbPass) {
        String hexString = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            byte[] publicKey = md.digest(dbPass.getBytes());
            hexString = byte2HexFormatted(publicKey);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return hexString;
    }

    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);

        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }

        return str.toString();
    }
    @SuppressLint("HardwareIds")
    public static String getEncryptionPassword(Context ctx) {
        String code = Base64.encodeToString(getSHA256Hash("Th1$|s" + Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID) + "FiSSH!").getBytes(), Base64.NO_WRAP);
        code += "FiSSH!";
        return code;
    }

}
