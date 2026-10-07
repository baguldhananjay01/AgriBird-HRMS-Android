package com.agribird.hrmsapp.ui.Attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.AttendanceApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.AttendanceHistoryAdapter;
import com.agribird.hrmsapp.api.AttendanceApi;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.agribird.hrmsapp.repository.AttendanceRepository;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModel;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModelFactory;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttendanceHistoryFragment extends Fragment {

    private RecyclerView rvAttendanceHistory;
    private View txtNoHistory;
    private ImageView btnBack;
    private EditText etSearchAttendance;
    private ChipGroup chipGroupFilter;

    private AttendanceHistoryAdapter adapter;

    private SharedPreferences sharedPreferences;

    private long employeeId = -1L;

    private AttendanceViewModel attendanceViewModel;
    private List<AttendanceApiModel> fullAttendanceList = new ArrayList<>();

    private String currentFilter = "All";
    private String currentSearchText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_attendance_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAttendanceHistory = view.findViewById(R.id.rvAttendanceHistory);
        txtNoHistory = view.findViewById(R.id.txtNoHistory);
        btnBack = view.findViewById(R.id.btnBack);
        etSearchAttendance = view.findViewById(R.id.etSearchAttendance);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);


        if (rvAttendanceHistory != null) {
            rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvAttendanceHistory.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        if (btnBack != null) {

            btnBack.setOnClickListener(v -> {

                if (getParentFragmentManager() != null) {

                    getParentFragmentManager().popBackStack();
                }
            });
        }

        sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);

        String userId = sharedPreferences.getString("userId", "");

        if (userId == null || userId.trim().isEmpty()) {
            showNoHistory();
            return;
        }

        try {
            employeeId = Long.parseLong(userId.trim());

        } catch (NumberFormatException e) {
            showNoHistory();
            return;
        }

        // Retrofit API
        AttendanceApi attendanceApi = RetrofitClient.getAttendanceApi(requireContext());

        AttendanceRepository repository = new AttendanceRepository(attendanceApi);


        AttendanceViewModelFactory factory = new AttendanceViewModelFactory(repository);


        attendanceViewModel = new ViewModelProvider(this, factory).get(AttendanceViewModel.class);

        fetchAttendanceFromApi();
        setupSearchAndFilters();
    }

    private void fetchAttendanceFromApi() {

        if (employeeId <= 0) {

            showNoHistory();
            return;
        }

        attendanceViewModel.getEmployeeAttendance(employeeId).observe(getViewLifecycleOwner(), state -> {

                            if (state == null) {
                                return;
                            }

                            if (state.isLoading()) {

                                if (txtNoHistory != null) {
                                    txtNoHistory.setVisibility(View.GONE);
                                }

                                if (rvAttendanceHistory != null) {
                                    rvAttendanceHistory.setVisibility(View.VISIBLE);
                                }
                            }

                            // Success
                            else if (state.isSuccess()) {

                                List<AttendanceApiModel> list = state.getData();

                                if (list != null && !list.isEmpty()) {

                                    fullAttendanceList = new ArrayList<>(list);

                                    Collections.reverse(fullAttendanceList);

                                    currentFilter = "All";
                                    currentSearchText = "";

                                    if (etSearchAttendance != null) {etSearchAttendance.setText("");
                                    }

                                    showAttendanceList(fullAttendanceList);

                                } else {

                                    fullAttendanceList.clear();

                                    showNoHistory();
                                }
                            }

                            // Error
                            else if (state.isError()) {

                                fullAttendanceList.clear();

                                showNoHistory();
                            }
                        }
                );
    }

    private void setupSearchAndFilters() {

        // Search
        if (etSearchAttendance != null) {

            etSearchAttendance.addTextChangedListener(
                    new TextWatcher() {

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(
                                CharSequence s, int start, int before, int count) {

                            currentSearchText = s != null ? s.toString() : "";

                            applyFilters();
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    }
            );
        }

        if (chipGroupFilter != null) {

            chipGroupFilter.setOnCheckedStateChangeListener(
                    (group, checkedIds) -> {

                        if (checkedIds == null || checkedIds.isEmpty()) {

                            currentFilter = "All";

                            applyFilters();

                            return;
                        }

                        int checkedId = checkedIds.get(0);

                        if (checkedId == R.id.chipAll) {

                            currentFilter = "All";

                        } else if (checkedId == R.id.chipPresent) {

                            currentFilter = "Present";

                        } else if (checkedId == R.id.chipAbsent) {

                            currentFilter = "Absent";

                        } else if (checkedId == R.id.chipLate) {

                            currentFilter = "Late";

                        } else {
                            currentFilter = "All";
                        }
                        applyFilters();
                    }
            );
        }
    }

    private void applyFilters() {

        if (fullAttendanceList == null || fullAttendanceList.isEmpty()) {

            showNoHistory();
            return;
        }

        List<AttendanceApiModel> filteredList = new ArrayList<>();

        String search = currentSearchText == null ? "" : currentSearchText.trim().toLowerCase();

        for (AttendanceApiModel item : fullAttendanceList) {

            if (item == null) {
                continue;
            }

            boolean matchesStatus = true;

            if (!"All".equalsIgnoreCase(currentFilter)) {

                String status = item.getStatus();
                matchesStatus = status != null && status.equalsIgnoreCase(currentFilter);
            }

            if (!matchesStatus) {
                continue;
            }

            boolean matchesSearch = true;

            if (!search.isEmpty()) {

                String date = item.getDate() != null ? item.getDate().toLowerCase() : "";
                String status = item.getStatus() != null ? item.getStatus().toLowerCase() : "";
                String checkIn = item.getCheckInTime() != null ? item.getCheckInTime().toLowerCase() : "";
                String checkOut = item.getCheckOutTime() != null ? item.getCheckOutTime().toLowerCase() : "";

                matchesSearch = date.contains(search)
                                || status.contains(search)
                                || checkIn.contains(search)
                                || checkOut.contains(search);
            }

            if (matchesSearch) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {

            showNoHistory();

        } else {

            showAttendanceList(filteredList);
        }
    }

    private void showAttendanceList(List<AttendanceApiModel> list) {

        if (!isAdded()) {
            return;
        }

        if (txtNoHistory != null) {

            txtNoHistory.setVisibility(View.GONE);
        }

        if (rvAttendanceHistory != null) {

            rvAttendanceHistory.setVisibility(View.VISIBLE);
            adapter = new AttendanceHistoryAdapter(list);
            rvAttendanceHistory.setAdapter(adapter);
        }
    }

    private void showNoHistory() {

        if (!isAdded()) {
            return;
        }
        if (txtNoHistory != null) {
            txtNoHistory.setVisibility(View.VISIBLE);
        }
        if (rvAttendanceHistory != null) {
            rvAttendanceHistory.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        rvAttendanceHistory = null;
        txtNoHistory = null;
        btnBack = null;
        etSearchAttendance = null;
        chipGroupFilter = null;
        adapter = null;
    }
}
