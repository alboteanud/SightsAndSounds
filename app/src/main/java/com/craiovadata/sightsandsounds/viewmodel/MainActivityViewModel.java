package com.craiovadata.sightsandsounds.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.craiovadata.sightsandsounds.Filters;

/**
 * ViewModel for {@link .MainActivity}.
 */

public class MainActivityViewModel extends ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;

    public MainActivityViewModel() {
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    public Filters getFilters() {
        return mFilters;
    }

    public void setFilters(Filters mFilters) {
        this.mFilters = mFilters;
    }
}
