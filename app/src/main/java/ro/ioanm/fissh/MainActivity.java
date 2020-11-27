package ro.ioanm.fissh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Computer> COMPUTERS;
    private ArrayAdapter<Computer> ADAPTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init selfish (self signed utils)
        new Selfish(this);

        // Copy all the old settings
        Selfish.selfish.DB.upgradeFromPreferences();

        // Prepare to load computers from Database
        ADAPTER = new ArrayAdapter<Computer>(this, android.R.layout.simple_list_item_2, android.R.id.text1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(COMPUTERS.get(position).Nickname);
                text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                text1.setPadding(0, 0,0, getResources().getDimensionPixelSize(R.dimen.lvPadding));

                text2.setText(COMPUTERS.get(position).ComputerIP);
                text2.setPadding(0, 0,0, getResources().getDimensionPixelSize(R.dimen.lvPadding));

                return view;
            }
        };

        ListView lvComputers = findViewById(R.id.lvComputers);
        lvComputers.setAdapter(ADAPTER);

        // Configure listview event handler
        lvComputers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                scanFingerprint(COMPUTERS.get(i));
            }
        });

        registerForContextMenu(lvComputers);

        // Actually load the computers
        loadComputers();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComputer();
            }
        });

        // Show MIT LICENSE first time
        showLicenseFirstTime();
    }

    private void showLicenseFirstTime() {
        final String PREFS_NAME = "FiSSH";

        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        if (settings.getBoolean("license_seen", false)) return;

        showLicense();

        editor.putBoolean("license_seen", true);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_main_menu, menu); //your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_about:
                showLicense();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLicense() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("MIT License");
        builder.setMessage(
                "Copyright (c) 2020 Ioan Moldovan\n" +
                "\n" +
                "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                "of this software and associated documentation files (the \"Software\"), to deal\n" +
                "in the Software without restriction, including without limitation the rights\n" +
                "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                "copies of the Software, and to permit persons to whom the Software is\n" +
                "furnished to do so, subject to the following conditions:\n" +
                "\n" +
                "The above copyright notice and this permission notice shall be included in all\n" +
                "copies or substantial portions of the Software.\n" +
                "\n" +
                "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                "SOFTWARE.\n");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dlg = builder.create();
        dlg.show();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lvComputers) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle("What do you want to do?");
            menu.add(Menu.NONE, 0, 0, "Edit computer");
            menu.add(Menu.NONE, 1, 1, "Delete computer");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = item.getItemId();


        switch (index){
            case 0:
                editComputer(info.position);
                break;
            case 1:
                removeComputer(info.position);
                break;
        }

        return true;
    }

    private void scanFingerprint(Computer c)
    {
        Intent scanIntent = new Intent(this, ScanActivity.class);
        scanIntent.putExtra("computer", c);
        startActivity(scanIntent);
    }

    private void removeComputer (final int pos)
    {
        // Display an error message
        AlertDialog.Builder bld = new AlertDialog.Builder(this);

        bld.setTitle("Delete computer?");

        bld.setMessage("Are you sure you want to remove this computer from FiSSH?");

        bld.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        bld.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                Selfish.selfish.DB.deleteComputer(COMPUTERS.get(pos));
                loadComputers();
            }
        });

        AlertDialog errorMsg = bld.create();
        errorMsg.show();
    }

    private void loadComputers()
    {
        COMPUTERS = Selfish.selfish.DB.getComputers();

        ADAPTER.clear();

        for(Computer computer: COMPUTERS)
            ADAPTER.add(computer);


        ADAPTER.notifyDataSetChanged();
    }


    private void addComputer()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 0);

    }

    private void editComputer(int pos)
    {
        Intent intent = new Intent(this, SettingsActivity.class);

        intent.putExtra("nickname", COMPUTERS.get(pos).Nickname);
        intent.putExtra("computer_ip", COMPUTERS.get(pos).ComputerIP);
        intent.putExtra("password", COMPUTERS.get(pos).Password);
        intent.putExtra("id", pos);

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComputers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0)
        {
            if (resultCode == RESULT_OK)
            {
                String nickname = data.getStringExtra("nickname");
                String computerIP = data.getStringExtra("computer_ip");
                String password = data.getStringExtra("password");

                Selfish.selfish.DB.addComputer(new Computer(nickname, computerIP, password));

                loadComputers();
            }
        }
        else if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                int id = data.getIntExtra("id", -1);

                Computer toEdit = COMPUTERS.get(id);

                toEdit.Nickname = data.getStringExtra("nickname");
                toEdit.ComputerIP = data.getStringExtra("computer_ip");
                toEdit.Password = data.getStringExtra("password");

                Selfish.selfish.DB.updateComputer(toEdit);

                loadComputers();
            }
        }
    }
}
