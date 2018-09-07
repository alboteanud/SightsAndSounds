package com.craiovadata.sightsandsounds;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.craiovadata.sightsandsounds.adapter.RatingAdapter;
import com.craiovadata.sightsandsounds.model.Item;
import com.craiovadata.sightsandsounds.model.Rating;
import com.craiovadata.sightsandsounds.util.GlideApp;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;


public class DetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener {

    private static final String TAG = "ItemDetail";

    public static final String KEY_ITEM_ID = "key_item_id";
    private MediaPlayer mediaPlayer;
    private Uri soundUri;
    private Item item;

    @BindView(R.id.item_image)
    ImageView mImageView;

//    @BindView(R.id.song_title)
//    TextView songNameTextView;


    @BindView(R.id.item_rating)
    MaterialRatingBar mRatingIndicator;

    @BindView(R.id.item_num_ratings)
    TextView mNumRatingsView;

    @BindView(R.id.item_img_title)
    TextView img_titleTextView;

    @BindView(R.id.item_country)
    TextView countryView;

//    @BindView(R.id.restaurant_price)
//    TextView mPriceView;

    @BindView(R.id.view_empty_ratings)
    ViewGroup mEmptyView;

    @BindView(R.id.recycler_ratings)
    RecyclerView mRatingsRecycler;

    private RatingDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference itemRef;
    private ListenerRegistration itemRegistration;

    private RatingAdapter mRatingAdapter;
    private String itemID;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        ButterKnife.bind(this);

        // Get restaurant ID from extras
        itemID = getIntent().getExtras().getString(KEY_ITEM_ID);
        if (itemID == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_ITEM_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the item
        itemRef = mFirestore.collection(MainActivity.COLLECTION_NAME).document(itemID);

        // Get ratings
        Query ratingsQuery = itemRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };
        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mRatingAdapter);

        mRatingDialog = new RatingDialogFragment();


    }

    private void loadAdBanner() {
        if (adView != null) return;

        adView = new AdView(this);

        adView.setAdSize(AdSize.BANNER);
        final String bannerId = getAdBannerId();
        adView.setAdUnitId(bannerId);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                FrameLayout frameLayout = findViewById(R.id.ad_frame);
                frameLayout.addView(adView);
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private String getAdBannerId() {
        int n = new Random().nextInt(10);
        if (n == 0)
            return "ca-app-pub-3052455927658337/1039456339";  // adriana
        else if (n < 4)
            return "ca-app-pub-1015344817183694/5656809454";  // victoria55
        else
            return "ca-app-pub-3931793949981809/2134822250"; // dan
    }

    @Override
    public void onStart() {
        super.onStart();

        mRatingAdapter.startListening();
        itemRegistration = itemRef.addSnapshotListener(this);
        initSoundSource();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView!=null)
            adView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.pause();
        if (adView!=null)
            adView.pause();
    }

    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        releaseMediaPlayer();

        if (itemRegistration != null) {
            itemRegistration.remove();
            itemRegistration = null;
        }


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    /**
     * Listener for the Restaurant document ({@link #itemRef}).
     */
    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e);
            return;
        }
        Item item = snapshot.toObject(Item.class);
        if (item != null) {
            onItemLoaded(item);
        }
    }

    private void onItemLoaded(Item item) {
        this.item = item;

        img_titleTextView.setText(item.getImg_title());

        mRatingIndicator.setRating((float) item.getAvgRating());
        if (item.getNumRatings() != 0)
            mNumRatingsView.setText(getString(R.string.fmt_num_ratings, item.getNumRatings()));
//        songNameTextView.setText(item.getMusic_title());
        countryView.setText(item.getCountry());
//        mPriceView.setText(RestaurantUtil.getPriceString(item));

        StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("images/" + itemID + ".jpg");
        StorageReference thumbnailRef = FirebaseStorage.getInstance().getReference().child("image_thumbs/" + itemID + "_tn.jpg");
        // Background image
        GlideApp.with(mImageView.getContext())
                .load(imgRef)
                .thumbnail(GlideApp.with(mImageView.getContext()).load(thumbnailRef))
                .into(mImageView);

        findViewById(R.id.item_img_info).setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.item_button_back)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.fab_add_rating)
    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @OnClick(R.id.item_img_info)
    public void onInfoImgClicked(View view) {
        if (item != null) {
            InfoDialogFragment infoDialogFragment = new InfoDialogFragment();
            infoDialogFragment.setItem(item);
            infoDialogFragment.show(getSupportFragmentManager(), InfoDialogFragment.TAG);
        }
    }


    @OnClick(R.id.fab_play_sound)
    public void onPlayClicked(View view) {
        if (mediaPlayer == null)
            initMediaPlayer(true);
        else if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        } else {
            try {
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                releaseMediaPlayer();
            }

        }
        loadAdBanner();
    }

    @Override
    public void onRating(Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(itemRef, rating)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);

                        Snackbar.make(findViewById(R.id.myDetailCoordinatorLayout), R.string.review_sent,
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private Task<Void> addRating(final DocumentReference itemRef, final Rating rating) {
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = itemRef.collection("ratings").document();

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                Item item = transaction.get(itemRef).toObject(Item.class);

                // Compute new number of ratings
                int newNumRatings = item.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = item.getAvgRating() * item.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) / newNumRatings;

                // Set new restaurant info
                item.setNumRatings(newNumRatings);
                item.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(itemRef, item);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initSoundSource() {
        StorageReference soundRef = FirebaseStorage.getInstance().getReference().child("sounds/" + itemID + "_x264.mp4");
        Task<Uri> downloadUrl = soundRef.getDownloadUrl();
        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                soundUri = uri;
//                findViewById(R.id.play_sound).setVisibility(View.VISIBLE);
                initMediaPlayer(false);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FloatingActionButton buttonPlay = findViewById(R.id.fab_play_sound);
                        buttonPlay.setClickable(false);
                        buttonPlay.setAlpha(.5f);
                    }
                });
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initMediaPlayer(final boolean startPlay) {
        if (soundUri == null) return;
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.seekTo(0);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                releaseMediaPlayer();
                return true;
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                FloatingActionButton buttonPlay = findViewById(R.id.fab_play_sound);
                buttonPlay.setClickable(true);
                buttonPlay.setAlpha(1.0f);
                if (startPlay)
                    mediaPlayer.start();
            }
        });
        try {
            mediaPlayer.setDataSource(this, soundUri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaPlayer();
        }
    }


}
