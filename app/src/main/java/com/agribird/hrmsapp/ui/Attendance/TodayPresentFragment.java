package com.agribird.hrmsapp.ui.Attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.AttendanceApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.EmployeeAdapter;
import com.agribird.hrmsapp.api.AttendanceApi;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.agribird.hrmsapp.repository.AttendanceRepository;
import com.agribird.hrmsapp.utils.ApiState;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModel;
import com.agribird.hrmsapp.viewmodel.AttendanceViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class TodayPresentFragment extends Fragment {

    private ImageView iconArrow;
    private TextView txtTotalStaff, txtPresentCount, txtAbsentCount;
    private RecyclerView recyclerTodayPresent;

    private AttendanceViewModel attendanceViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_today_present, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        iconArrow = view.findViewById(R.id.iconArrow);
        txtTotalStaff = view.findViewById(R.id.txtTotalStaff);
        txtPresentCount = view.findViewById(R.id.txtPresentCount);
        txtAbsentCount = view.findViewById(R.id.txtAbsentCount);
        recyclerTodayPresent = view.findViewById(R.id.recyclerTodayPresent);

        recyclerTodayPresent.setLayoutManager(new LinearLayoutManager(requireContext()));

        AttendanceApi attendanceApi = RetrofitClient.getAttendanceApi(requireContext());

        AttendanceRepository repository = new AttendanceRepository(attendanceApi);

        AttendanceViewModelFactory factory = new AttendanceViewModelFactory(repository);

        attendanceViewModel = new ViewModelProvider(this, factory).get(AttendanceViewModel.class);

        iconArrow.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        loadTodayAttendance();
        loadTodayPresentCount();
        loadTodayAttendanceCount();
    }

    private void loadTodayAttendance() {

        attendanceViewModel.getTodayAttendance()
                .observe(getViewLifecycleOwner(), state -> {

                    if (state == null) {
                        return;
                    }
                    if (state.isLoading()) {
                    } else if (state.isSuccess()) {
                        List<AttendanceApiModel> attendanceList = state.getData();

                        if (attendanceList == null) {
                            attendanceList = new ArrayList<>();
                        }

                        final List<AttendanceApiModel> finalList = attendanceList;

                        EmployeeAdapter adapter = new EmployeeAdapter(new ArrayList<>(), "ADMIN", (employee, position) -> {});

                        recyclerTodayPresent.setAdapter(adapter);

                    } else if (state.isError()) {
                        Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadTodayPresentCount() {

        attendanceViewModel.getTodayPresentCount()
                .observe(getViewLifecycleOwner(), state -> {

                    if (state == null) {
                        return;
                    }

                    if (state.isSuccess()) {
                        Long count = state.getData();
                        txtPresentCount.setText(String.valueOf(count != null ? count : 0));
                    } else if (state.isError()) {

                        txtPresentCount.setText("0");
                    }
                });
    }

    private void loadTodayAttendanceCount() {

        attendanceViewModel.getTodayAttendanceCount().observe(getViewLifecycleOwner(), state -> {
                    if (state == null) {
                        return;
                    }

                    if (state.isSuccess()) {

                        Long count = state.getData();

                        int total = count != null ? count.intValue() : 0;
                        txtTotalStaff.setText(String.valueOf(total)
                        );

                        updateAbsentCount();

                    } else if (state.isError()) {

                        txtTotalStaff.setText("0");
                        txtAbsentCount.setText("0");
                    }
                });
    }

    private void updateAbsentCount() {

        String totalText = txtTotalStaff.getText().toString();
        String presentText = txtPresentCount.getText().toString();

        try {

            int total = Integer.parseInt(totalText);
            int present = Integer.parseInt(presentText);

            int absent = total - present;

            if (absent < 0) {
                absent = 0;
            }
            txtAbsentCount.setText(String.valueOf(absent));
        } catch (Exception e) {
            txtAbsentCount.setText("0");
        }
    }
}
//package com.agribird.hrmsapp.ui.Attendance;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.adapter.EmployeeAdapter;
//import com.agribird.hrmsapp.dao.AttendanceDao;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class TodayPresentFragment extends Fragment {
//
//    private ImageView iconArrow, imgLogo;
//    private TextView txtSubtitle, txtTotalStaff, txtPresentCount, txtAbsentCount;
//    private RecyclerView recyclerTodayPresent;
//
//    private HRMSDatabase database;
//    private EmployeeDao employeeDao;
//    private AttendanceDao attendanceDao;
//
//    private String todayDateString;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_today_present, container, false);
//
//        iconArrow = view.findViewById(R.id.iconArrow);
////        imgLogo = view.findViewById(R.id.imgLogo);
////        txtSubtitle = view.findViewById(R.id.txtSubtitle);
//        txtTotalStaff = view.findViewById(R.id.txtTotalStaff);
//        txtPresentCount = view.findViewById(R.id.txtPresentCount);
//        txtAbsentCount = view.findViewById(R.id.txtAbsentCount);
//        recyclerTodayPresent = view.findViewById(R.id.recyclerTodayPresent);
//
//        database = HRMSDatabase.getInstance(requireContext());
//        employeeDao = database.employeeDao();
//        attendanceDao = database.attendanceDao();
//
//        recyclerTodayPresent.setLayoutManager(new LinearLayoutManager(requireContext()));
//
////        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
////        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
////
////        Date currentDate = new Date();
////        txtSubtitle.setText(displayFormat.format(currentDate));
////        todayDateString = dbFormat.format(currentDate);
//
//        iconArrow.setOnClickListener(v -> {
//            if (getParentFragmentManager() != null) {
//                getParentFragmentManager().popBackStack();
//            }
//        });
//
//        loadAttendanceData();
//
//        return view;
//    }
//
//    private void loadAttendanceData() {
//        new Thread(() -> {
//            List<Employee> allEmployees = employeeDao.getAllEmployee();
//            int totalStaff = (allEmployees != null) ? allEmployees.size() : 0;
//
//            List<Employee> presentEmployees = attendanceDao.getTodayPresentEmployees(todayDateString);
//
//            if (presentEmployees == null) {
//                presentEmployees = new ArrayList<>();
//            }
//
//            int presentCount = presentEmployees.size();
//            int absentCount = totalStaff - presentCount;
//            if (absentCount < 0) absentCount = 0;
//
//            ArrayList<Employee> presentArrayList = new ArrayList<>(presentEmployees);
//            final int finalAbsentCount = absentCount;
//
//            if (isAdded() && getActivity() != null) {
//                requireActivity().runOnUiThread(() -> {
//                    txtTotalStaff.setText(String.valueOf(totalStaff));
//                    txtPresentCount.setText(String.valueOf(presentCount));
//                    txtAbsentCount.setText(String.valueOf(finalAbsentCount));
//
//                    EmployeeAdapter adapter = new EmployeeAdapter(
//                            presentArrayList,
//                            "ADMIN",
//                            (employee, position) -> {
//                                // Toast.makeText(requireContext(), employee.getName(), Toast.LENGTH_SHORT).show();
//                            }
//                    );
//
//                    recyclerTodayPresent.setAdapter(adapter);
//                });
//            }
//        }).start();
//    }
//}