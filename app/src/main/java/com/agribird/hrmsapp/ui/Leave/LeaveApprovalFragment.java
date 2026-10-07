package com.agribird.hrmsapp.ui.Leave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.AdminLeaveAdapter;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveApprovalFragment extends Fragment
        implements AdminLeaveAdapter.OnLeaveStatusChangeListener {

    private ImageView iconfilter;
    private TextView txtSubtitle;
    private SearchView searchView;
    private RecyclerView recyclerView;

    private AdminLeaveAdapter adapter;

    private List<LeaveApiModel> allLeaveList = new ArrayList<>();

    private String currentStatusFilter = "All";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leave_approval, container, false);

        iconfilter = view.findViewById(R.id.iconFilter);
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        //txtSubtitle = view.findViewById(R.id.txtSubtitle);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupSearch();
        setupFilter();
        setupAdapter();
        loadDataFromApi();

        return view;
    }

    private void setupSearch() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        currentSearchQuery = query != null ? query.trim() : "";

                        applyFilters();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        currentSearchQuery = newText != null ? newText.trim() : "";

                        applyFilters();

                        return true;
                    }
                }
        );
    }

    private void setupFilter() {
        iconfilter.setOnClickListener(v -> showFilterPopupMenu());
    }

    private void loadDataFromApi() {
        RetrofitClient.getLeaveApi(requireContext()).getAllLeave().enqueue(new Callback<ApiResponse<List<LeaveApiModel>>>() {

                            @Override
                            public void onResponse(@NonNull Call<ApiResponse<List<LeaveApiModel>>> call,
                                    @NonNull Response<ApiResponse<List<LeaveApiModel>>> response) {

                                if (!isAdded()) {
                                    return;
                                }

                                if (response.isSuccessful() && response.body() != null) {
                                    ApiResponse<List<LeaveApiModel>> apiResponse = response.body();

                                    if (apiResponse.getData() != null) {
                                        allLeaveList = new ArrayList<>(apiResponse.getData());

                                    } else {
                                        allLeaveList = new ArrayList<>();
                                    }
                                    adapter.updateData(allLeaveList);
                                    applyFilters();

                                } else {
                                    Toast.makeText(requireContext(), "Failed to load leave requests. Code: " + response.code(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull Call<ApiResponse<List<LeaveApiModel>>> call,
                                    @NonNull Throwable t) {

                                if (!isAdded()) {
                                    return;
                                }

                                Toast.makeText(requireContext(), "API Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                );
    }
    private void setupAdapter() {

        if (!isAdded() || recyclerView == null) {
            return;
        }

        adapter = new AdminLeaveAdapter(requireContext(), allLeaveList,
                new ArrayList<>(allLeaveList),
                this);

        recyclerView.setAdapter(adapter);
    }

    private void showFilterPopupMenu() {

        if (!isAdded() || iconfilter == null) {
            return;
        }

        PopupMenu popup = new PopupMenu(requireContext(), iconfilter);

        popup.getMenu().add("All Requests");
        popup.getMenu().add("Pending Only");
        popup.getMenu().add("Approved Only");
        popup.getMenu().add("Rejected Only");

        popup.setOnMenuItemClickListener(item -> {

            String choice = item.getTitle().toString();

            switch (choice) {

                case "All Requests":

                    currentStatusFilter = "All";

                    if (txtSubtitle != null) {
                        txtSubtitle.setText("All leave requests");
                    }

                    break;

                case "Pending Only":

                    currentStatusFilter = "Pending";

                    if (txtSubtitle != null) {
                        txtSubtitle.setText("Pending leave requests");
                    }
                    break;

                case "Approved Only":

                    currentStatusFilter = "Approved";

                    if (txtSubtitle != null) {
                        txtSubtitle.setText("Approved leave requests");
                    }

                    break;

                case "Rejected Only":

                    currentStatusFilter = "Rejected";

                    if (txtSubtitle != null) {
                        txtSubtitle.setText("Rejected leave requests");
                    }

                    break;
            }
            applyFilters();

            return true;
        });
        popup.show();
    }
    private void applyFilters() {

        if (adapter == null) {
            return;
        }

        adapter.filterList(currentSearchQuery, currentStatusFilter);
    }

    @Override
    public void onStatusChanged(long leaveId, String newStatus) {

        if (leaveId <= 0) {

            Toast.makeText(requireContext(), "Invalid leave ID", Toast.LENGTH_SHORT).show();

            return;
        }

        RetrofitClient.getLeaveApi(requireContext()).updateLeaveStatus(leaveId, newStatus).enqueue(
                        new Callback<ApiResponse<LeaveApiModel>>() {

                            @Override
                            public void onResponse(@NonNull Call<ApiResponse<LeaveApiModel>> call,
                                    @NonNull Response<ApiResponse<LeaveApiModel>> response) {

                                if (!isAdded()) {
                                    return;
                                }

                                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                                    LeaveApiModel updatedLeave = response.body().getData();

                                    String message = response.body().getMessage();

                                    if (message == null || message.trim().isEmpty()) {

                                        message = "Leave " + newStatus.toLowerCase() + " successfully";
                                    }

                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                                    for (int i = 0; i < allLeaveList.size(); i++) {

                                        LeaveApiModel leave = allLeaveList.get(i);

                                        if (leave != null && leave.getId() == leaveId) {

                                            leave.setStatus(newStatus);
                                            break;
                                        }
                                    }

                                    adapter.updateData(allLeaveList);

                                    applyFilters();

                                    loadDataFromApi();

                                } else {

                                    String errorMessage = "Failed to update leave status";

                                    if (response.body() != null && response.body().getMessage() != null) {

                                        errorMessage = response.body().getMessage();
                                    }

                                    Toast.makeText(requireContext(), errorMessage + " (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiResponse<LeaveApiModel>> call,
                                    @NonNull Throwable t) {

                                if (!isAdded()) {
                                    return;
                                }

                                Toast.makeText(requireContext(), "API Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                );
    }


    @Override
    public void onDestroyView() {

        super.onDestroyView();

        iconfilter = null;
        txtSubtitle = null;
        searchView = null;
        recyclerView = null;
        adapter = null;
    }
}

//package com.agribird.hrmsapp.ui.Leave;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import androidx.appcompat.widget.PopupMenu;
//import androidx.appcompat.widget.SearchView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.LeaveWithEmployee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.adapter.AdminLeaveAdapter;
//import com.agribird.hrmsapp.dao.LeaveDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class LeaveApprovalFragment extends Fragment implements AdminLeaveAdapter.OnLeaveStatusChangeListener {
//
//    private ImageView iconArrow, iconfilter;
//    private TextView txtSubtitle;
//    private SearchView searchView;
//    private RecyclerView recyclerView;
//
//    private HRMSDatabase database;
//    private LeaveDao leaveDao;
//    private AdminLeaveAdapter adapter;
//    private List<LeaveWithEmployee> allLeaveList;
//
//    private String currentStatusFilter = "All";
//    private String currentSearchQuery = "";
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_leave_approval, container, false);
//
////        iconArrow = view.findViewById(R.id.iconArrow);
//        iconfilter = view.findViewById(R.id.iconFilter);
////        txtSubtitle = view.findViewById(R.id.txtSubtitle);
//        searchView = view.findViewById(R.id.searchView);
//        recyclerView = view.findViewById(R.id.recyclerView);
//
//        database = HRMSDatabase.getInstance(requireContext());
//        leaveDao = database.leaveDao();
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        allLeaveList = new ArrayList<>();
//
////        iconArrow.setOnClickListener(v -> {
////            if (getParentFragmentManager() != null) {
////                getParentFragmentManager().popBackStack();
////            }
////        });
//
//        loadDataFromDatabase();
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                currentSearchQuery = newText;
//                applyFilters();
//                return true;
//            }
//        });
//
//        iconfilter.setOnClickListener(v -> showFilterPopupMenu());
//
//        return view;
//    }
//
//    private void loadDataFromDatabase() {
//        new Thread(() -> {
//            List<LeaveWithEmployee> data = leaveDao.getAllLeaveRequests();
//
//            if (isAdded() && getActivity() != null) {
//                requireActivity().runOnUiThread(() -> {
//                    if (data != null) {
//                        allLeaveList = new ArrayList<>(data);
//                    } else {
//                        allLeaveList = new ArrayList<>();
//                    }
//                    adapter = new AdminLeaveAdapter(
//                            requireContext(),
//                            allLeaveList,
//                            new ArrayList<>(allLeaveList),
//                            LeaveApprovalFragment.this
//                    );
//                    recyclerView.setAdapter(adapter);
//                    applyFilters();
//                });
//            }
//        }).start();
//    }
//
//    private void showFilterPopupMenu() {
//        PopupMenu popup = new PopupMenu(requireContext(), iconfilter);
//        popup.getMenu().add("All Requests");
//        popup.getMenu().add("Pending Only");
//        popup.getMenu().add("Approved Only");
//        popup.getMenu().add("Rejected Only");
//
//        popup.setOnMenuItemClickListener(item -> {
//            String choice = item.getTitle().toString();
//
//            if (choice.equals("All Requests")) {
//                currentStatusFilter = "All";
//                txtSubtitle.setText("All leave requests");
//            } else if (choice.equals("Pending Only")) {
//                currentStatusFilter = "Pending";
//                txtSubtitle.setText("Pending leave requests");
//            } else if (choice.equals("Approved Only")) {
//                currentStatusFilter = "Approved";
//                txtSubtitle.setText("Approved leave requests");
//            } else if (choice.equals("Rejected Only")) {
//                currentStatusFilter = "Rejected";
//                txtSubtitle.setText("Rejected leave requests");
//            }
//
//            applyFilters();
//            return true;
//        });
//
//        popup.show();
//    }
//
//    private void applyFilters() {
//        if (adapter != null) {
//            adapter.filterList(currentSearchQuery, currentStatusFilter);
//        }
//    }
//
//    @Override
//    public void onStatusChanged(int leaveId, String newStatus) {
//        new Thread(() -> {
//            leaveDao.updateLeaveStatus(leaveId, newStatus);
//
//            if (isAdded() && getActivity() != null) {
//                requireActivity().runOnUiThread(() -> {
//                    Toast.makeText(requireContext(), "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
//                    loadDataFromDatabase();
//                });
//            }
//        }).start();
//    }
//}