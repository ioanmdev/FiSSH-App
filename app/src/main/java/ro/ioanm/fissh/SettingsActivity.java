package ro.ioanm.fissh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    EditText computerIP;
    EditText password;
    EditText nickname;
    int ID; // Computer ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        computerIP  = (EditText) findViewById(R.id.txtComputerIp);
        password =  (EditText) findViewById(R.id.txtPassword);
        nickname = (EditText) findViewById(R.id.txtNickname);

        addComputerIPErrorCheck();

        computerIP.setText(getIntent().getStringExtra("computer_ip"));
        password.setText(getIntent().getStringExtra("password"));
        nickname.setText(getIntent().getStringExtra("nickname"));

        ID = getIntent().getIntExtra("id", -1);
    }

    private void addComputerIPErrorCheck() {
        computerIP.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                // Check on defocus
                if (!b) validateComputerIP();
            }
        });
    }

    private void validateComputerIP()
    {
        if (computerIP.getText().toString().equals("")) {
            // Check if there's already an error set
            if (computerIP.getError() != null) return;

            computerIP.setError("Please enter Computer IP");
        }
    }

    public void save_settings(View sender)
    {
        validateComputerIP();
        if (computerIP.getError() != null) return;

        Intent result = new Intent();
        result.putExtra("computer_ip", computerIP.getText().toString());
        result.putExtra("password", password.getText().toString());
        result.putExtra("nickname", nickname.getText().toString());

        if (ID != -1) result.putExtra("id", ID);

        setResult(RESULT_OK, result);
        finish();
    }
}
