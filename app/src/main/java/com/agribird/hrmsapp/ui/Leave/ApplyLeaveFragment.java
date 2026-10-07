package com.agribird.hrmsapp.ui.Leave;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.ApplyLeaveAdapter;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplyLeaveFragment extends Fragment {

    private RecyclerView recyclerViewApplyLeave;
    private ApplyLeaveAdapter applyLeaveAdapter;
    private SharedPreferences sharedPreferences;

    private String loggedInId;
    private String loggedInName;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_apply_leave, container, false);

        recyclerViewApplyLeave = view.findViewById(R.id.recyclerViewApplyLeave);
        recyclerViewApplyLeave.setLayoutManager(new LinearLayoutManager(requireContext()));

        recyclerViewApplyLeave.setHasFixedSize(false);
        sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);

        loggedInName = sharedPreferences.getString("userName", "Auto Filled");
        loggedInId = sharedPreferences.getString("userId", "");

        if (loggedInId.isEmpty()) {
            Toast.makeText(requireContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();

            return view;
        }
        applyLeaveAdapter = new ApplyLeaveAdapter(requireContext(),

                new ApplyLeaveAdapter.OnLeaveActionListener() {

                    @Override
                    public void onSubmitLeave(
                            int position,
                            String employeeName,
                            String employeeId,
                            String leaveType,
                            String startDate,
                            String endDate,
                            String reason) {

                        checkAndSubmitLeave(
                                employeeId,
                                leaveType,
                                startDate,
                                endDate,
                                reason
                        );
                    }
                }, loggedInName, loggedInId);

        recyclerViewApplyLeave.setAdapter(applyLeaveAdapter);

        return view;
    }

    private void checkAndSubmitLeave(
            String employeeId,
            String selectedLeave,
            String startDate,
            String endDate,
            String reason) {

        if (selectedLeave == null || selectedLeave.trim().isEmpty() || selectedLeave.equalsIgnoreCase("Select Leave Type")) {
            Toast.makeText(requireContext(), "Please Select a Leave Type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate == null || startDate.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Start Date is required", Toast.LENGTH_SHORT).show();

            return;
        }

        if (endDate == null || endDate.trim().isEmpty()) {
            Toast.makeText(requireContext(), "End Date is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Reason cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.trim().length() < 10) {
            Toast.makeText(requireContext(), "Please write a detailed reason (minimum 10 characters)!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            dateFormat.setLenient(false);
            Date dateStart = dateFormat.parse(startDate);
            Date dateEnd = dateFormat.parse(endDate);

            if (dateStart == null || dateEnd == null) {
                Toast.makeText(requireContext(), "Invalid date format",Toast.LENGTH_SHORT).show();
                return;
            }

            if (dateEnd.before(dateStart)) {
                Toast.makeText(requireContext(), "End Date cannot be before Start Date!", Toast.LENGTH_LONG).show();
                return;
            }

            checkPendingLeave(
                    employeeId,
                    selectedLeave,
                    startDate,
                    endDate,
                    reason
            );

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPendingLeave(
            String employeeId,
            String selectedLeave,
            String startDate,
            String endDate,
            String reason) {

        RetrofitClient.getLeaveApi(requireContext()).getEmployeePendingLeaveCount(employeeId).enqueue(
                        new Callback<ApiResponse<Long>>() {

                            @Override
                            public void onResponse(
                                    @NonNull Call<ApiResponse<Long>> call,
                                    @NonNull Response<ApiResponse<Long>> response) {

                                if (!isAdded()) {
                                    return;
                                }
                                if (!response.isSuccessful() ||
                                        response.body() == null) {

                                    Toast.makeText(requireContext(), "Unable to check leave status", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                ApiResponse<Long> apiResponse = response.body();

                                Long pendingCount = apiResponse.getData();

                                if (pendingCount == null) {
                                    pendingCount = 0L;
                                }

                                if (pendingCount > 0) {
                                    Toast.makeText(requireContext(), "You already have a Pending Leave request! You cannot apply for a new one.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                saveLeaveRequest(
                                        employeeId,
                                        selectedLeave,
                                        startDate,
                                        endDate,
                                        reason
                                );
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiResponse<Long>> call,
                                    @NonNull Throwable t) {

                                if (!isAdded()) {
                                    return;
                                }
                                Toast.makeText(requireContext(), "Server error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                );
    }
    private void saveLeaveRequest(
            String empId,
            String leaveType,
            String startStr,
            String endStr,
            String reasonStr) {
        String backendStartDate = convertDateToBackendFormat(startStr);

        String backendEndDate = convertDateToBackendFormat(endStr);

        if (backendStartDate == null ||
                backendEndDate == null) {
            Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        LeaveApiModel leave = new LeaveApiModel();

        leave.setLeaveType(leaveType);
        leave.setReason(reasonStr);
        leave.setStartDate(backendStartDate);
        leave.setEndDate(backendEndDate);
        leave.setStatus("Pending");

        RetrofitClient.getLeaveApi(requireContext()).applyLeave(empId, leave).enqueue(
                        new Callback<ApiResponse<LeaveApiModel>>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponse<LeaveApiModel>> call,
                                    @NonNull Response<ApiResponse<LeaveApiModel>> response) {

                                if (!isAdded()) {
                                    return;
                                }

                                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                                    Toast.makeText(requireContext(), "Leave Request Submitted Successfully.", Toast.LENGTH_LONG).show();

                                    getParentFragmentManager().popBackStack();

                                } else {
                                    String message = "Failed to submit leave request";

                                    if (response.body() != null && response.body().getMessage() != null) {
                                        message = response.body().getMessage();
                                    }
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiResponse<LeaveApiModel>> call, @NonNull Throwable t) {

                                if (!isAdded()) {
                                    return;
                                }
                                Toast.makeText(requireContext(), "Server error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                );
    }

    private String convertDateToBackendFormat(
            String date) {

        try {
            SimpleDateFormat input = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

            input.setLenient(false);

            Date parsedDate = input.parse(date);

            if (parsedDate == null) {
                return null;
            }

            SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            return output.format(parsedDate);

        } catch (Exception e) {

            return null;
        }
    }
}
