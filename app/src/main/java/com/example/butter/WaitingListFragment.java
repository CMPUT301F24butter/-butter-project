package com.example.butter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.butter.EventsFragment;

/**
 * WaitingListFragment for viewing the waiting list
 * Simply gets and returns the view
 * Is called to from {@link ViewPagerAdapter}
 * @author Arsalan
 */
public class WaitingListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waitinglist, container, false);
        ListView waitinglistList = view.findViewById(R.id.waitinglistList);

        // Set up your ListView adapter and data here

        return view;
    }
}