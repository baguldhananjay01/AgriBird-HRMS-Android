package com.agribird.hrmsapp.ui.dashboard;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.MainActivity;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.DashboardAdapter;
import com.agribird.hrmsapp.api.AttendanceApi;
import com.agribird.hrmsapp.api.LeaveApi;
import com.agribird.hrmsapp.api.LeaveBalanceApi;
import com.agribird.hrmsapp.dao.BreakDao;
import com.agribird.hrmsapp.databasecon.HRMSDatabase;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.agribird.hrmsapp.repository.AttendanceRepository;
import com.agribird.hrmsapp.repository.LeaveBalanceRepository;
import com.agribird.hrmsapp.repository.LeaveRepository;
import com.agribird.hrmsapp.ui.Attendance.AttendanceFragment;
import com.agribird.hrmsapp.ui.Attendance.AttendanceHistoryFragment;
import com.agribird.hrmsapp.ui.Leave.ApplyLeaveFragment;
import com.agribird.hrmsapp.ui.Leave.LeaveApprovalFragment;
import com.agribird.hrmsapp.ui.Leave.MyLeaveHistoryFragment;
import com.agribird.hrmsapp.ui.Profile.ProfileFragment;
import com.agribird.hrmsapp.ui.Reports.EmployeesReportsFragment;
import com.agribird.hrmsapp.ui.employee.EmployeeAddFragment;
import com.agribird.hrmsapp.ui.employee.EmployeeListFragment;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModel;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModelFactory;
import com.agribird.hrmsapp.viewmodel.LeaveBalanceViewModel;
import com.agribird.hrmsapp.viewmodel.LeaveBalanceViewModelFactory;
import com.agribird.hrmsapp.viewmodel.LeaveViewModel;
import com.agribird.hrmsapp.viewmodel.LeaveViewModelFactory;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment implements DashboardAdapter.DashboardClickListener {

    private RecyclerView recyclerView;
    private DashboardAdapter dashboardAdapter;
    private AttendanceViewModel attendanceViewModel;

    private LeaveBalanceViewModel leaveBalanceViewModel;

    private LeaveViewModel leaveViewModel;
    private TextView txtUserName, greetingText;
    private ImageView imagemenu, imgemployeeProfile;
    private MaterialCardView cardEmployeeProfileImage;

    private View cardHeader;

    private SharedPreferences sharedPreferences;
    private String name, role, email, empId;

    private NestedScrollView nestedScrollView;
    private float startY = 0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AttendanceApi attendanceApi= RetrofitClient.getAttendanceApi(requireContext());

        AttendanceRepository attendanceRepository = new AttendanceRepository(attendanceApi);

        AttendanceViewModelFactory factory = new AttendanceViewModelFactory(attendanceRepository);

        attendanceViewModel = new ViewModelProvider(this, factory).get(AttendanceViewModel.class);

        LeaveApi leaveApi = RetrofitClient.getLeaveApi(requireContext());

        LeaveRepository leaveRepository = new LeaveRepository(leaveApi);

        LeaveViewModelFactory leaveFactory = new LeaveViewModelFactory(leaveRepository);

        leaveViewModel = new ViewModelProvider(this, leaveFactory).get(LeaveViewModel.class);

        LeaveBalanceApi leaveBalanceApi = RetrofitClient.getLeaveBalanceApi(requireContext());

        LeaveBalanceRepository leaveBalanceRepository = new LeaveBalanceRepository(leaveBalanceApi);

        LeaveBalanceViewModelFactory factory2 = new LeaveBalanceViewModelFactory(leaveBalanceRepository);

        leaveBalanceViewModel = new ViewModelProvider(this, factory2).get(LeaveBalanceViewModel.class);

        cardHeader = view.findViewById(R.id.cardHeader);

        sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);

        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            return;
        }

        name = sharedPreferences.getString("userName", "");
        email = sharedPreferences.getString("userEmail", "");
        role = sharedPreferences.getString("userRole", "");
        empId = sharedPreferences.getString("userId", "");

        initHeaderViews(view);
        setUserInfo();

        setupRecyclerView(view);

        loadBreakTime();

//        if (recyclerView != null) {
//            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                    super.onScrolled(recyclerView, dx, dy);
//
//                    if (cardHeader != null) {
//                        if (recyclerView.canScrollVertically(-1)) {
//                            cardHeader.setBackgroundColor(android.graphics.Color.parseColor("#E6E6FA"));
//                        } else {
//                            cardHeader.setBackgroundColor(android.graphics.Color.parseColor("#F4F7F5"));
//                        }
//                    }
//                }
//            });
//        }
    }




    private void initHeaderViews(View view) {
        txtUserName = view.findViewById(R.id.txtUserName);
        greetingText = view.findViewById(R.id.Greeting);
        imagemenu = view.findViewById(R.id.imagemenu);
        imgemployeeProfile = view.findViewById(R.id.imgemployeeProfile);
        cardEmployeeProfileImage = view.findViewById(R.id.cardEmployeeProfileImage);

        View.OnClickListener profileClickListener = v -> openProfileFragment();
        if (cardEmployeeProfileImage != null) cardEmployeeProfileImage.setOnClickListener(profileClickListener);
        if (imgemployeeProfile != null) imgemployeeProfile.setOnClickListener(profileClickListener);

        if (imagemenu != null) {
            imagemenu.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Menu Clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setUserInfo() {
        if (txtUserName != null) txtUserName.setText(name);
        setGreeting();
    }

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (hour < 12) ? "Good Morning, 👋" : (hour < 17) ? "Good Afternoon, 👋" : "Good Evening, 👋";
        if (greetingText != null) greetingText.setText(greeting);
    }

//    private void setupRecyclerView(View view) {
//        recyclerView = view.findViewById(R.id.recyclerView);
//        dashboardAdapter = new DashboardAdapter(requireContext(), getViewLifecycleOwner(), this);
//        if (recyclerView != null) {
//            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
//            recyclerView.setLayoutManager(layoutManager);
//            recyclerView.setAdapter(dashboardAdapter);
//
//            recyclerView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
//            recyclerView.setEdgeEffectFactory(new RecyclerView.EdgeEffectFactory() {
//                @NonNull
//                @Override
//                protected android.widget.EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction) {
//                    return new android.widget.EdgeEffect(view.getContext());
//                }
//            });
//        }
//    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);

        dashboardAdapter = new DashboardAdapter(requireContext(), getViewLifecycleOwner(), this, attendanceViewModel,
                leaveBalanceViewModel, leaveViewModel);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(dashboardAdapter);
            recyclerView.setNestedScrollingEnabled(false);
        }

        if (nestedScrollView != null) {
            SpringAnimation springY = new SpringAnimation(recyclerView, SpringAnimation.TRANSLATION_Y, 0f);
            springY.getSpring().setStiffness(SpringForce.STIFFNESS_LOW);
            springY.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

            nestedScrollView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaY = event.getRawY() - startY;
                        float translation = deltaY * 0.12f;
                        if (deltaY > 0 && !nestedScrollView.canScrollVertically(-1)) {
                            translation = Math.min(translation, 40f); // 👈 Limit control
                            recyclerView.setTranslationY(translation);
                        }
                        else if (deltaY < 0 && !nestedScrollView.canScrollVertically(1)) {
                            translation = Math.max(translation, -50f); // 👈 Limit control
                            recyclerView.setTranslationY(translation);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        springY.start();
                        break;
                }
                return false;
            });
        }
    }

    private void loadBreakTime() {
        new Thread(() -> {
            try {
                BreakDao breakDao = HRMSDatabase.getInstance(requireContext()).breakDao();
                String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
                Long totalBreakMinutes = breakDao.getTotalBreakMinutesForDay(empId, todayDate);

                String breakTime = (totalBreakMinutes != null) ? totalBreakMinutes + "m" : "0m";

                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (dashboardAdapter != null) {
                            dashboardAdapter.setBreakTime(breakTime);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openProfileFragment() {
        com.agribird.hrmsapp.ui.Profile.EditProfileFragment editProfileFragment = new com.agribird.hrmsapp.ui.Profile.EditProfileFragment();

        String currentName = sharedPreferences.getString("userName", "");
        String currentEmail = sharedPreferences.getString("userEmail", "");
        String currentPhone = sharedPreferences.getString("userPhone", "");
        String currentEmergency = sharedPreferences.getString("userEmergencyPhone", "");
        String currentAddress = sharedPreferences.getString("userAddress", "");

        Bundle bundle = new Bundle();
        bundle.putString("Name", currentName);
        bundle.putString("Email", currentEmail);
        bundle.putString("Phone", currentPhone);
        bundle.putString("EmergencyPhone", currentEmergency);
        bundle.putString("Address", currentAddress);

        editProfileFragment.setArguments(bundle);
        navigateToFragment(editProfileFragment);
    }

    private void navigateToFragment(Fragment fragment) {
        MainActivity mainActivity=(MainActivity) requireActivity();

        if(fragment instanceof AttendanceFragment){

            mainActivity.showBottomNavigation(true);
        }else{
            mainActivity.showBottomNavigation(false);
        }

        requireActivity().getSupportFragmentManager()
                .beginTransaction().setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                ).replace(R.id.fragmentContainer,fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onNotificationClick() {
        Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckOutClick() {
        Toast.makeText(requireContext(), "Checkout clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttendanceClick() {
        navigateToFragment(new AttendanceFragment());
    }

    @Override
    public void onApplyLeaveClick() {
        navigateToFragment(new ApplyLeaveFragment());
    }

    @Override
    public void onMyLeavesClick() {
        MyLeaveHistoryFragment fragment = new MyLeaveHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", empId);
        fragment.setArguments(bundle);
        navigateToFragment(fragment);
    }

    @Override
    public void onPayslipClick() {
        Toast.makeText(requireContext(), "Coming Soon...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewAllAttendance() {
        if (getActivity() != null) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
            String role = sharedPreferences.getString("userRole", "");

            if (role.equalsIgnoreCase("Super Administrator") && role.equalsIgnoreCase("Super Administrator")
                    ||role.equalsIgnoreCase("Administrator")
                    ||role.equalsIgnoreCase("Admin staff")
                    ||role.equalsIgnoreCase("Admin Manager")) {
                Toast.makeText(requireContext(), "This section is for employees only!", Toast.LENGTH_SHORT).show();
                return;
            }

            AttendanceHistoryFragment fragment = new AttendanceHistoryFragment();

            Bundle bundle = new Bundle();
            bundle.putString("LOGGED_IN_ROLE", role);
            bundle.putString("EMPLOYEE_ID", sharedPreferences.getString("userId", ""));
            fragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onViewAllAnnouncements() {
        Toast.makeText(requireContext(), "View All Announcements", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCalendar() {
        Toast.makeText(requireContext(), "View Calendar", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddEmployeeClick() {
        navigateToFragment(new EmployeeAddFragment());
    }

    @Override
    public void onEmployeeListClick() {
        EmployeeListFragment fragment = new EmployeeListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("LOGGED_IN_ROLE", role);
        fragment.setArguments(bundle);
        navigateToFragment(fragment);
    }

    @Override
    public void onDepartmentsClick() {
        Toast.makeText(requireContext(), "Coming Soon...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReportsClick() {
        navigateToFragment(new EmployeesReportsFragment());
    }

    @Override
    public void onLeaveApprovalClick() {
        navigateToFragment(new LeaveApprovalFragment());
    }

    @Override
    public void onRegularizationClick() {
        Toast.makeText(requireContext(), "Regularization Requests Coming Soon...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewAllApprovals() {
        Toast.makeText(requireContext(), "View All Pending Approvals", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewAllActivity() {
        Toast.makeText(requireContext(), "View All Recent Activity", Toast.LENGTH_SHORT).show();

        // ===== Logout =====
//        view.findViewById(R.id.cardLogout).setOnClickListener(v -> {
//            new AlertDialog.Builder(requireActivity())
//                    .setTitle("\uD83D\uDEAA Logout")
//                    .setMessage("Are you sure you want to logout from AgriBird HRMS?")
//                    .setCancelable(false)
//                    .setPositiveButton("Yes, Logout", (dialog, which) -> {
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.clear();
//                        editor.apply();
//                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                        requireActivity().finish();
//                        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
//                    })
//                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                    .show();
//        });
    }
}


//package com.agribird.hrmsapp.ui.dashboard;
//
//import static android.content.Context.MODE_PRIVATE;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
//import androidx.constraintlayout.widget.Group;
//import androidx.fragment.app.Fragment;
//
//import com.agribird.hrmsapp.LoginActivity;
//import com.agribird.hrmsapp.MainActivity;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.dao.AttendanceDao;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.dao.LeaveDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//import com.agribird.hrmsapp.ui.Attendance.AttendanceFragment;
//import com.agribird.hrmsapp.ui.Attendance.AttendanceHistoryFragment;
//import com.agribird.hrmsapp.ui.Leave.ApplyLeaveFragment;
//import com.agribird.hrmsapp.ui.Leave.LeaveApprovalFragment;
//import com.agribird.hrmsapp.ui.Leave.MyLeaveHistoryFragment;
//import com.agribird.hrmsapp.ui.Profile.ProfileFragment;
//import com.agribird.hrmsapp.ui.Reports.EmployeesReportsFragment;
//import com.agribird.hrmsapp.ui.employee.EmployeeAddFragment;
//import com.agribird.hrmsapp.ui.employee.EmployeeListFragment;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.card.MaterialCardView;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//public class DashboardFragment extends Fragment {
//    private MaterialCardView cardEmployee;
//    private MaterialCardView cardEmployeeList;
//    private MaterialCardView cardAttendanceMain;
//    private MaterialCardView cardMyLeave;
//    private MaterialCardView cardAttendanceHistory;
//    private MaterialCardView cardApplyLeave;
//    private MaterialCardView cardLeaveApproval;
//    private MaterialCardView cardEmployeeReport;
//    private MaterialCardView cardAttendanceReport;
//    private MaterialCardView cardUserSalary;
//    private MaterialCardView cardLogout;
//    private MaterialCardView cardEmployees;
//    private MaterialCardView cardPresent;
//    private MaterialCardView cardPending;
//
//    private Group groupAdminCards;
//    private TextView textWel;
//    private TextView txtRole;
//    private TextView txtEmployeeName;
//    private TextView textDate;
//    private TextView textDay;
//
//    private TextView txtCountPresent;
//    private TextView lblPresent;
//
//    private TextView txtCountPending;
//    private TextView lblPending;
//
//    private TextView txtCountEmployees;
//    private TextView lblEmployees;
//
//    private ImageView imageProfile;
//
//    private SharedPreferences sharedPreferences;
//
//    private String name;
//    private String role;
//    private String email;
//    private String empId;
//
//    // Database
//    private HRMSDatabase database;
//    private EmployeeDao employeeDao;
//    private LeaveDao leaveDao;
//    private AttendanceDao attendanceDao;
//
//
//    @Nullable
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState) {
//
//        return inflater.inflate(
//                R.layout.fragment_dashboard,
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
//         database =
//                HRMSDatabase.getInstance(requireContext());
//
//        employeeDao = database.employeeDao();
//        leaveDao = database.leaveDao();
//        attendanceDao = database.attendanceDao();
//
//        textWel =view.findViewById(R.id.textWel);
//        txtRole =view.findViewById(R.id.txtRole);
//        txtEmployeeName =view.findViewById(R.id.txtEmployeeName);
//
//        textDate =view.findViewById(R.id.textDate);
//        textDay =view.findViewById(R.id.textDay);
//
//        txtCountEmployees=view.findViewById(R.id.txtCountEmployees);
//        txtCountPresent=view.findViewById(R.id.txtCountPresent);
//        txtCountPending=view.findViewById(R.id.txtCountPending);
//
//        lblEmployees=view.findViewById(R.id.lblEmployees);
//        lblPending=view.findViewById(R.id.lblPending);
//        lblPresent=view.findViewById(R.id.lblPresent);
//
//        cardEmployee =view.findViewById(R.id.cardEmployee);
//        cardEmployeeList =view.findViewById(R.id.cardEmployeeList);
//        cardAttendanceReport=view.findViewById(R.id.cardAttendanceReport);
//        cardAttendanceMain =view.findViewById(R.id.cardAttendanceMain);
//        cardApplyLeave =view.findViewById(R.id.cardApplyLeave);
//        cardMyLeave =view.findViewById(R.id.cardMyLeave);
//        cardLeaveApproval =view.findViewById(R.id.cardLeaveApproval);
//        cardAttendanceHistory =view.findViewById(R.id.cardAttendanceHistory);
//        cardEmployeeReport =view.findViewById(R.id.cardEmployeeReport);
//        cardUserSalary =view.findViewById(R.id.cardUserSalary);
//        cardLogout =view.findViewById(R.id.cardLogout);
//        groupAdminCards=view.findViewById(R.id.groupAdminCards);
//        imageProfile=view.findViewById(R.id.imageProfile);
//
//
//        cardEmployees =view.findViewById(R.id.cardEmployees);
//        cardPresent = view.findViewById(R.id.cardPresent);
//        cardPending =view.findViewById(R.id.cardPending);
//
//        sharedPreferences = requireActivity()
//                .getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);
//
//        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
//
//            Intent intent = new Intent(requireContext(), MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//            startActivity(intent);
//            requireActivity().finish();
//            return;
//        }
//
//        name = sharedPreferences.getString("userName", "");
//        email = sharedPreferences.getString("userEmail", "");
//        role = sharedPreferences.getString("userRole", "");
//        empId=sharedPreferences.getString("userId","");
//        txtRole.setText(role);
//
//        String firstName = name;
//        if (name != null && name.trim().contains(" ")) {
//            firstName = name.trim().split(" ")[0];
//        }
//
//        txtEmployeeName.setText(firstName);
//
//        SimpleDateFormat dateFormat =
//                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
//
//        SimpleDateFormat dayFormat =
//                new SimpleDateFormat("EEEE", Locale.getDefault());
//
//        Date currentDate = new Date();
//
//        textDate.setText(dateFormat.format(currentDate));
//        textDay.setText(dayFormat.format(currentDate));
//
//
//        boolean isAdmin=role.equals("Super Administrator")
//                ||role.equals("Administrator")
//                ||role.equals("Admin staff")
//                ||role.equals("Admin Manager");
//
//
//        if(isAdmin){
//            groupAdminCards.setVisibility(View.VISIBLE);
//            cardLeaveApproval.setVisibility(View.VISIBLE);
//            summaryData();
//
//        }else{
//
//            groupAdminCards.setVisibility(View.GONE);
//            // cardLeaveApproval.setVisibility(View.INVISIBLE);
//            loadLeaveBalance();
//
//        }
//
//            cardEmployee.setOnClickListener(v -> {
//
//                requireActivity()
//                        .getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(
//                                R.id.fragmentContainer,
//                                new EmployeeAddFragment()
//                        )
//                        .addToBackStack(null)
//                        .commit();
//
//                Toast.makeText(requireContext(),
//                        "Opening Employees...",
//                        Toast.LENGTH_SHORT).show();
//
//            });
//
//        cardEmployeeList.setOnClickListener(v -> {
//
//            EmployeeListFragment employeeListFragment =
//                    new EmployeeListFragment();
//
//            Bundle bundle = new Bundle();
//
//            bundle.putString(
//                    "LOGGED_IN_ROLE",
//                    role
//            );
//
//            employeeListFragment.setArguments(
//                    bundle
//            );
//
//            requireActivity()
//                    .getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(
//                            R.id.fragmentContainer,
//                            employeeListFragment
//                    )
//                    .addToBackStack(null)
//                    .commit();
//
//        });
//
//        cardEmployeeReport.setOnClickListener(v -> {
//            EmployeesReportsFragment reportsFragment = new EmployeesReportsFragment();
//
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, reportsFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        cardAttendanceMain.setOnClickListener(v -> {
//            AttendanceFragment attendanceFragment = new AttendanceFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString("Name", name);
//            // bundle.putString("EmployeeId", employeeId);
//
//            attendanceFragment.setArguments(bundle);
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, attendanceFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//
//        cardAttendanceHistory.setOnClickListener(v -> {
//            AttendanceHistoryFragment historyFragment = new AttendanceHistoryFragment();
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, historyFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        cardApplyLeave.setOnClickListener(v-> {
//            ApplyLeaveFragment applyLeaveFragment=new ApplyLeaveFragment();
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer,applyLeaveFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        cardAttendanceReport.setOnClickListener(v ->
//                Toast.makeText(requireContext(), "Coming Soon...", Toast.LENGTH_SHORT).show()
//        );
//
//        cardLeaveApproval.setOnClickListener(v -> {
//            LeaveApprovalFragment leaveApprovalFragment = new LeaveApprovalFragment();
//
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, leaveApprovalFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        cardMyLeave.setOnClickListener(v -> {
//            MyLeaveHistoryFragment myLeaveHistoryFragment = new MyLeaveHistoryFragment();
//
//            Bundle bundle = new Bundle();
//            bundle.putString("userId", empId);
//            myLeaveHistoryFragment.setArguments(bundle);
//
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, myLeaveHistoryFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        imageProfile.setOnClickListener(v -> {
//            sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
//            String currentName = sharedPreferences.getString("userName", "");
//            String currentEmail = sharedPreferences.getString("userEmail", "");
//            String currentRole = sharedPreferences.getString("userRole", "");
//
//            ProfileFragment profileFragment = new ProfileFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString("Name", currentName);
//            bundle.putString("Role", currentRole);
//            bundle.putString("Email", currentEmail);
//            profileFragment.setArguments(bundle);
//
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, profileFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        cardUserSalary.setOnClickListener(v ->
//                Toast.makeText(requireContext(), "Coming Soon...", Toast.LENGTH_SHORT).show()
//        );
//
//        cardLogout.setOnClickListener(v -> new AlertDialog.Builder(requireActivity())
//                .setTitle("\uD83D\uDEAA Logout")
//                .setMessage("Are you sure you want to logout from AgriBird HRMS?")
//                .setCancelable(false)
//                .setPositiveButton("Yes, Logout", (dialog, which) -> {
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.clear();
//                    editor.apply();
//
//                    Intent intent = new Intent(requireActivity(),LoginActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//
//                    requireActivity().finish();
//
//                    Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
//                })
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                .show()
//        );
//
//    }
//    public void summaryData(){
//        employeeDao.getTotalEmployeesCount().observe(getViewLifecycleOwner(), integer -> txtCountEmployees.setText(integer !=null ? String .valueOf(integer):"0"));
//        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
//        attendanceDao.getPresentCountByDate(todayDate).observe(getViewLifecycleOwner(), count -> txtCountPresent.setText(count !=null ? String.valueOf(count):"0"));
//
//        leaveDao.getPendingLeavesCount().observe(getViewLifecycleOwner(), count -> txtCountPending.setText(count !=null ? String.valueOf(count):"0"));
//    }
//    @SuppressLint("SetTextI18n")
//    public void loadLeaveBalance(){
//
//        int totalLeave=24;
//
//        int usedLeave=leaveDao.getApprovedLeaveCount(empId);
//
//        int remainingLeave=totalLeave-usedLeave;
//
//        txtCountEmployees.setText(String.valueOf(totalLeave));
//        txtCountPresent.setText(String.valueOf(usedLeave));
//        txtCountPending.setText(String.valueOf(remainingLeave));
//
//        lblEmployees.setText("Total Leave");
//        lblPresent.setText("Used Leave");
//        lblPending.setText("Remaining");
//
//    }
//}