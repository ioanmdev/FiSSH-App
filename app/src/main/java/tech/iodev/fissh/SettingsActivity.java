package tech.iodev.fissh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {


    TextView computerIP;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        computerIP  = (TextView) findViewById(R.id.txtComputerIp);
        password =  (EditText) findViewById(R.id.txtPassword);

        computerIP.setText(getIntent().getStringExtra("computer_ip"));
        password.setText(getIntent().getStringExtra("password"));
    }

    public void save_settings(View sender)
    {
        Intent result = new Intent();
        result.putExtra("computer_ip", computerIP.getText().toString());
        result.putExtra("password", password.getText().toString());

        setResult(RESULT_OK, result);
        finish();
    }

}
