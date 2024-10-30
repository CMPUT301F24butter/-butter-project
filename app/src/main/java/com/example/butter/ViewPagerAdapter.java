package com.example.butter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.butter.RegisteredFragment;
import com.example.butter.WaitingListFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RegisteredFragment();
            case 1:
                return new WaitingListFragment();
            default:
                return new RegisteredFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs
    }
}