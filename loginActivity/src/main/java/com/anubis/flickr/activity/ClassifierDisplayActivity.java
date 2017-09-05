package com.anubis.flickr.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.anubis.flickr.FlickrClientApp;
import com.anubis.flickr.R;
import com.anubis.flickr.models.Photo;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.tensorflow.tensorlib.activity.BitmapClassifier;
import org.tensorflow.tensorlib.classifier.Classifier;
import org.tensorflow.tensorlib.classifier.ClassifierType;
import org.tensorflow.tensorlib.view.ResultsView;

import io.realm.Realm;

import static com.anubis.flickr.fragments.FlickrBaseFragment.CLASSIFIER_TYPE;
import static com.anubis.flickr.fragments.FlickrBaseFragment.CLASSIFIER_WIDTH;
import static com.anubis.flickr.fragments.FlickrBaseFragment.RESULT;

public class ClassifierDisplayActivity extends AppCompatActivity {

    public static final int TL_REQ = 007;
    public static final String TAG = "ClierDisplayActivity";
    String mUid = "";
    static Photo mPhoto;
    Realm pRealm;
    AdView mPublisherAdView;
    ResultsView resultsView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classifier_image_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String pid = getIntent().getStringExtra(RESULT);
        pRealm = Realm.getDefaultInstance();
        mPhoto = pRealm.where(Photo.class).equalTo("id", pid).findFirst();


        mUid = mPhoto.getId();
        //@todo comments do not get refreshed w sync adapter
        //@todo this needs to check if 24 hr has passed


        ImageView imageView = (ImageView) findViewById(R.id.ivResult);
        Picasso.with(FlickrClientApp.getAppContext()).load(mPhoto.getUrl()).fit().into(imageView);

        //@todo cache this?

        mPublisherAdView = (AdView) findViewById(R.id.publisherAdView);
       /* AdRequest adRequest = new AdRequest.Builder()
               // .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
               // .addTestDevice("9D3A392231B42400A9CCA1CBED2D006F")  // My Galaxy Nexus test phone
                .build();
        mPublisherAdView.loadAd(adRequest);*/
        resultsView = (ResultsView) findViewById(R.id.recogView);
        String classifier = getIntent().getStringExtra(CLASSIFIER_TYPE);
        int width = getIntent().getIntExtra(CLASSIFIER_WIDTH, 0);
        //@todo pass in the byte[] from getByteArrayFromImageView maybe
        getBitMap(mPhoto.getUrl(), classifier, width);


    }


    //@todo  do not recycle bitmap as per picasso
    //run in bg
    private void getBitMap(String url, String classifier, int width) {

        String this_classifer = classifier;
        int this_width = width;
        Picasso.with(this)
                .load(url)
                //keeps a weak ref to target by default; must be run from main thread

                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        // loaded bitmap is here (bitmap)
                        // run in bg
//@todo this might be very inefficient see http://www.vogella.com/tutorials/AndroidApplicationOptimization/article.html
                        //try to do something lighter here  find lightest bitmap/image lib

                        runClassifier(bitmap, this_classifer, this_width);

                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }


    //@todo change this to ensure bitmap is recycled
    public void runClassifier(Bitmap bitmap, String type, int width) {
        float[] f = BitmapClassifier.process(bitmap, type);  //deal with bitmap here & quickly as possible
        ClassifierType t = ClassifierType.getTypeForString(type);
        Classifier c = BitmapClassifier.getClassifierForType(t);
        BitmapClassifier.getInstance().runClassifier(c,f,resultsView);
        //@todo from here we could try intent approach again
        //@todo save the results to the photo
        //resultsView.getResults
    }
/*
    public void runClassifier(Bitmap bitmap, String classifier) {
        //Intent intent = new Intent(this, BitmapActivity.class);
        //transactiontoolarge exception
        //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Bundle extras = new Bundle();
        extras.putParcelable("bitmap", bitmap);
        extras.putString(CLASSIFIER_TYPE, ClassifierType.CLASSIFIER_INCEPTION.getName());
        intent.putExtras(extras);
        Log.d(TAG, "Starting activity for tensorlib");
        startActivityForResult(intent, TL_REQ);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == TL_REQ) {
            List<Classifier.Recognition> results = data.getParcelableExtra("results");

        }

    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapClassifier.getInstance().cleanUp();


    }

}
