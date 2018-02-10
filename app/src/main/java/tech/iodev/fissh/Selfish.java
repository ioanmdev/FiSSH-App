package tech.iodev.fissh;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Created by ioan on 1/25/18.
 */

public class Selfish {
    private Context context;
    private FileInputStream inputStream;
    private String FILENAME = "fissh_cert";

    public static Selfish selfish;

    public ComputerDatabase DB;

    public Selfish(Context ctx) {
        this.selfish = this;
        this.selfish.context = ctx;

        DB = new ComputerDatabase(context);
    }

    // Kept only for backwards compatibility
    public byte[] getStoredCertificateInFile() throws IOException {

        inputStream = context.openFileInput(FILENAME);
        byte[] cert = new byte[inputStream.available()];

        inputStream.read(cert);
        inputStream.close();

        return cert;
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

}
