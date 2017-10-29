package tech.iodev.fissh;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "FiSSH";

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    TextView computerIP;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        computerIP  = (TextView) findViewById(R.id.txtComputerIp);
        password =  (EditText) findViewById(R.id.txtPassword);

        computerIP.setText(settings.getString("computer_ip", "10.0.0.0"));
        password.setText(settings.getString("password", ""));
    }

    public void save_settings(View sender)
    {
        // Disable config screen on startup
        editor.putBoolean("first_run", false);

        // Save IP and password
        editor.putString("computer_ip", computerIP.getText().toString());
        editor.putString("password", password.getText().toString());

        // COMMIT!
        editor.commit();

        finish();
    }

}
