package com.craiovadata.sightsandsounds;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.craiovadata.sightsandsounds.model.Rating;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * Dialog Fragment containing rating form.
 */
public class RatingDialogFragment extends DialogFragment {

    public static final String TAG = "RatingDialog";

    @BindView(R.id.restaurant_form_rating)
    MaterialRatingBar mRatingBar;

    @BindView(R.id.item_form_text)
    EditText mRatingText;

    @BindView(R.id.item_form_username)
    EditText usernameText;

    interface RatingListener {

        void onRating(Rating rating);

    }

    private RatingListener mRatingListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_rating, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof RatingListener) {
            mRatingListener = (RatingListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @OnClick(R.id.restaurant_form_button)
    public void onSubmitClicked(View view) {

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInAnonymously().addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
//                            updateUI(user);

                    Rating rating = new Rating(
                            user, usernameText.getText().toString(),
                            mRatingBar.getRating(),
                            mRatingText.getText().toString());

                    if (mRatingListener != null) {
                        mRatingListener.onRating(rating);
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
//                            updateUI(null);
                }
            }
        });

        dismiss();
    }

    @OnClick(R.id.restaurant_form_cancel)
    public void onCancelClicked(View view) {
        dismiss();
    }
}
