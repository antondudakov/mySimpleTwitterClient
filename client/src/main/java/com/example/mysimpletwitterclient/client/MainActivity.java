package com.example.mysimpletwitterclient.client;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class MainActivity extends ActionBarActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    supportRequestWindowFeature(Window.FEATURE_PROGRESS);
    setContentView(R.layout.activity_main);

    FragmentManager fm = getSupportFragmentManager();
    Fragment fragment;

    if (findViewById(R.id.data_container) != null){
      //two fragments
      fragment = fm.findFragmentByTag(TwitFragment.TAG);
      if (fragment == null) fragment = fm.findFragmentByTag(LoginFragment.TAG);

      if (fragment != null) {
        String tag = fragment.getTag();
        fm.beginTransaction()
                .remove(fragment)
                .commit();
        fm.executePendingTransactions();
        fm.popBackStackImmediate();
        fm.beginTransaction()
                .replace(R.id.data_container, fragment, tag)
                .commit();
        fm.executePendingTransactions();
      }
      fragment = fm.findFragmentByTag(MainFragment.TAG);
      if (fragment == null) {
        fragment = new MainFragment();
      }

      FragmentTransaction ft = fm.beginTransaction();
      String tag = fragment.getTag();
      if (tag == null) {
        tag = MainFragment.TAG;
      } else {
        ft.remove(fragment);
        ft.commit();
        fm.executePendingTransactions();
        ft = fm.beginTransaction();
      }
      ft.replace(R.id.rows_container, fragment, tag)
        .commit();

    } else {
      //only one fragment needed

      fragment = fm.findFragmentByTag(TwitFragment.TAG);
      if (fragment == null){
        fragment = fm.findFragmentByTag(LoginFragment.TAG);
      }
      String tag = null;
      if (fragment != null) tag = fragment.getTag();

      Fragment mainFragment = fm.findFragmentByTag(MainFragment.TAG);
      if (mainFragment != null) {
        fm.beginTransaction().remove(mainFragment).commit();
        fm.executePendingTransactions();
        if (fm.getBackStackEntryCount() > 0 ){
          fm.popBackStackImmediate();
        }
      } else if (!LoginFragment.TAG.equals(tag)) {
        mainFragment = new MainFragment();
      }
      if (!LoginFragment.TAG.equals(tag)){
        fm.beginTransaction().replace(R.id.container, mainFragment, MainFragment.TAG).commit();
        fm.executePendingTransactions();
      }

      if (fragment != null ){
        fm.beginTransaction()
                .remove(fragment)
                .commit();
        fm.executePendingTransactions();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, tag);
        if (!LoginFragment.TAG.equals(tag)) {
          Log.d(Const.TAG, "adding Back Stack");
          ft.addToBackStack(null);
        }
        ft.commit();
      }
    }

  }

}
