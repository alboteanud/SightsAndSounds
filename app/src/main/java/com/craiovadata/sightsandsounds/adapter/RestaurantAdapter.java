package com.craiovadata.sightsandsounds.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.craiovadata.sightsandsounds.R;
import com.craiovadata.sightsandsounds.model.Entry;
import com.craiovadata.sightsandsounds.util.GlideApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of Restaurants.
 */
public class RestaurantAdapter extends FirestoreAdapter<RestaurantAdapter.ViewHolder> {

    public interface OnRestaurantSelectedListener {

        void onRestaurantSelected(DocumentSnapshot restaurant);

    }

    private OnRestaurantSelectedListener mListener;
    public StorageReference mStorageRef;

    public RestaurantAdapter(Query query, OnRestaurantSelectedListener listener) {
        super(query);
        mListener = listener;
        // Initialize Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.restaurant_item_image)
        ImageView imageView;

        @BindView(R.id.restaurant_item_name)
        TextView nameView;

        @BindView(R.id.restaurant_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.restaurant_item_num_ratings)
        TextView numRatingsView;

        @BindView(R.id.restaurant_item_price)
        TextView priceView;

        @BindView(R.id.item_country)
        TextView countryView;

        @BindView(R.id.restaurant_item_city)
        TextView cityView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnRestaurantSelectedListener listener) {

            Entry entry = snapshot.toObject(Entry.class);
            Resources resources = itemView.getResources();

            StorageReference thumbnailRef = FirebaseStorage.getInstance().getReference().child("image_thumbs/" + snapshot.getId() + "_tn.jpg");

            GlideApp.with(imageView.getContext())
//                    .using(new FirebaseImageLoader())
                    .load(thumbnailRef)
                    .into(imageView);


            nameView.setText(entry.getImg_title());
//            ratingBar.setRating((float) entry.getAvgRating());
//            cityView.setText(entry.getCity());
            countryView.setText(entry.getCountry());
//            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
//                    entry.getNumRatings()));
//            priceView.setText(RestaurantUtil.getPriceString(entry));

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onRestaurantSelected(snapshot);
                    }
                }
            });
        }

    }
}
