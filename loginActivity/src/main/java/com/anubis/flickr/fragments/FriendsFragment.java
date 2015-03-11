package com.anubis.flickr.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.anubis.flickr.R;
import com.anubis.flickr.adapter.PhotoArrayAdapter;
import com.anubis.flickr.models.FlickrPhoto;
import com.anubis.flickr.models.FriendsFlickrPhoto;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends FlickrBaseFragment {

    FriendsFlickrPhoto p;
    List<FriendsFlickrPhoto.Comment> comments;
    private ArrayList<FriendsFlickrPhoto> extraPhotos;

    protected PhotoArrayAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new PhotoArrayAdapter(getActivity(), mPhotoItems, true);
        mType = FriendsFlickrPhoto.class;
        setRetainInstance(true);
        loadPhotos();
    }

    void customLoadMoreDataFromApi(int page) {
        //@todo add the endless scroll

    }

    protected void loadPhotos() {
        clearAdapter();
        client.getFriendsList(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject json) {
                try {
                    JSONArray photos = json.getJSONObject("photos")
                            .getJSONArray("photo");
                    for (int x = 0; x < photos.length(); x++) {
                        JSONObject jsonObject = photos.getJSONObject(x);
                        String uid = jsonObject.getString("id");
                        FlickrPhoto p = FriendsFlickrPhoto
                                .byPhotoUid(uid, mType);
                        if (p == null) {
                            p = new FriendsFlickrPhoto(jsonObject);
                        }
                        p.save();
                        mAdapter.add(p);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Error getting JSON", e.toString());
                }
            }

            @Override
            public void onFailure(Throwable arg0, JSONObject arg1) {
                Log.e("ERROR", ": onFailure: FriendsFragment " + arg0 + " " + arg1);
            }

        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container,
                false);
        vPhotos = (ListView) view.findViewById(R.id.lvPhotos);
        vPhotos.setAdapter(mAdapter);
        vPhotos.setOnItemClickListener(mListener);
        setHasOptionsMenu(true);
        return view;
    }


}
