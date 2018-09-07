package com.craiovadata.sightsandsounds;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.craiovadata.sightsandsounds.adapter.ItemAdapter;
import com.craiovadata.sightsandsounds.viewmodel.MainActivityViewModel;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        FilterDialogFragment.FilterListener,
        ItemAdapter.OnItemSelectedListener {

    private static final String TAG = "MainActivity";
    public static final String COLLECTION_NAME = "sights_and_sounds_";
//    private Parcelable listState;

//    private static final int LIMIT = 50;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

//    @BindView(R.id.text_current_search)
//    TextView mCurrentSearchView;

//    @BindView(R.id.text_current_sort_by)
//    TextView mCurrentSortByView;

    @BindView(R.id.recycler_restaurants)
    RecyclerView recyclerView;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private ItemAdapter adapter;

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
                .orderBy("country", Query.Direction.ASCENDING);
//                .limit(LIMIT);

        // RecyclerView
        adapter = new ItemAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);

                    int savedListPosition = getPreferences(MODE_PRIVATE).getInt("listPosition", 0);
                    recyclerView.getLayoutManager().scrollToPosition(savedListPosition);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();

        MobileAds.initialize(this, getString(R.string.admob_app_id));

    }

    @Override
    public void onStart() {
        super.onStart();

        // Apply filters
        onFilter(mViewModel.getFilters());

        // Start listening for Firestore updates
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
        int currentVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        getPreferences(MODE_PRIVATE).edit().putInt("listPosition", currentVisiblePosition).apply();
    }


//    @OnClick(R.id.filter_bar)
//    public void onFilterClicked() {
//        // Show the dialog containing filter options
//        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
//    }

//    @OnClick(R.id.button_clear_filter)
//    public void onClearFilterClicked() {
//        mFilterDialog.resetFilters();
//
//        onFilter(Filters.getDefault());
//    }

    @Override
    public void onRestaurantSelected(DocumentSnapshot restaurant) {
        // Go to the details page for the selected restaurant
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.KEY_ITEM_ID, restaurant.getId());

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
//        adapter.setQuery(query);
//
//        // Set header
//        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));
//        mCurrentSortByView.setText(filters.getOrderDescription(this));
//
//        // Save filters
//        mViewModel.setFilters(filters);
    }

}
