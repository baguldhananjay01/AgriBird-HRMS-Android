package com.agribird.hrmsapp.ui.Leave;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.LeaveHistoryAdapter;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLeaveHistoryFragment extends Fragment {

    private ImageView iconArrow, iconCal;
    private TextView txtTitle, txtSubtitle;
    private SearchView searchView;
    private RecyclerView recyclerView;

    private LeaveHistoryAdapter adapter;
    private List<LeaveApiModel> leaveList = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private String employeeId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_leave_history, container, false);

        // iconArrow = view.findViewById(R.id.iconArrow);
        // iconCal = view.findViewById(R.id.iconCal);
        txtTitle = view.findViewById(R.id.txtTitle);
        // txtSubtitle = view.findViewById(R.id.txtSubtitle);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
        employeeId = sharedPreferences.getString("userId", "");
        loadLeaveHistory();

        // iconArrow.setOnClickListener(v -> {
        //     getParentFragmentManager().popBackStack();
        // });

        return view;
    }

    private void loadLeaveHistory() {

        if (employeeId == null || employeeId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Employee ID not found!", Toast.LENGTH_SHORT).show();

            leaveList = new ArrayList<>();
            adapter = new LeaveHistoryAdapter(leaveList);
            recyclerView.setAdapter(adapter);
            return;
        }

        RetrofitClient.getLeaveApi(requireContext()).getEmployeeLeaves(employeeId)
                .enqueue(new Callback<ApiResponse<List<LeaveApiModel>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<LeaveApiModel>>> call,
                            Response<ApiResponse<List<LeaveApiModel>>> response) {

                        if (!isAdded()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            leaveList = new ArrayList<>(response.body().getData());

                        } else {
                            leaveList = new ArrayList<>();
                        }

                        adapter = new LeaveHistoryAdapter(leaveList);
                        recyclerView.setAdapter(adapter);

                        if (leaveList.isEmpty()) {
                            Toast.makeText(requireContext(), "No leave history found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<LeaveApiModel>>> call,
                            Throwable t) {

                        if (!isAdded()) {
                            return;
                        }

                        leaveList = new ArrayList<>();

                        adapter = new LeaveHistoryAdapter(leaveList);
                        recyclerView.setAdapter(adapter);

                        Toast.makeText(requireContext(), "Failed to load leave history", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
//package com.agribird.hrmsapp.ui.Leave;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.SearchView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.LeaveEntity;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.adapter.LeaveHistoryAdapter;
//import com.agribird.hrmsapp.dao.LeaveDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MyLeaveHistoryFragment extends Fragment {
//
//    private ImageView iconArrow, iconCal;
//    private TextView txtTitle, txtSubtitle;
//    private SearchView searchView;
//    private RecyclerView recyclerView;
//
//    private HRMSDatabase database;
//    private LeaveDao leaveDao;
//    private LeaveHistoryAdapter adapter;
//    private List<LeaveEntity> leaveList;
//    private SharedPreferences sharedPreferences;
//    private String employeeId;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_my_leave_history, container, false);
//
//        database = HRMSDatabase.getInstance(requireContext());
//        leaveDao = database.leaveDao();
//
////        iconArrow = view.findViewById(R.id.iconArrow);
////        iconCal = view.findViewById(R.id.iconCal);
//        txtTitle = view.findViewById(R.id.txtTitle);
////        txtSubtitle = view.findViewById(R.id.txtSubtitle);
//        recyclerView = view.findViewById(R.id.recyclerView);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//
//        sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
//        employeeId = sharedPreferences.getString("userId", "");
//
//        loadLeaveHistory();
//
////        iconArrow.setOnClickListener(v -> {
////            if (getParentFragmentManager() != null) {
////                getParentFragmentManager().popBackStack();
////            }
////        });
//
//        return view;
//    }
//
//    private void loadLeaveHistory() {
//        if (employeeId == null || employeeId.isEmpty()) {
//            Toast.makeText(requireContext(), "Employee ID not found!", Toast.LENGTH_SHORT).show();
//            leaveList = new ArrayList<>();
//            adapter = new LeaveHistoryAdapter((ArrayList<LeaveEntity>) leaveList);
//            recyclerView.setAdapter(adapter);
//            return;
//        }
//
//        new Thread(() -> {
//            List<LeaveEntity> dblist = leaveDao.getEmployeeLeaves(employeeId);
//
//            if (dblist != null) {
//                leaveList = new ArrayList<>(dblist);
//            } else {
//                leaveList = new ArrayList<>();
//            }
//
//            if (isAdded() && getActivity() != null) {
//                requireActivity().runOnUiThread(() -> {
//                    adapter = new LeaveHistoryAdapter((ArrayList<LeaveEntity>) leaveList);
//                    recyclerView.setAdapter(adapter);
//
//                    if (leaveList.isEmpty()) {
//                        Toast.makeText(requireContext(), "No leave history found.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        }).start();
//    }
//}