package com.craiovadata.sightsandsounds;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.craiovadata.sightsandsounds.adapter.RestaurantAdapter;
import com.craiovadata.sightsandsounds.model.Entry;
import com.craiovadata.sightsandsounds.model.Rating;
import com.craiovadata.sightsandsounds.model.Restaurant;
import com.craiovadata.sightsandsounds.util.RatingUtil;
import com.craiovadata.sightsandsounds.util.RestaurantUtil;
import com.craiovadata.sightsandsounds.viewmodel.MainActivityViewModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static java.lang.Double.NaN;

public class MainActivity extends AppCompatActivity implements
        FilterDialogFragment.FilterListener,
        RestaurantAdapter.OnRestaurantSelectedListener {

    private static final String TAG = "MainActivity";
    public static final String COLLECTION_NAME = "sights_and_sounds_";

    private static final int LIMIT = 50;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

    @BindView(R.id.recycler_restaurants)
    RecyclerView mRestaurantsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private RestaurantAdapter mAdapter;

    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection(COLLECTION_NAME)
                .orderBy("country", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new RestaurantAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mRestaurantsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRestaurantsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mRestaurantsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRestaurantsRecycler.setAdapter(mAdapter);

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
//        if (shouldStartSignIn()) {
//            startSignIn();
//            return;
//        }

        // Apply filters
        onFilter(mViewModel.getFilters());

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_items:
                onAddItemsClicked();
                break;
            case R.id.menu_add_sights_and_sounds:
                onAddSightsAndSoundsClicked();
                break;
            case R.id.menu_sign_out:
                AuthUI.getInstance().signOut(this);
                break;
            case R.id.menu_add_ratings:
                onAddRatingsClicked();
                break;
            case R.id.menu_calc_avrg:
                onUpdateAvregeRatingClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.filter_bar)
    public void onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    @OnClick(R.id.button_clear_filter)
    public void onClearFilterClicked() {
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }

    @Override
    public void onRestaurantSelected(DocumentSnapshot restaurant) {
        // Go to the details page for the selected restaurant
        Intent intent = new Intent(this, RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.getId());

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @Override
    public void onFilter(Filters filters) {
        // Construct query basic query
//        Query query = mFirestore.collection(COLLECTION_NAME);
//
//        // Category (equality filter)
//        if (filters.hasCategory()) {
//            query = query.whereEqualTo(Restaurant.FIELD_CATEGORY, filters.getCategory());
//        }
//
//        // City (equality filter)
//        if (filters.hasCity()) {
//            query = query.whereEqualTo(Restaurant.FIELD_CITY, filters.getCity());
//        }
//
//        // Price (equality filter)
//        if (filters.hasPrice()) {
//            query = query.whereEqualTo(Restaurant.FIELD_PRICE, filters.getPrice());
//        }
//
//        // Sort by (orderBy with direction)
//        if (filters.hasSortBy()) {
//            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
//        }
//
//        // Limit items
//        query = query.limit(LIMIT);
//
//        // Update the query
//        mAdapter.setQuery(query);
//
//        // Set header
//        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));
//        mCurrentSortByView.setText(filters.getOrderDescription(this));
//
//        // Save filters
//        mViewModel.setFilters(filters);
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }


    private String loadJSONFromAsset() {
        String json = null;
        InputStream file = null;
        try {
            file = getAssets().open("data.json");
            byte[] buffer = new byte[file.available()];
            int bytesRead = file.read(buffer);
            file.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;

    }

    private void onAddItemsClicked() {
        // Add a bunch of random restaurants
        WriteBatch batch = mFirestore.batch();
        for (int i = 0; i < 10; i++) {
            DocumentReference restRef = mFirestore.collection("sights_and_sounds").document();

            // Create random restaurant / ratings
            Restaurant randomRestaurant = RestaurantUtil.getRandom(this);
            List<Rating> randomRatings = RatingUtil.getRandomList(randomRestaurant.getNumRatings());
            randomRestaurant.setAvgRating(RatingUtil.getAverageRating(randomRatings));

            // Add restaurant
            batch.set(restRef, randomRestaurant);

            // Add ratings to subcollection
            for (Rating rating : randomRatings) {
                batch.set(restRef.collection("ratings").document(), rating);
            }
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch succeeded.");
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });
    }

    private void onAddRatingsClicked() {
        Random random = new Random();
        for (int i = 0; i < 319; i++) {
            String refId = String.valueOf(i + 2); // inclus decalajul din lista
            DocumentReference entryRef = mFirestore.collection("sights_and_sounds_").document(refId);

            List<Rating> randomRatings = RatingUtil.getRandomList(1 + random.nextInt(3));
            addRatings(entryRef, randomRatings);
        }

    }

    // update avrege. To repaire mystake - avrgRating was NaN for some countries
    void onUpdateAvregeRatingClicked() {
        mFirestore.collection(MainActivity.COLLECTION_NAME)
                .whereEqualTo("avgRating", NaN)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
                                final Entry entry = document.toObject(Entry.class);

                                final DocumentReference itemRef = mFirestore.collection(MainActivity.COLLECTION_NAME).document(document.getId());
                                Query ratingsQuery = itemRef.collection("ratings");

                                ratingsQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            double sum = 0.0;
                                            int numRatings = task.getResult().size();
                                            for (QueryDocumentSnapshot document : task.getResult()) {

//                                                Log.d(TAG, document.getId() + " rating => " + document.getData());
                                                Rating rating = document.toObject(Rating.class);
                                                sum += rating.getRating();

                                            }
                                            double avrg = sum / numRatings;
                                            Log.d(TAG, entry.getCountry() + " avregeRating: " + avrg + "  din " + numRatings);

                                            entry.setAvgRating(avrg);
                                            itemRef.set(entry);

                                        } else {
                                            Log.d(TAG, "Error getting ratings: ", task.getException());
                                        }
                                    }
                                });


                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }


    private Task<Void> addRatings(final DocumentReference restaurantRef, final List<Rating> randomRatings) {
        // Create reference for new rating, for use inside the transaction


        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                Entry entry = transaction.get(restaurantRef).toObject(Entry.class);


                // Compute new number of ratings
                int newNumRatings = entry.getNumRatings() + randomRatings.size();

                double oldRatingTotal = 0.0;
                if (entry.getAvgRating() != NaN){
                    // Compute new average rating
                 oldRatingTotal = entry.getAvgRating() * entry.getNumRatings();
                }

                double newAvgRating = (oldRatingTotal + RatingUtil.getTotalRating(randomRatings)) / newNumRatings;

                // Set new restaurant info
                entry.setNumRatings(newNumRatings);
                entry.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(restaurantRef, entry);

                // Add ratings to subcollection
                for (Rating rating : randomRatings) {
                    final DocumentReference ratingRef = restaurantRef.collection("ratings").document();
                    transaction.set(ratingRef, rating);
                }

                return null;
            }
        });
    }

    private void onAddSightsAndSoundsClicked() {
        WriteBatch batch = mFirestore.batch();
        try {
            String jsonString = loadJSONFromAsset();
            JSONArray jsonArray = new JSONArray(jsonString);
            Random random = new Random();
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject json = jsonArray.getJSONObject(i);
//                Entry entry = new Entry(
//                        json.getString("country"),
//                        json.getString("img_title"),
//                        json.getString("img_description"),
//                        json.getString("music_title"),
//                        json.getString("music_description"),
//                        random.nextInt(20), );
//                        json.getDouble("lat"),
//                        json.getDouble("lon"));
                Entry entry1 = new Entry();
                entry1.setCountry(json.getString("country"));
                entry1.setImg_title(json.getString("img_title"));
                entry1.setImg_description(json.getString("img_description"));
                entry1.setMusic_title(json.getString("music_title"));
                entry1.setMusic_description(json.getString("music_description"));
                entry1.setNumRatings(random.nextInt(20));


                String refId = String.valueOf(i + 2); // 2 - decalajul din lista
                DocumentReference entryRef = mFirestore.collection("sights_and_sounds_").document(refId);
                batch.set(entryRef, entry1);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch succeeded.");
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });

    }


}
