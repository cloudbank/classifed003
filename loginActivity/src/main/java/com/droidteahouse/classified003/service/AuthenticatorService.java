package com.droidteahouse.classified003.service;
/**
 * Created by sabine on 10/6/16.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.droidteahouse.classified003.sync.Authenticator;

public class AuthenticatorService extends Service {
  private Authenticator mAuthenticator;

  @Override
  public void onCreate() {
    mAuthenticator = new Authenticator(this);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mAuthenticator.getIBinder();
  }
}
