package com.agribird.hrmsapp.ui.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.Employee;
import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.EmployeeEditAdapter;
import com.agribird.hrmsapp.api.EmployeeApi;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.agribird.hrmsapp.viewmodel.EmployeeViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEmployeeFragment extends Fragment {

    private RecyclerView recyclerView;
    private Employee employee;

    //private EmployeeApi employeeApi;

    private EmployeeViewModel employeeViewModel;

    private ConstraintLayout editEmployeeHeader;
    private int employeePosition = -1;

    public EditEmployeeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_edit_employee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        employeeViewModel = new ViewModelProvider(this)
                .get(EmployeeViewModel.class);

       // employeeApi = RetrofitClient.getEmployeeApi();
        editEmployeeHeader = view.findViewById(R.id.editEmployeeHeader);
        recyclerView = view.findViewById(R.id.recyclerEditEmployee);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        if (editEmployeeHeader != null) {

                            if (recyclerView.canScrollVertically(-1)) {

                                editEmployeeHeader.setBackgroundColor(android.graphics.Color.parseColor("#E6E6FA"));
                            } else {
                                editEmployeeHeader.setBackgroundColor(android.graphics.Color.parseColor("#F4F7F5")
                                );
                            }
                        }
                    }
                }
        );
        Bundle args = getArguments();

        if (args == null) {
            Toast.makeText(requireContext(), "Employee data not found", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        String employeeId = args.getString("EMP_ID", "");

        employeePosition = args.getInt("POSITION", -1);

        if (employeeId.isEmpty()) {

            Toast.makeText(requireContext(), "Employee ID not found", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        loadEmployeeFromBackend(employeeId);
    }

    // GET EMPLOYEE FROM BACKEND
    private void loadEmployeeFromBackend(String employeeId) {

        employeeViewModel.getEmployeeByEmpId(employeeId).observe(getViewLifecycleOwner(), state -> {

                    if (state == null) {
                        return;
                    }

                    switch (state.getStatus()) {

                        case LOADING:
                            Toast.makeText(requireContext(), "Loading employee...", Toast.LENGTH_SHORT).show();
                            break;

                        case SUCCESS:

                            EmployeeApiModel apiEmployee = state.getData();

                            if (apiEmployee == null) {
                                Toast.makeText(requireContext(), "Employee data not found", Toast.LENGTH_LONG).show();
                                return;
                            }

                            employee = convertToRoomEmployee(apiEmployee);
                            setupAdapter();
                            break;

                        case ERROR:

                            String message = state.getMessage();

                            if (message == null || message.trim().isEmpty()) {
                                message = "Failed to load employee";
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                            break;
                    }
                });
    }

    private Employee convertToRoomEmployee(
            EmployeeApiModel apiEmployee) {

        Employee employee = new Employee(
                apiEmployee.getEmpID(),
                apiEmployee.getName(),
                apiEmployee.getEmail(),
                apiEmployee.getRole(),
                apiEmployee.getDepartment(),
                apiEmployee.getPhone(),
                apiEmployee.getJoiningDate(),
                apiEmployee.getPassword(),
                apiEmployee.getEmergencyPhone(),
                apiEmployee.getBloodGroup(),
                apiEmployee.getAddress()
        );

        employee.setStatus(apiEmployee.getStatus());

        return employee;
    }
    private void setupAdapter() {

        if (!isAdded() || employee == null) {
            return;
        }

        EmployeeEditAdapter adapter = new EmployeeEditAdapter(
                requireContext(),
                employee,
                employeePosition,
                new EmployeeEditAdapter.OnEmployeeEditListener() {

                    @Override
                    public void onUpdateEmployee(
                            String employeeId,
                            EmployeeApiModel apiEmployee) {

                        updateEmployee(employeeId, apiEmployee);
                    }

                    @Override
                    public void onCancel() {

                        getParentFragmentManager()
                                .popBackStack();
                    }
                }
        );

        recyclerView.setAdapter(adapter);
    }

    private void updateEmployee(String employeeId, EmployeeApiModel apiEmployee) {

        employeeViewModel.updateEmployee(employeeId, apiEmployee).observe(getViewLifecycleOwner(), state -> {

                    if (state == null) {
                        return;
                    }

                    switch (state.getStatus()) {
                        case LOADING:
                            Toast.makeText(requireContext(), "Updating employee...", Toast.LENGTH_SHORT).show();
                            break;

                        case SUCCESS:

                            EmployeeApiModel updatedApiEmployee = state.getData();
                            if (updatedApiEmployee == null) {

                                Toast.makeText(requireContext(), "Invalid server response", Toast.LENGTH_LONG).show();
                                return;
                            }

                            Employee updatedEmployee = convertToRoomEmployee(updatedApiEmployee);

                            sendResult(updatedEmployee, employeePosition);

                            Toast.makeText(requireContext(), "Employee Updated Successfully", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager()
                                    .popBackStack();

                            break;

                        case ERROR:

                            String message = state.getMessage();

                            if (message == null || message.trim().isEmpty()) {

                                message = "Failed to update employee";
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            break;
                    }
                });
    }
    private void sendResult(Employee updatedEmployee, int position) {

        Bundle result = new Bundle();

        result.putBoolean("EDIT_EMPLOYEE", true);

        result.putInt("POSITION", position);
        result.putString("EMP_ID", updatedEmployee.getEmpId());
        result.putString("EMP_NAME", updatedEmployee.getName());
        result.putString("EMP_EMAIL",updatedEmployee.getEmail());
        result.putString("EMP_PHONE", updatedEmployee.getPhone());
        result.putString("EMP_EMERGENCY_PHONE", updatedEmployee.getEmergencyPhone());
        result.putString("EMP_BLOOD_GROUP", updatedEmployee.getBloodGroup());
        result.putString("EMP_ADDRESS", updatedEmployee.getAddress());
        result.putString("EMP_ROLE", updatedEmployee.getRole());
        result.putString("EMP_DEPARTMENT", updatedEmployee.getDepartment());
        result.putString("EMP_Date", updatedEmployee.getJoiningDate());
        getParentFragmentManager().setFragmentResult("EDIT_EMPLOYEE_RESULT", result);
        getParentFragmentManager().setFragmentResult("EMPLOYEE_DETAILS_RESULT", result);
    }
}
//=================================================================================================
//package com.agribird.hrmsapp.ui.employee;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.adapter.EmployeeEditAdapter;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//public class EditEmployeeFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private HRMSDatabase database;
//    private EmployeeDao employeeDao;
//    private Employee employee;
//    private ConstraintLayout editEmployeeHeader;
//    private int employeePosition = -1;
//
//    public EditEmployeeFragment() {
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState) {
//
//        return inflater.inflate(R.layout.fragment_edit_employee, container, false);
//    }
//
//    @Override
//    public void onViewCreated(
//            @NonNull View view,
//            @Nullable Bundle savedInstanceState) {
//
//        super.onViewCreated(view, savedInstanceState);
//
//        // DATABASE INITIALIZATION
//        database = HRMSDatabase.getInstance(requireContext());
//        employeeDao = database.employeeDao();
//
//        editEmployeeHeader=view.findViewById(R.id.editEmployeeHeader);
//        recyclerView = view.findViewById(R.id.recyclerEditEmployee);
//
//
//
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView,
//                                   int dx,
//                                   int dy
//            ) {
//
//                super.onScrolled(recyclerView, dx, dy);
//
//                if (editEmployeeHeader != null) {
//                    if (recyclerView.canScrollVertically(-1)) {
//                        editEmployeeHeader.setBackgroundColor(android.graphics.Color.parseColor("#E6E6FA"));
//
//                    } else {
//
//                        editEmployeeHeader.setBackgroundColor(android.graphics.Color.parseColor("#F4F7F5"));
//                    }
//                }
//            }
//        }
//        );
//
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        recyclerView.setNestedScrollingEnabled(false);
//
//        Bundle args = getArguments();
//
//        if (args == null) {
//            Toast.makeText(requireContext(), "Employee data not found", Toast.LENGTH_SHORT).show();
//            getParentFragmentManager().popBackStack();
//            return;
//        }
//
//        String employeeId = args.getString("EMP_ID", "");
//        employeePosition = args.getInt("POSITION", -1);
//
//        // LOAD EMPLOYEE FROM ROOM DATABASE
//        new Thread(() -> {
//            Employee loadedEmployee = employeeDao.getEmployeeById(employeeId);
//
//            if (getActivity() == null || !isAdded()) return;
//
//            requireActivity().runOnUiThread(() -> {
//                if (loadedEmployee == null) {
//                    Toast.makeText(requireContext(), "Employee not found", Toast.LENGTH_SHORT).show();
//                    getParentFragmentManager().popBackStack();
//                    return;
//                }
//
//                employee = loadedEmployee;
//                setupAdapter();
//            });
//
//        }).start();
//    }
//
//    private void setupAdapter() {
//        if (!isAdded()) return;
//
//        EmployeeEditAdapter adapter = new EmployeeEditAdapter(
//                requireContext(),
//                employeeDao,
//                employee,
//                employeePosition,
//                new EmployeeEditAdapter.OnEmployeeEditListener() {
//
//                    @Override
//                    public void onEmployeeUpdated(Employee updatedEmployee, int position) {
//                        sendResult(updatedEmployee, position);
//                        getParentFragmentManager().popBackStack();
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        getParentFragmentManager().popBackStack();
//                    }
//                }
//        );
//
//        recyclerView.setAdapter(adapter);
//    }
//
//    private void sendResult(Employee updatedEmployee, int position) {
//        Bundle result = new Bundle();
//
//        result.putBoolean("EDIT_EMPLOYEE", true);
//        result.putInt("POSITION", position);
//        result.putString("EMP_ID", updatedEmployee.getEmpId());
//        result.putString("EMP_NAME", updatedEmployee.getName());
//        result.putString("EMP_EMAIL", updatedEmployee.getEmail());
//        result.putString("EMP_PHONE", updatedEmployee.getPhone());
//        result.putString("EMP_EMERGENCY_PHONE", updatedEmployee.getEmergencyPhone());
//        result.putString("EMP_BLOOD_GROUP", updatedEmployee.getBloodGroup());
//        result.putString("EMP_ADDRESS", updatedEmployee.getAddress());
//        result.putString("EMP_ROLE", updatedEmployee.getRole());
//        result.putString("EMP_DEPARTMENT", updatedEmployee.getDepartment());
//        result.putString("EMP_Date", updatedEmployee.getJoiningDate());
//
//        getParentFragmentManager().setFragmentResult("EDIT_EMPLOYEE_RESULT", result);
//        getParentFragmentManager().setFragmentResult("EMPLOYEE_DETAILS_RESULT", result);
//    }
//}

//package com.agribird.hrmsapp.ui.employee;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.adapter.EmployeeEditAdapter;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//public class EditEmployeeFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private HRMSDatabase database;
//    private EmployeeDao employeeDao;
//    private Employee employee;
//    private int employeePosition = -1;
//
//    public EditEmployeeFragment() {
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState) {
//
//        return inflater.inflate(
//                R.layout.fragment_edit_employee,
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
//        // DATABASE INITIALIZATION
//        database = HRMSDatabase.getInstance(requireContext());
//        employeeDao = database.employeeDao();
//
//        // RECYCLER VIEW
//        recyclerView = view.findViewById(R.id.recyclerEditEmployee);
//        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        recyclerView.setNestedScrollingEnabled(false);
//
//        // GET ARGUMENTS
//        Bundle args = getArguments();
//
//        if (args == null) {
//            Toast.makeText(requireContext(), "Employee data not found", Toast.LENGTH_SHORT).show();
//            getParentFragmentManager().popBackStack();
//            return;
//        }
//
//        String employeeId = args.getString("EMP_ID", "");
//        employeePosition = args.getInt("POSITION", -1);
//
//        // LOAD EMPLOYEE FROM ROOM DATABASE
//        new Thread(() -> {
//            Employee loadedEmployee = employeeDao.getEmployeeById(employeeId);
//
//            if (getActivity() == null || !isAdded()) return;
//
//            requireActivity().runOnUiThread(() -> {
//                if (loadedEmployee == null) {
//                    Toast.makeText(requireContext(), "Employee not found", Toast.LENGTH_SHORT).show();
//                    getParentFragmentManager().popBackStack();
//                    return;
//                }
//
//                employee = loadedEmployee;
//                setupAdapter();
//            });
//
//        }).start();
//    }
//
//    private void setupAdapter() {
//        if (!isAdded()) return;
//
//        EmployeeEditAdapter adapter = new EmployeeEditAdapter(
//                requireContext(),
//                employeeDao,
//                employee,
//                employeePosition,
//                new EmployeeEditAdapter.OnEmployeeEditListener() {
//
//                    @Override
//                    public void onEmployeeUpdated(Employee updatedEmployee, int position) {
//                        sendResult(updatedEmployee, position);
//                        getParentFragmentManager().popBackStack();
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        getParentFragmentManager()
//                                .popBackStack();
//                    }
//                }
//        );
//
//        recyclerView.setAdapter(adapter);
//    }
//
//    private void sendResult(Employee updatedEmployee, int position) {
//        Bundle result = new Bundle();
//
//        result.putBoolean("EDIT_EMPLOYEE", true);
//        result.putInt("POSITION", position);
//        result.putString("EMP_ID", updatedEmployee.getEmpId());
//        result.putString("EMP_NAME", updatedEmployee.getName());
//        result.putString("EMP_EMAIL", updatedEmployee.getEmail());
//        result.putString("EMP_PHONE", updatedEmployee.getPhone());
//        result.putString("EMP_ROLE", updatedEmployee.getRole());
//        result.putString("EMP_DEPARTMENT", updatedEmployee.getDepartment());
//        result.putString("EMP_Date", updatedEmployee.getJoiningDate());
//
//        getParentFragmentManager().setFragmentResult("EDIT_EMPLOYEE_RESULT", result);
//        getParentFragmentManager().setFragmentResult("EMPLOYEE_DETAILS_RESULT", result);
//    }
//}