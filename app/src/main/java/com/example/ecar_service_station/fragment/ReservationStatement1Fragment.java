package com.example.ecar_service_station.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.ecar_service_station.R;
import com.example.ecar_service_station.dto.resoponse.common.CommonResponse;
import com.example.ecar_service_station.dto.resoponse.common.ListResultResponse;
import com.example.ecar_service_station.domain.ReservationStatement;
import com.example.ecar_service_station.infra.app.SnackBarManager;
import com.example.ecar_service_station.service.UserMainService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ReservationStatement1Fragment extends Fragment {

    private static final long USER_MAIN_SERVICE_GET_RESERVATION_STATEMENTS = -25;

    private final String[] reservationStates = {"STAND_BY", "PAYMENT", "CHARGING"};

    private Context currentContext;
    private View currentView;

    private TextView textStandByNotFound, textPaymentNotFound, textChargingNotFound;
    private ListView listViewStandBy, listViewPayment, listViewCharging;

    private UserMainService userMainService;

    private String loginAccessToken;
    private List<ReservationStatement> standByStatementList;
    private List<ReservationStatement> paymentStatementList;
    private List<ReservationStatement> chargingStatementList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentContext = context;

        if (getArguments() != null) {
            loginAccessToken = getArguments().getString("LOGIN_ACCESS_TOKEN");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_reservation_statement1, container, false);

        textStandByNotFound = currentView.findViewById(R.id.textView_reservation_statement_stand_by_notFound);
        textPaymentNotFound = currentView.findViewById(R.id.textView_reservation_statement_payment_notFound);
        textChargingNotFound = currentView.findViewById(R.id.textView_reservation_statement_charging_notFound);
        listViewStandBy = currentView.findViewById(R.id.listView_reservation_statement_stand_by);
        listViewPayment = currentView.findViewById(R.id.listView_reservation_statement_payment);
        listViewCharging = currentView.findViewById(R.id.listView_reservation_statement_charging);

        standByStatementList = new ArrayList<>();
        paymentStatementList = new ArrayList<>();
        chargingStatementList = new ArrayList<>();

        return currentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserReservationStatements();
        showReservationStatements();
    }

    private void loadUserReservationStatements() {
        userMainService = new UserMainService(loginAccessToken);

        try {
            CommonResponse commonResponse = userMainService.execute(USER_MAIN_SERVICE_GET_RESERVATION_STATEMENTS, 0L).get();

            if (commonResponse.isSuccess()) {
                ListResultResponse<ReservationStatement> listResultResponse = (ListResultResponse<ReservationStatement>) commonResponse;
                List<ReservationStatement> statements = listResultResponse.getDataList();

                statements.stream()
                        .collect(Collectors.groupingBy(ReservationStatement::getState))
                        .forEach((state, reservationStatements) -> {
                            if (state.equals(reservationStates[0])) {           // 예약 확정 대기
                                standByStatementList = reservationStatements;

                            } else if (state.equals(reservationStates[1])) {    // 예약 확정
                                paymentStatementList = reservationStatements;

                            } else if (state.equals(reservationStates[2])) {    // 충전중
                                chargingStatementList = reservationStatements;
                            }
                        });

            } else {
                String loadUserReservationStatementsFailedMsg = "예약/충전 목록을 불러올 수 없습니다.";

                SnackBarManager.showMessage(currentView, loadUserReservationStatementsFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Reservation Statement", "Loading user reservation statements failed.");
        }
    }

    private void showReservationStatements() {
        if (standByStatementList.size() == 0) {
            listViewStandBy.setVisibility(View.GONE);
            textStandByNotFound.setVisibility(View.VISIBLE);

        } else {
            textStandByNotFound.setVisibility(View.GONE);
            listViewStandBy.setVisibility(View.VISIBLE);

            listViewStandBy.setAdapter(new CustomStandByStatementList((Activity) currentContext, standByStatementList));
        }

        if (paymentStatementList.size() == 0) {
            listViewPayment.setVisibility(View.GONE);
            textPaymentNotFound.setVisibility(View.VISIBLE);

        } else {
            textPaymentNotFound.setVisibility(View.GONE);
            listViewPayment.setVisibility(View.VISIBLE);

            listViewPayment.setAdapter(new CustomPaymentStatementList((Activity) currentContext, paymentStatementList));
        }

        if (chargingStatementList.size() == 0) {
            listViewCharging.setVisibility(View.GONE);
            textChargingNotFound.setVisibility(View.VISIBLE);

        } else {
            textChargingNotFound.setVisibility(View.GONE);
            listViewCharging.setVisibility(View.VISIBLE);

            listViewCharging.setAdapter(new CustomChargingStatementList((Activity) currentContext, chargingStatementList));
        }
    }

    private class CustomStandByStatementList extends ArrayAdapter<ReservationStatement> {

        private final Activity context;
        private final List<ReservationStatement> standByStatementList;

        public CustomStandByStatementList(Activity context, List<ReservationStatement> standByStatementList) {
            super(context, R.layout.listview_reservation_statement_stand_by, standByStatementList);
            this.context = context;
            this.standByStatementList = standByStatementList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_reservation_statement_stand_by, null, true);

            return rowView;
        }
    }

    private class CustomPaymentStatementList extends ArrayAdapter<ReservationStatement> {

        private final Activity context;
        private final List<ReservationStatement> paymentStatementList;

        public CustomPaymentStatementList(Activity context, List<ReservationStatement> paymentStatementList) {
            super(context, R.layout.listview_reservation_statement_payment, paymentStatementList);
            this.context = context;
            this.paymentStatementList = paymentStatementList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_reservation_statement_payment, null, true);


            return rowView;
        }
    }

    private class CustomChargingStatementList extends ArrayAdapter<ReservationStatement> {

        private final Activity context;
        private final List<ReservationStatement> chargingStatementList;

        public CustomChargingStatementList(Activity context, List<ReservationStatement> chargingStatementList) {
            super(context, R.layout.listview_reservation_statement_charging, chargingStatementList);
            this.context = context;
            this.chargingStatementList = chargingStatementList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_reservation_statement_charging, null, true);


            return rowView;
        }
    }
}
