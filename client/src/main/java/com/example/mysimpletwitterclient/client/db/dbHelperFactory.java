package com.example.mysimpletwitterclient.client.db;

import android.content.Context;

/**
 * Created by bwd on 21.02.14.
 */
public final class dbHelperFactory {

  private static volatile dbHelper instance;

  public static synchronized final dbHelper createHelper(Context context) {
    if (instance == null) {
      instance = new dbHelper(context, "twi.db", 1);
    }
    return instance;
  }
}
