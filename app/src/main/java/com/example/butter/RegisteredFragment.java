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
 * Registered fragment to check if we are in the registered list.
 * Simply gets and returns the view
 * Is called to from {@link ViewPagerAdapter}
 * @author Arsalan
 */
public class RegisteredFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registered, container, false);
        ListView registeredList = view.findViewById(R.id.registeredList);

        // Set up your ListView adapter and data here

        return view;
    }
}
