package com.anubis.flickr.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import com.anubis.flickr.FlickrClientApp;
import com.anubis.flickr.R;
import com.anubis.flickr.fragments.FlickrBaseFragment;
import com.anubis.flickr.models.Comment;
import com.anubis.flickr.models.Comments;
import com.anubis.flickr.models.Comments_;
import com.anubis.flickr.models.Photo;
import com.anubis.flickr.util.DateUtility;
import com.anubis.flickr.util.ImageRoundedTransformation;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.hkm.soltag.TagContainerLayout;
import io.realm.Realm;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.anubis.flickr.R.id.comments;
import static com.anubis.flickr.R.id.username;

public class ImageDisplayActivity extends AppCompatActivity {

    WebView wvComments;
    List<String> mTagsList = new ArrayList<String>();
    TagContainerLayout mTags;
    EditText etComments;
    String mUid = "";
    String mContent;
    StringBuilder mBuilder;
    private Subscription subscription, subscription2;
    Map<String, String> data = new HashMap<>();
    Photo mPhoto;
    Realm pRealm;
    HandlerThread handlerThread, handlerThread2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        String pid = getIntent().getStringExtra(FlickrBaseFragment.RESULT);
        pRealm = Realm.getDefaultInstance();
        mPhoto = pRealm.where(Photo.class).equalTo("id", pid).findFirst();
        pRealm.close();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
// ...
// Display icon in the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_rocket);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        // getSupportActionBar().setElevation(3);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setSubtitle(R.string.image_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView imageView = (ImageView) findViewById(R.id.ivResult);
        Picasso.with(getBaseContext()).load(mPhoto.getUrl()).transform(new ImageRoundedTransformation(5, 5)).resize(300, 300).centerCrop().into(imageView);
        //ImageLoader imageLoader = ImageLoader.getInstance();
        //imageLoader.getComments(mPhoto.getUrl(), image);
        TextView tvUsername = (TextView) findViewById(username);
        tvUsername.setText("By: " + mPhoto.getOwnername());
        TextView tvTimestamp = (TextView) findViewById(R.id.timestamp);
        tvTimestamp.setText(DateUtility.relativeTime(mPhoto.getDatetaken(), this));
        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText(mPhoto.getTitle());
        etComments = (EditText) findViewById(R.id.etComments);
        etComments.setScroller(new Scroller(this));
        etComments.setMaxLines(1);
        etComments.setVerticalScrollBarEnabled(true);
        etComments.setMovementMethod(new ScrollingMovementMethod());
        wvComments = (WebView) findViewById(comments);
        wvComments.setBackgroundColor(getResources().getColor(R.color.AliceBlue));
        wvComments.setVerticalScrollBarEnabled(true);
        wvComments.setHorizontalScrollBarEnabled(true);
        wvComments.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        wvComments.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        wvComments.getSettings().setJavaScriptEnabled(true);
        mUid = mPhoto.getId();
        mTags = (TagContainerLayout) findViewById(R.id.tag_group);
        //@todo
        //getPhotoFromRealm
        //if the photo comments and tags are in the realm already
        Realm cRealm = Realm.getDefaultInstance();
        Comments_ c = cRealm.where(Comments_.class).equalTo("photoId", mUid).findFirst();
        // else go get that info
        //comments change a lot; should we cache &  wait for them for 24 hrs
        if (null == c) {
            getComments(mUid);
        } else {
            displayComments(wvComments, c.commentsList);
        }
        cRealm.close();
        //mTagsList.clear();
        displayTags(mPhoto.getTags());
        // get focus off edittext, hide kb
        // WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

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


    //now we have the tags;
    private void getComments(final String uid) {
        //Observable<PhotoInfo> photoInfo = FlickrClientApp.getJacksonService().getPhotoInfo(uid);
        subscription = FlickrClientApp.getJacksonService().getComments(uid)
                /*.zipWith(photoInfo, new Func2<Comments, PhotoInfo, ImageDisplay>() {
            @Override
            public ImageDisplay call(Comments c, PhotoInfo p) {
                return new ImageDisplay(p, c);
            }


        })*/

                .subscribeOn(Schedulers.io()) // optional if you do not wish to override the default behavior
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Comments>() {
                    @Override
                    public void onCompleted() {


                        //ringProgressDialog.dismiss();
                        //Log.d("DEBUG","oncompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        // cast to retrofit.HttpException to get the response code
                        if (e instanceof HttpException) {
                            HttpException response = (HttpException) e;
                            int code = response.code();
                            Log.e("ERROR", String.valueOf(code));
                        }
                        Log.e("ERROR", "error getting comments" + e);
                    }

                    @Override
                    public void onNext(Comments c) {
                        Log.d("DEBUG", "comments: " + comments);
                        //right now this is only called once if null obj in realm

                        List<Comment> comments = c.getComments().getComments();
                        if (comments.size() > 0) {
                            if (saveComments(comments, mUid)) {
                                displayComments(wvComments, comments);
                            } else {
                                //@todo throw exception
                                Log.e("ERROR", "comments not saved: " + c);
                            }
                        }
                    }
                });

    }

    public void displayTags(String tags) {
        //tags.stream().map(it -> it.getContent()).collect(Collectors.toCollection())
        //when android catches up to 1.8
        for (String s : tags.split(" ")) {
            mTags.addTag(s);
        }


    }

    public boolean saveComments(final List<Comment> cList, final String uid) {
        Log.d("SAVE COMMENT", String.valueOf(cList.get(0).getContent()) + ":" + uid);
        handlerThread = new HandlerThread("BackgroundHandler");
        handlerThread.start();
        final Handler backgroundHandler = new Handler(handlerThread.getLooper());
        return backgroundHandler.post(new Runnable() {

            @Override
            public void run() {
                Realm realm = null;
                try {
                    realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    Comments_ c = realm.where(Comments_.class).equalTo("photoId", uid).findFirst();
                    if (null == c) {
                        c = realm.createObject(Comments_.class, uid);
                    }

                    for (Comment comment : cList) {
                        // Comment cm = cRealm.copyFromRealm(comment);
                        if (!c.commentsList.contains(comment)) {
                            c.commentsList.add(comment);
                        }
                    }
                    realm.copyToRealmOrUpdate(c);
                    realm.commitTransaction();
                } finally {
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        });


    }


    //@todo check for idempotence only once put, and in reverse w date
    public void addComment(View v) {
        String commentString = etComments.getText().toString();
        etComments.setText("");
        etComments.clearFocus();
        /*try {
           // commentString = URLEncoder.encode(commentString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("ERROR","Encoding not supported in comment");
        }*/
        if (commentString.length() > 0) {

            data.put("comment_text", commentString);
            data.put("photo_id", mPhoto.getId());
            subscription2 = FlickrClientApp.getJacksonService().addComment(data)
                    .subscribeOn(Schedulers.io())  // can be optional if not overriding
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Comment>() {
                        @Override
                        public void onCompleted() {


                        }

                        @Override
                        public void onError(Throwable e) {
                            // cast to retrofit.HttpException to get the response code
                            if (e instanceof HttpException) {
                                HttpException response = (HttpException) e;
                                int code = response.code();
                                Log.e("ERROR", String.valueOf(code));
                            }
                            Log.e("ERROR", "error add comment" + e);
                        }

                        @Override
                        public void onNext(Comment c) {
                            //@todo maybe we should let this be a network call to have fresh data?
                            List<Comment> displayList = new ArrayList<Comment>();
                            //@todo  workaround realm slow to add w bg thread
                            Realm cRealm = Realm.getDefaultInstance();
                            Comments_ comments = cRealm.where(Comments_.class).equalTo("photoId", mUid).findFirst();
                            if (null != comments && comments.getCommentsList().size() > 0) {
                                displayList.addAll(comments.getCommentsList());
                            }


                            List<Comment> commentsList = new ArrayList<Comment>();
                            Comment comment = new Comment();
                            //@todo  workaroundthe json maps anon obj { comment: {
                            Map comObj = (Map) c.getAdditionalProperties().get("comment");
                            comment.setAuthor((String) comObj.get("author"));
                            comment.setId((String) comObj.get("id"));
                            comment.setAuthorname((String) comObj.get("authorname"));
                            comment.setDatecreate((String) comObj.get("datecreate"));
                            comment.setContent((String) comObj.get("_content"));
                            commentsList.add(comment);
                            if (saveComments(commentsList, mUid)) {
                                displayList.add(comment);
                                displayComments(wvComments, displayList);
                            } else {
                                //@todo comment not added
                                Log.e("ERROR", "comment not added: " + c);
                            }
                            cRealm.close();
                        }
                    });

        }
    }


    //
    private void displayComments(WebView commentsView, List<Comment> comments) {
        mBuilder = new StringBuilder();
        mBuilder.append("<html><head>  <style> body {color: #4169E1;  font-size: 12px;}</style></head><br>");
        for (int i = comments.size() - 1; i >= 0; i--) {
            Comment c = comments.get(i);
            if (null != c && null != c.getContent()) {
                mContent = c.getContent();
                String htmlString = mContent;

                if (mContent.contains("[http") && !mContent.contains("src")) {
                    //make the link work but leave images
                    htmlString = mContent.replaceAll("\\[(\\s*http\\S+\\s*)\\]", "<a href=\"" + "$1" + "\">$1</a><br>");
                    //@todo look for other corner cases
                    // } else if (mContent.contains("http") && !added) {
                    //     htmlString = mContent.replaceAll("http\\S+", "<a href=\"" + "$0" + "\">$0</a>");
                    // }
                }
                String time = new PrettyTime().format(new Date(Long.parseLong(c.getDatecreate()) * 1000L));
                mBuilder.append("<b>");
                mBuilder.append(c.getAuthorname());
                mBuilder.append(":</b> (");
                mBuilder.append(time);
                mBuilder.append(")<br>");
                mBuilder.append(htmlString);
                mBuilder.append("<br><br>");
            }
        }

        mBuilder.append("</body></html>");
        commentsView.loadUrl("about:blank");
        commentsView.loadData(mBuilder.toString(), "text/html", "utf-8");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != subscription) {
            subscription.unsubscribe();
        }
        if (null != subscription2) {
            subscription2.unsubscribe();
        }
        if (null != handlerThread) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (null != handlerThread.getLooper()) {
                    handlerThread.getLooper().quitSafely();
                }
            } else {
                handlerThread.quit();
            }
        }

    }

}
