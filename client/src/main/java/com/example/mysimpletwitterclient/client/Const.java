package com.example.mysimpletwitterclient.client;

/**
 * Created by bwd on 20.02.14.
 */
public class Const {
  public final static String TAG = "mytwit";

  public static final String API_KEY = "nj8WwVPKTOa2Uez1qxmHw";
  public static final String API_SECRET= "UV1VmyJXydiDEVofSm46wV1RNKgYOcYrmA9DGzaVa4s";
  public static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
  public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
  public static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";

  public static final String OAUTH_CALLBACK_SCHEME = "mysimpletwi";
  public static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://tweeter";

  public static final String TKN_PRF_NAME = "tknprf";
  public static final String TOKEN_PREF = "token_pref";
  public static final String TOKEN_SECRET_PREF = "token_secret_pref";



  //twitter urls
  public final static String T_HOME_URL = "https://api.twitter.com/1.1/statuses/home_timeline.json";
  public final static String T_LIKE_URL =   "https://api.twitter.com/1.1/favorites/create.json";
  public final static String T_UNLIKE_URL = "https://api.twitter.com/1.1/favorites/destroy.json";



}
