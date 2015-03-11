package com.anubis.flickr.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.anubis.flickr.FlickrClient;
import com.anubis.flickr.R;
import com.codepath.oauth.OAuthLoginActivity;

public class LoginActivity extends OAuthLoginActivity<FlickrClient> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
    public void onLoginSuccess() {
        Intent i = new Intent(this, PhotosActivity.class);
        startActivity(i);
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
            getClient().connect();
        }
        ringProgressDialog.dismiss();
    }


}
