package com.example.mysimpletwitterclient.client;

import com.example.mysimpletwitterclient.client.db.Twit;
import com.example.mysimpletwitterclient.client.utils.ImageUtils;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by bwd on 22.02.14.
 */
public class TwitListAdapter extends ArrayAdapter<Twit> {

  Context mContext;
  List<Twit> mTwits;
  OnRefreshListener mOnRefreshListener;

  public TwitListAdapter(Context context,
          List<Twit> objects, OnRefreshListener listener) {
    super(context, 0, objects);
    this.mContext = context;
    this.mTwits = objects;
    this.mOnRefreshListener = listener;
  }

  static class ViewHolder{
    public ImageView avatar;
    public TextView name;
    public TextView username;
    public TextView date;
    public ImageView like;
    public TextView text;
  }

  @Override
  public void add(Twit object) {
    super.add(object);
    sort(new Comparator<Twit>() {
      @Override
      public int compare(Twit lhs, Twit rhs) {
        if (lhs.getId() > rhs.getId()) return -1;
        else if (lhs.getId() == rhs.getId()) return 0;
        else return 1;
      }
    });
  }

  @Override
  public View getView(int position, View rowView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (rowView == null){
      LayoutInflater inflater = LayoutInflater.from(mContext);
      rowView = inflater.inflate(R.layout.twit_item,parent,false);
      viewHolder = new ViewHolder();

      viewHolder.avatar = (ImageView) rowView.findViewById(R.id.avatar);
      viewHolder.name = (TextView) rowView.findViewById(R.id.name);
      viewHolder.username = (TextView) rowView.findViewById(R.id.username);
      viewHolder.date = (TextView) rowView.findViewById(R.id.datetime);
      viewHolder.like = (ImageView) rowView.findViewById(R.id.like_button);
      viewHolder.text = (TextView) rowView.findViewById(R.id.text);

      rowView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder)rowView.getTag();
    }

    Twit twit = mTwits.get(position);

    viewHolder.name.setText(twit.getName());
    viewHolder.username.setText("@" + twit.getUsername());
    final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
    SimpleDateFormat format = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
    SimpleDateFormat newFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    newFormat.setTimeZone(TimeZone.getDefault());
    try {
      Date date = format.parse(twit.getTime());
      viewHolder.date.setText(newFormat.format(date));
    } catch (ParseException e) {
      viewHolder.date.setText(twit.getTime());
      e.printStackTrace();
    }
    viewHolder.text.setText(twit.getText());
    viewHolder.like.setImageResource(twit.isLiked(mContext) ?
            android.R.drawable.btn_star_big_on :
            android.R.drawable.btn_star_big_off);

    if (position == mTwits.size()-1){
      if (mOnRefreshListener != null) {
        View view = new View(mContext);
        Boolean oldTweets = true;
        view.setTag(oldTweets);
        mOnRefreshListener.onRefreshStarted(view);
      }
    }

    ImageUtils.loadImageFromFile(twit.getAvatar_cache(mContext),
            viewHolder.avatar, true);

    return rowView;
  }

}
