package com.example.ecar_service_station;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ecar_service_station.domain.Charger;
import com.example.ecar_service_station.dto.request.search.SearchConditionDto;
import com.example.ecar_service_station.dto.request.search.SearchLocationDto;
import com.example.ecar_service_station.dto.resoponse.common.CommonResponse;
import com.example.ecar_service_station.dto.resoponse.common.ListResultResponse;
import com.example.ecar_service_station.infra.app.GpsTracker;
import com.example.ecar_service_station.infra.app.PreferenceManager;
import com.example.ecar_service_station.infra.app.SnackBarManager;
import com.example.ecar_service_station.service.SearchService;
import com.example.ecar_service_station.service.UserBasicService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_CODE = 3000;
    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;
    private static final int SEARCH_SERVICE_BY_TEXT = -15;
    private static final int SEARCH_SERVICE_BY_LOCATION = -16;

    private Toolbar toolBarMain;
    private DrawerLayout drawerLayoutMain;
    private NavigationView navigationMain;

    private EditText eTextSearch;
    private ImageView iViewSearch, iViewSpeaker, iViewGps;
    private LinearLayout layoutRecentAndBookmark, layoutReservationList;

    private Spinner spinnerCpType, spinnerChargerType;
    private int conditionCpType, conditionChargerType;

    private GoogleMap map;
    private SupportMapFragment mapFragment;

    private GpsTracker gpsTracker;
    private Location currentLocation;

    private UserBasicService userBasicService;
    private SearchService searchService;

    // ?????? ??????
    private final String[] requiredPermissions =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    // StartActivityForResult (STT)
    private final ActivityResultLauncher<Intent> startActivityResultForStt =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                            eTextSearch.setText(results.get(0));
                        }
                    }
            );

    // StartActivityForResult (?????? ????????? ??????)
    private final ActivityResultLauncher<Intent> startActivityResultForLocationServiceSetting =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            checkRuntimePermissions();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ????????? ?????? ??????
        saveLoginToken();

        // ?????? ??????
        toolBarMain = findViewById(R.id.toolbar_main);
        drawerLayoutMain = findViewById(R.id.drawer_main);
        navigationMain = findViewById(R.id.nav_main);
        eTextSearch = findViewById(R.id.editText_search);
        iViewSearch = findViewById(R.id.imageView_search);
        iViewSpeaker = findViewById(R.id.imageView_stt_speaker);
        iViewGps = findViewById(R.id.imageView_gps);
        layoutRecentAndBookmark = findViewById(R.id.layout_recent_search_and_bookmark);
        layoutReservationList = findViewById(R.id.layout_reservationList);
        spinnerCpType = findViewById(R.id.spinner_cpType);
        spinnerChargerType = findViewById(R.id.spinner_chargerType);

        // ?????????????????? ??? ????????? ?????? ??????
        settingDrawer();
        settingCustomViews();

        // ?????? ??????(1) : ?????? ?????? (STT)
        iViewSpeaker.setOnClickListener(v -> new Thread(this::getVoice).start());

        // ?????? ??????(2) : ????????? ????????? ??????(??????/????????????)
        iViewSearch.setOnClickListener(v -> {
            String search = eTextSearch.getText().toString();

            if (search.isEmpty()) {
                String searchEmptyMsg = "???????????? ????????? ?????????.";

                SnackBarManager.showMessage(v, searchEmptyMsg);

            } else if (currentLocation == null) {
                String currentLocationNullMsg = "?????? ?????? ????????? ????????? ??? ????????????.";

                SnackBarManager.showMessage(v, currentLocationNullMsg);

            } else {
                // ?????? ?????? ?????? ??????
                SearchConditionDto searchConditionDto = getSearchConditionDto(search);
                String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

                // ?????? ????????? ??????
                searchService = new SearchService(loginAccessToken, searchConditionDto);

                // ????????? ??????
                try {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("Search", search);
                    intent.putExtra("ConditionCpType", conditionCpType);
                    intent.putExtra("ConditionChargerType", conditionChargerType);

                    CommonResponse commonResponse = searchService.execute(SEARCH_SERVICE_BY_TEXT).get();

                    if (commonResponse.isSuccess()) {
                        ListResultResponse<Charger> listResultResponse = (ListResultResponse<Charger>) commonResponse;

                        intent.putExtra("SearchResults", (Serializable) listResultResponse.getDataList());

                    } else {
                        intent.putExtra("ErrorCode", commonResponse.getResponseCode());
                    }

                    startActivity(intent);

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Main", "Search failed.");
                }
            }
        });

        // ?????? ??????(3) : ????????? ????????? ??????(?????? ?????? ??????)
        iViewGps.setOnClickListener(v -> {
            if (currentLocation == null) {
                String currentLocationNullMsg = "?????? ?????? ????????? ????????? ??? ????????????.";

                SnackBarManager.showMessage(v, currentLocationNullMsg);

            } else {
                // ?????? ?????? ?????? ??????
                SearchLocationDto searchLocationDto = getSearchLocationDto();
                String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

                // ?????? ????????? ??????
                searchService = new SearchService(loginAccessToken, searchLocationDto);

                // ????????? ??????
                try {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("ConditionCpType", conditionCpType);
                    intent.putExtra("ConditionChargerType", conditionChargerType);

                    CommonResponse commonResponse = searchService.execute(SEARCH_SERVICE_BY_LOCATION).get();

                    if (commonResponse.isSuccess()) {
                        ListResultResponse<Charger> listResultResponse = (ListResultResponse<Charger>) commonResponse;

                        intent.putExtra("SearchResults", (Serializable) listResultResponse.getDataList());

                    } else {
                        intent.putExtra("ErrorCode", commonResponse.getResponseCode());
                    }

                    startActivity(intent);

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Main", "Search failed.");
                }
            }
        });

        // ?????? ??????(4) : ?????? ??????/????????????
        layoutRecentAndBookmark.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(MainActivity.this, HistoryAndBookmarkActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("REQUEST_POSITION", 0);

            startActivity(intent);
        });

        // ?????? ??????(6) : ?????? ??????
        layoutReservationList.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(MainActivity.this, ReservationStatementActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("REQUEST_POSITION", 0);

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginUserInfo();

        if (conditionCpType != 0 || conditionChargerType != 0) {
            settingCustomViews();
        }
    }

    // ?????? ????????? ??? ?????? ?????? ??????(Marker) ??????
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();

        } else {
            checkRuntimePermissions();
        }

        gpsTracker = new GpsTracker(MainActivity.this);
        currentLocation = gpsTracker.getLocation();

        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            map.addMarker(new MarkerOptions().position(latLng).title("?????????"));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == requiredPermissions.length) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    String permissionSettingMsg = "?????? ?????? ????????? ?????????????????????.\n????????????????????? ?????? ??????????????? ???????????? ????????? ???????????? ?????????.";

                    SnackBarManager.showMessage(findViewById(R.id.layout_main), permissionSettingMsg);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            TextView textNavName = findViewById(R.id.textView_nav_name);
            TextView textNavEmail = findViewById(R.id.textView_nav_email);
            TextView textNavCash = findViewById(R.id.textView_nav_cash);
            TextView textNavCashPoint = findViewById(R.id.textView_nav_cash_point);
            Button btnCash = findViewById(R.id.btn_nav_cash);

            String userName = PreferenceManager.getString(MainActivity.this, "USER_NAME");
            String userEmail = PreferenceManager.getString(MainActivity.this, "USER_EMAIL");
            int userCash = PreferenceManager.getInt(MainActivity.this, "USER_CASH");
            int userCashPoint = PreferenceManager.getInt(MainActivity.this, "USER_CASH_POINT");

            textNavName.setText(userName);
            textNavEmail.setText(userEmail);
            textNavCash.setText(String.valueOf(userCash));
            textNavCashPoint.setText(String.valueOf(userCashPoint));

            btnCash.setOnClickListener(v -> {
                String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

                Intent intent = new Intent(MainActivity.this, CashActivity.class);
                intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                startActivity(intent);
            });

            drawerLayoutMain.openDrawer(GravityCompat.START);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayoutMain.isDrawerOpen(GravityCompat.START)) {
            drawerLayoutMain.closeDrawers();
        }
    }

    private void saveLoginToken() {
        if (getIntent().hasExtra("LOGIN_ACCESS_TOKEN")) {
            String loginAccessToken = getIntent().getStringExtra("LOGIN_ACCESS_TOKEN");

            PreferenceManager.setString(MainActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }
    }

    private void updateLoginUserInfo() {
        String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

        userBasicService = new UserBasicService(loginAccessToken, MainActivity.this);
        userBasicService.execute(USER_BASIC_SERVICE_GET_USER_INFO);
    }

    private void settingDrawer() {
        setSupportActionBar(toolBarMain);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_bars_solid);

        navigationMain.setNavigationItemSelectedListener(item -> {
            drawerLayoutMain.closeDrawers();

            Intent intent;
            String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

            switch (item.getItemId()) {
                case R.id.menu_user: {
                    intent = new Intent(MainActivity.this, UserActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                    startActivity(intent);
                    break;
                }
                case R.id.menu_reservation: {
                    intent = new Intent(MainActivity.this, ReservationStatementActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("REQUEST_POSITION", 0);

                    startActivity(intent);
                    break;

                }
                case R.id.menu_bookmark: {
                    intent = new Intent(MainActivity.this, HistoryAndBookmarkActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("REQUEST_POSITION", 1);

                    startActivity(intent);
                    break;
                }
                case R.id.menu_car: {
                    intent = new Intent(MainActivity.this, CarActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                    startActivity(intent);
                    break;
                }
                case R.id.menu_account: {
                    intent = new Intent(MainActivity.this, BankActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                    startActivity(intent);
                    break;
                }
                case R.id.menu_notification: {
                    intent = new Intent(MainActivity.this, UserSettingActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("REQUEST_POSITION", 2);

                    startActivity(intent);
                }
            }

            return true;
        });
    }

    private void settingCustomViews() {
        // ?????? ??????(1) : ?????? ??????
        conditionCpType = 0;

        ArrayAdapter<CharSequence> adapterCpType =
                ArrayAdapter.createFromResource(MainActivity.this, R.array.custom_array_cpType, android.R.layout.simple_spinner_item);

        adapterCpType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCpType.setAdapter(adapterCpType);
        spinnerCpType.setFocusable(true);
        spinnerCpType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                conditionCpType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // ?????? ??????(2) : ????????? ??????
        conditionChargerType = 0;

        ArrayAdapter<CharSequence> adapterChargerType =
                ArrayAdapter.createFromResource(MainActivity.this, R.array.custom_array_chargerType, android.R.layout.simple_spinner_item);


        adapterChargerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerChargerType.setAdapter(adapterChargerType);
        spinnerChargerType.setFocusable(true);
        spinnerChargerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                conditionChargerType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // ?????????
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
    }

    // ?????? ????????? ?????? ??????
    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        alertDialogBuilder
                .setTitle("?????? ????????? ????????????")
                .setMessage("??? ?????????????????? ????????? ?????? ?????? ???????????? ???????????????.")
                .setCancelable(true)
                .setPositiveButton("??????", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);

                    startActivityResultForLocationServiceSetting.launch(intent);
                })
                .setNegativeButton("??????", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    // ?????? ??????
    private void checkRuntimePermissions() {
        int fineLocationPermission
                = ContextCompat.checkSelfPermission(MainActivity.this, requiredPermissions[0]);

        int coarseLocationPermission
                = ContextCompat.checkSelfPermission(MainActivity.this, requiredPermissions[1]);

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED
                && coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, requiredPermissions[0])
                || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, requiredPermissions[1])) {

            String permissionSettingMsg = "??? ????????????????????? ??????????????? ?????? ?????? ????????? ???????????????.";

            SnackBarManager.showMessage(findViewById(R.id.layout_main), permissionSettingMsg);
        }

        ActivityCompat.requestPermissions(MainActivity.this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
    }

    // STT
    private void getVoice() {
        Intent intent = new Intent();
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        intent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        startActivityResultForStt.launch(intent);
    }

    private SearchConditionDto getSearchConditionDto(String search) {
        SearchConditionDto searchConditionDto = new SearchConditionDto();
        searchConditionDto.setSearch(search);
        searchConditionDto.setLatitude(currentLocation.getLatitude());
        searchConditionDto.setLongitude(currentLocation.getLongitude());

        if (conditionCpType != 0) {
            searchConditionDto.setCpTp(conditionCpType);
        }

        if (conditionChargerType != 0) {
            searchConditionDto.setChargerTp(conditionChargerType);
        }

        return searchConditionDto;
    }

    private SearchLocationDto getSearchLocationDto() {
        SearchLocationDto searchLocationDto = new SearchLocationDto();
        searchLocationDto.setLatitude(currentLocation.getLatitude());
        searchLocationDto.setLongitude(currentLocation.getLongitude());

        if (conditionCpType != 0) {
            searchLocationDto.setCpTp(conditionCpType);
        }

        if (conditionChargerType != 0) {
            searchLocationDto.setChargerTp(conditionChargerType);
        }

        return searchLocationDto;
    }
}