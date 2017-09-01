package org.tensorflow.tensorlib.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LruCache;
import android.util.TypedValue;

import org.tensorflow.tensorlib.R;
import org.tensorflow.tensorlib.TensorLib;
import org.tensorflow.tensorlib.classifier.Classifier;
import org.tensorflow.tensorlib.classifier.ClassifierType;
import org.tensorflow.tensorlib.classifier.TensorFlowImageClassifier;
import org.tensorflow.tensorlib.env.BorderedText;
import org.tensorflow.tensorlib.env.Logger;
import org.tensorflow.tensorlib.fragment.BitmapFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sabine on 7/16/17.
 */

public class BitmapActivity extends Activity {

    Fragment fragment;
    private static final Logger LOGGER = new Logger();
    private BorderedText borderedText;

    private static final float TEXT_SIZE_DIP = 10;
    List<Classifier.Recognition> results = new ArrayList<>();
    Handler handler;
    HandlerThread handlerThread;
    ClassifierType classifierType;
    Classifier classifier = null;
    int mInputSize;
    public static final String CLASSIFIER_TYPE = "classifierType";
    public static final String TAG = "BitmapActivity";


    //use weakrefs?

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getApplicationContext().getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        //choose either paintings or inception classifier at this point
        //@todo lifetime of this wrt to closing app pausing/ use for camera/stylize classifier
        //LRU cache?\\
        Bundle extras = getIntent().getExtras();
        Bitmap bmp = (Bitmap) extras.getParcelable("bitmap");
        String cType = extras.getString(CLASSIFIER_TYPE);


        LruCache<String, Classifier> objs = TensorLib.classifierCache;
        classifier = null;
        if (cType.equals(ClassifierType.CLASSIFIER_RETRAINED.getName())) {
            classifierType = ClassifierType.CLASSIFIER_RETRAINED;
            if (objs.get(cType) != null) {
                classifier = objs.get(cType);
            }

        } else if (cType.equals(ClassifierType.CLASSIFIER_INCEPTION.getName())) {
            classifierType = ClassifierType.CLASSIFIER_INCEPTION;
            if (objs.get(cType) != null) {
                classifier = objs.get(cType);
            }
        }
//@toso erese deodorant context here must match assets copy fn
        if (classifier == null) {
            classifier =
                    TensorFlowImageClassifier.create(
                            TensorLib.context, classifierType.getModelFilename(), classifierType.getLabelFilename(), classifierType.getInputSize(),
                            classifierType.getImageMean(), classifierType.getImageStd(), classifierType.getInputName(), classifierType.getOutputName()
                    );
            objs.put(cType, classifier);
        }
        mInputSize = classifierType.getInputSize();
        //@todo compress bmp earlier
        runClassifier(bmp);

    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();

    }


    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }


    protected int getLayoutId() {
        return R.layout.bitmap_fragment;
    }

    protected void setFragment() {

        fragment = BitmapFragment.newInstance(getLayoutId());
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void runClassifier(final Bitmap bitmap) {
        //change this to static
        Bitmap b = null;
        //@todo do we need to deal with JPEG decoding after all?  removed decodejpeg from retrained
        if (bitmap.getWidth() > mInputSize || bitmap.getHeight() > mInputSize) {
            b = getResizedBitmap(bitmap, mInputSize, mInputSize);
        } else {
            b = bitmap;
        }

        // handlerThread = new HandlerThread("bitmap");
        // handlerThread.start();
        // handler = new Handler(handlerThread.getLooper());
       /*handler.post(new Runnable() {
            @Override
            public void run() {

*/
        final long startTime = SystemClock.uptimeMillis();
        //@todo run in bg return on main
        //final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        //todo cache the results using a Future

        results = (ArrayList<Classifier.Recognition>) classifier.recognizeImage(b);
        if (results.size() == 0) {
            results = new ArrayList<Classifier.Recognition>();
            results.add(new Classifier.Recognition("", "There were no results!", 0f, null));
        }
        //rv.setResults(results);

        final long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
        Log.d(TAG, "runClassifier() time : " + lastProcessingTimeMs);
        //b.recycle(); //picasso limitation with callback
        Intent data = new Intent();
        data.putParcelableArrayListExtra("results", (ArrayList<Classifier.Recognition>) results);
        data.putExtra("code", 700);
        setResult(RESULT_OK, data);
        this.finish();
    }
    // });
    //ProgressDialog d = new ProgressDialog(c);
        /*while (results == null) {
            //progress bar

            d.show();
        }*/

    //d.dismiss();

    //resultsView.setResults(results);
    // requestRender();
    //final long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
    //  }
    // cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
    // resultsView.setResults(results);
    // requestRender();
    // computing = false;

    //  });

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, newWidth, newHeight, matrix, false);
        //bm.recycle();
        return resizedBitmap;
    }


    //@todo
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }


}
