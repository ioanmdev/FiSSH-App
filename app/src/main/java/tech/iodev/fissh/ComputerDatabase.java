package tech.iodev.fissh;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ioan on 2/1/18.
 */

public class ComputerDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FiSSH";

    private static final String TABLE_COMPUTERS = "Computer";
    private static final String KEY_ID = "id";
    private static final String KEY_COMPUTERIP = "computerip";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_CERTIFICATE = "certificate";
    private static final String KEY_NICKNAME = "nickname";

    private Context CONTEXT;

    public ComputerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        CONTEXT = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Initialize table
        String CREATE_FISSH_TABLE = "CREATE TABLE " + TABLE_COMPUTERS + " (" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NICKNAME + " TEXT," + KEY_COMPUTERIP + " TEXT," + KEY_PASSWORD + " TEXT," + KEY_CERTIFICATE + " BLOB);";
        sqLiteDatabase.execSQL(CREATE_FISSH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Recreate
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPUTERS);
        onCreate(sqLiteDatabase);
    }

    // CRUD methods

    public void addComputer(Computer computer) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NICKNAME, computer.Nickname);
        values.put(KEY_COMPUTERIP, computer.ComputerIP);
        values.put(KEY_PASSWORD, computer.Password);

        sqLiteDatabase.insert(TABLE_COMPUTERS, null, values);
        sqLiteDatabase.close();
    }

    public List<Computer> getComputers() {
        List<Computer> results = new ArrayList<Computer>();

        String SELECT_QUERY = "SELECT * FROM " + TABLE_COMPUTERS;

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                Computer computerRow = new Computer(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getBlob(4));
                results.add(computerRow);
            } while(cursor.moveToNext());
        }

        sqLiteDatabase.close();

        return results;
    }

    public void updateComputer (Computer newInfo) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NICKNAME, newInfo.Nickname);
        values.put(KEY_COMPUTERIP, newInfo.ComputerIP);
        values.put(KEY_PASSWORD, newInfo.Password);
        values.put(KEY_CERTIFICATE, newInfo.Certificate);

        sqLiteDatabase.update(TABLE_COMPUTERS, values,KEY_ID + " = ? ", new String[] {String.valueOf(newInfo.Id)});
        sqLiteDatabase.close();
    }

    public void deleteComputer (Computer toDelete) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_COMPUTERS, KEY_ID + " = ? ", new String[] {String.valueOf(toDelete.Id)});
        sqLiteDatabase.close();
    }

    /*
     This method loads the SharedPreferences used by old versions of FiSSH.
     After it inserts the computerIP and the password into the table, it deletes them
     (from SharedPreferences)
      */

    public void upgradeFromPreferences()
    {
        final String PREFS_NAME = "FiSSH";

        SharedPreferences settings = CONTEXT.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        if (settings.getBoolean("first_run", true)) return;

        String computerIP = settings.getString("computer_ip", "-0xDNE");
        String password = settings.getString("password", "-0xDNE");

        if (computerIP != "-0xDNE" && password != "-0xDNE")
        {
            // Delete all SharedPreferences
            editor.clear();
            editor.commit();

            // Insert new computer
            Computer backPorted = new Computer(computerIP, password);
            backPorted.Nickname = "Unnamed Computer";

            try {
                backPorted.Certificate = Selfish.selfish.getStoredCertificateInFile();
            }
            catch (Exception ex)
            {
                // Ignore it, no big deal
            }

            addComputer(backPorted);
        }
    }

}
