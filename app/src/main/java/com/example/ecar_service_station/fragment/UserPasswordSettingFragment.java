package com.example.ecar_service_station.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class UserPasswordSettingFragment extends Fragment {

    private String loginAccessToken;

    public UserPasswordSettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            loginAccessToken = getArguments().getString("LOGIN_ACCESS_TOKEN");
        }
    }
}
