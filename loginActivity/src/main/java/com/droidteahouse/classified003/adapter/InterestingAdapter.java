package com.droidteahouse.classified003.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.droidteahouse.classified003.R;
import com.droidteahouse.classified003.models.Photo;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

/**
 * Created by sabine on 9/26/16.
 */
public class InterestingAdapter extends RecyclerView.Adapter<InterestingAdapter.ViewHolder> {
  private OnItemClickListener listener;

  public OnItemClickListener getListener() {
    return this.listener;
  }

  public interface OnItemClickListener {
    void onItemClick(View itemView, int position);
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView ivImage;
    CardView cardView;

    public ViewHolder(final View itemView, final OnItemClickListener listener) {
      super(itemView);
      ivImage = (ImageView) itemView.findViewById(R.id.ivPhoto);
      itemView.setOnClickListener(v -> {
        if (listener != null) {
          int position = getAdapterPosition();
          if (position != RecyclerView.NO_POSITION) {
            listener.onItemClick(itemView, position);
          }
        }
      });
      cardView = (CardView) itemView.findViewById(R.id.cardView);
    }
  }

  private List<Photo> mPhotos;
  private Context mContext;
  private boolean mStaggered;

  public InterestingAdapter(Context context, List<Photo> photos, boolean staggered) {
    mStaggered = staggered;
    mPhotos = photos;
    mContext = context;
    this.setHasStableIds(true);
  }
  private static final String TAG = "InterestingAdapter";
  private Context getContext() {
    return mContext;
  }

  @Override
  public InterestingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View photosView = inflater.inflate(R.layout.photo_item, parent, false);
    ViewHolder viewHolder = new ViewHolder(photosView, getListener());
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(InterestingAdapter.ViewHolder viewHolder, int position) {
    Photo photo = mPhotos.get(position);
    CardView cv = viewHolder.cardView;
    cv.setUseCompatPadding(true);
    //cv.setCardElevation(2.0f);
    StaggeredGridLayoutManager.LayoutParams fp = (StaggeredGridLayoutManager.LayoutParams) viewHolder.cardView.getLayoutParams();
    ImageView imageView = viewHolder.ivImage;
    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) imageView
        .getLayoutParams();
    int aspectRatio = 0;
    if ((photo != null && photo.getHeight() != null) && photo.getWidth() != null) {
      aspectRatio = (Integer.parseInt(photo.getHeight()) / Integer.parseInt(photo.getWidth()));
    } else {
      Log.d(TAG, " photo is null" + photo);
    }
    Random rand = new Random();
    int n = rand.nextInt(100) + 375;
    lp.height = n; // photo.getPhotoHeight() * 2;
    lp.width = aspectRatio > 0 ? n / aspectRatio : n;//
    fp.width = lp.width;
    fp.height = lp.height;
    cv.setLayoutParams(fp);
    Picasso.with(this.getContext()).load(photo.getUrl()).fit().centerCrop()
        .placeholder(android.R.drawable.btn_star)
        .error(android.R.drawable.btn_star)
        .into(imageView);
  }

  @Override
  public int getItemCount() {
    return mPhotos.size();
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemViewType(int position) {
    return position;
  }
}
