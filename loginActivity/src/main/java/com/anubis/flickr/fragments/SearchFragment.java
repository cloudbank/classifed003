package com.anubis.flickr.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anubis.flickr.FlickrClientApp;
import com.anubis.flickr.R;
import com.anubis.flickr.activity.ImageDisplayActivity;
import com.anubis.flickr.adapter.SearchAdapter;
import com.anubis.flickr.adapter.SpacesItemDecoration;
import com.anubis.flickr.listener.EndlessRecyclerViewScrollListener;
import com.anubis.flickr.models.Photo;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SearchFragment extends FlickrBaseFragment {


    RecyclerView rvPhotos;
    SearchAdapter searchAdapter;
    List<Photo> sPhotos = new ArrayList<Photo>();
    Realm commonsRealm;
    RealmChangeListener changeListener;

    @Override
    public void onDestroy() {
        super.onDestroy();
        commonsRealm.close();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //this gets called along w lifecycle when vp recycles fragment ie  commons tab
        //get from realm in random order
        commonsRealm = Realm.getDefaultInstance();
        RealmResults<Photo> commons = commonsRealm.where(Photo.class).equalTo("isCommon", true).findAll();
Log.d("COMMONS","commons"+commons);
        changeListener = new RealmChangeListener<RealmResults<Photo> >() {

            @Override
            public void onChange(RealmResults<Photo> commons) {
                // This is called anytime the Realm database changes on any thread.
                // Please note, change listeners only work on Looper threads.
                // For non-looper threads, you manually have to use Realm.waitForChange() instead.
                sPhotos.addAll(commons);
                searchAdapter.notifyDataSetChanged();

            }


        };

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchAdapter = new SearchAdapter(FlickrClientApp.getAppContext(), sPhotos, true);
        //loadPhotos(1, true);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        rvPhotos = (RecyclerView) view.findViewById(R.id.rvSearch);

        rvPhotos.setAdapter(searchAdapter);
        //rvPhotos.setOnItemClickListener(mListener);
        //rvPhotos.setOnScrollListener(mScrollListener);
        //rvPhotos.setLayoutManager(gridLayoutManager);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvPhotos.setLayoutManager(gridLayoutManager);
        SpacesItemDecoration decoration = new SpacesItemDecoration(15);
        rvPhotos.addItemDecoration(decoration);
        rvPhotos.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                customLoadMoreDataFromApi(page);
            }
        });
        //rvPhotos.setOnItemClickListener(mListener);
        //rvPhotos.setOnScrollListener(mScrollListener);
        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // String title = mTags.get(position).getTitle();
                Intent intent = new Intent(getActivity(),
                        ImageDisplayActivity.class);
                Photo photo = sPhotos.get(position);
                intent.putExtra(RESULT, photo.getId());
                startActivity(intent);
                //Toast.makeText(getActivity(), title + " was clicked!", Toast.LENGTH_SHORT).show();
            }
        });


        setHasOptionsMenu(true);
        return view;

    }




    void customLoadMoreDataFromApi(int page) {
        /*loadPhotos(page, false);*/
    }

    void clearAdapter() {
        sPhotos.clear();
        searchAdapter.notifyDataSetChanged();
    }



}
