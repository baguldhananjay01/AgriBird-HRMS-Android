package com.agribird.hrmsapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.AttendanceApiModel;
import com.agribird.hrmsapp.Model.AttendanceSummaryWithTotal;
import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.Model.LeaveBalance;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.api.AttendanceApi;
import com.agribird.hrmsapp.api.EmployeeApi;
import com.agribird.hrmsapp.api.LeaveApi;
import com.agribird.hrmsapp.dao.AttendanceDao;
import com.agribird.hrmsapp.dao.EmployeeDao;
import com.agribird.hrmsapp.dao.LeaveDao;
import com.agribird.hrmsapp.databasecon.HRMSDatabase;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModel;
import com.agribird.hrmsapp.viewmodel.LeaveBalanceViewModel;
import com.agribird.hrmsapp.viewmodel.LeaveViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EMPLOYEE = 1;
    private static final int VIEW_TYPE_ADMIN = 2;

    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private  DashboardClickListener clickListener;

    private final String userRole;
    private final String userId;
    private final String userName;



    private String breakTime = "00m";

    private final EmployeeDao employeeDao;
    private final LeaveDao leaveDao;
    private final AttendanceDao attendanceDao;

    private final AttendanceViewModel attendanceViewModel;

    private final LeaveBalanceViewModel leaveBalanceViewModel;

    private final LeaveViewModel leaveViewModel;

    public DashboardAdapter(
            Context context,
            LifecycleOwner lifecycleOwner,
            DashboardClickListener clickListener,
            AttendanceViewModel attendanceViewModel, LeaveBalanceViewModel leaveBalanceViewModel,
            LeaveViewModel leaveViewModel1) {

        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.clickListener = clickListener;
        this.attendanceViewModel = attendanceViewModel;
        this.leaveBalanceViewModel = leaveBalanceViewModel;
        this.leaveViewModel = leaveViewModel1;

        SharedPreferences prefs = context.getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);

        userRole = prefs.getString("userRole", "");
        userId = prefs.getString("userId", "");
        userName = prefs.getString("userName", "");

        HRMSDatabase database = HRMSDatabase.getInstance(context);

        employeeDao = database.employeeDao();
        leaveDao = database.leaveDao();
        attendanceDao = database.attendanceDao();
    }

    private boolean isAdmin() {

        return userRole != null
                && (
                userRole.equalsIgnoreCase("SUPER_ADMIN")
                        || userRole.equalsIgnoreCase("Super Administrator")
                        || userRole.equalsIgnoreCase("Administrator")
                        || userRole.equalsIgnoreCase("Admin staff")
                        || userRole.equalsIgnoreCase("Admin Manager")
        );
    }

    @Override
    public int getItemViewType(int position) {
        return isAdmin() ? VIEW_TYPE_ADMIN : VIEW_TYPE_EMPLOYEE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_ADMIN) {
            View view = inflater.inflate(R.layout.item_admin_dashboard, parent, false);
            return new AdminDashboardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_employee_dashboard, parent, false);
            return new EmployeeDashboardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position) {

        if (holder instanceof EmployeeDashboardViewHolder) {
            bindEmployeeDashboard((EmployeeDashboardViewHolder) holder);
        } else if (holder instanceof AdminDashboardViewHolder) {
            bindAdminDashboard((AdminDashboardViewHolder) holder);
        }
    }

    @Override
    public int getItemCount() {

        return 1;
    }

    private void bindEmployeeDashboard(EmployeeDashboardViewHolder holder) {
        loadEmployeeAttendance(holder);
        loadEmployeeAttendanceSummary(holder);
        loadEmployeeLeaveBalance(holder);
        setupEmployeeClickListeners(holder);

        if (holder.breakTimeDashboard != null) {
            holder.breakTimeDashboard.setText(breakTime);
        }
    }

    private void loadEmployeeAttendance(EmployeeDashboardViewHolder holder) {

        if (holder == null) {
            return;
        }

        if (attendanceViewModel == null) {

            if (holder.presentStatus != null) {
                holder.presentStatus.setText("UNAVAILABLE");
            }

            if (holder.btnCheckOut != null) {
                holder.btnCheckOut.setEnabled(false);
                holder.btnCheckOut.setText("UNAVAILABLE");
            }

            return;
        }

        if (userId == null || userId.trim().isEmpty()) {

            if (holder.checkInTime != null) {
                holder.checkInTime.setText("--:--");
            }

            if (holder.checkOutTime != null) {
                holder.checkOutTime.setText("--:--");
            }

            if (holder.workingHours != null) {
                holder.workingHours.setText("00h 00m");
            }

            if (holder.presentStatus != null) {
                holder.presentStatus.setText("ID NOT FOUND");
                holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }

            if (holder.btnCheckOut != null) {
                holder.btnCheckOut.setEnabled(false);
                holder.btnCheckOut.setText("UNAVAILABLE");
            }

            return;
        }


        final long employeeDbId;

        try {

            employeeDbId = Long.parseLong(userId.trim());

        } catch (NumberFormatException e) {

            if (holder.checkInTime != null) {
                holder.checkInTime.setText("--:--");
            }

            if (holder.checkOutTime != null) {
                holder.checkOutTime.setText("--:--");
            }

            if (holder.workingHours != null) {
                holder.workingHours.setText("00h 00m");
            }

            if (holder.presentStatus != null) {
                holder.presentStatus.setText("INVALID ID");
                holder.presentStatus.setTextColor(
                        context.getResources().getColor(android.R.color.holo_red_dark)
                );
            }

            if (holder.btnCheckOut != null) {
                holder.btnCheckOut.setEnabled(false);
                holder.btnCheckOut.setText("UNAVAILABLE");
            }

            return;
        }


        if (holder.checkInTime != null) {
            holder.checkInTime.setText("--:--");
        }

        if (holder.checkOutTime != null) {
            holder.checkOutTime.setText("--:--");
        }

        if (holder.workingHours != null) {
            holder.workingHours.setText("00h 00m");
        }

        if (holder.presentStatus != null) {
            holder.presentStatus.setText("LOADING...");
        }

        if (holder.btnCheckOut != null) {
            holder.btnCheckOut.setEnabled(false);
            holder.btnCheckOut.setText("LOADING...");
        }

        attendanceViewModel.getEmployeeAttendance(employeeDbId).observe(lifecycleOwner, state -> {

                    if (state == null) {
                        return;
                    }


                    if (state.isLoading()) {

                        if (holder.checkInTime != null) {
                            holder.checkInTime.setText("--:--");
                        }

                        if (holder.checkOutTime != null) {
                            holder.checkOutTime.setText("--:--");
                        }

                        if (holder.workingHours != null) {
                            holder.workingHours.setText("00h 00m");
                        }

                        if (holder.presentStatus != null) {
                            holder.presentStatus.setText("LOADING...");
                        }

                        if (holder.btnCheckOut != null) {
                            holder.btnCheckOut.setEnabled(false);
                            holder.btnCheckOut.setText("LOADING...");
                        }

                        return;
                    }
                    if (state.isSuccess()) {

                        List<AttendanceApiModel> list = state.getData();
                        AttendanceApiModel todayAttendance = null;

                        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

                        if (list != null && !list.isEmpty()) {

                            for (AttendanceApiModel attendance : list) {

                                if (attendance == null) {
                                    continue;
                                }

                                String backendDate = attendance.getDate();

                                if (backendDate == null) {
                                    continue;
                                }

                                backendDate = backendDate.trim();

                                if (todayDate.equals(backendDate)) {

                                    todayAttendance = attendance;
                                    break;
                                }
                            }
                        }

                        if (todayAttendance == null) {

                            if (holder.checkInTime != null) {
                                holder.checkInTime.setText("--:--");
                            }

                            if (holder.checkOutTime != null) {
                                holder.checkOutTime.setText("--:--");
                            }

                            if (holder.workingHours != null) {
                                holder.workingHours.setText("00h 00m");
                            }

                            if (holder.presentStatus != null) {
                                holder.presentStatus.setText("NOT MARKED");
                                holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                            }

                            if (holder.btnCheckOut != null) {

                                holder.btnCheckOut.setEnabled(false);
                                holder.btnCheckOut.setText("CHECK-IN FIRST");
                            }

                            return;
                        }

                        String checkInTime = todayAttendance.getCheckInTime();

                        String checkOutTime = todayAttendance.getCheckOutTime();

                        String totalHours = todayAttendance.getTotalHours();

                        String status = todayAttendance.getStatus();

                        if (checkInTime == null || checkInTime.trim().isEmpty()) {
                            checkInTime = "--:--";
                        }

                        if (checkOutTime == null || checkOutTime.trim().isEmpty()) {
                            checkOutTime = "--:--";
                        }

                        if (totalHours == null || totalHours.trim().isEmpty()) {
                            totalHours = "00h 00m";
                        }

                        if (status == null || status.trim().isEmpty()) {
                            status = "Present";
                        }

                        if (holder.checkInTime != null) {
                            holder.checkInTime.setText(checkInTime);
                        }

                        if (holder.checkOutTime != null) {holder.checkOutTime.setText(checkOutTime);
                        }


                        if (holder.workingHours != null) {
                            holder.workingHours.setText(totalHours);
                        }
                        if (holder.presentStatus != null) {
                            holder.presentStatus.setText(status.toUpperCase(Locale.getDefault()));
                            if (status.equalsIgnoreCase("Present") || status.equalsIgnoreCase("Completed")) {
                                holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));

                            } else if (status.equalsIgnoreCase("Late")) {

                                holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                            } else if (status.equalsIgnoreCase("Absent")) {

                                holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));

                            } else {
                                holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                            }
                        }


                        if (holder.btnCheckOut != null) {

                            boolean alreadyCheckedOut = !checkOutTime.equals("--:--") && !checkOutTime.isEmpty();
                            if (alreadyCheckedOut) {

                                holder.btnCheckOut.setEnabled(false);
                                holder.btnCheckOut.setText("CHECKED OUT");

                            } else {

                                holder.btnCheckOut.setEnabled(true);
                                holder.btnCheckOut.setText("CHECK-OUT");
                            }
                        }

                        return;
                    }

                    if (state.isError()) {

                        if (holder.checkInTime != null) {
                            holder.checkInTime.setText("--:--");
                        }

                        if (holder.checkOutTime != null) {
                            holder.checkOutTime.setText("--:--");
                        }

                        if (holder.workingHours != null) {
                            holder.workingHours.setText("00h 00m");
                        }

                        if (holder.presentStatus != null) {
                            holder.presentStatus.setText("API ERROR");
                            holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                        }

                        if (holder.btnCheckOut != null) {
                            holder.btnCheckOut.setEnabled(false);
                            holder.btnCheckOut.setText("UNAVAILABLE");
                        }
                    }
                });
    }

    private void loadEmployeeAttendanceSummary(EmployeeDashboardViewHolder holder) {

        if (attendanceViewModel == null || holder == null) {
            return;
        }

        long employeeDbId;

        try {
            employeeDbId = Long.parseLong(userId.trim());
        } catch (Exception e) {

            if (holder.presentCount != null)
                holder.presentCount.setText("0");

            if (holder.absentCount != null)
                holder.absentCount.setText("0");

            if (holder.leaveCount != null)
                holder.leaveCount.setText("0");

            return;
        }

        attendanceViewModel.getEmployeeAttendance(employeeDbId)
                .observe(lifecycleOwner, state -> {

                    if (state == null) {
                        return;
                    }

                    if (state.isLoading()) {

                        if (holder.presentCount != null)
                            holder.presentCount.setText("0");

                        if (holder.absentCount != null)
                            holder.absentCount.setText("0");

                        if (holder.leaveCount != null)
                            holder.leaveCount.setText("0");

                        return;
                    }

                    if (state.isError()) {

                        if (holder.presentCount != null)
                            holder.presentCount.setText("0");

                        if (holder.absentCount != null)
                            holder.absentCount.setText("0");

                        if (holder.leaveCount != null)
                            holder.leaveCount.setText("0");

                        return;
                    }

                    if (!state.isSuccess()) {
                        return;
                    }

                    List<AttendanceApiModel> list = state.getData();

                    int present = 0;
                    int absent = 0;
                    int leave = 0;

                    Calendar calendar = Calendar.getInstance();

                    int currentMonth = calendar.get(Calendar.MONTH);
                    int currentYear = calendar.get(Calendar.YEAR);

                    if (list != null) {

                        for (AttendanceApiModel attendance : list) {

                            if (attendance == null) {
                                continue;
                            }

                            String dateString = attendance.getDate();

                            if (dateString == null || dateString.trim().isEmpty()) {
                                continue;
                            }

                            dateString = dateString.trim();

                            Date attendanceDate = null;

                            String[] dateFormats = {
                                    "yyyy-MM-dd",
                                    "yyyy-MM-dd'T'HH:mm:ss",
                                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                                    "dd-MM-yyyy",
                                    "dd/MM/yyyy",
                                    "MM/dd/yyyy"
                            };

                            for (String format : dateFormats) {

                                try {

                                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

                                    sdf.setLenient(false);

                                    attendanceDate = sdf.parse(dateString);

                                    if (attendanceDate != null) {
                                        break;
                                    }

                                } catch (Exception ignored) {
                                }
                            }

                            if (attendanceDate == null) {
                                continue;
                            }

                            Calendar attendanceCalendar = Calendar.getInstance();

                            attendanceCalendar.setTime(attendanceDate);

                            int attendanceMonth = attendanceCalendar.get(Calendar.MONTH);

                            int attendanceYear = attendanceCalendar.get(Calendar.YEAR);

                            if (attendanceMonth != currentMonth || attendanceYear != currentYear) {
                                continue;
                            }

                            String status = attendance.getStatus();

                            if (status == null) {
                                continue;
                            }

                            status = status.trim();

                            if (status.equalsIgnoreCase("Present")
                                    || status.equalsIgnoreCase("Completed")
                                    || status.equalsIgnoreCase("Late")) {

                                present++;
                            }

                            else if (status.equalsIgnoreCase("Absent")) {

                                absent++;
                            }

                            else if (status.equalsIgnoreCase("Leave")
                                    || status.equalsIgnoreCase("On Leave")
                                    || status.equalsIgnoreCase("Approved Leave")) {

                                leave++;
                            }
                        }
                    }

                    if (holder.presentCount != null) {
                        holder.presentCount.setText(
                                String.valueOf(present)
                        );
                    }

                    if (holder.absentCount != null) {
                        holder.absentCount.setText(
                                String.valueOf(absent)
                        );
                    }

                    if (holder.leaveCount != null) {
                        holder.leaveCount.setText(
                                String.valueOf(leave)
                        );
                    }
                });
    }
    private void loadEmployeeLeaveBalance(EmployeeDashboardViewHolder holder) {

        if (leaveBalanceViewModel == null || userId == null || userId.trim().isEmpty()) {

            setLeaveBalanceValues(holder, 0, 0, 0, 0);
            return;
        }

        String year = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            year = String.valueOf(
                    Year.now().getValue()
            );
        }

        leaveBalanceViewModel.getEmployeeLeaveBalance(userId, year).observe(lifecycleOwner, state -> {

                    if (state == null) return;

                    if (state.isLoading()) {

                        setLeaveBalanceValues(
                                holder,
                                0,
                                0,
                                0,
                                0
                        );

                        return;
                    }

                    if (state.isSuccess()) {

                        int casual = 0;
                        int sick = 0;
                        int earned = 0;
                        int privilege = 0;

                        List<LeaveBalance> balances = state.getData();

                        if (balances != null) {

                            for (LeaveBalance balance : balances) {

                                if (balance == null || balance.getLeaveType() == null) {
                                    continue;
                                }

                                String type = balance.getLeaveType().trim();

                                int remaining = balance.getRemainingDays();

                                if (type.equalsIgnoreCase("Casual") || type.equalsIgnoreCase("Casual Leave")) {

                                    casual = remaining;

                                } else if (
                                        type.equalsIgnoreCase("Sick") || type.equalsIgnoreCase("Sick Leave")) {

                                    sick = remaining;

                                } else if (
                                        type.equalsIgnoreCase("Earned") || type.equalsIgnoreCase("Earned Leave")) {
                                    earned = remaining;

                                } else if (
                                        type.equalsIgnoreCase("Privilege") || type.equalsIgnoreCase("Privilege Leave")) {
                                    privilege = remaining;
                                }
                            }
                        }

                        setLeaveBalanceValues(
                                holder,
                                casual,
                                sick,
                                earned,
                                privilege
                        );

                    } else if (state.isError()) {

                        setLeaveBalanceValues(
                                holder,
                                0,
                                0,
                                0,
                                0
                        );
                    }
                });
    }

    private void setLeaveBalanceValues(
            EmployeeDashboardViewHolder holder,
            int casual,
            int sick,
            int earned,
            int privilege) {

        if (holder.casualCount != null) {
            holder.casualCount.setText(
                    String.valueOf(casual)
            );
        }

        if (holder.sickCount != null) {
            holder.sickCount.setText(
                    String.valueOf(sick)
            );
        }

        if (holder.earnedCount != null) {
            holder.earnedCount.setText(
                    String.valueOf(earned)
            );
        }

        if (holder.privilegeCount != null) {
            holder.privilegeCount.setText(
                    String.valueOf(privilege)
            );
        }
    }

    private void setupEmployeeClickListeners(EmployeeDashboardViewHolder holder) {
        if (holder.btnCheckOut != null) {
            holder.btnCheckOut.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onCheckOutClick();
            });
        }

        if (holder.btnActionAttend != null) {
            holder.btnActionAttend.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onAttendanceClick();
            });
        }

        if (holder.btnActionApplyLeave != null) {
            holder.btnActionApplyLeave.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onApplyLeaveClick();
            });
        }

        if (holder.btnActionMyLeaves != null) {
            holder.btnActionMyLeaves.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onMyLeavesClick();
            });
        }

        if (holder.btnActionPaySlip != null) {
            holder.btnActionPaySlip.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onPayslipClick();
            });
        }

        View.OnClickListener attendanceHistoryClickListener = v -> {
            if (clickListener != null) clickListener.onViewAllAttendance();
        };

        if (holder.txtViewAll != null) {
            holder.txtViewAll.setOnClickListener(attendanceHistoryClickListener);
        }

        if (holder.cardAttendanceSummary != null) {
            holder.cardAttendanceSummary.setOnClickListener(attendanceHistoryClickListener);
        }
    }

    // ADMIN DASHBOARD
    private void bindAdminDashboard(AdminDashboardViewHolder holder) {
        loadWorkforceOverview(holder);
        loadPendingApprovals(holder);
        loadAdminTodayAttendance(holder);
        setupAdminClickListeners(holder);
        setupDonutChart(holder);
    }

    private void loadWorkforceOverview(AdminDashboardViewHolder holder) {
        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        //room data find method
//        if (holder.totalEmployeesCount != null) {
//            employeeDao.getTotalEmployeesCount().observe(lifecycleOwner, count ->
//                    holder.totalEmployeesCount.setText(count != null ? String.valueOf(count) : "0")
//            );
//        }

        if (holder.totalEmployeesCount != null) {

            EmployeeApi employeeApi = RetrofitClient.getEmployeeApi(context);
            employeeApi.getAllEmployees()
                    .enqueue(new Callback<ApiResponse<List<EmployeeApiModel>>>() {

                        @Override
                        public void onResponse(
                                @NonNull Call<ApiResponse<List<EmployeeApiModel>>> call,
                                @NonNull Response<ApiResponse<List<EmployeeApiModel>>> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().getData() != null) {

                                int totalEmployees = response.body().getData().size();
                                holder.totalEmployeesCount.setText(String.valueOf(totalEmployees));

                            } else {
                                holder.totalEmployeesCount.setText("0");
                            }
                        }

                        @Override
                        public void onFailure(
                                @NonNull Call<ApiResponse<List<EmployeeApiModel>>> call,
                                @NonNull Throwable t) {

                            holder.totalEmployeesCount.setText("0");
                        }
                    });
        }

        AttendanceApi attendanceApi=RetrofitClient.getAttendanceApi(context);
        if (holder.presentTodayCount != null) {

            attendanceApi.getPresentCountByDate(todayDate)
                    .enqueue(new Callback<ApiResponse<Long>>() {

                        @Override
                        public void onResponse(Call<ApiResponse<Long>> call,
                                               Response<ApiResponse<Long>> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().getData() != null) {

                                holder.presentTodayCount.setText(
                                        String.valueOf(response.body().getData())
                                );

                            } else {
                                holder.presentTodayCount.setText("0");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                            holder.presentTodayCount.setText("0");
                        }
                    });
        }

        if (holder.onLeaveCount != null) {

            attendanceApi.getOnLeaveCount(todayDate)
                    .enqueue(new Callback<ApiResponse<Long>>() {

                        @Override
                        public void onResponse(Call<ApiResponse<Long>> call,
                                               Response<ApiResponse<Long>> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().getData() != null) {

                                holder.onLeaveCount.setText(
                                        String.valueOf(response.body().getData())
                                );

                            } else {
                                holder.onLeaveCount.setText("0");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                            holder.onLeaveCount.setText("0");
                        }
                    });
        }

        LeaveApi leaveApi=RetrofitClient.getLeaveApi(context);
        if (holder.pendingRequestsCount != null) {

            leaveApi.getPendingLeaveCount()
                    .enqueue(new Callback<ApiResponse<Long>>() {

                        @Override
                        public void onResponse(Call<ApiResponse<Long>> call,
                                               Response<ApiResponse<Long>> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().getData() != null) {

                                holder.pendingRequestsCount.setText(
                                        String.valueOf(response.body().getData())
                                );

                            } else {
                                holder.pendingRequestsCount.setText("0");
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                            holder.pendingRequestsCount.setText("0");
                        }
                    });
        }
    }

    public void setBreakTime(String breakTime) {
        this.breakTime = breakTime;
        notifyDataSetChanged();
    }

    private void loadPendingApprovals(AdminDashboardViewHolder holder) {
        if (holder.leaveRequestCount != null) {

            RetrofitClient.getLeaveApi(context).getPendingLeaveCount().enqueue(new Callback<ApiResponse<Long>>() {

                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<Long>> call,
                                @NonNull Response<ApiResponse<Long>> response) {

                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                                holder.leaveRequestCount.setText(String.valueOf(response.body().getData()));

                            } else {
                                holder.leaveRequestCount.setText("0");
                            }
                        }

                        @Override
                        public void onFailure(
                                @NonNull Call<ApiResponse<Long>> call,
                                @NonNull Throwable t) {

                            holder.leaveRequestCount.setText("0");
                        }
                    });
        }

        if (holder.regularizationCount != null) {
            attendanceDao.getPendingRegularizationCount().observe(lifecycleOwner, count ->
                    holder.regularizationCount.setText(count != null ? String.valueOf(count) : "0")
            );
        }
    }
    private void loadAdminTodayAttendance(AdminDashboardViewHolder holder) {

        AttendanceApi attendanceApi = RetrofitClient.getAttendanceApi(context);

        attendanceApi.getTodayAttendanceSummary().enqueue(new Callback<ApiResponse<AttendanceSummaryWithTotal>>() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(Call<ApiResponse<AttendanceSummaryWithTotal>> call, Response<ApiResponse<AttendanceSummaryWithTotal>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                            AttendanceSummaryWithTotal summary = response.body().getData();

                            int total = summary.getTotalCount();
                            int present = summary.getPresentCount();
                            int absent = summary.getAbsentCount();
                            int leave = summary.getLeaveCount();
                            int halfDay = summary.getHalfDayCount();

                            if (total > 0) {

                                if (holder.presentValue != null) {
                                    holder.presentValue.setText(present + "  (" + formatPercentage(present, total) + "%)");
                                }
                                if (holder.absentValue != null) {holder.absentValue.setText(absent + "  (" + formatPercentage(absent, total) + "%)");
                                }

                                if (holder.onLeaveValue != null) {
                                    holder.onLeaveValue.setText(leave + "  (" + formatPercentage(leave, total) + "%)");
                                }

                                if (holder.halfDayValue != null) {
                                    holder.halfDayValue.setText(halfDay + "  (" + formatPercentage(halfDay, total) + "%)");
                                }

                            } else {

                                if (holder.presentValue != null) {
                                    holder.presentValue.setText("0  (0.0%)");
                                }

                                if (holder.absentValue != null) {
                                    holder.absentValue.setText("0  (0.0%)");
                                }

                                if (holder.onLeaveValue != null) {
                                    holder.onLeaveValue.setText("0  (0.0%)");
                                }

                                if (holder.halfDayValue != null) {
                                    holder.halfDayValue.setText("0  (0.0%)");
                                }
                            }

                            updateDonutChart(
                                    holder,
                                    present,
                                    absent,
                                    leave,
                                    halfDay
                            );

                        } else {

                            setAdminAttendanceZero(holder);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<AttendanceSummaryWithTotal>> call,
                            Throwable t) {

                        setAdminAttendanceZero(holder);

                        Log.e("DashboardAdapter", "Attendance API Error: " + t.getMessage());
                    }
                });
    }
    private void setAdminAttendanceZero(AdminDashboardViewHolder holder) {

        if (holder.presentValue != null) {
            holder.presentValue.setText("0  (0.0%)");
        }

        if (holder.absentValue != null) {
            holder.absentValue.setText("0  (0.0%)");
        }

        if (holder.onLeaveValue != null) {
            holder.onLeaveValue.setText("0  (0.0%)");
        }

        if (holder.halfDayValue != null) {
            holder.halfDayValue.setText("0  (0.0%)");
        }

        updateDonutChart(holder, 0, 0, 0, 0);
    }
    private void setupDonutChart(AdminDashboardViewHolder holder) {
        if (holder.donutChart == null) return;

        // 1. Create Data Entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(81.6f, "Present"));
        entries.add(new PieEntry(8.3f, "Absent"));
        entries.add(new PieEntry(6.6f, "On Leave"));
        entries.add(new PieEntry(3.3f, "Half Day"));

        // 2. Create Data Set
        PieDataSet dataSet = new PieDataSet(entries, "Attendance");

        // 3. Set Colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(context.getColor(R.color.present_green));    // #12B76A
        colors.add(context.getColor(R.color.absent_red));       // #F04438
        colors.add(context.getColor(R.color.leave_orange));     // #F79009
        colors.add(context.getColor(R.color.halfday_purple));   // #8B5CF6
        dataSet.setColors(colors);

        // 4. Hide Values on Chart
        dataSet.setValueTextSize(0f);
        dataSet.setDrawValues(false);

        // 5. Create Pie Data
        PieData data = new PieData(dataSet);
        holder.donutChart.setData(data);

        // 6. Configure Donut Chart
        holder.donutChart.setDrawHoleEnabled(true);
        holder.donutChart.setHoleRadius(55f);
        holder.donutChart.setTransparentCircleRadius(60f);
        holder.donutChart.setDrawEntryLabels(false);
        holder.donutChart.setUsePercentValues(true);
        holder.donutChart.getDescription().setEnabled(false);

        // 7. Center Text
        holder.donutChart.setDrawCenterText(true);
        holder.donutChart.setCenterText("120\nEmployees");
        holder.donutChart.setCenterTextSize(12f);
        holder.donutChart.setCenterTextColor(context.getColor(R.color.text_primary));

        // 8. Animation
        holder.donutChart.animateY(1000);

        // 9. Refresh Chart
        holder.donutChart.invalidate();
    }

    private void updateDonutChart(AdminDashboardViewHolder holder,
                                  int present, int absent, int leave, int halfDay) {
        if (holder.donutChart == null) return;

        int total = present + absent + leave + halfDay;

        if (total == 0) {
            // No data - show empty state
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(100f, "No Data"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(context.getColor(R.color.gray_light));
            dataSet.setValueTextSize(0f);
            dataSet.setDrawValues(false);

            PieData data = new PieData(dataSet);
            holder.donutChart.setData(data);
            holder.donutChart.setCenterText("0\nEmployees");
            holder.donutChart.invalidate();
            return;
        }

        // Calculate Percentages
        float presentPercent = (float) present / total * 100;
        float absentPercent = (float) absent / total * 100;
        float leavePercent = (float) leave / total * 100;
        float halfDayPercent = (float) halfDay / total * 100;

        // Create Data Entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(presentPercent, "Present"));
        entries.add(new PieEntry(absentPercent, "Absent"));
        entries.add(new PieEntry(leavePercent, "On Leave"));
        entries.add(new PieEntry(halfDayPercent, "Half Day"));

        // Create Data Set
        PieDataSet dataSet = new PieDataSet(entries, "Attendance");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(context.getColor(R.color.present_green));
        colors.add(context.getColor(R.color.absent_red));
        colors.add(context.getColor(R.color.leave_orange));
        colors.add(context.getColor(R.color.halfday_purple));
        dataSet.setColors(colors);
        dataSet.setValueTextSize(0f);
        dataSet.setDrawValues(false);

        // Update Chart
        PieData data = new PieData(dataSet);
        holder.donutChart.setData(data);

        // Update Center Text
        holder.donutChart.setCenterText(total + "\nEmployees");

        // Refresh with Animation
        holder.donutChart.invalidate();
        holder.donutChart.animateY(1000);
    }

    private void setupAdminClickListeners(AdminDashboardViewHolder holder) {
        if (holder.btnActionAddEmp != null) {
            holder.btnActionAddEmp.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onAddEmployeeClick();
            });
        }

        if (holder.btnActionEmployeeList != null) {
            holder.btnActionEmployeeList.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onEmployeeListClick();
            });
        }

        if (holder.btnActionDepartments != null) {
            holder.btnActionDepartments.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onDepartmentsClick();
            });
        }

        if (holder.btnActionReports != null) {
            holder.btnActionReports.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onReportsClick();
            });
        }

        if (holder.cardLeaveRequests != null) {
            holder.cardLeaveRequests.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onLeaveApprovalClick();
            });
        }

        if (holder.cardRegularizationRequests != null) {
            holder.cardRegularizationRequests.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onRegularizationClick();
            });
        }

        if (holder.txtPendingApprovalsViewAll != null) {
            holder.txtPendingApprovalsViewAll.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onViewAllApprovals();
            });
        }

        if (holder.txtRecentActivityViewAll != null) {
            holder.txtRecentActivityViewAll.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onViewAllActivity();
            });
        }

        if (holder.txtAttendViewAllViewAll != null) {
            holder.txtAttendViewAllViewAll.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onViewAllAttendance();
            });
        }
    }

    private String formatPercentage(int value, int total) {
        return String.format(Locale.getDefault(), "%.1f", (value * 100.0f) / total);
    }
    static class EmployeeDashboardViewHolder extends RecyclerView.ViewHolder {

        TextView checkInTime, checkOutTime, workingHours, presentStatus;
        MaterialButton btnCheckOut;
        LinearLayout btnActionAttend, btnActionApplyLeave, btnActionMyLeaves, btnActionPaySlip;
        TextView presentCount, absentCount, leaveCount,breakTimeDashboard;
        TextView casualCount, sickCount, earnedCount, privilegeCount;
        TextView txtViewAll;
        MaterialCardView cardAttendanceSummary;

        EmployeeDashboardViewHolder(@NonNull View itemView) {
            super(itemView);

            checkInTime = itemView.findViewById(R.id.txtCheckInTime);
            checkOutTime = itemView.findViewById(R.id.txtCheckOutTime);
            workingHours = itemView.findViewById(R.id.txtWorkingHours);
            presentStatus = itemView.findViewById(R.id.txtPresentStatus);
            btnCheckOut = itemView.findViewById(R.id.btnCheckOut);

            btnActionAttend = itemView.findViewById(R.id.btnActionAttend);
            btnActionApplyLeave = itemView.findViewById(R.id.btnActionApplyLeave);
            btnActionMyLeaves = itemView.findViewById(R.id.btnActionMyLeaves);
            btnActionPaySlip = itemView.findViewById(R.id.btnActionPaySlip);
            breakTimeDashboard=itemView.findViewById(R.id.txtBreakTimeDashboard);

            presentCount = itemView.findViewById(R.id.txtPresentCount);
            absentCount = itemView.findViewById(R.id.txtAbsentCount);
            leaveCount = itemView.findViewById(R.id.txtLeaveCount);

            casualCount = itemView.findViewById(R.id.txtCasualCount);
            sickCount = itemView.findViewById(R.id.txtSickCount);
            earnedCount = itemView.findViewById(R.id.txtEarnedCount);
            privilegeCount = itemView.findViewById(R.id.txtPrivilegeCount);

            txtViewAll = itemView.findViewById(R.id.txtViewAll);
            cardAttendanceSummary = itemView.findViewById(R.id.cardAttendanceSummary);
        }
    }

    // =========================================================
    // ADMIN VIEW HOLDER
    // =========================================================

    static class AdminDashboardViewHolder extends RecyclerView.ViewHolder {

        TextView totalEmployeesCount, presentTodayCount, onLeaveCount, pendingRequestsCount;
        View btnActionAddEmp, btnActionEmployeeList, btnActionDepartments, btnActionReports;
        TextView leaveRequestCount, regularizationCount;
        MaterialCardView cardLeaveRequests, cardRegularizationRequests;
        TextView presentValue, absentValue, onLeaveValue, halfDayValue, txtAttendViewAllViewAll;
        TextView txtPendingApprovalsViewAll, txtRecentActivityViewAll;
        PieChart donutChart;

        AdminDashboardViewHolder(@NonNull View itemView) {
            super(itemView);

            totalEmployeesCount = itemView.findViewById(R.id.txtTotalEmployeesCount);
            presentTodayCount = itemView.findViewById(R.id.txtPresentTodayCount);
            onLeaveCount = itemView.findViewById(R.id.txtOnLeaveCount);
            pendingRequestsCount = itemView.findViewById(R.id.txtPendingRequestsCount);

            btnActionAddEmp = itemView.findViewById(R.id.btnActionAddEmp);
            btnActionEmployeeList = itemView.findViewById(R.id.btnActionEmployeeList);
            btnActionDepartments = itemView.findViewById(R.id.btnActionDepartments);
            btnActionReports = itemView.findViewById(R.id.btnActionReports);

            leaveRequestCount = itemView.findViewById(R.id.txtLeaveRequestCount);
            regularizationCount = itemView.findViewById(R.id.txtRegularizationCount);

            cardLeaveRequests = itemView.findViewById(R.id.cardLeaveRequests);
            cardRegularizationRequests = itemView.findViewById(R.id.cardRegularizationRequests);

            presentValue = itemView.findViewById(R.id.txtPresentValue);
            absentValue = itemView.findViewById(R.id.txtAbsentValue);
            onLeaveValue = itemView.findViewById(R.id.txtOnLeaveValue);
            halfDayValue = itemView.findViewById(R.id.txtHalfDayValue);

            txtPendingApprovalsViewAll = itemView.findViewById(R.id.txtPendingApprovalsViewAll);
            txtRecentActivityViewAll = itemView.findViewById(R.id.txtRecentActivityViewAll);
            txtAttendViewAllViewAll = itemView.findViewById(R.id.txtAttendViewAllViewAll);

            donutChart = itemView.findViewById(R.id.donutChart);
        }
    }
    public interface DashboardClickListener {
        void onNotificationClick();
        void onCheckOutClick();
        void onAttendanceClick();
        void onApplyLeaveClick();
        void onMyLeavesClick();
        void onPayslipClick();
        void onViewAllAttendance();
        void onViewAllAnnouncements();
        void onViewCalendar();
        void onAddEmployeeClick();
        void onEmployeeListClick();
        void onDepartmentsClick();
        void onReportsClick();
        void onLeaveApprovalClick();
        void onRegularizationClick();
        void onViewAllApprovals();
        void onViewAllActivity();
    }
}

//package com.agribird.hrmsapp.adapter;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.LifecycleOwner;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.dao.AttendanceDao;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.dao.LeaveDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.card.MaterialCardView;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private static final int VIEW_TYPE_EMPLOYEE = 1;
//    private static final int VIEW_TYPE_ADMIN = 2;
//
//    private final Context context;
//    private final LifecycleOwner lifecycleOwner;
//    private final DashboardClickListener clickListener;
//
//    private final String userRole;
//    private final String userId;
//    private final String userName;
//
//    private final EmployeeDao employeeDao;
//    private final LeaveDao leaveDao;
//    private final AttendanceDao attendanceDao;
//
//    public DashboardAdapter(
//            Context context,
//            LifecycleOwner lifecycleOwner,
//            DashboardClickListener clickListener) {
//
//        this.context = context;
//        this.lifecycleOwner = lifecycleOwner;
//        this.clickListener = clickListener;
//
//        SharedPreferences prefs =
//                context.getSharedPreferences(
//                        "HRMS_SESSION",
//                        Context.MODE_PRIVATE
//                );
//
//        userRole = prefs.getString("userRole", "");
//        userId = prefs.getString("userId", "");
//        userName = prefs.getString("userName", "");
//
//        HRMSDatabase database =
//                HRMSDatabase.getInstance(context);
//
//        employeeDao = database.employeeDao();
//        leaveDao = database.leaveDao();
//        attendanceDao = database.attendanceDao();
//    }
//
//    private boolean isAdmin() {
//        return userRole != null
//                && (userRole.equalsIgnoreCase("Super Administrator")
//                || userRole.equalsIgnoreCase("Administrator")
//                || userRole.equalsIgnoreCase("Admin staff")
//                || userRole.equalsIgnoreCase("Admin Manager"));
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return isAdmin() ? VIEW_TYPE_ADMIN : VIEW_TYPE_EMPLOYEE;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(
//            @NonNull ViewGroup parent,
//            int viewType) {
//
//        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//
//        if (viewType == VIEW_TYPE_ADMIN) {
//            View view = inflater.inflate(R.layout.item_admin_dashboard, parent, false);
//            return new AdminDashboardViewHolder(view);
//        } else {
//            View view = inflater.inflate(R.layout.item_employee_dashboard, parent, false);
//            return new EmployeeDashboardViewHolder(view);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(
//            @NonNull RecyclerView.ViewHolder holder,
//            int position) {
//
//        if (holder instanceof EmployeeDashboardViewHolder) {
//            bindEmployeeDashboard((EmployeeDashboardViewHolder) holder);
//        } else if (holder instanceof AdminDashboardViewHolder) {
//            bindAdminDashboard((AdminDashboardViewHolder) holder);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return 1;
//    }
//
//    // =========================================================
//    // EMPLOYEE DASHBOARD
//    // =========================================================
//
//    private void bindEmployeeDashboard(EmployeeDashboardViewHolder holder) {
//        loadEmployeeAttendance(holder);
//        loadEmployeeAttendanceSummary(holder);
//        loadEmployeeLeaveBalance(holder);
//        setupEmployeeClickListeners(holder);
//    }
//
//    private void loadEmployeeAttendance(EmployeeDashboardViewHolder holder) {
//        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
//
//        attendanceDao.getTodayAttendance(userId, todayDate)
//                .observe(lifecycleOwner, attendance -> {
//                    if (attendance != null) {
//                        if (holder.checkInTime != null) {
//                            holder.checkInTime.setText(
//                                    attendance.getCheckInTime() != null ? attendance.getCheckInTime() : "--:--"
//                            );
//                        }
//                        if (holder.checkOutTime != null) {
//                            holder.checkOutTime.setText(
//                                    attendance.getCheckOutTime() != null ? attendance.getCheckOutTime() : "--:--"
//                            );
//                        }
//                        if (holder.workingHours != null) {
//                            holder.workingHours.setText(
//                                    attendance.getTotalHours() != null ? attendance.getTotalHours() : "00h 00m"
//                            );
//                        }
//
//                        if (holder.presentStatus != null) {
//                            String status = attendance.getStatus();
//                            if (status != null && !status.trim().isEmpty()) {
//                                holder.presentStatus.setText(status.toUpperCase());
//                                if (status.equalsIgnoreCase("Present") || status.equalsIgnoreCase("Completed")) {
//                                    holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
//                                } else if (status.equalsIgnoreCase("Late")) {
//                                    holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
//                                } else if (status.equalsIgnoreCase("Absent")) {
//                                    holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
//                                } else {
//                                    holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
//                                }
//                            } else {
//                                holder.presentStatus.setText("NOT MARKED");
//                                holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
//                            }
//                        }
//
//                        if (holder.btnCheckOut != null) {
//                            if (attendance.getCheckOutTime() != null
//                                    && !attendance.getCheckOutTime().trim().isEmpty()
//                                    && !attendance.getCheckOutTime().equals("--:--")) {
//                                holder.btnCheckOut.setEnabled(false);
//                                holder.btnCheckOut.setText("CHECKED OUT");
//                            } else {
//                                holder.btnCheckOut.setEnabled(true);
//                                holder.btnCheckOut.setText("CHECK-OUT");
//                            }
//                        }
//
//                    } else {
//                        if (holder.checkInTime != null) holder.checkInTime.setText("--:--");
//                        if (holder.checkOutTime != null) holder.checkOutTime.setText("--:--");
//                        if (holder.workingHours != null) holder.workingHours.setText("00h 00m");
//                        if (holder.presentStatus != null) {
//                            holder.presentStatus.setText("NOT MARKED");
//                            holder.presentStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
//                        }
//                        if (holder.btnCheckOut != null) {
//                            holder.btnCheckOut.setEnabled(false);
//                            holder.btnCheckOut.setText("CHECK-IN FIRST");
//                        }
//                    }
//                });
//    }
//
//    private void loadEmployeeAttendanceSummary(EmployeeDashboardViewHolder holder) {
//        String monthPattern = "%" + new SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(new Date());
//
//        attendanceDao.getMonthlyAttendanceSummary(userId, monthPattern)
//                .observe(lifecycleOwner, summary -> {
//                    if (summary != null) {
//                        if (holder.presentCount != null) holder.presentCount.setText(String.valueOf(summary.getPresentCount()));
//                        if (holder.absentCount != null) holder.absentCount.setText(String.valueOf(summary.getAbsentCount()));
//                        if (holder.leaveCount != null) holder.leaveCount.setText(String.valueOf(summary.getLeaveCount()));
//                    }
//                });
//    }
//
//    private void loadEmployeeLeaveBalance(EmployeeDashboardViewHolder holder) {
//        // Static data - replace with actual database calls if needed
//        if (holder.casualCount != null) holder.casualCount.setText("05");
//        if (holder.sickCount != null) holder.sickCount.setText("03");
//        if (holder.earnedCount != null) holder.earnedCount.setText("08");
//        if (holder.privilegeCount != null) holder.privilegeCount.setText("02");
//    }
//
//    private void setupEmployeeClickListeners(EmployeeDashboardViewHolder holder) {
//        if (holder.btnCheckOut != null) {
//            holder.btnCheckOut.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onCheckOutClick();
//            });
//        }
//
//        if (holder.btnActionAttend != null) {
//            holder.btnActionAttend.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onAttendanceClick();
//            });
//        }
//
//        if (holder.btnActionApplyLeave != null) {
//            holder.btnActionApplyLeave.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onApplyLeaveClick();
//            });
//        }
//
//        if (holder.btnActionMyLeaves != null) {
//            holder.btnActionMyLeaves.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onMyLeavesClick();
//            });
//        }
//
//        if (holder.btnActionPaySlip != null) {
//            holder.btnActionPaySlip.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onPayslipClick();
//            });
//        }
//
//        if (holder.txtViewAll != null) {
//            holder.txtViewAll.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onViewAllAttendance();
//            });
//        }
//
//        if (holder.cardAttendanceAction != null) {
//            holder.cardAttendanceAction.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onViewAllAttendance();
//            });
//        }
//    }
//
//    // =========================================================
//    // ADMIN DASHBOARD
//    // =========================================================
//
//    private void bindAdminDashboard(AdminDashboardViewHolder holder) {
//        loadWorkforceOverview(holder);
//        loadPendingApprovals(holder);
//        loadAdminTodayAttendance(holder);
//        setupAdminClickListeners(holder);
//    }
//
//    private void loadWorkforceOverview(AdminDashboardViewHolder holder) {
//        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
//
//        if (holder.totalEmployeesCount != null) {
//            employeeDao.getTotalEmployeesCount().observe(lifecycleOwner, count ->
//                    holder.totalEmployeesCount.setText(count != null ? String.valueOf(count) : "0")
//            );
//        }
//
////        if (holder.totalEmployeesCount != null) {
////
////            EmployeeApi api = RetrofitClient.getEmployeeApi();
////
////            api.getAllEmployees().enqueue(
////                    new Callback<ApiResponse<List<EmployeeApiModel>>>() {
////
////                        @Override
////                        public void onResponse(
////                                @NonNull Call<ApiResponse<List<EmployeeApiModel>>> call,
////                                @NonNull Response<ApiResponse<List<EmployeeApiModel>>> response) {
////
////                            if (response.isSuccessful()
////                                    && response.body() != null
////                                    && response.body().getData() != null) {
////
////                                int totalEmployees =
////                                        response.body().getData().size();
////
////                                holder.totalEmployeesCount.setText(
////                                        String.valueOf(totalEmployees)
////                                );
////
////                            } else {
////
////                                holder.totalEmployeesCount.setText("0");
////                            }
////                        }
////
////                        @Override
////                        public void onFailure(
////                                @NonNull Call<ApiResponse<List<EmployeeApiModel>>> call,
////                                @NonNull Throwable t) {
////
////                            holder.totalEmployeesCount.setText("0");
////
////                            Toast.makeText(
////                                    context,
////                                    "Unable to load employee count",
////                                    Toast.LENGTH_SHORT
////                            ).show();
////                        }
////                    }
////            );
////        }
//
//        if (holder.presentTodayCount != null) {
//            attendanceDao.getPresentCountByDate(todayDate).observe(lifecycleOwner, count ->
//                    holder.presentTodayCount.setText(count != null ? String.valueOf(count) : "0")
//            );
//        }
//
//        if (holder.onLeaveCount != null) {
//            attendanceDao.getOnLeaveCount(todayDate).observe(lifecycleOwner, count ->
//                    holder.onLeaveCount.setText(count != null ? String.valueOf(count) : "0")
//            );
//        }
//
//        if (holder.pendingRequestsCount != null) {
//            leaveDao.getPendingLeavesCount().observe(lifecycleOwner, count ->
//                    holder.pendingRequestsCount.setText(count != null ? String.valueOf(count) : "0")
//            );
//        }
//    }
//
//    private void loadPendingApprovals(AdminDashboardViewHolder holder) {
//        if (holder.leaveRequestCount != null) {
//            leaveDao.getPendingLeavesCount().observe(lifecycleOwner, count ->
//                    holder.leaveRequestCount.setText(count != null ? String.valueOf(count) : "0")
//            );
//        }
//
//        if (holder.regularizationCount != null) {
//            attendanceDao.getPendingRegularizationCount().observe(lifecycleOwner, count ->
//                    holder.regularizationCount.setText(count != null ? String.valueOf(count) : "0")
//            );
//        }
//    }
//
//    private void loadAdminTodayAttendance(AdminDashboardViewHolder holder) {
//        String todayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
//
//        attendanceDao.getAttendanceSummaryWithTotal(todayDate).observe(lifecycleOwner, summary -> {
//            if (summary != null) {
//                int total = summary.getTotalCount();
//                int present = summary.getPresentCount();
//                int absent = summary.getAbsentCount();
//                int leave = summary.getLeaveCount();
//                int halfDay = summary.getHalfDayCount();
//
//                if (total > 0) {
//                    if (holder.presentValue != null) holder.presentValue.setText(present + "  (" + formatPercentage(present, total) + "%)");
//                    if (holder.absentValue != null) holder.absentValue.setText(absent + "  (" + formatPercentage(absent, total) + "%)");
//                    if (holder.onLeaveValue != null) holder.onLeaveValue.setText(leave + "  (" + formatPercentage(leave, total) + "%)");
//                    if (holder.halfDayValue != null) holder.halfDayValue.setText(halfDay + "  (" + formatPercentage(halfDay, total) + "%)");
//                } else {
//                    if (holder.presentValue != null) holder.presentValue.setText("0  (0.0%)");
//                    if (holder.absentValue != null) holder.absentValue.setText("0  (0.0%)");
//                    if (holder.onLeaveValue != null) holder.onLeaveValue.setText("0  (0.0%)");
//                    if (holder.halfDayValue != null) holder.halfDayValue.setText("0  (0.0%)");
//                }
//            }
//        });
//    }
//
//    private void setupAdminClickListeners(AdminDashboardViewHolder holder) {
//        if (holder.btnActionAddEmp != null) {
//            holder.btnActionAddEmp.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onAddEmployeeClick();
//            });
//        }
//
//        if (holder.btnActionEmployeeList != null) {
//            holder.btnActionEmployeeList.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onEmployeeListClick();
//            });
//        }
//
//        if (holder.btnActionDepartments != null) {
//            holder.btnActionDepartments.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onDepartmentsClick();
//            });
//        }
//
//        if (holder.btnActionReports != null) {
//            holder.btnActionReports.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onReportsClick();
//            });
//        }
//
//        if (holder.cardLeaveRequests != null) {
//            holder.cardLeaveRequests.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onLeaveApprovalClick();
//            });
//        }
//
//        if (holder.cardRegularizationRequests != null) {
//            holder.cardRegularizationRequests.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onRegularizationClick();
//            });
//        }
//
//        if (holder.txtPendingApprovalsViewAll != null) {
//            holder.txtPendingApprovalsViewAll.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onViewAllApprovals();
//            });
//        }
//
//        if (holder.txtRecentActivityViewAll != null) {
//            holder.txtRecentActivityViewAll.setOnClickListener(v -> {
//                if (clickListener != null) clickListener.onViewAllActivity();
//            });
//        }
//
//        if(holder.txtAttendViewAllViewAll !=null){
//            holder.txtAttendViewAllViewAll.setOnClickListener(v->{
//                if(clickListener !=null) clickListener.onViewAllAttendance();
//            });
//        }
//    }
//
//    private String formatPercentage(int value, int total) {
//        return String.format(Locale.getDefault(), "%.1f", (value * 100.0f) / total);
//    }
//
//    // =========================================================
//    // EMPLOYEE VIEW HOLDER
//    // =========================================================
//
//    static class EmployeeDashboardViewHolder extends RecyclerView.ViewHolder {
//
//        TextView checkInTime, checkOutTime, workingHours, presentStatus;
//        MaterialButton btnCheckOut,cardAttendanceAction ;
////      MaterialCardView cardAttendanceAction, cardApplyLeaveAction, cardMyLeavesAction, cardPayslipAction;
//        LinearLayout btnActionAttend,btnActionApplyLeave,btnActionMyLeaves,btnActionPaySlip;
//        TextView presentCount, absentCount, leaveCount;
//        TextView casualCount, sickCount, earnedCount, privilegeCount;
//        TextView txtViewAll;
//
//        EmployeeDashboardViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            checkInTime = itemView.findViewById(R.id.txtCheckInTime);
//            checkOutTime = itemView.findViewById(R.id.txtCheckOutTime);
//            workingHours = itemView.findViewById(R.id.txtWorkingHours);
//            presentStatus = itemView.findViewById(R.id.txtPresentStatus);
//            btnCheckOut = itemView.findViewById(R.id.btnCheckOut);
//
//            btnActionAttend = itemView.findViewById(R.id.btnActionAttend);
//            btnActionApplyLeave = itemView.findViewById(R.id.btnActionApplyLeave);
//            btnActionMyLeaves = itemView.findViewById(R.id.btnActionMyLeaves);
//            btnActionPaySlip = itemView.findViewById(R.id.btnActionPaySlip);
//            cardAttendanceAction=itemView.findViewById(R.id.cardAttendanceSummary);
//
//            presentCount = itemView.findViewById(R.id.txtPresentCount);
//            absentCount = itemView.findViewById(R.id.txtAbsentCount);
//            leaveCount = itemView.findViewById(R.id.txtLeaveCount);
//
//            casualCount = itemView.findViewById(R.id.txtCasualCount);
//            sickCount = itemView.findViewById(R.id.txtSickCount);
//            earnedCount = itemView.findViewById(R.id.txtEarnedCount);
//            privilegeCount = itemView.findViewById(R.id.txtPrivilegeCount);
//
//            txtViewAll = itemView.findViewById(R.id.txtViewAll);
//        }
//    }
//
//    // =========================================================
//    // ADMIN VIEW HOLDER
//    // =========================================================
//
//    static class AdminDashboardViewHolder extends RecyclerView.ViewHolder {
//
//        TextView totalEmployeesCount, presentTodayCount, onLeaveCount, pendingRequestsCount;
//        View btnActionAddEmp, btnActionEmployeeList, btnActionDepartments, btnActionReports;
//        TextView leaveRequestCount, regularizationCount;
//        MaterialCardView cardLeaveRequests, cardRegularizationRequests;
//        TextView presentValue, absentValue, onLeaveValue, halfDayValue,txtAttendViewAllViewAll;
//        TextView txtPendingApprovalsViewAll, txtRecentActivityViewAll;
//
//        AdminDashboardViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            totalEmployeesCount = itemView.findViewById(R.id.txtTotalEmployeesCount);
//            presentTodayCount = itemView.findViewById(R.id.txtPresentTodayCount);
//            onLeaveCount = itemView.findViewById(R.id.txtOnLeaveCount);
//            pendingRequestsCount = itemView.findViewById(R.id.txtPendingRequestsCount);
//
//            btnActionAddEmp = itemView.findViewById(R.id.btnActionAddEmp);
//            btnActionEmployeeList = itemView.findViewById(R.id.btnActionEmployeeList);
//            btnActionDepartments = itemView.findViewById(R.id.btnActionDepartments);
//            btnActionReports = itemView.findViewById(R.id.btnActionReports);
//
//            leaveRequestCount = itemView.findViewById(R.id.txtLeaveRequestCount);
//            regularizationCount = itemView.findViewById(R.id.txtRegularizationCount);
//
//            cardLeaveRequests = itemView.findViewById(R.id.cardLeaveRequests);
//            cardRegularizationRequests = itemView.findViewById(R.id.cardRegularizationRequests);
//
//            presentValue = itemView.findViewById(R.id.txtPresentValue);
//            absentValue = itemView.findViewById(R.id.txtAbsentValue);
//            onLeaveValue = itemView.findViewById(R.id.txtOnLeaveValue);
//            halfDayValue = itemView.findViewById(R.id.txtHalfDayValue);
//
//            txtPendingApprovalsViewAll = itemView.findViewById(R.id.txtPendingApprovalsViewAll);
//            txtRecentActivityViewAll = itemView.findViewById(R.id.txtRecentActivityViewAll);
//            txtAttendViewAllViewAll=itemView.findViewById(R.id.txtAttendViewAllViewAll);
//        }
//    }
//
//    // =========================================================
//    // CLICK LISTENER INTERFACE
//    // =========================================================
//
//    public interface DashboardClickListener {
//        void onNotificationClick();
//        void onCheckOutClick();
//        void onAttendanceClick();
//        void onApplyLeaveClick();
//        void onMyLeavesClick();
//        void onPayslipClick();
//        void onViewAllAttendance();
//        void onViewAllAnnouncements();
//        void onViewCalendar();
//        void onAddEmployeeClick();
//        void onEmployeeListClick();
//        void onDepartmentsClick();
//        void onReportsClick();
//        void onLeaveApprovalClick();
//        void onRegularizationClick();
//        void onViewAllApprovals();
//        void onViewAllActivity();
//    }
//}