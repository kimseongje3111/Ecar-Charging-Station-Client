package com.example.ecar_service_station;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.ecar_service_station.domain.Car;
import com.example.ecar_service_station.dto.request.car.RegisterCarDto;
import com.example.ecar_service_station.dto.resoponse.common.CommonResponse;
import com.example.ecar_service_station.dto.resoponse.common.ListResultResponse;
import com.example.ecar_service_station.infra.app.PreferenceManager;
import com.example.ecar_service_station.infra.app.SnackBarManager;
import com.example.ecar_service_station.service.CarService;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CarRegistrationActivity extends AppCompatActivity {

    private static final int CAR_SERVICE_REGISTER_USER_CAR = -6;

    private EditText eTextCarModel, eTextCarModelYear, eTextCarType, eTextCarNumber;
    private Button btnCarRegister;

    private Toolbar toolbarCarRegistration;

    private CarService carService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_registration);

        // 로그인 토큰 저장
        saveLoginToken();

        // 화면 설정
        eTextCarModel = findViewById(R.id.editText_car_model);
        eTextCarModelYear = findViewById(R.id.editText_car_model_year);
        eTextCarType = findViewById(R.id.editText_car_type);
        eTextCarNumber = findViewById(R.id.editText_car_number);
        btnCarRegister = findViewById(R.id.btn_car_register);
        toolbarCarRegistration = findViewById(R.id.toolbar_car_registration);

        // 상단바 설정
        settingActionBar();

        btnCarRegister.setOnClickListener(v -> {
            String carModel = eTextCarModel.getText().toString();
            String carModelYear = eTextCarModelYear.getText().toString();
            String carType = eTextCarType.getText().toString();
            String carNumber = eTextCarNumber.getText().toString();

            String loginAccessToken = PreferenceManager.getString(CarRegistrationActivity.this, "LOGIN_ACCESS_TOKEN");
            RegisterCarDto registerCarDto = getRegisterCarDto(carModel, carModelYear, carType, carNumber);

            carService = new CarService(loginAccessToken, registerCarDto);

            try {
                CommonResponse commonResponse = carService.execute(CAR_SERVICE_REGISTER_USER_CAR).get();

                if (commonResponse.isSuccess()) {
                    finish();
                    startActivity(new Intent(CarRegistrationActivity.this, CarActivity.class));

                } else {
                    String registerCarFailedMsg = "등록할 차량의 입력 정보가 부족하거나 이미 등록된 차량입니다.";

                    SnackBarManager.showMessage(findViewById(R.id.frameLayout_user), registerCarFailedMsg);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.w("Car", "Loading user car list failed.");
            }
        });
    }

    private void saveLoginToken() {
        if (getIntent().hasExtra("LOGIN_ACCESS_TOKEN")) {
            String loginAccessToken = getIntent().getStringExtra("LOGIN_ACCESS_TOKEN");

            PreferenceManager.setString(CarRegistrationActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarCarRegistration);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private RegisterCarDto getRegisterCarDto(String model, String modelYear, String type, String number) {
        RegisterCarDto registerCarDto = new RegisterCarDto();
        registerCarDto.setCarModel(model);
        registerCarDto.setCarModelYear(modelYear);
        registerCarDto.setCarType(type);
        registerCarDto.setCarNumber(number);

        return registerCarDto;
    }
}