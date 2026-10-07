package com.agribird.hrmsapp.ui.Attendance;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.AttendanceApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.AttendanceAdapter;
import com.agribird.hrmsapp.api.AttendanceApi;
import com.agribird.hrmsapp.dao.BreakDao;
import com.agribird.hrmsapp.databasecon.HRMSDatabase;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.agribird.hrmsapp.repository.AttendanceRepository;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModel;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModelFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceFragment extends Fragment {

    private RecyclerView recyclerViewAttendance;
    private AttendanceAdapter attendanceAdapter;

    private HRMSDatabase database;

    private SharedPreferences sharedPreferences;

    private String employeeId;
    private String employeeName;

    private String currentDate;
    private String currentTime;

    private AttendanceViewModel attendanceViewModel;

    private final Handler handler = new Handler();

    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQ_CODE = 1001;
    private static final int SELFIE_REQUEST_CODE = 1001;

    private String currentPhotoPath = "";

    private static final double OFFICE_LATITUDE = 21.156025;
    private static final double OFFICE_LONGITUDE = 74.430331;

    private static final float OFFICE_RADIUS_METERS = 1000.0f;

    private static final String OFFICE_WIFI_1 = "AGRIBIRD_2.4G";
    private static final String OFFICE_WIFI_2 = "AGRIBIRD_5G";

    // ---------------------------------------------------------
    // SECURITY STATE
    // ---------------------------------------------------------

    private boolean locationValidated = false;
    private boolean insideOfficeRadius = false;
    private boolean securityCheckRunning = false;

    // ---------------------------------------------------------
    // ACTION STATE
    // ---------------------------------------------------------

    private boolean checkInRequestRunning = false;
    private boolean checkOutRequestRunning = false;

    // Prevent selfie callback from creating duplicate check-in
    private boolean selfieCheckInHandled = false;

    // ---------------------------------------------------------
    // VIEW LIFECYCLE
    // ---------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_attendance,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // -----------------------------------------------------
        // API / VIEWMODEL
        // -----------------------------------------------------

        AttendanceApi attendanceApi =
                RetrofitClient.getAttendanceApi(requireContext());

        AttendanceRepository attendanceRepository =
                new AttendanceRepository(attendanceApi);

        AttendanceViewModelFactory factory =
                new AttendanceViewModelFactory(attendanceRepository);

        attendanceViewModel =
                new ViewModelProvider(this, factory)
                        .get(AttendanceViewModel.class);

        // -----------------------------------------------------
        // RECYCLER VIEW
        // -----------------------------------------------------

        recyclerViewAttendance =
                view.findViewById(R.id.recyclerView);

        recyclerViewAttendance.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        recyclerViewAttendance.setNestedScrollingEnabled(true);
        recyclerViewAttendance.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        currentDate =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                ).format(new Date());

        // -----------------------------------------------------
        // ADAPTER
        // -----------------------------------------------------

        attendanceAdapter =
                new AttendanceAdapter(
                        new AttendanceAdapter.OnAttendanceActionListener() {

                            @Override
                            public void onCheckInClicked() {
                                processCheckIn();
                            }

                            @Override
                            public void onCheckOutClicked() {
                                showCheckOutConfirmation();
                            }
                        }
                );

        recyclerViewAttendance.setAdapter(
                attendanceAdapter
        );

        // -----------------------------------------------------
        // DATABASE
        // Only used for break statistics
        // -----------------------------------------------------

        database =
                HRMSDatabase.getInstance(
                        requireContext()
                );

        // -----------------------------------------------------
        // SESSION
        // -----------------------------------------------------

        sharedPreferences =
                requireContext().getSharedPreferences(
                        "HRMS_SESSION",
                        Context.MODE_PRIVATE
                );

        employeeId =
                sharedPreferences.getString(
                        "userId",
                        ""
                );

        employeeName =
                sharedPreferences.getString(
                        "userName",
                        "Employee"
                );

        // -----------------------------------------------------
        // SUPER ADMIN
        // -----------------------------------------------------

        if ("SUPER_ADMIN_ID".equals(employeeId)) {

            Toast.makeText(
                    requireContext(),
                    "Administrators are exempt from attendance tracking.",
                    Toast.LENGTH_LONG
            ).show();

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();

            return;
        }

        // -----------------------------------------------------
        // LOCATION
        // -----------------------------------------------------

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(
                        requireContext()
                );

        // -----------------------------------------------------
        // DEFAULT UI
        // -----------------------------------------------------

        attendanceAdapter.setEmployeeName(
                employeeName
        );

        attendanceAdapter.setTodayStatus(
                "● Checking Security..."
        );

        attendanceAdapter.setCurrentTime(
                "--:--"
        );

        attendanceAdapter.setCurrentDate(
                "--"
        );

        attendanceAdapter.setTotalLoggedHours(
                "00h 00m"
        );

        attendanceAdapter.setOnlineTime(
                "00h 00m"
        );

        attendanceAdapter.setBreakTime(
                "0m"
        );

        attendanceAdapter.setYesterdayStatus(
                "N/A"
        );

        attendanceAdapter.setCheckInEnabled(false);
        attendanceAdapter.setCheckOutEnabled(false);

        // -----------------------------------------------------
        // START
        // -----------------------------------------------------

        startDateAndTime();

        // Only ONE security check at startup
        runSecurityAndLocationCheck();

        // Only ONE attendance load
        loadAttendanceData();

        // Quick stats are loaded separately
        loadQuickStats();
    }

    // =========================================================
    // DATE / TIME
    // =========================================================

    private void startDateAndTime() {

        handler.post(new Runnable() {

            @Override
            public void run() {

                if (!isAdded()) {
                    return;
                }

                Date now = new Date();

                currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now);

                String displayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now);
                currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now);
                if (attendanceAdapter != null) {

                    attendanceAdapter.setCurrentDate(displayDate);

                    attendanceAdapter.setCurrentTime(currentTime);
                }

                handler.postDelayed(this, 1000
                );
            }
        });
    }

    // =========================================================
    // SECURITY
    // =========================================================

    private void runSecurityAndLocationCheck() {

        if (securityCheckRunning) {
            return;
        }

        securityCheckRunning = true;

        checkLocationPermissionAndValidate();
    }

    private boolean isConnectedToOfficeWifi() {

        if (!isAdded()) {
            return false;
        }

        try {

            Context context = requireContext();

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                return false;
            }

            Network network = connectivityManager.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

            if (capabilities == null || !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                return false;
            }

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager == null) {
                return false;
            }

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo == null) {
                return false;
            }

            String currentWifiSSID = wifiInfo.getSSID();

            if (currentWifiSSID == null) {
                return false;
            }

            if (currentWifiSSID.startsWith("\"") && currentWifiSSID.endsWith("\"")) {

                currentWifiSSID = currentWifiSSID.substring(1, currentWifiSSID.length() - 1);
            }

            return OFFICE_WIFI_1.equalsIgnoreCase(currentWifiSSID
            ) || OFFICE_WIFI_2.equalsIgnoreCase(currentWifiSSID);

        } catch (Exception e) {

            Log.e("ATTENDANCE_SECURITY", "WiFi check error", e);

            return false;
        }
    }

    private void checkLocationPermissionAndValidate() {

        if (!isAdded()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQ_CODE);

        } else {

            getCurrentLocationAndCheckRadius();
        }
    }

    private void getCurrentLocationAndCheckRadius() {

        if (!isAdded() || fusedLocationClient == null) {

            return;
        }

        try {

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                            null).addOnSuccessListener(requireActivity(), location -> {

                                securityCheckRunning = false;

                                if (!isAdded() || getContext() == null || attendanceAdapter == null) {

                                    return;
                                }

                                if (location == null) {

                                    locationValidated = false;
                                    insideOfficeRadius = false;
                                    attendanceAdapter.setTodayStatus("● GPS Location Error");
                                    attendanceAdapter.setCheckInEnabled(false);
                                    attendanceAdapter.setCheckOutEnabled(false);
                                    Toast.makeText(requireContext(), "Unable to get GPS location. Please turn on Location/GPS!", Toast.LENGTH_LONG).show();

                                    return;
                                }


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && location.isFromMockProvider()) {

                                    locationValidated = false;
                                    insideOfficeRadius = false;
                                    attendanceAdapter.setTodayStatus("● Fake GPS Detected! 🚨");
                                    attendanceAdapter.setCheckInEnabled(false);
                                    attendanceAdapter.setCheckOutEnabled(false);

                                    Toast.makeText(requireContext(), "Fake GPS App detected. Attendance Blocked!", Toast.LENGTH_LONG).show();

                                    return;
                                }

                                Location officeLocation = new Location("Office");

                                officeLocation.setLatitude(OFFICE_LATITUDE);
                                officeLocation.setLongitude(OFFICE_LONGITUDE);

                                float distanceInMeters = location.distanceTo(officeLocation);

                                locationValidated = true;
                                insideOfficeRadius = distanceInMeters <= OFFICE_RADIUS_METERS;


                                if (insideOfficeRadius) {
                                    checkTodayStatus(true
                                    );
                                    Toast.makeText(requireContext(), "You are in office premises. 🟢", Toast.LENGTH_SHORT).show();

                                } else {

                                    checkTodayStatus(false);

                                    attendanceAdapter.setTodayStatus(String.format(Locale.getDefault(), "● Out of Office Radius (%.1fm away) ❌", distanceInMeters));

                                    Toast.makeText(requireContext(), "You are outside office premises.", Toast.LENGTH_SHORT).show();
                                }
                            }
                    )
                    .addOnFailureListener(requireActivity(), e -> {

                                securityCheckRunning = false;

                                if (!isAdded()) {
                                    return;
                                }

                                locationValidated = false;
                                insideOfficeRadius = false;
                                attendanceAdapter.setTodayStatus("● GPS Error ❌");
                                attendanceAdapter.setCheckInEnabled(false);
                                attendanceAdapter.setCheckOutEnabled(false);

                                Toast.makeText(requireContext(), "Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    );

        } catch (SecurityException e) {

            securityCheckRunning = false;

            Log.e("ATTENDANCE_SECURITY", "Location permission error", e);

            if (!isAdded()) {
                return;
            }

            locationValidated = false;
            insideOfficeRadius = false;
            attendanceAdapter.setTodayStatus("● Location Permission Required ❌");
            attendanceAdapter.setCheckInEnabled(false);
            attendanceAdapter.setCheckOutEnabled(false);

            Toast.makeText(requireContext(), "Location permission is required!", Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQ_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                securityCheckRunning = false;

                getCurrentLocationAndCheckRadius();

            } else {

                securityCheckRunning = false;
                locationValidated = false;
                insideOfficeRadius = false;

                if (!isAdded()) {
                    return;
                }

                attendanceAdapter.setTodayStatus("● Location Permission Required");
                attendanceAdapter.setCheckInEnabled(false);
                attendanceAdapter.setCheckOutEnabled(false);

                Toast.makeText(requireContext(), "Location permission is compulsory for Attendance!", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void checkTodayStatus(
            boolean isInRadius) {

        if (!isAdded() || attendanceViewModel == null || employeeId == null || employeeId.trim().isEmpty()) {

            return;
        }

        long employeeDbId;

        try {
            employeeDbId = Long.parseLong(employeeId.trim());

        } catch (NumberFormatException e) {
            Log.e("ATTENDANCE_DEBUG", "Invalid employeeId = " + employeeId, e);

            return;
        }

        attendanceViewModel.getEmployeeAttendance(employeeDbId).observe(getViewLifecycleOwner(), state -> {

                            if (state == null || !isAdded() || attendanceAdapter == null) {

                                return;
                            }

                            if (state.isLoading()) {
                                attendanceAdapter.setTodayStatus("● Checking Attendance...");

                                return;
                            }

                            if (state.isError()) {
                                attendanceAdapter.setCheckInEnabled(false);
                                attendanceAdapter.setCheckOutEnabled(false);
                                attendanceAdapter.setTodayStatus("● Attendance Load Error");

                                return;
                            }

                            if (!state.isSuccess()) {
                                return;
                            }
                            List<AttendanceApiModel> list = state.getData();

                            AttendanceApiModel existing = findTodayAttendance(list);
                            updateAttendanceUI(existing, isInRadius);
                        }
                );
    }

    private AttendanceApiModel findTodayAttendance(
            List<AttendanceApiModel> list) {

        if (list == null || list.isEmpty()) {

            return null;
        }

        for (AttendanceApiModel attendance : list) {

            if (attendance == null) {
                continue;
            }

            String backendDate = attendance.getDate();

            if (backendDate == null) {
                continue;
            }

            if (currentDate != null && currentDate.equals(backendDate.trim())) {

                return attendance;
            }
        }

        return null;
    }


    private void updateAttendanceUI(AttendanceApiModel attendance, boolean isInRadius) {

        if (!isAdded() || attendanceAdapter == null) {
            return;
        }
        if (attendance == null) {

            attendanceAdapter.setCheckInEnabled(isInRadius);
            attendanceAdapter.setCheckOutEnabled(false);
            attendanceAdapter.setTotalLoggedHours("00h 00m");

            if (isInRadius) {
                attendanceAdapter.setTodayStatus("● Not Checked In");
            } else {
                attendanceAdapter.setTodayStatus("● Office Location Required");
            }

            return;
        }
        String inTime = attendance.getCheckInTime() != null ? attendance.getCheckInTime() : "--:--";
        String outTime = attendance.getCheckOutTime() != null ? attendance.getCheckOutTime() : "--:--";
        String status = attendance.getStatus() != null ? attendance.getStatus() : "Present";
        boolean checkedOut = !outTime.equals("--:--") && !outTime.trim().isEmpty();


        if (checkedOut) {

            attendanceAdapter.setCheckInEnabled(false);
            attendanceAdapter.setCheckOutEnabled(false);
            String totalHours = attendance.getTotalHours() != null ? attendance.getTotalHours() : calculateTotalHours(inTime, outTime);
            attendanceAdapter.setTotalLoggedHours(totalHours);

            if ("Late".equalsIgnoreCase(status)) {
                attendanceAdapter.setTodayStatus("● Late (In: " + inTime + " | Out: " + outTime + ")");

            } else {
                attendanceAdapter.setTodayStatus("● Completed (In: " + inTime + " | Out: " + outTime + ")");
            }
            return;
        }

        attendanceAdapter.setCheckInEnabled(false);
        attendanceAdapter.setCheckOutEnabled(true);
        String liveHours;

        if (currentTime != null && !currentTime.trim().isEmpty()) {
            liveHours = calculateTotalHours(inTime, currentTime);

        } else {
            liveHours = "00h 00m";
        }
        attendanceAdapter.setTotalLoggedHours(liveHours);

        if ("Late".equalsIgnoreCase(status)) {
            attendanceAdapter.setTodayStatus("● Late (In: " + inTime + ")");
        } else {
            attendanceAdapter.setTodayStatus("● Present (In: " + inTime + ")");
        }
    }

    private void processCheckIn() {

        // Prevent duplicate click
        if (checkInRequestRunning) {
            return;
        }

        // Employee validation
        if (employeeId == null || employeeId.trim().isEmpty()) {

            Toast.makeText(requireContext(), "Employee ID not found. Please login again.", Toast.LENGTH_LONG).show();

            return;
        }

        // Current time validation
        if (currentTime == null || currentTime.trim().isEmpty()) {

            Toast.makeText(requireContext(), "Please wait a second...", Toast.LENGTH_SHORT).show();
            return;
        }


        checkInRequestRunning = true;
        attendanceAdapter.setCheckInEnabled(false);
        attendanceAdapter.setCheckOutEnabled(false);
        attendanceAdapter.setTodayStatus("● Re-verifying Location... ⏳");

        try {

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(requireActivity(), location -> {

                                if (!isAdded() || getContext() == null) {

                                    checkInRequestRunning = false;
                                    return;
                                }

                                if (location == null) {

                                    checkInRequestRunning = false;
                                    attendanceAdapter.setTodayStatus("● GPS Location Error ❌");
                                    attendanceAdapter.setCheckInEnabled(false);

                                    Toast.makeText(requireContext(), "Unable to get current location!", Toast.LENGTH_LONG).show();

                                    return;
                                }


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && location.isFromMockProvider()) {

                                    checkInRequestRunning = false;
                                    locationValidated = false;
                                    insideOfficeRadius = false;
                                    attendanceAdapter.setTodayStatus("● Fake GPS Detected! 🚨");
                                    attendanceAdapter.setCheckInEnabled(false);
                                    Toast.makeText(requireContext(), "Fake GPS detected. Attendance blocked!", Toast.LENGTH_LONG).show();

                                    return;
                                }


                                double latitude = location.getLatitude();

                                double longitude = location.getLongitude();

                                Location officeLocation = new Location("Office");
                                officeLocation.setLatitude(OFFICE_LATITUDE);
                                officeLocation.setLongitude(OFFICE_LONGITUDE);

                                float distanceInMeters = location.distanceTo(officeLocation);

                                boolean isInRadius = distanceInMeters <= OFFICE_RADIUS_METERS;


                                String wifiSsid = getCurrentWifiSsid();

                                boolean isWifiMatched = isConnectedToOfficeWifi();

                                if (isInRadius || isWifiMatched) {

                                    locationValidated = true;
                                    insideOfficeRadius = isInRadius;

                                    saveAttendanceToDatabase(
                                            latitude,
                                            longitude,
                                            wifiSsid);

                                } else {

                                    checkInRequestRunning = false;

                                    locationValidated = true;
                                    insideOfficeRadius = false;
                                    attendanceAdapter.setTodayStatus(String.format(Locale.getDefault(), "● Authorization Failed! (%.1fm away) ❌", distanceInMeters));
                                    attendanceAdapter.setCheckInEnabled(false);
                                    Toast.makeText(requireContext(), "You are outside the office and not connected to office Wi-Fi!", Toast.LENGTH_LONG).show();
                                }
                            }
                    )
                    .addOnFailureListener(requireActivity(), e -> {
                                checkInRequestRunning = false;

                                if (!isAdded()) {
                                    return;
                                }
                                attendanceAdapter.setTodayStatus("● GPS Error ❌");
                                attendanceAdapter.setCheckInEnabled(false);
                                Toast.makeText(requireContext(), "Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

        } catch (SecurityException e) {

            checkInRequestRunning = false;

            Log.e("ATTENDANCE_DEBUG", "Location permission error", e);
            attendanceAdapter.setTodayStatus("● Location Permission Required ❌");
            attendanceAdapter.setCheckInEnabled(false);

            Toast.makeText(requireContext(), "Location permission is required!", Toast.LENGTH_LONG).show();
        }
    }


    private String getCurrentWifiSsid() {

        if (!isAdded()) {
            return "";
        }

        try {

            WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager == null) {
                return "";
            }

            WifiInfo wifiInfo =
                    wifiManager.getConnectionInfo();

            if (wifiInfo == null) {
                return "";
            }

            String wifiSsid = wifiInfo.getSSID();

            if (wifiSsid == null) {
                return "";
            }

            if (wifiSsid.startsWith("\"") && wifiSsid.endsWith("\"")) {

                wifiSsid = wifiSsid.substring(1, wifiSsid.length() - 1);
            }

            return wifiSsid;

        } catch (Exception e) {

            Log.e("ATTENDANCE_DEBUG", "Unable to get WiFi SSID", e);

            return "";
        }
    }

    private void saveAttendanceToDatabase(
            double latitude,
            double longitude,
            String wifiSsid) {

        if (!isAdded() || attendanceViewModel == null) {

            checkInRequestRunning = false;
            return;
        }

        if (employeeId == null || employeeId.trim().isEmpty()) {

            checkInRequestRunning = false;

            Toast.makeText(requireContext(), "Employee ID not found. Please login again.", Toast.LENGTH_LONG).show();

            return;
        }

        try {

            Long.parseLong(employeeId.trim());

        } catch (NumberFormatException e) {

            checkInRequestRunning = false;

            Log.e("ATTENDANCE_DEBUG", "Invalid employeeId = " + employeeId, e);

            Toast.makeText(requireContext(), "Invalid Employee ID: " + employeeId, Toast.LENGTH_LONG).show();
            return;
        }

        boolean mockLocation = false;

        attendanceViewModel.checkIn(
                        employeeId,
                        latitude,
                        longitude,
                        wifiSsid,
                        mockLocation).observe(getViewLifecycleOwner(), state -> {

                            if (state == null || !isAdded() || attendanceAdapter == null) {

                                return;
                            }

                            if (state.isLoading()) {

                                attendanceAdapter.setCheckInEnabled(false);
                                attendanceAdapter.setCheckOutEnabled(false);

                                attendanceAdapter.setTodayStatus("● Checking In... ⏳");

                                return;
                            }

                            if (state.isSuccess()) {

                                checkInRequestRunning = false;
                                AttendanceApiModel attendance = state.getData();

                                if (attendance == null) {
                                    attendanceAdapter.setCheckInEnabled(false);
                                    attendanceAdapter.setCheckOutEnabled(insideOfficeRadius);

                                    return;
                                }

                                String status = attendance.getStatus() != null ? attendance.getStatus() : "Present";
                                String inTime = attendance.getCheckInTime() != null ? attendance.getCheckInTime() : currentTime;
                                String totalHours = attendance.getTotalHours() != null ? attendance.getTotalHours() : "00h 00m";
                                attendanceAdapter.setCheckInEnabled(false);
                                attendanceAdapter.setCheckOutEnabled(insideOfficeRadius);
                                attendanceAdapter.setTodayStatus("● " + status + " (In: " + inTime + ")");

                                attendanceAdapter.setTotalLoggedHours(totalHours);

                                Toast.makeText(requireContext(), "Checked In Successfully! 🟢", Toast.LENGTH_SHORT).show();

                                loadAttendanceData();

                                loadQuickStats();

                                return;
                            }

                            if (state.isError()) {

                                checkInRequestRunning = false;

                                attendanceAdapter.setCheckInEnabled(locationValidated && insideOfficeRadius);
                                attendanceAdapter.setCheckOutEnabled(false);
                                attendanceAdapter.setTodayStatus("● Check-In Failed");

                                String message = state.getMessage();

                                if (message == null || message.trim().isEmpty()) {

                                    message = "Check-In failed.";
                                }

                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            }
                        }
                        );
    }

    private void showCheckOutConfirmation() {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("🛑 Check-Out Confirmation")
                .setMessage(
                        "Are you sure you want to Check-Out for the day? " + "After this, you won't be able to change your attendance today.")
                .setCancelable(false)
                .setPositiveButton(
                        "Yes, Check-Out", (dialog, which) ->
                                processCheckOut()).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
    }


    private void processCheckOut() {

        if (checkOutRequestRunning) {
            return;
        }
        if (employeeId == null || employeeId.trim().isEmpty()) {

            Toast.makeText(requireContext(), "Employee ID not found. Please login again.", Toast.LENGTH_LONG).show();

            return;
        }

        long employeeDbId;

        try {
            employeeDbId = Long.parseLong(employeeId.trim());

        } catch (NumberFormatException e) {

            Log.e("ATTENDANCE_DEBUG", "Invalid employeeId = " + employeeId, e);
            Toast.makeText(requireContext(), "Invalid Employee ID.", Toast.LENGTH_LONG).show();

            return;
        }


        checkOutRequestRunning = true;

        attendanceAdapter.setCheckInEnabled(false);
        attendanceAdapter.setCheckOutEnabled(false);
        attendanceAdapter.setTodayStatus("● Re-verifying Location... ⏳");


        try {

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(requireActivity(), location -> {

                                if (!isAdded() || getContext() == null) {

                                    checkOutRequestRunning = false;
                                    return;
                                }



                                if (location == null) {

                                    checkOutRequestRunning = false;
                                    attendanceAdapter.setCheckOutEnabled(true);
                                    attendanceAdapter.setTodayStatus("● GPS Location Error ❌");
                                    Toast.makeText(requireContext(), "Unable to get current location!", Toast.LENGTH_LONG).show();

                                    return;
                                }


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && location.isFromMockProvider()) {

                                    checkOutRequestRunning = false;
                                    locationValidated = false;
                                    insideOfficeRadius = false;

                                    attendanceAdapter.setCheckOutEnabled(true);
                                    attendanceAdapter.setTodayStatus("● Fake GPS Detected! 🚨");

                                    Toast.makeText(requireContext(), "Fake GPS detected. Check-Out blocked!", Toast.LENGTH_LONG).show();

                                    return;
                                }



                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                Location officeLocation = new Location("Office");
                                officeLocation.setLatitude(OFFICE_LATITUDE);
                                officeLocation.setLongitude(OFFICE_LONGITUDE);

                                float distanceInMeters = location.distanceTo(officeLocation);

                                boolean isInRadius = distanceInMeters <= OFFICE_RADIUS_METERS;

                                String wifiSsid = getCurrentWifiSsid();

                                boolean isWifiMatched = isConnectedToOfficeWifi();



                                if (!isInRadius && !isWifiMatched) {

                                    checkOutRequestRunning = false;
                                    locationValidated = true;
                                    insideOfficeRadius = false;

                                    attendanceAdapter.setCheckOutEnabled(true);
                                    attendanceAdapter.setTodayStatus(String.format(Locale.getDefault(), "● Authorization Failed! (%.1fm away) ❌", distanceInMeters));

                                    Toast.makeText(requireContext(), "You are outside the office and not connected to office Wi-Fi!", Toast.LENGTH_LONG).show();

                                    return;
                                }



                                locationValidated = true;
                                insideOfficeRadius = isInRadius;



                                attendanceViewModel.checkOut(employeeDbId).observe(getViewLifecycleOwner(), state -> {

                                                    if (state == null
                                                            || !isAdded()
                                                            || attendanceAdapter == null) {

                                                        return;
                                                    }

                                                    if (state.isLoading()) {

                                                        attendanceAdapter.setCheckInEnabled(false);
                                                        attendanceAdapter.setCheckOutEnabled(false);
                                                        attendanceAdapter.setTodayStatus("● Checking Out... ⏳");

                                                        return;
                                                    }

                                                    if (state.isSuccess()) {

                                                        checkOutRequestRunning = false;
                                                        AttendanceApiModel attendance = state.getData();


                                                        if (attendance == null) {

                                                            attendanceAdapter.setCheckInEnabled(false);
                                                            attendanceAdapter.setCheckOutEnabled(false);
                                                            attendanceAdapter.setTodayStatus("● Completed");

                                                            return;
                                                        }


                                                        String inTime =
                                                                attendance.getCheckInTime() != null ? attendance.getCheckInTime() : "--:--";


                                                        String outTime = attendance.getCheckOutTime() != null ? attendance.getCheckOutTime() : currentTime;


                                                        String status = attendance.getStatus() != null ? attendance.getStatus() : "Completed";


                                                        String totalHours =
                                                                attendance.getTotalHours() != null
                                                                        ? attendance.getTotalHours() : calculateTotalHours(inTime, outTime);


                                                        attendanceAdapter.setCheckInEnabled(false);
                                                        attendanceAdapter.setCheckOutEnabled(false);
                                                        attendanceAdapter.setTotalLoggedHours(totalHours);


                                                        if ("Late".equalsIgnoreCase(status)) {

                                                            attendanceAdapter.setTodayStatus("● Late (In: " + inTime + " | Out: " + outTime + ")");

                                                        } else {
                                                            attendanceAdapter.setTodayStatus("● Completed (In: " + inTime + " | Out: " + outTime + ")");
                                                        }


                                                        Toast.makeText(requireContext(), "Checked Out Successfully! 🛑", Toast.LENGTH_SHORT).show();

                                                        loadAttendanceData();
                                                        loadQuickStats();

                                                        return;
                                                    }




                                                    if (state.isError()) {

                                                        checkOutRequestRunning = false;

                                                        attendanceAdapter.setCheckInEnabled(false);
                                                        attendanceAdapter.setCheckOutEnabled(true);
                                                        attendanceAdapter.setTodayStatus("● Check-Out Failed");

                                                        String message = state.getMessage();

                                                        if (message == null || message.trim().isEmpty()) {
                                                            message = "Check-Out failed.";
                                                        }

                                                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                        );
                            }
                    )
                    .addOnFailureListener(requireActivity(), e -> {

                                checkOutRequestRunning = false;

                                if (!isAdded()) {
                                    return;
                                }

                                attendanceAdapter.setCheckOutEnabled(true);
                                attendanceAdapter.setTodayStatus("● GPS Error ❌");
                                Toast.makeText(requireContext(), "Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

        } catch (SecurityException e) {

            checkOutRequestRunning = false;
            Log.e("ATTENDANCE_DEBUG", "Location permission error", e);

            attendanceAdapter.setCheckOutEnabled(true);
            attendanceAdapter.setTodayStatus("● Location Permission Required ❌");
            Toast.makeText(requireContext(), "Location permission is required!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data);

        if (requestCode != SELFIE_REQUEST_CODE || resultCode != Activity.RESULT_OK || data == null) {

            return;
        }

        boolean verified = data.getBooleanExtra("SELFIE_VERIFIED", false);

        if (!verified) {
            return;
        }



        if (selfieCheckInHandled) {
            return;
        }

        selfieCheckInHandled = true;
        currentPhotoPath = data.getStringExtra("PHOTO_PATH");

        if (currentPhotoPath == null) {
            currentPhotoPath = "";
        }


        if (checkInRequestRunning) {
            return;
        }

        try {

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(requireActivity(), location -> {

                                if (!isAdded() || location == null) {

                                    selfieCheckInHandled = false;

                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Unable to get current location!", Toast.LENGTH_LONG).show();
                                    }

                                    return;
                                }

                                if (location.isFromMockProvider()) {

                                    selfieCheckInHandled = false;
                                    attendanceAdapter.setTodayStatus("● Fake GPS Detected! 🚨");
                                    attendanceAdapter.setCheckInEnabled(false);

                                    Toast.makeText(requireContext(), "Fake GPS detected. Attendance blocked!", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Location officeLocation = new Location("Office");

                                officeLocation.setLatitude(OFFICE_LATITUDE);
                                officeLocation.setLongitude(OFFICE_LONGITUDE);

                                float distanceInMeters = location.distanceTo(officeLocation);
                                boolean isInRadius = distanceInMeters <= OFFICE_RADIUS_METERS;

                                boolean isWifiMatched = isConnectedToOfficeWifi();

                                String wifiSsid = getCurrentWifiSsid();

                                if (isInRadius || isWifiMatched) {

                                    insideOfficeRadius = isInRadius;
                                    locationValidated = true;
                                    saveAttendanceToDatabase(latitude, longitude, wifiSsid);

                                } else {

                                    selfieCheckInHandled = false;

                                    attendanceAdapter.setTodayStatus(String.format(Locale.getDefault(), "● Authorization Failed! (%.1fm away) ❌", distanceInMeters));
                                    attendanceAdapter.setCheckInEnabled(false);
                                    Toast.makeText(requireContext(), "You are outside the office and not connected to office Wi-Fi!", Toast.LENGTH_LONG).show();
                                }
                            }
                    )
                    .addOnFailureListener(requireActivity(), e -> {
                                selfieCheckInHandled = false;

                                if (!isAdded()) {
                                    return;
                                }
                                Toast.makeText(requireContext(), "Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
        } catch (SecurityException e) {

            selfieCheckInHandled = false;

            if (!isAdded()) {
                return;
            }

            Toast.makeText(requireContext(), "Location permission required!", Toast.LENGTH_LONG).show();
        }
    }


    private void loadAttendanceData() {

        if (!isAdded() || attendanceViewModel == null || attendanceAdapter == null) {

            return;
        }

        if (employeeId == null || employeeId.trim().isEmpty()) {

            Log.e("ATTENDANCE_DEBUG", "employeeId is NULL or EMPTY");
            attendanceAdapter.setCheckInEnabled(false);
            attendanceAdapter.setCheckOutEnabled(false);
            attendanceAdapter.setTodayStatus("● Employee ID not found");
            attendanceAdapter.setTotalLoggedHours("00h 00m");
            return;
        }

        long employeeDbId;

        try {
            employeeDbId = Long.parseLong(employeeId.trim());

        } catch (NumberFormatException e) {

            Log.e("ATTENDANCE_DEBUG", "Invalid employeeId = " + employeeId, e);
            attendanceAdapter.setCheckInEnabled(false);
            attendanceAdapter.setCheckOutEnabled(false);
            attendanceAdapter.setTodayStatus("● Invalid Employee ID");
            attendanceAdapter.setTotalLoggedHours("00h 00m");

            return;
        }

        attendanceViewModel.getEmployeeAttendance(employeeDbId).observe(getViewLifecycleOwner(), state -> {

                            if (state == null || !isAdded() || attendanceAdapter == null) {

                                return;
                            }

                            if (state.isLoading()) {

                                attendanceAdapter.setCheckInEnabled(false);
                                attendanceAdapter.setCheckOutEnabled(false);
                                attendanceAdapter.setTodayStatus("● Loading Attendance...");

                                return;
                            }

                            if (state.isError()) {

                                attendanceAdapter.setCheckInEnabled(false);
                                attendanceAdapter.setCheckOutEnabled(false);
                                attendanceAdapter.setTodayStatus("● Unable to Load Attendance");
                                String message = state.getMessage();
                                if (message != null && !message.trim().isEmpty()) {
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                                }

                                return;
                            }

                            if (!state.isSuccess()) {
                                return;
                            }

                            List<AttendanceApiModel> list = state.getData();
                            AttendanceApiModel todayAttendance = findTodayAttendance(list);
                            updateAttendanceUI(todayAttendance, insideOfficeRadius);
                        }
                );
    }

    private String calculateTotalHours(String inTime, String outTime) {

        if (inTime == null || outTime == null || inTime.equals("--:--") || outTime.equals("--:--")) {

            return "00h 00m";
        }

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        try {

            Date dateIn = format.parse(inTime);
            Date dateOut = format.parse(outTime);

            if (dateIn != null && dateOut != null) {

                long diff = dateOut.getTime() - dateIn.getTime();
                if (diff < 0) {
                    diff += 24 * 60 * 60 * 1000;
                }

                long hours = diff / (60 * 60 * 1000);
                long minutes = (diff / (60 * 1000)) % 60;

                return String.format(Locale.getDefault(), "%02dh %02dm", hours, minutes);
            }

        } catch (Exception e) {

            Log.e("ATTENDANCE_DEBUG", "Hour calculation error", e);
        }
        return "00h 00m";
    }

    private void loadQuickStats() {

        if (!isAdded() || attendanceViewModel == null || attendanceAdapter == null) {
            return;
        }
        if (employeeId == null || employeeId.trim().isEmpty()) {
            attendanceAdapter.setOnlineTime("00h 00m");
            attendanceAdapter.setYesterdayStatus("Absent");
            attendanceAdapter.setBreakTime("0m");
            return;
        }

        long employeeDbId;

        try {
            employeeDbId = Long.parseLong(employeeId.trim());

        } catch (NumberFormatException e) {
            attendanceAdapter.setOnlineTime("00h 00m");
            attendanceAdapter.setYesterdayStatus("Absent");
            attendanceAdapter.setBreakTime("0m");

            return;
        }

        attendanceViewModel.getEmployeeAttendance(employeeDbId).observe(getViewLifecycleOwner(), state -> {

                            if (state == null || !state.isSuccess() || !isAdded() || attendanceAdapter == null) {

                                return;
                            }
                            List<AttendanceApiModel> list = state.getData();
                            String onlineTime = "00h 00m";
                            String yesterdayStatus = "Absent";

                            if (list != null) {

                                AttendanceApiModel todayAttendance = findTodayAttendance(list);

                                if (todayAttendance != null) {

                                    String inTime = todayAttendance.getCheckInTime();
                                    String outTime = todayAttendance.getCheckOutTime();

                                    if (inTime != null && !inTime.equals("--:--") && !inTime.trim().isEmpty()) {

                                        if (outTime != null && !outTime.equals("--:--") && !outTime.trim().isEmpty()) {

                                            if (todayAttendance.getTotalHours() != null && !todayAttendance.getTotalHours().trim().isEmpty()) {

                                                onlineTime = todayAttendance.getTotalHours();

                                            } else {
                                                onlineTime = calculateTotalHours(inTime, outTime);
                                            }

                                        } else {
                                            onlineTime = calculateTotalHours(inTime, currentTime);
                                        }
                                    }
                                }

                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.DAY_OF_YEAR, -1);

                                String yesterdayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());

                                for (AttendanceApiModel attendance : list) {

                                    if (attendance == null) {
                                        continue;
                                    }

                                    String date = attendance.getDate();

                                    if (date == null) {
                                        continue;
                                    }

                                    if (yesterdayDate.equals(
                                            date.trim()
                                    )) {
                                        String status = attendance.getStatus();
                                        if (status != null && !status.trim().isEmpty()) {
                                            yesterdayStatus = status;
                                        }

                                        break;
                                    }
                                }
                            }
                            attendanceAdapter.setOnlineTime(onlineTime);

                            final String finalYesterdayStatus = yesterdayStatus;

                            new Thread(() -> {
                                        if (database == null) {
                                            return;
                                        }
                                        BreakDao breakDao = database.breakDao();

                Long totalBreakMinutes = breakDao.getTotalBreakMinutesForDay(String.valueOf(employeeDbId), currentDate);
                String breakTime = totalBreakMinutes != null && totalBreakMinutes > 0 ? totalBreakMinutes + "m" : "0m";

                 if (!isAdded()) {

                 return;
                }
                    requireActivity().runOnUiThread(() -> {

                 if (attendanceAdapter == null) {
                   return;
                  }
            attendanceAdapter.setBreakTime(breakTime);
            attendanceAdapter.setYesterdayStatus(finalYesterdayStatus);
                                                        }
                                                );
                                    }).start();
                        }
                );
    }

    @Override
    public void onResume() {

        super.onResume();

        selfieCheckInHandled = false;
    }

    @Override
    public void onDestroyView() {

        handler.removeCallbacksAndMessages(null);

        checkInRequestRunning = false;
        checkOutRequestRunning = false;
        securityCheckRunning = false;

        super.onDestroyView();

        recyclerViewAttendance = null;
        attendanceAdapter = null;
    }
}


//package com.agribird.hrmsapp.ui.Attendance;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Bundle;
//import android.os.Handler;
//import android.provider.Settings;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import android.content.Context;
//import android.net.ConnectivityManager;
//import android.net.NetworkCapabilities;
//import android.net.wifi.WifiInfo;
//import android.net.wifi.WifiManager;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//
//import com.agribird.hrmsapp.Model.Attendance;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.dao.AttendanceDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class AttendanceFragment extends Fragment {
//
//    TextView txtEmpName, txtTodayStatus, txtTime, txtDate, txtTotalLoggedHours;
//    TextView txtSummaryPresent, txtSummaryAbsent, txtSummaryLeave;
//    Button btnCheckIn, btnCheckOut;
//
//    HRMSDatabase database;
//    AttendanceDao attendanceDao;
//
//    SharedPreferences sharedPreferences;
//
//    String employeeId, employeeName;
//    String currentDate, currentTime;
//
//    Handler handler = new Handler();
//
//    FusedLocationProviderClient fusedLocationClient;
//
//    private String currentPhotoPath = "";
//
//    private static final int LOCATION_PERMISSION_REQ_CODE = 1001;
//
//    private static final double OFFICE_LATITUDE = 18.561132;
//    private static final double OFFICE_LONGITUDE = 73.944612;
//    private static final float OFFICE_RADIUS_METERS = 100.0f;
//
//    private static final String OFFICE_WIFI_1 = "AGRIBIRD_2.4G";
//    private static final String OFFICE_WIFI_2 = "AGRIBIRD_5G";
//
//    @Nullable
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState) {
//
//        return inflater.inflate(
//                R.layout.fragment_attendance,
//                container,
//                false
//        );
//    }
//
//    @Override
//    public void onViewCreated(
//            @NonNull View view,
//            @Nullable Bundle savedInstanceState) {
//
//        super.onViewCreated(view, savedInstanceState);
//
//        txtEmpName = view.findViewById(R.id.txtEmpName);
//        txtTodayStatus = view.findViewById(R.id.txtTodayStatus);
//        txtTime = view.findViewById(R.id.txtTime);
//        txtDate = view.findViewById(R.id.txtDate);
//        txtTotalLoggedHours = view.findViewById(R.id.txtTotalLoggedHours);
//
//        txtSummaryPresent = view.findViewById(R.id.txtSummaryPresent);
//        txtSummaryAbsent = view.findViewById(R.id.txtSummaryAbsent);
//        txtSummaryLeave = view.findViewById(R.id.txtSummaryLeave);
//
//        btnCheckIn = view.findViewById(R.id.btnCheckIn);
//        btnCheckOut = view.findViewById(R.id.btnCheckOut);
//
//        database = HRMSDatabase.getInstance(requireContext());
//        attendanceDao = database.attendanceDao();
//        sharedPreferences = requireContext().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
//        employeeId = sharedPreferences.getString("userId", "");
//        employeeName = sharedPreferences.getString("userName", "Employee");
//        txtEmpName.setText(employeeName);
//
//        if (employeeId != null && employeeId.equals("SUPER_ADMIN_ID")) {
//
//            Toast.makeText(requireContext(),
//                    "Administrators are exempt from attendance tracking.", Toast.LENGTH_LONG).show();
//
//            requireActivity().getSupportFragmentManager()
//                    .popBackStack();
//            return;
//        }
//        fusedLocationClient =
//                LocationServices.getFusedLocationProviderClient(requireContext());
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//
//        currentDate = dateFormat.format(new Date());
//        txtDate.setText(currentDate);
//
//        handler.post(new Runnable() {
//
//            @Override
//            public void run() {
//
//                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//                currentTime = timeFormat.format(new Date());
//                txtTime.setText(currentTime);
//                handler.postDelayed(this, 1000);
//            }
//        });
//
//        txtTodayStatus.setText("● Checking Security...");
//
//        btnCheckIn.setEnabled(false);
//        btnCheckOut.setEnabled(false);
//
//        runSecurityAndLocationCheck();
//        loadMonthlySummary();
//
//        btnCheckIn.setOnClickListener(v -> {
//            processCheckIn();
//        });
//
//        btnCheckOut.setOnClickListener(v -> {
//            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
//                    .setTitle("🛑 Check-Out Confirmation")
//                    .setMessage("Are you sure you want to Check-Out for the day? " +
//                            "After this, you won't be able to change your attendance today."
//                    )
//                    .setCancelable(false)
//
//                    .setPositiveButton(
//                            "Yes, Check-Out",
//                            (dialog, which) -> {
//
//                                processCheckOut();
//
//                            }
//                    )
//
//                    .setNegativeButton("Cancel",
//                            (dialog, which) -> dialog.dismiss()).show();
//        });
//
//    }
//
//    private void runSecurityAndLocationCheck() {
////        if (isDeveloperOptionsOn()) {
////            txtTodayStatus.setText("● Security Alert: Developer Options Enabled! 🚨");
////            txtTodayStatus.setTextColor(Color.RED);
////            btnCheckIn.setEnabled(false);
////            btnCheckOut.setEnabled(false);
////            Toast.makeText(this, "Please disable Developer Options in phone settings to use Attendance!", Toast.LENGTH_LONG).show();
//        //} else {
//        checkLocationPermissionAndValidate();
//        // }
//    }
//
////    private boolean isDeveloperOptionsOn() {
////
////        int devOptions = Settings.Global.getInt(
////                requireContext().getContentResolver(),
////                "development_settings_enabled",
////                0
////        );
////
////        return devOptions != 0;
////    }
//
//    private boolean isConnectedToOfficeWifi() {
//        Context context = requireContext();
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        if (cm != null) {
//            android.net.Network network = cm.getActiveNetwork();
//            if (network != null) {
//
//                NetworkCapabilities capabilities =
//                        cm.getNetworkCapabilities(network);
//
//                if (capabilities != null &&
//                        capabilities.hasTransport(
//                                NetworkCapabilities.TRANSPORT_WIFI
//                        )) {
//
//                    WifiManager wifiManager =
//                            (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                    if (wifiManager != null) {
//
//                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//
//                        if (wifiInfo != null) {
//                            String currentWifiSSID =
//                                    wifiInfo.getSSID();
//                            if (currentWifiSSID != null &&
//                                    currentWifiSSID.startsWith("\"") &&
//                                    currentWifiSSID.endsWith("\"")) {
//
//                                currentWifiSSID = currentWifiSSID.substring(1, currentWifiSSID.length() - 1);
//                            }
//                            return OFFICE_WIFI_1.equalsIgnoreCase(currentWifiSSID) || OFFICE_WIFI_2.equalsIgnoreCase(currentWifiSSID);
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    private void checkLocationPermissionAndValidate() {
//
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//        ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
//
//            requestPermissions(
//                    new String[]{
//                            android.Manifest.permission.ACCESS_FINE_LOCATION
//                    },
//                    LOCATION_PERMISSION_REQ_CODE
//            );
//
//        } else {
//
//            getCurrentLocationAndCheckRadius();
//        }
//    }
//
//    private void getCurrentLocationAndCheckRadius() {
//        try {
//            fusedLocationClient.getCurrentLocation(
//                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
//                    null
//            ).addOnSuccessListener(requireActivity(), location -> {
//                if (!isAdded() || getContext() == null) {
//                    return;
//                }
//                if (location != null) {
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                        if (location.isFromMockProvider()) {
//                            txtTodayStatus.setText("● Fake GPS Detected! 🚨");
//                            txtTodayStatus.setTextColor(Color.RED);
//                            btnCheckIn.setEnabled(false);
//                            btnCheckOut.setEnabled(false);
//
//                            Toast.makeText(requireContext(), "Fake GPS App detected. Attendance Blocked!", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                    }
//                    Location officeLocation = new Location("Office");
//                    officeLocation.setLatitude(OFFICE_LATITUDE);
//                    officeLocation.setLongitude(OFFICE_LONGITUDE);
//                    float distanceInMeters = location.distanceTo(officeLocation);
//
//                    if (distanceInMeters <= OFFICE_RADIUS_METERS) {
//                        checkTodayStatus(true);
//
//                        Toast.makeText(requireContext(), "You are in office premises. 🟢", Toast.LENGTH_SHORT).show();
//                    } else {
//                        checkTodayStatus(false);
//                        txtTodayStatus.setText(String.format(Locale.getDefault(),
//                                "● Out of Office Radius (%.1fm away) ❌", distanceInMeters));
//                        txtTodayStatus.setTextColor(Color.RED);
//                    }
//                } else {
//                    txtTodayStatus.setText("● GPS Location Error");
//                    Toast.makeText(
//                            requireContext(),
//                            "Unable to get GPS location. Please turn on Location/GPS!", Toast.LENGTH_LONG).show();
//                }
//            });
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQ_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
//                getCurrentLocationAndCheckRadius();
//            } else {
//                Toast.makeText(requireContext(), "Location permission is compulsory for Attendance!", Toast.LENGTH_LONG).show();
//                requireActivity().finish();
//            }
//        }
//    }
//    private void checkTodayStatus(boolean isInRadius) {
//        new Thread(() -> {
//            // Background Thread: Database Call
//            Attendance existing = attendanceDao.getAttendanceByDate(employeeId, currentDate);
//
//            // Activity Check करून सुरक्षितपणे UI Thread वर येणे
//            if (isAdded() && getActivity() != null) {
//                requireActivity().runOnUiThread(() -> {
//
//                    if (existing != null) {
//                        String inTime = existing.getCheckInTime() != null ? existing.getCheckInTime() : "--:--";
//                        String outTime = existing.getCheckOutTime() != null ? existing.getCheckOutTime() : "--:--";
//
//                        if (existing.getCheckOutTime() != null && !existing.getCheckOutTime().equals("--:--") && !existing.getCheckOutTime().isEmpty()) {
//                            btnCheckIn.setEnabled(false);
//                            btnCheckOut.setEnabled(false);
//
//                            String totalHours = calculateTotalHours(inTime, outTime);
//                            txtTotalLoggedHours.setText(totalHours);
//                            updateHoursTextColor(inTime, outTime);
//
//                            if (existing.getStatus().equalsIgnoreCase("Late")) {
//                                txtTodayStatus.setText("● Late (In: " + inTime + " | Out: " + outTime + ")");
//                                txtTodayStatus.setTextColor(Color.parseColor("#E65100"));
//                            } else {
//                                txtTodayStatus.setText("● Completed (In: " + inTime + " | Out: " + outTime + ")");
//                                txtTodayStatus.setTextColor(Color.parseColor("#1976D2"));
//                            }
//                        } else {
//                            btnCheckIn.setEnabled(false);
//                            btnCheckOut.setEnabled(isInRadius);
//                            txtTotalLoggedHours.setText("00h 00m");
//
//                            if (existing.getStatus().equalsIgnoreCase("Late")) {
//                                txtTodayStatus.setText("● Late (In: " + inTime + ")");
//                                txtTodayStatus.setTextColor(Color.parseColor("#E65100"));
//                            } else {
//                                txtTodayStatus.setText("● Present (In: " + inTime + ")");
//                                txtTodayStatus.setTextColor(Color.parseColor("#2E7D32"));
//                            }
//                        }
//                    } else {
//                        btnCheckIn.setEnabled(isInRadius);
//                        btnCheckOut.setEnabled(false);
//                        if (isInRadius) {
//                            txtTodayStatus.setText("● Not Checked In");
//                            txtTodayStatus.setTextColor(Color.GRAY);
//                        }
//                    }
//                });
//            }
//        }).start();
//    }
//    private void processCheckIn() {
//        if (currentTime == null || currentTime.isEmpty()) {
//            Toast.makeText(requireContext(),
//                    "Please wait a second...",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }
//        txtTodayStatus.setText("● Re-verifying Location... ⏳");
//        try {
//            fusedLocationClient.getCurrentLocation(
//                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
//                    null
//            ).addOnSuccessListener(requireActivity(), location -> {
//                if (!isAdded() || getContext() == null) return;
//
//                boolean isWifiMatched = isConnectedToOfficeWifi();
//                boolean isInRadius = false;
//
//                if (location != null) {
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && location.isFromMockProvider()) {
//                        Toast.makeText(requireContext(), "Security Alert: Fake GPS App detected!", Toast.LENGTH_LONG).show();
//                        return;
//                    }
//                    Location officeLocation = new Location("Office");
//                    officeLocation.setLatitude(OFFICE_LATITUDE);
//                    officeLocation.setLongitude(OFFICE_LONGITUDE);
//                    if (location.distanceTo(officeLocation) <= OFFICE_RADIUS_METERS) {
//                        isInRadius = true;
//                    }
//                }
//
//                if (isInRadius || isWifiMatched) {
//                    // Intent intent = new Intent(requireContext(), CameraActivity.class);
//                    // startActivityForResult(intent, 1001);
//                    saveAttendanceToDatabase();
//                } else {
//                    txtTodayStatus.setText("● Authorization Failed! ❌");
//                    txtTodayStatus.setTextColor(Color.RED);
//                    btnCheckIn.setEnabled(false);
//                    Toast.makeText(requireContext(), "You moved out of office radius! Attendance blocked.", Toast.LENGTH_LONG).show();
//                }
//            });
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
//    private void saveAttendanceToDatabase() {
//        new Thread(() -> {
//            Attendance existing = attendanceDao.getAttendanceByDate(employeeId, currentDate);
//
//            if (existing == null) {
//                String calculatedStatus = "Present";
//                try {
//                    SimpleDateFormat parser = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//                    Date officeTime = parser.parse("09:30 AM");
//                    Date userTime = parser.parse(currentTime);
//                    if (userTime != null && officeTime != null && userTime.after(officeTime)) {
//                        calculatedStatus = "Late";
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                final String finalStatus = calculatedStatus;
//                Attendance attendance = new Attendance(
//                        employeeId,
//                        employeeName,
//                        currentDate,
//                        currentTime,
//                        "--:--",
//                        finalStatus,
//                        currentPhotoPath,
//                        "00h 00m"  // <--- हे add करा
//                );
////                Attendance attendance = new Attendance(
////                        employeeId,
////                        employeeName,
////                        currentDate,
////                        currentTime,
////                        "--:--",
////                        finalStatus,
////                        currentPhotoPath
////                );
//
//                attendanceDao.insertAttendance(attendance);
//                if (isAdded() && getActivity() != null) {
//                    requireActivity().runOnUiThread(() -> {
//                        txtTodayStatus.setText("● " + finalStatus);
//
//                        if (finalStatus.equals("Late")) {
//                            txtTodayStatus.setTextColor(Color.parseColor("#E65100"));
//                            Toast.makeText(requireContext(), "Checked In (Late) 🟠 Selfie Saved!", Toast.LENGTH_SHORT).show();
//                        } else {
//                            txtTodayStatus.setTextColor(Color.parseColor("#2E7D32"));
//                            Toast.makeText(requireContext(), "Checked In On Time! 🟢 Selfie Saved!", Toast.LENGTH_SHORT).show();
//                        }
//
//                        btnCheckIn.setEnabled(false);
//                        btnCheckOut.setEnabled(true);
//
//                        loadMonthlySummary();
//                    });
//                }
//            }
//        }).start();
//    }
//    private void processCheckOut() {
//        new Thread(() -> {
//            Attendance existing = attendanceDao.getAttendanceByDate(employeeId, currentDate);
//            if (existing != null) {
//                existing.setCheckOutTime(currentTime);
//                if (!existing.getStatus().equalsIgnoreCase("Late")) {
//                    existing.setStatus("Completed");
//                }
//                attendanceDao.updateAttendance(existing);
//                if (isAdded() && getActivity() != null) {
//                    requireActivity().runOnUiThread(() -> {
//                        String inTime = existing.getCheckInTime() != null ? existing.getCheckInTime() : "--:--";
//                        String outTime = currentTime;
//
//                        String totalHours = calculateTotalHours(inTime, outTime);
//                        txtTotalLoggedHours.setText(totalHours);
//                        updateHoursTextColor(inTime, outTime);
//
//                        if (existing.getStatus().equalsIgnoreCase("Late")) {
//                            txtTodayStatus.setText("● Late (In: " + inTime + " | Out: " + outTime + ")");
//                            txtTodayStatus.setTextColor(Color.parseColor("#E65100"));
//                        } else {
//                            txtTodayStatus.setText("● Completed (In: " + inTime + " | Out: " + outTime + ")");
//                            txtTodayStatus.setTextColor(Color.parseColor("#1976D2"));
//                        }
//
//                        btnCheckIn.setEnabled(false);
//                        btnCheckOut.setEnabled(false);
//                        Toast.makeText(requireContext(), "Checked Out Successfully! 🛑", Toast.LENGTH_SHORT).show();
//                        loadMonthlySummary();
//                    });
//                }
//            }
//        }).start();
//    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
//
//            boolean verified = data.getBooleanExtra("SELFIE_VERIFIED", false);
//            String photoPath = data.getStringExtra("PHOTO_PATH");
//
//            if (verified) {
//                currentPhotoPath = photoPath;
//                if (isAdded()) {
//                    saveAttendanceToDatabase();
//                }
//            }
//        }
//    }
//    private void loadMonthlySummary() {
//        new Thread(() -> {
//            if (employeeId != null && !employeeId.isEmpty()) {
//                List<Attendance> list = attendanceDao.getAllAttendance(employeeId);
//                int presentCount = 0;
//                int absentCount = 0;
//                int leaveCount = 0;
//
//                if (list != null) {
//                    for (Attendance att : list) {
//                        String status = att.getStatus();
//                        if (status != null) {
//                            if (status.equalsIgnoreCase("Present") || status.equalsIgnoreCase("Late") || status.equalsIgnoreCase("Completed")) {
//                                presentCount++;
//                            } else if (status.equalsIgnoreCase("Absent")) {
//                                absentCount++;
//                            } else if (status.equalsIgnoreCase("Leave") || status.equalsIgnoreCase("On Leave")) {
//                                leaveCount++;
//                            }
//                        }
//                    }
//                }
//                final int finalPresent = presentCount;
//                final int finalAbsent = absentCount;
//                final int finalLeave = leaveCount;
//                if (isAdded() && getActivity() != null) {
//                    requireActivity().runOnUiThread(() -> {
//                        txtSummaryPresent.setText(String.valueOf(finalPresent));
//                        txtSummaryAbsent.setText(String.valueOf(finalAbsent));
//                        txtSummaryLeave.setText(String.valueOf(finalLeave));
//                    });
//                }
//            }
//        }).start();
//    }
//    private String calculateTotalHours(String inTime, String outTime) {
//        if (inTime == null || outTime == null || inTime.equals("--:--") || outTime.equals("--:--")) return "00h 00m";
//        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//        try {
//            Date dateIn = format.parse(inTime);
//            Date dateOut = format.parse(outTime);
//            if (dateIn != null && dateOut != null) {
//                long differenceInMilli = dateOut.getTime() - dateIn.getTime();
//                if (differenceInMilli < 0) differenceInMilli += 24 * 60 * 60 * 1000;
//                long hours = differenceInMilli / (60 * 60 * 1000);
//                long minutes = (differenceInMilli / (60 * 1000)) % 60;
//                return String.format(Locale.getDefault(), "%02dh %02dm", hours, minutes);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "00h 00m";
//    }
//    private void updateHoursTextColor(String inTime, String outTime) {
//        if (inTime == null || outTime == null || inTime.equals("--:--") || outTime.equals("--:--")) {
//            txtTotalLoggedHours.setTextColor(Color.parseColor("#1F2937"));
//            return;
//        }
//        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//        try {
//            Date dateIn = format.parse(inTime);
//            Date dateOut = format.parse(outTime);
//            if (dateIn != null && dateOut != null) {
//                long differenceInMilli = dateOut.getTime() - dateIn.getTime();
//                if (differenceInMilli < 0) differenceInMilli += 24 * 60 * 60 * 1000;
//                long hours = differenceInMilli / (60 * 60 * 1000);
//
//                if (hours < 8) {
//                    txtTotalLoggedHours.setTextColor(Color.parseColor("#D32F2F"));
//                } else {
//                    txtTotalLoggedHours.setTextColor(Color.parseColor("#2E7D32"));
//                }
//            }
//        } catch (Exception e) {
//            txtTotalLoggedHours.setTextColor(Color.parseColor("#1F2937"));
//        }
//    }
//
//}
