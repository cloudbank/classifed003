package com.droidteahouse.classified003.fragments;

import static com.droidteahouse.classified003.Classified003App.getJacksonService;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.droidteahouse.classified003.Classified003App;
import com.droidteahouse.classified003.R;
import com.droidteahouse.classified003.activity.ImageDisplayActivity;
import com.droidteahouse.classified003.adapter.FriendsAdapter;
import com.droidteahouse.classified003.models.Photo;
import com.droidteahouse.classified003.models.Photos;
import com.droidteahouse.classified003.models.Tag;
import com.droidteahouse.classified003.models.UserInfo;
import com.droidteahouse.classified003.models.UserModel;
import com.droidteahouse.classified003.models.Who;
import com.droidteahouse.classified003.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import co.hkm.soltag.TagContainerLayout;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class FriendsFragment extends FlickrBaseFragment {
  AdView mPublisherAdView;
  FriendsAdapter fAdapter;
  RecyclerView rvPhotos;
  List<Photo> mPhotos = new ArrayList<Photo>();
  List<Photo> cPhotos = new ArrayList<Photo>();
  List<Tag> mTags = new ArrayList<Tag>();
  TagContainerLayout mTagView;
  Realm userRealm, r;
  UserModel mUser;
  RealmChangeListener changeListener;
  RadioGroup rg;
  RadioButton rb1, rb5;
  View view;
  Subscription friendSubscription;

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    /*changeListener = new RealmChangeListener<UserModel>() {
      @Override
      public void onChange(UserModel u) {
        updateDisplay(u);
               /* if (rb1.isChecked()) {
                    makeSingle(cPhotos);
                    fAdapter.notifyDataSetChanged();
                }*/
     /* }
    };
    b
    userRealm = Realm.getDefaultInstance();
    final String user_id = Util.getUserId();
    mUser = userRealm.where(UserModel.class).equalTo("userId", user_id).findFirst();
    if (mUser == null) {
      showProgress("Loading data, please wait...");
      userRealm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          realm.createObject(UserModel.class, user_id);
          mUser = userRealm.where(UserModel.class).equalTo("userId", user_id).findFirst();
          mUser.addChangeListener(changeListener);
        }
      });
      getFriends();
    } else {
      mUser.addChangeListener(changeListener);
      updateDisplay(mUser);
    }*/
  }

  // Store instance variables based on arguments passed
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fAdapter = new FriendsAdapter(getActivity(), mPhotos, false);
    setRetainInstance(true);
  }

  private void updateDisplay(UserModel u) {
    mPhotos.clear();
    cPhotos.clear();
    if (null != u) {
      displayTags(u.getTagsList());
      cPhotos.addAll(u.getFriendsList());
      mPhotos.addAll(u.getFriendsList());
    }
    fAdapter.notifyDataSetChanged();
  }

  public void displayTags(List<Tag> tags) {
    //tags.stream().map(it -> it.getContent()).collect(Collectors.toCollection())
    //when android catches up to 1.8
    if (null != mTagView) {
      mTagView.removeAllTags();
      for (Tag t : tags) {
        mTagView.addTag(t.getContent());
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mPublisherAdView != null) {
      mPublisherAdView.destroy();
    }
    if (null != userRealm && !userRealm.isClosed()) {
      userRealm.close();
    }
    if (null != r && !r.isClosed()) {
      r.close();
    }
  }

  @Override
  public void onPause() {
    if (mPublisherAdView != null) {
      mPublisherAdView.pause();
    }
    super.onPause();
  }

  /**
   * Called when returning to the activity
   */
  @Override
  public void onResume() {
    super.onResume();
    if (mPublisherAdView != null) {
      mPublisherAdView.resume();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_friends, container,
        false);
    rvPhotos = (RecyclerView) view.findViewById(R.id.rvPhotos);
    rvPhotos.setAdapter(fAdapter);
    rvPhotos.setLayoutManager(new GridLayoutManager(Classified003App.getAppContext(), 3));
    fAdapter.setOnItemClickListener((view1, position) -> {
      Intent intent = new Intent(getActivity(),
          ImageDisplayActivity.class);
      Photo photo = mPhotos.get(position);
      intent.putExtra(RESULT, photo.getId());
      startActivity(intent);
    });
       /*mTagView = (TagContainerLayout) view.findViewById(R.id.my_tag_group);
        rg = (RadioGroup) view.findViewById(R.id.radioGroup1);
        rb1 = (RadioButton) view.findViewById(R.id.radio1);
        rb5 = (RadioButton) view.findViewById(R.id.radio5);
        */
    mPublisherAdView = (AdView) view.findViewById(R.id.publisherAdView);
    AdRequest adRequest = new AdRequest.Builder()
        //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
        //.addTestDevice("9D3A392231B42400A9CCA1CBED2D006F")  // My Galaxy Nexus test phone
        .build();
    mPublisherAdView.loadAd(adRequest);
    setHasOptionsMenu(true);
    return view;
  }

  private void makeSingle(List<Photo> p) {
    mPhotos.clear();
    String current = "";
    for (int i = 0; i < p.size(); i++) {
      if (!current.equals(p.get(i).getOwnername())) {
        mPhotos.add(p.get(i));
        current = p.get(i).getOwnername();
      }
    }
  }

  private void getFriends() {
    //new Func2<Photos, Who, UserInfo>()
    //public UserInfo call(Photos p, Who w)  {   return new UserInfo(w, p);
    Observable<Who> tagsObservable = getJacksonService().getTags(Util.getUserId());
    friendSubscription = getJacksonService().getFriendsPhotos(Util.getUserId())
        .zipWith(tagsObservable, (p, w) -> {
          return new UserInfo(w, p);
        })
        .retry()
        .subscribeOn(Schedulers.io()) // thread pool; bg + bg
        .observeOn(Schedulers.io())
        .subscribe(new Subscriber<UserInfo>() {
          @Override
          public void onCompleted() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> dismissProgress());
          }

          @Override
          public void onError(Throwable e) {
            // cast to retrofit.HttpException to get the response code
            if (e instanceof HttpException) {
              HttpException response = (HttpException) e;
              int code = response.code();
              Log.e("ERROR", String.valueOf(code));
            }
            Log.e("ERROR", "error getting friends" + e);
            //signout
          }

          @Override
          public void onNext(UserInfo userInfo) {
            //add photos to real
            //if (username.length() == 0) {
            //throw new Exception("username is not set")
            //stop the sync adapter and remove account
            //try to sign out gracefully
            Photos photos = userInfo.getFriends();
            Who w = userInfo.getWho();
            List<Tag> tags = w.getWho().getTags().getTag();
            // }
            Realm realm2 = null;
            try {
              realm2 = Realm.getDefaultInstance();
              realm2.beginTransaction();
              String user_id = Util.getUserId();
              UserModel u = null;
              u = realm2.where(UserModel.class).equalTo("userId", user_id).findFirst();
              if (u.friendsList.size() > 0) {
                u.friendsList = null;
              }
              for (Photo p : photos.getPhotos().getPhotoList()) {
                u.friendsList.add(p);
              }
              if (u.tagsList.size() < tags.size()) {
                for (Tag t : tags) {
                  if (!u.tagsList.contains(t)) {
                    t.setAuthorname(Util.getCurrentUser());
                    u.tagsList.add(t);
                  }
                }
              }
              //f.user.username.content =
              u.name = Util.getCurrentUser();
              u.timestamp = Calendar.getInstance().getTime();
              realm2.copyToRealmOrUpdate(u);  //deep copy
              realm2.commitTransaction();
              Log.d("DEBUG", "end get userinfo: " + u);
            } finally {
              if (realm2 != null) {
                realm2.close();
              }
            }
          }
        });
  }
}
