package ro.ioanm.fissh.core;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ioan Moldovan on 2/1/18.
 * SQLCipher implemented 7/25/21.
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

    ComputerDatabase(Context context) {
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
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase(Selfish.getEncryptionPassword(CONTEXT));

        ContentValues values = new ContentValues();

        values.put(KEY_NICKNAME, computer.Nickname);
        values.put(KEY_COMPUTERIP, computer.ComputerIP);
        values.put(KEY_PASSWORD, computer.Password);
        values.put(KEY_CERTIFICATE, computer.Certificate);

        sqLiteDatabase.insert(TABLE_COMPUTERS, null, values);
        sqLiteDatabase.close();
    }

    public ArrayList<Computer> getComputers() {
        ArrayList<Computer> results = new ArrayList<Computer>();

        String SELECT_QUERY = "SELECT * FROM " + TABLE_COMPUTERS;

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase(Selfish.getEncryptionPassword(CONTEXT));
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

    public void updateComputer(Computer newInfo) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase(Selfish.getEncryptionPassword(CONTEXT));

        ContentValues values = new ContentValues();

        values.put(KEY_NICKNAME, newInfo.Nickname);
        values.put(KEY_COMPUTERIP, newInfo.ComputerIP);
        values.put(KEY_PASSWORD, newInfo.Password);
        values.put(KEY_CERTIFICATE, newInfo.Certificate);

        sqLiteDatabase.update(TABLE_COMPUTERS, values,KEY_ID + " = ? ", new String[] {String.valueOf(newInfo.Id)});
        sqLiteDatabase.close();
    }

    public void deleteComputer(Computer toDelete) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase(Selfish.getEncryptionPassword(CONTEXT));
        sqLiteDatabase.delete(TABLE_COMPUTERS, KEY_ID + " = ? ", new String[] {String.valueOf(toDelete.Id)});
        sqLiteDatabase.close();
    }



}
