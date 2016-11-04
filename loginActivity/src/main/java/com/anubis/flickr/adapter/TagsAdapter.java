package com.anubis.flickr.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anubis.flickr.R;
import com.anubis.flickr.models.Photo;
import com.anubis.flickr.util.Util;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

import static com.anubis.flickr.R.id.checkBox;

/**
 * Created by sabine on 9/26/16.
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {


    // Define listener member variable
    private OnItemClickListener listener;
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor;


    public OnItemClickListener getListener() {
        return this.listener;

    }

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView tags;
        TextView title;
        ImageView imageView;
        TextView timestamp;
        CheckBox checkbox;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView, final OnItemClickListener listener) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);


            imageView = (ImageView) itemView.findViewById(R.id.ivPhoto);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (listener != null && position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
            tags = (TextView) itemView.findViewById(R.id.checkboxtags);
            checkbox = (CheckBox) itemView.findViewById(checkBox);

        }
    }

    // Store a member variable for the contacts
    private List<Photo> mPhotos;
    // Store the context for easy access
    private Context mContext;
    private boolean mStaggered;

    // Pass in the contact array into the constructor
    public TagsAdapter(Context context, List<Photo> photos, boolean staggered) {
        mStaggered = staggered;
        mPhotos = photos;
        mContext = context;
        this.prefs = Util.getUserPrefs();
        this.editor = this.prefs.edit();

    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public TagsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View photosView = inflater.inflate(R.layout.photo_item_friends, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(photosView, getListener());
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(TagsAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Photo photo = mPhotos.get(position);

        // Set item views based on your views and data model
        ImageView imageView = viewHolder.imageView;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewHolder.imageView
                .getLayoutParams();

        TextView tags = viewHolder.tags;

        tags.setText(photo.getTags());
        tags.setTextColor(mContext.getResources().getColor(R.color.white));
        CheckBox cb = viewHolder.checkbox;

        cb.setVisibility(View.VISIBLE);
        int id = Resources.getSystem().getIdentifier("btn_check_holo_dark", "drawable", "android");
        cb.setButtonDrawable(id);
        cb.setHint("Batch Tag");
        cb.setChecked(true);

        //the images come back the same size as thumbnails
        //set random widths and heights between 75 and 200
        if (mStaggered) {
            Random rand = new Random();
            int n = rand.nextInt(300) + 200;
            lp.height = n; // photo.getPhotoHeight() * 2;
            //n = rand.nextInt(200) + 100;
            lp.width = 400; // photo.getPhotoList//set the title, name, comments
            imageView.setLayoutParams(lp);

        } else {
            lp.height= 250;
            lp.width = 300;
        }
        Picasso.with(this.getContext()).load(photo.getUrl()).fit().centerCrop()
                .placeholder(android.R.drawable.btn_star)
                .error(android.R.drawable.btn_star)
                .into(imageView);

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mPhotos.size();
    }


}
