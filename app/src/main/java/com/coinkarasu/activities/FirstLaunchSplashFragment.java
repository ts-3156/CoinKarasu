package com.coinkarasu.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;
import com.coinkarasu.utils.CKLog;

public class FirstLaunchSplashFragment extends Fragment {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "FirstLaunchSplashFragment";

    public FirstLaunchSplashFragment() {
    }

    public static FirstLaunchSplashFragment newInstance() {
        return new FirstLaunchSplashFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first_launch_splash, container, false);

        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    ((FirstLaunchActivity) getActivity()).goToNext(FirstLaunchSplashFragment.this);
                }
            }
        });
        return view;
    }
}
