package com.example.mysimpletwitterclient.client;

import com.example.mysimpletwitterclient.client.db.Twit;
import com.example.mysimpletwitterclient.client.db.dbHelper;
import com.example.mysimpletwitterclient.client.db.dbHelperFactory;
import com.example.mysimpletwitterclient.client.utils.ImageUtils;
import com.j256.ormlite.dao.Dao;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class TwitFragment extends Fragment {

  public final static String TAG = "TwitFragment";

  private Twit mTwit;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

  }

  public TwitFragment() {
    // Required empty public constructor
  }

  public TwitFragment(Twit twit) {
    mTwit = twit;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_twit, container, false);
    if (mTwit == null) return rootView;

    TextView name = (TextView) rootView.findViewById(R.id.name);
    TextView username= (TextView) rootView.findViewById(R.id.username);
    TextView text = (TextView) rootView.findViewById(R.id.text);
    TextView dateView = (TextView) rootView.findViewById(R.id.datetime);

    ImageView loadAvatarButton = (ImageView) rootView.findViewById(R.id.avatar);
    ImageView loadImageButton = (ImageView) rootView.findViewById(R.id.image);

    ImageUtils.loadImageFromFile(mTwit.getAvatar_cache(getActivity()), loadAvatarButton);
    loadAvatarButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        new LoadImageTask((ImageView)v, mTwit, LoadImageTask.TYPE_PROFILE).execute();
        return false;
      }
    });

    ImageUtils.loadImageFromFile(mTwit.getImage_cache(), loadImageButton);
    loadImageButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        new LoadImageTask((ImageView) v, mTwit, LoadImageTask.TYPE_IMAGE).execute();
        return false;
      }
    });


    final ImageView like = (ImageView) rootView.findViewById(R.id.like_button);

    like.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        mTwit.switchLike();
        dbHelper dbHelper = dbHelperFactory.createHelper(getActivity());
        try {
          Dao dao = dbHelper.getDao(Twit.class);
          dao.update(mTwit);
          setLikeImage(like);
        } catch (SQLException e) {
          e.printStackTrace();
        }

        return false;
      }
    });

    name.setText(mTwit.getName());
    username.setText("@" + mTwit.getUsername());
    final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
    SimpleDateFormat format = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
    SimpleDateFormat newFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    newFormat.setTimeZone(TimeZone.getDefault());
    try {
      Date date = format.parse(mTwit.getTime());
      dateView.setText(newFormat.format(date));
    } catch (ParseException e) {
      dateView.setText(mTwit.getTime());
      e.printStackTrace();
    }
    text.setText(mTwit.getText());

    setLikeImage(like);

    if (mTwit.getImage_url() == null) {
      loadImageButton.setVisibility(View.GONE);
    }

    return rootView;
  }

  private void setLikeImage(ImageView like) {
    like.setImageResource(mTwit.isLiked(getActivity()) ?
            android.R.drawable.btn_star_big_on :
            android.R.drawable.btn_star_big_off);
  }


  private class LoadImageTask extends AsyncTask<Void, Void, Void> {

    public static final int TYPE_PROFILE = 0;
    public static final int TYPE_IMAGE = 1;

    ImageView mView;
    Twit mTaskTwit;
    int mType;

    private LoadImageTask(ImageView view, Twit taskTwit, int type) {
      super();
      mView = view;
      mTaskTwit = taskTwit;
      mType = type;
    }

    @Override
    protected Void doInBackground(Void... params) {
      if (mType == TYPE_PROFILE) {
        mTaskTwit.setAvatar_cache(ImageUtils.getAndSaveImage(mTaskTwit.getAvatar_url(),
                getActivity().getCacheDir().getAbsolutePath() + File.separator + "profile"));
      } else {
        mTaskTwit.setImage_cache(ImageUtils.getAndSaveImage(mTaskTwit.getImage_url(),
                getActivity().getCacheDir().getAbsolutePath() + File.separator + "media"));
      }
      dbHelper dbHelper = dbHelperFactory.createHelper(getActivity());
      try {
        Dao dao = dbHelper.getDao(Twit.class);
        dao.update(mTaskTwit);
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if (mType == TYPE_PROFILE) {
        ImageUtils.loadImageFromFile(mTaskTwit.getAvatar_cache(getActivity()), mView);
      } else {
        ImageUtils.loadImageFromFile(mTaskTwit.getImage_cache(), mView);
      }
      super.onPostExecute(aVoid);
    }
  }

}
