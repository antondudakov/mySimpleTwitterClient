package com.example.mysimpletwitterclient.client;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.example.mysimpletwitterclient.client.db.Twit;
import com.example.mysimpletwitterclient.client.db.dbHelper;
import com.example.mysimpletwitterclient.client.db.dbHelperFactory;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by bwd on 20.02.14.
 */
public class MainFragment extends Fragment implements OnRefreshListener {

  public static final String TAG = "MainFragment";

  private PullToRefreshLayout mPullToRefreshLayout;
  private ListView mTwitListView;
  private TwitListAdapter mAdapter;

  private String mToken;
  private String mToken_secret;
  private Button mButton;

  private Menu mMenu;

  private boolean updateTwits = false;
  public MainFragment() {
  }

  public MainFragment(boolean updateTwits){
    this.updateTwits = updateTwits;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    mMenu = menu;
    mMenu.clear();
    inflater.inflate(R.menu.main, menu);
    SharedPreferences prefs = getActivity().
            getSharedPreferences(Const.TKN_PRF_NAME, Context.MODE_PRIVATE);
    mToken = prefs.getString(Const.TOKEN_PREF, null);
    mToken_secret = prefs.getString(Const.TOKEN_SECRET_PREF, null);
    if (mToken == null || mToken_secret == null) {
      mMenu.clear();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_logout) {
      SharedPreferences prefs = getActivity().
              getSharedPreferences(Const.TKN_PRF_NAME, Context.MODE_PRIVATE);
      prefs.edit().remove(Const.TOKEN_PREF)
                  .remove(Const.TOKEN_SECRET_PREF)
                  .commit();
      dbHelper dbHelper = dbHelperFactory.createHelper(getActivity());
      try {
        TableUtils.clearTable(dbHelper.getConnectionSource(), Twit.class);
      } catch (SQLException e) {
        e.printStackTrace();
      }
      if (mAdapter!=null){
        mAdapter.clear();
      }
      mButton.setVisibility(View.VISIBLE);
      mMenu.clear();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    ((ActionBarActivity)getActivity()).setSupportProgressBarIndeterminate(true);

    mButton = (Button)rootView.findViewById(R.id.login_button);
    mButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().getSupportFragmentManager().beginTransaction()
          .replace((getActivity().getWindow().findViewById(R.id.data_container) != null ?
          R.id.data_container : R.id.container), new LoginFragment(), LoginFragment.TAG)
          .commit();
      }
    });

    mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
    mTwitListView = (ListView)rootView.findViewById(R.id.twitlist);

    SharedPreferences prefs = getActivity().
            getSharedPreferences(Const.TKN_PRF_NAME, Context.MODE_PRIVATE);
    mToken = prefs.getString(Const.TOKEN_PREF, null);
    mToken_secret = prefs.getString(Const.TOKEN_SECRET_PREF, null);
    if (mToken != null && mToken_secret != null) {
      mButton.setVisibility(View.GONE);
    } else {
      return rootView;
    }

    ActionBarPullToRefresh.from(getActivity())
            .allChildrenArePullable()
            .listener(this)
            .setup(mPullToRefreshLayout);

    if (mAdapter == null){
      ((ActionBarActivity)getActivity()).setSupportProgressBarVisibility(true);
      new AsyncTask<Void, Void, List<Twit>>(){

        @Override
        protected List<Twit> doInBackground(Void... params) {
          return updateTwits ? getTwits(loadTwitsToDB(true)+1l, Long.MAX_VALUE) : getTwits();
        }

        @Override
        protected void onPostExecute(List<Twit> twits) {
          ((ActionBarActivity)getActivity()).setSupportProgressBarVisibility(false);
          setTwitArray(twits);
        }
      }.execute();
    } else {
      mTwitListView.setAdapter(mAdapter);
      mAdapter.notifyDataSetChanged();
    }

    mTwitListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Fragment fragment = new TwitFragment(mAdapter.getItem(position));
        if (getActivity().getWindow().findViewById(R.id.data_container) != null){
          getActivity().getSupportFragmentManager().beginTransaction()
                  .replace(R.id.data_container, fragment, TwitFragment.TAG)
                  .commit();
        } else {
          getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                  .replace(R.id.container, fragment, TwitFragment.TAG)
                  .commit();
        }
      }
    });

    return rootView;
  }

  @Override
  public void onRefreshStarted(View view) {
    final Boolean[] onlyOld = {null};

    if (view != null){
      onlyOld[0] = (Boolean)view.getTag();
      onlyOld[0] = onlyOld[0] == null ? false : true;
    }
    if (!mPullToRefreshLayout.isRefreshing()) {
      ((ActionBarActivity)getActivity()).setSupportProgressBarVisibility(true);
    }
    new AsyncTask<Void, Void, List<Twit>>(){

      @Override
      protected List<Twit> doInBackground(Void... params) {
        return onlyOld[0] ? getTwits(Long.MIN_VALUE, loadTwitsToDB(false))
                       : getTwits(loadTwitsToDB(true)+1l, Long.MAX_VALUE);
      }

      @Override
      protected void onPostExecute(List<Twit> twits) {
        onlyOld[0] = false;
        mPullToRefreshLayout.setRefreshComplete();
        ((ActionBarActivity)getActivity()).setSupportProgressBarVisibility(false);
        setTwitArray(twits);
      }
    }.execute();
  }

  private void setTwitArray(List<Twit> twits) {
    if (twits == null)
      return;
    if (mAdapter == null) {
      mAdapter = new TwitListAdapter(getActivity(), twits, this);
      mTwitListView.setAdapter(mAdapter);
    } else {
      for (Twit twit : twits) {
        mAdapter.add(twit);
      }
    }
  }

  private List<Twit> getTwits() {
    return getTwits(Long.MIN_VALUE, Long.MAX_VALUE);
  }

  private List<Twit> getTwits(long afterEqual, long beforeEqual) {
    List<Twit> twits = null;
    try {
      dbHelper dbHelper = dbHelperFactory.createHelper(getActivity());
      Dao dao = dbHelper.getDao(Twit.class);

      QueryBuilder<Twit,Long> qb = dao.queryBuilder();
      qb.orderBy("_id", false);
      Where where = qb.where();
      where.ge("_id", afterEqual).and().le("_id", beforeEqual);
      qb.limit(5l);

      twits = dao.query(qb.prepare());

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return twits;
  }

  private long loadTwitsToDB(boolean newTwits){
    Dao dao = null;
    dbHelper dbHelper = dbHelperFactory.createHelper(getActivity());
    try {
      dao = dbHelper.getDao(Twit.class);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (!newTwits){
      long lastShowdId = mAdapter.getItem(mAdapter.getCount()-1).getId();
      try {
        QueryBuilder<Twit,Long> qb = dao.queryBuilder();
        qb.orderBy("_id", !newTwits);
        Where where = qb.where();
        where.lt("_id", lastShowdId);
        List<Twit> count = dao.query(qb.prepare());
        if (count.size() > 0){
          return count.get(count.size()-1).getId();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    long front_id = Long.MIN_VALUE;
    try {
      QueryBuilder<Twit,Long> qb = dao.queryBuilder();
      qb.orderBy("_id", !newTwits)
              .limit(1L);
      Twit twit = (Twit) dao.queryForFirst(qb.prepare());
      if (twit != null){
        front_id = twit.getId();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    OAuthHmacSigner signer = new OAuthHmacSigner();
    signer.clientSharedSecret = Const.API_SECRET;
    signer.tokenSharedSecret=mToken_secret;

    OAuthParameters params = new OAuthParameters();
    params.consumerKey = Const.API_KEY;
    params.token=mToken;
    params.signer=signer;

    HttpTransport transport = new ApacheHttpTransport();

    HttpRequestFactory factory = transport.createRequestFactory(params);
    GenericUrl url = new GenericUrl(Const.T_HOME_URL);
    url.put("count", 30 );
    if (newTwits && front_id != Long.MIN_VALUE)
      url.put("since_id", front_id);
    else if (!newTwits){
      url.put("max_id", front_id-1);
    }

    HttpRequest req;
    HttpResponse resp;
    String result;
    try {
      req = factory.buildGetRequest(url);
      Log.d(Const.TAG, "before connect");
      resp = req.execute();
      result = resp.parseAsString();
    } catch (IOException e) {
      e.printStackTrace();
      return front_id;
    }

    Gson gson = new Gson();
    JsonArray jsonArray = gson.fromJson(result, JsonArray.class);

    for (JsonElement jsonElement : jsonArray){
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      JsonObject user = jsonObject.getAsJsonObject("user");
      JsonArray mediaArray = jsonObject.getAsJsonObject("entities").getAsJsonArray("media");

      String media_url = null;
      if (mediaArray != null) {
        for(JsonElement mediaElement : mediaArray){

          String type = mediaElement.getAsJsonObject().get("type").getAsString();
          if (type.equalsIgnoreCase("photo")){
            media_url = mediaElement.getAsJsonObject().get("media_url_https").getAsString();
          }
        }
      }


      JsonElement place = jsonObject.get("place");
      String placeName = null;
      if (!place.isJsonNull())
        placeName = place.getAsJsonObject().getAsJsonPrimitive("full_name").getAsString();


      Twit twit = new Twit.Builder()
              .id(jsonObject.getAsJsonPrimitive("id").getAsLong())
              .avatar_url(user.getAsJsonPrimitive("profile_image_url_https").getAsString())
              .name(user.getAsJsonPrimitive("name").getAsString())
              .username(user.getAsJsonPrimitive("screen_name").getAsString())
              .time(jsonObject.getAsJsonPrimitive("created_at").getAsString())
              .liked(jsonObject.getAsJsonPrimitive("favorited").getAsBoolean())
              .text(jsonObject.getAsJsonPrimitive("text").getAsString())
              .image_url(media_url)
              .place(placeName)
              .build();
      try {
        dao.createOrUpdate(twit);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return front_id;
  }
}
