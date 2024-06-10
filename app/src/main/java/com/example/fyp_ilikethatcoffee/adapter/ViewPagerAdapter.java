package com.example.fyp_ilikethatcoffee.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.fyp_ilikethatcoffee.profile.FavLocationFragment;
import com.example.fyp_ilikethatcoffee.profile.ReviewFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {


    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ReviewFragment();
            case 1:
                return new FavLocationFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2; // Number of tabs
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "My Past Reviews";
            case 1:
                return "Favorite";
            default:
                return null;
        }
    }
}
