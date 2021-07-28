package com.example.ecar_service_station.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ecar_service_station.fragment.UserInfoSettingFragment;
import com.example.ecar_service_station.fragment.UserNotificationSettingFragment;
import com.example.ecar_service_station.fragment.UserPasswordSettingFragment;

import java.util.ArrayList;
import java.util.List;

public class UserSettingFragmentAdapter extends FragmentStateAdapter {

    private List<Fragment> fragmentItems;

    public UserSettingFragmentAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);

        fragmentItems = new ArrayList<>();
    }

    @Override
    public Fragment createFragment(int position) {
        return fragmentItems.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentItems.size();
    }

    public void addFragment(Fragment fragment) {
        fragmentItems.add(fragment);
    }
}
