package science.iodev.fissh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
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
