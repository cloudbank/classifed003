package com.droidteahouse.classified003;

import static com.droidteahouse.classified003.service.ServiceGenerator.createRetrofitRxService;
import static com.droidteahouse.oauthkit.BuildConfig.baseUrl;

import com.google.gson.Gson;

import android.support.multidex.MultiDexApplication;

import com.droidteahouse.classified003.service.FlickrService;
import com.droidteahouse.classified003.service.ServiceGenerator;
import com.droidteahouse.classified003.util.Util;
import com.facebook.stetho.Stetho;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import org.tensorflow.tensorlib.TensorLib;

import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class Classified003App extends MultiDexApplication {
  private static Classified003App instance;

  public static Classified003App getAppContext() {
    return instance;
  }

  private static com.droidteahouse.classified003.service.FlickrService jacksonService;
  private static com.droidteahouse.classified003.service.FlickrService defaultService;
  //prevent leaking activity context http://bit.ly/6LRzfx

  public static FlickrService getJacksonService() {
    return ((jacksonService == null) ? createJacksonService(getConsumer()) : jacksonService);
  }

  public static FlickrService getDefaultService() {
    return ((defaultService == null) ? createDefaultService(getConsumer()) : defaultService);
  }

  private static OkHttpOAuthConsumer getConsumer() {
    Gson gson = new Gson();
    String json = Util.getUserPrefs().getString(Classified003App.getAppContext().getString(R.string.Consumer), "");
    return gson.fromJson(json, OkHttpOAuthConsumer.class);
  }

  //@todo change docs for oauthkit with release
  public static FlickrService createJacksonService(OkHttpOAuthConsumer consumer) {
    jacksonService = ServiceGenerator.createRetrofitRxService(consumer, com.droidteahouse.classified003.service.FlickrService.class, baseUrl, JacksonConverterFactory.create());
    return jacksonService;
  }

  public static FlickrService createDefaultService(OkHttpOAuthConsumer consumer) {
    defaultService = createRetrofitRxService(consumer, com.droidteahouse.classified003.service.FlickrService.class, baseUrl, SimpleXmlConverterFactory.create());
    return defaultService;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    TensorLib.init(this);
    Stetho.initializeWithDefaults(this);
    Realm.init(this);
    RealmConfiguration config = new RealmConfiguration.Builder().build();
    Realm.setDefaultConfiguration(config);
    Picasso.Builder builder = new Picasso.Builder(this);
    //wharton lib requires picasso 2.5.2 right now
    builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
    Picasso built = builder.build();
    //built.setIndicatorsEnabled(true);
    built.setLoggingEnabled(false);
    Picasso.setSingletonInstance(built);
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
  }
}
