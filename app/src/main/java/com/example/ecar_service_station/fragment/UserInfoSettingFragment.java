package com.example.ecar_service_station.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.ecar_service_station.R;
import com.example.ecar_service_station.service.UserBasicService;

public class UserInfoSettingFragment extends Fragment {

    private EditText eTextUserSettingName, eTextUserSettingPhoneNumber;
    private Button btnUserSettingName, btnUserSettingPhoneNumber;

    private String loginAccessToken;
    private UserBasicService userBasicService;

    public UserInfoSettingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            loginAccessToken = getArguments().getString("LOGIN_ACCESS_TOKEN");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info_setting, container, false);

        // 화면 설정
        eTextUserSettingName = view.findViewById(R.id.editText_user_setting_name);
        eTextUserSettingPhoneNumber = view.findViewById(R.id.editText_user_setting_phoneNumber);
        btnUserSettingName = view.findViewById(R.id.btn_user_setting_name);
        btnUserSettingPhoneNumber = view.findViewById(R.id.btn_user_setting_phoneNumber);

        // 화면 설정(1) : 사용자 이름 수정
        btnUserSettingName.setOnClickListener(v -> {

        });

        // 화면 설정(2) : 사용자 전화번호 수정
        btnUserSettingPhoneNumber.setOnClickListener(v -> {

        });

        return view;
    }


}
