package com.droidteahouse.classified003.activity;

import com.google.gson.Gson;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.droidteahouse.classified003.Classified003App;
import com.droidteahouse.classified003.R;
import com.droidteahouse.classified003.util.Util;
import com.droidteahouse.oauthkit.OAuthBaseClient;
import com.droidteahouse.oauthkit.OAuthLoginActivity;

import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;

public class LoginActivity extends OAuthLoginActivity {
  OAuthBaseClient client;
  protected SharedPreferences prefs;
  protected SharedPreferences.Editor editor;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //@todo clear tokens for notifications open--don't want someone to access someone else's account this way
    setContentView(R.layout.activity_login);
    View v = findViewById(R.id.loginBtn);
    v.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        loginToRest(v);
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //prevent leaking activity
    //@todo clean up of async tasks for fetching tokens
    if (null != this.client && null != this.client.getAccessHandler()) {
      OAuthBaseClient.OAuthAccessHandler handler = this.client.getAccessHandler();
      handler = null;
    }
  }

  private Boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager
        .getActiveNetworkInfo();
    return activeNetworkInfo != null
        && activeNetworkInfo.isConnectedOrConnecting();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.login, menu);
    return true;
  }

  @Override
  public void onLoginSuccess(OkHttpOAuthConsumer consumer) {
    Classified003App.createJacksonService(consumer);
    Classified003App.createDefaultService(consumer);
    saveConsumer(consumer);
    Intent i = new Intent(this, PhotosActivity.class);
    startActivity(i);
  }

  private static void saveConsumer(OkHttpOAuthConsumer consumer) {
    SharedPreferences mPrefs = Util.getUserPrefs();
    SharedPreferences.Editor prefsEditor = mPrefs.edit();
    Gson gson = new Gson();
    String json = gson.toJson(consumer);
    prefsEditor.putString(Classified003App.getAppContext().getString(R.string.Consumer), json);
    prefsEditor.commit();
  }

  @Override
  public void onLoginFailure(Exception e) {
    Toast.makeText(
        this,
        "There is a problem with login to the app.  Please check your internet connection and try again.",
        Toast.LENGTH_LONG).show();
    Log.e("ERROR", e.toString());
    e.printStackTrace();
  }

  public void loginToRest(View view) {
    final ProgressDialog ringProgressDialog = new ProgressDialog(this, R.style.CustomProgessBarStyle);
    ringProgressDialog.setTitle("Please wait");
    ringProgressDialog.setMessage("Preparing to login");
    ringProgressDialog.setCancelable(true);
    ringProgressDialog.show();
    if (!isNetworkAvailable()) {
      Toast.makeText(this, " You have no network/internet connection",
          Toast.LENGTH_SHORT).show();
    } else {
      client = OAuthBaseClient.getInstance(Classified003App.getAppContext(), this);
      client.connect();
    }
    ringProgressDialog.dismiss();
  }
}

