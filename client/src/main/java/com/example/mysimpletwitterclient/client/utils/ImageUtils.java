package com.example.mysimpletwitterclient.client.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bwd on 24.02.14.
 */
public class ImageUtils {

  public static String getAndSaveImage(String link, String dir){
    Bitmap bmp = getBitmapFromURL(link);
    String name = link.substring( link.lastIndexOf('/')+1, link.length() );

    return saveImageToSD(bmp, dir, name);
  }

  public static void loadImageFromFile(String filepath, ImageView view) {
    loadImageFromFile(filepath, view, false);
  }

  public static void loadImageFromFile(String filepath, ImageView view, boolean putTransparentFirst) {
    if (putTransparentFirst) view.setImageResource(android.R.color.transparent);
    if (filepath == null) return;

    File imgFile = new File(filepath);
    if(imgFile.exists()){
      Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
      view.setImageBitmap(myBitmap);

    }
  }

  private static String saveImageToSD(Bitmap bmp, String dir, String name) {

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    File directory = new File(dir);
    try {
      directory.mkdirs();
      File file = new File(directory, name);
      file.createNewFile();
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(bytes.toByteArray());
      fos.close();
      return file.getAbsolutePath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }


  private static Bitmap getBitmapFromURL(String link) {
    try {
      URL url = new URL(link);
      HttpURLConnection connection = (HttpURLConnection) url
              .openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      Bitmap myBitmap = BitmapFactory.decodeStream(input);

      return myBitmap;

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
