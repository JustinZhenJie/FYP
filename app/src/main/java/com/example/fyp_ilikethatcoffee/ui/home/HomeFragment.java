package com.example.fyp_ilikethatcoffee.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.fyp_ilikethatcoffee.Posts.PostFragment;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.profile.MyPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tabLayout = root.findViewById(R.id.tabs_home);
        viewPager = root.findViewById(R.id.view_pager_home);

        // Create an adapter that returns a fragment for each of the two primary sections
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        pagerAdapter.addFrag(new PostsFragment(), "Posts");
        pagerAdapter.addFrag(new FriendsReviewFragment(), "Friend's Review");
        viewPager.setAdapter(pagerAdapter);

        // Connect the TabLayout with the ViewPager
        tabLayout.setupWithViewPager(viewPager);


        return root;
    }
}
