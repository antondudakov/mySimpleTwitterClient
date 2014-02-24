package com.example.mysimpletwitterclient.client;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.http.apache.ApacheHttpTransport;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.net.URI;

public class LoginFragment extends Fragment {

  public final static String TAG = "LoginFragment";

  private WebView mWebView;
  private OAuthHmacSigner signer;
  private MyWebViewClient mMyWebViewClient;
  private Bundle mWebViewState;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    View rootView = inflater.inflate(R.layout.activity_login, container, false);
    ((ActionBarActivity)getActivity()).setSupportProgressBarIndeterminate(true);
    mWebView = (WebView)rootView.findViewById(R.id.webView);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.setVisibility(View.VISIBLE);
    if (mMyWebViewClient == null) {
      mMyWebViewClient = new MyWebViewClient();
    }
    mWebView.setWebViewClient(mMyWebViewClient);

    if (mWebViewState != null){
      mWebView.restoreState(mWebViewState);
      return rootView;
    }

    if ( authorizationLoad.getStatus() != AsyncTask.Status.FINISHED &&
            authorizationLoad.getStatus() != AsyncTask.Status.RUNNING ){
      ((ActionBarActivity)getActivity()).setSupportProgressBarVisibility(true);
      authorizationLoad.execute();
    }


    return rootView;
  }

  @Override
  public void onDestroyView() {
    mWebViewState = new Bundle();
    mWebView.saveState(mWebViewState);
    super.onDestroyView();
  }

  class MyWebViewClient extends WebViewClient {

    @Override
    public void onPageFinished(WebView view, String url) {
      ((ActionBarActivity)getActivity()).setSupportProgressBarVisibility(false);
      super.onPageFinished(view, url);
      Uri uri = Uri.parse(url);
      if (!uri.getScheme().equalsIgnoreCase(Const.OAUTH_CALLBACK_SCHEME)) {
        ((ActionBarActivity)getActivity()).getSupportActionBar().hide();
      }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      ((ActionBarActivity)getActivity()).setSupportProgressBarVisibility(true);
      Uri uri = Uri.parse(url);
      if (uri.getScheme().equalsIgnoreCase(Const.OAUTH_CALLBACK_SCHEME)) {
        view.setVisibility(View.INVISIBLE);
        ((ActionBarActivity)getActivity()).getSupportActionBar().show();
        String requestToken  = uri.getQueryParameter("oauth_token");
        String verifier= uri.getQueryParameter("oauth_verifier");

        signer.clientSharedSecret = Const.API_SECRET;

        final OAuthGetAccessToken accessToken = new OAuthGetAccessToken(Const.ACCESS_TOKEN_URL);
        accessToken.transport = new ApacheHttpTransport();
        accessToken.temporaryToken = requestToken;
        accessToken.signer = signer;
        accessToken.consumerKey = Const.API_KEY;
        accessToken.verifier = verifier;

        new AsyncTask<Void,Void,Void>() {

          @Override
          protected Void doInBackground(Void... params) {

            OAuthCredentialsResponse credentials = null;
            try {
              credentials = accessToken.execute();
              SharedPreferences prefs = getActivity()
                      .getSharedPreferences(Const.TKN_PRF_NAME, Context.MODE_PRIVATE);
              SharedPreferences.Editor editor = prefs.edit();
              editor.putString(Const.TOKEN_PREF, credentials.token);
              editor.putString(Const.TOKEN_SECRET_PREF, credentials.tokenSecret);
              editor.commit();
            } catch (IOException e) {
              e.printStackTrace();
            }
            return null;
          }

          @Override
          protected void onPostExecute(Void aVoid) {
            FragmentActivity activity = getActivity();
            if (activity == null) return;
            activity.getSupportFragmentManager().beginTransaction()
              .replace((getActivity().getWindow().findViewById(R.id.data_container) != null ?
                      R.id.rows_container : R.id.container), new MainFragment(true), MainFragment.TAG)
              .commit();
          }
        }.execute();

      }
      super.onPageStarted(view, url, favicon);
    }
  }

  AsyncTask<Void,Void,String> authorizationLoad = new AsyncTask<Void, Void, String>() {
    @Override
    protected String doInBackground(Void... params) {
      signer = new OAuthHmacSigner();
      signer.clientSharedSecret = Const.API_SECRET;

      OAuthGetTemporaryToken temporaryToken = new OAuthGetTemporaryToken(Const.REQUEST_TOKEN_URL);
      temporaryToken.transport = new ApacheHttpTransport();
      temporaryToken.signer = signer;
      temporaryToken.consumerKey = Const.API_KEY;
      temporaryToken.callback = Const.OAUTH_CALLBACK_URL;

      OAuthCredentialsResponse tempCredentials = null;
      try {
        tempCredentials = temporaryToken.execute();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }

      signer.tokenSharedSecret = tempCredentials.tokenSecret;

      OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(Const.AUTHORIZE_URL);
      authorizeUrl.temporaryToken = tempCredentials.token;
      String authorizationUrl = authorizeUrl.build();

      return authorizationUrl;
    }

    @Override
    protected void onPostExecute(String s) {
      mWebView.loadUrl(s);
      super.onPostExecute(s);
    }
  };
}
