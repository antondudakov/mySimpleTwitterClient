package com.example.mysimpletwitterclient.client.db;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

/**
 * Created by bwd on 21.02.14.
 */
public class dbHelper extends OrmLiteSqliteOpenHelper {

  public dbHelper(Context context, String databaseName,
          int databaseVersion) {
    super(context, databaseName, null, databaseVersion);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
    try {
      TableUtils.createTable(connectionSource, Twit.class);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion,
          int newVersion) {
    try {
      TableUtils.dropTable(connectionSource, Twit.class, true);
      onCreate(sqLiteDatabase, connectionSource);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
