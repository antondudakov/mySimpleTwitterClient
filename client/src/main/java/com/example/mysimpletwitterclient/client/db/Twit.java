package com.example.mysimpletwitterclient.client.db;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.example.mysimpletwitterclient.client.Const;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by bwd on 21.02.14.
 */
@DatabaseTable(tableName="twit")
public class Twit {

  public void setAvatar_cache(String avatar_cache) {
    this.avatar_cache = avatar_cache;
  }

  public void setImage_cache(String image_cache) {
    this.image_cache = image_cache;
  }

  public Twit() {
  }

  @DatabaseField(id=true, uniqueIndex=true)
  private long _id;

  @DatabaseField(dataType = DataType.STRING)
  private String avatar_url;

  @DatabaseField(dataType = DataType.STRING)
  private String avatar_cache;

  @DatabaseField(dataType = DataType.STRING)
  private String name;

  @DatabaseField(dataType = DataType.STRING)
  private String username;

  @DatabaseField(dataType = DataType.STRING)
  private String time;

  @DatabaseField(dataType = DataType.BOOLEAN)
  private boolean liked;

  @DatabaseField(dataType = DataType.BOOLEAN)
  private boolean like_confirmed;

  @DatabaseField(dataType = DataType.STRING)
  private String text;

  @DatabaseField(dataType = DataType.STRING)
  private String image_url;

  @DatabaseField(dataType = DataType.STRING)
  private String image_cache;

  @DatabaseField(dataType = DataType.STRING)
  private String place;

  public long getId() {
    return _id;
  }

  public String getAvatar_url() {
    return avatar_url;
  }

  public String getAvatar_cache(Context context) {

    if ( avatar_cache == null) {
      String directory = context.getCacheDir().getAbsolutePath() + File.separator + "profile";
      String name = avatar_url.substring( avatar_url.lastIndexOf('/')+1, avatar_url.length() );

      File file = new File(directory, name);
      if (file.exists()) avatar_cache = file.getAbsolutePath();
    }
    return avatar_cache;
  }

  public String getName() {
    return name;
  }

  public String getUsername() {
    return username;
  }

  public String getTime() {
    return time;
  }

  public void setLiked(boolean liked) {
    this.liked = liked;
  }

  public boolean isLiked(Context context) {
    if (liked != like_confirmed)
      confirmLike(context);
    return liked;
  }

  public boolean switchLike(){
    liked = !liked;
    return liked;
  }

  public boolean confirmLike(final Context context){

    new AsyncTask<Void,Void,Void>(){
      @Override
      protected Void doInBackground(Void... params) {
        Twit twit = Twit.this;
        SharedPreferences prefs = context.
                getSharedPreferences(Const.TKN_PRF_NAME, Context.MODE_PRIVATE);
        String Token = prefs.getString(Const.TOKEN_PREF, null);
        String Token_secret = prefs.getString(Const.TOKEN_SECRET_PREF, null);
        OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = Const.API_SECRET;
        signer.tokenSharedSecret=Token_secret;

        OAuthParameters oAuthParameters = new OAuthParameters();
        oAuthParameters.consumerKey = Const.API_KEY;
        oAuthParameters.token=Token;
        oAuthParameters.signer=signer;

        HttpTransport transport = new ApacheHttpTransport();

        HttpRequestFactory factory = transport.createRequestFactory(oAuthParameters);
        GenericUrl url = new GenericUrl(twit.liked ? Const.T_LIKE_URL : Const.T_UNLIKE_URL);
        url.put("id", twit._id);
        url.put("include_entities", true);
        HttpRequest req;
        HttpResponse resp = null;
        try {
          req = factory.buildPostRequest(url, null);
          req.setThrowExceptionOnExecuteError(false);
          resp = req.execute();
          Log.d(Const.TAG, "Like answer code:" + resp.getStatusCode() + " id:" + twit._id +
                  " Response: " + resp.parseAsString() );
          if (resp.getStatusCode() == 200 ||
                  resp.getStatusCode() == 139 ||
                  resp.getStatusCode() == 403) {
            twit.like_confirmed = twit.liked;

          }
        } catch (IOException e) {
          Log.e(Const.TAG, e.getMessage());
          e.printStackTrace();
        } finally {
          dbHelper dbHelper = dbHelperFactory.createHelper(context);
          try {
            Dao dao = dbHelper.getDao(Twit.class);
            if ( resp != null && resp.getStatusCode() == 404){
              twit.liked = twit.like_confirmed;
              Toast.makeText(context, "Can\'t change like status ", Toast.LENGTH_SHORT).show();
            }
            dao.update(twit);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
        return null;
      }
    }.execute();
    return true;
  }

  public String getText() {
    return text;
  }

  public String getImage_url() {
    return image_url;
  }

  public String getImage_cache() {
    return image_cache;
  }

  public String getPlace() {
    return place;
  }

  public static class Builder {
    private long id;
    private String avatar_url;
    private String avatar_cache;
    private String name;
    private String username;
    private String time;
    private boolean liked;
    private String text;
    private String image_url;
    private String image_cache;
    private String place;

    public Builder id(long id){
      this.id = id;
      return this;
    }
    public Builder avatar_url(String avatar_url){
      this.avatar_url = avatar_url;
      return this;
    }
    public Builder avatar_cache(String avatar_cache){
      this.avatar_cache = avatar_cache;
      return this;
    }
    public Builder name(String name){
      this.name = name;
      return this;
    }
    public Builder username(String username){
      this.username = username;
      return this;
    }
    public Builder time(String time){
      this.time = time;
      return this;
    }
    public Builder liked(boolean liked){
      this.liked = liked;
      return this;
    }
    public Builder text(String text){
      this.text = text;
      return this;
    }
    public Builder image_url(String image_url){
      this.image_url = image_url;
      return this;
    }
    public Builder image_cache(String image_cache){
      this.image_cache = image_cache;
      return this;
    }
    public Builder place(String place){
      this.place = place;
      return this;
    }

    public Twit build(){
      return new Twit(this);
    }

  }

  private Twit(Builder builder) {
    _id = builder.id;
    avatar_url = builder.avatar_url;
    avatar_cache = builder.avatar_cache;
    name = builder.name;
    username = builder.username;
    time = builder.time;
    liked = builder.liked;
    like_confirmed = builder.liked;
    text = builder.text;
    image_url = builder.image_url;
    image_cache = builder.image_cache;
    place = builder.place;
  }

}
