package org.bahmni.offline.dbServices.dao;

import android.app.Activity;
import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.bahmni.offline.Constants;
import org.bahmni.offline.services.EncryptionService;

import java.io.*;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    private Context myContext;

    private String encryptionKey;

    public DbHelper(Context context, String dbPath) {
        super(context, dbPath, null, DATABASE_VERSION);
        this.myContext = context;
        this.encryptionKey = new EncryptionService(context).generateKey();
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int currentVersion = oldVersion;
        String fileName;
        while(oldVersion <= newVersion) {
                fileName = "migrations_"+String.valueOf(currentVersion)+".sql";
                runMigration(db, fileName);
                currentVersion++;
        }
    }

    public void runMigration(SQLiteDatabase db, String filename) {
        db.beginTransaction();
        try {
            InputStream inputStream = myContext.getAssets().open(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String sqlStatements = "";
            while (bufferedReader.ready()) {
                sqlStatements += bufferedReader.readLine();
            }
            bufferedReader.close();
            db.rawExecSQL(sqlStatements);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createTable(String sqlToCreateTable) {
        executeSql(sqlToCreateTable);
    }

    private void executeSql(String sqlToCreateTable) {
        getWritableDatabase().execSQL(sqlToCreateTable);
    }

    public SQLiteDatabase getWritableDatabase(){
        return super.getWritableDatabase(encryptionKey);
    }

    public SQLiteDatabase getReadableDatabase(){
        return super.getReadableDatabase(encryptionKey);
    }

    public void createIndex(String sqlToCreateIndex) {
        executeSql(sqlToCreateIndex);
    }
}
