package com.agribird.hrmsapp.ui.employee;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.EmployeeAddAdapter;
import com.agribird.hrmsapp.api.EmployeeApi;
import com.agribird.hrmsapp.dao.EmployeeDao;
import com.agribird.hrmsapp.databasecon.HRMSDatabase;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.agribird.hrmsapp.viewmodel.EmployeeViewModel;

public class EmployeeAddFragment extends Fragment {

    private RecyclerView recyclerEmployeeForm;
    private ConstraintLayout headerEmployeeAdd;
    private EmployeeAddAdapter employeeAddAdapter;
   // private EmployeeApi employeeApi;
    private HRMSDatabase database;
    private EmployeeDao employeeDao;
    private SharedPreferences sharedPreferences;
    private EmployeeViewModel employeeViewModel;
    private float startY = 0f;

    public EmployeeAddFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_employee_add, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = HRMSDatabase.getInstance(requireContext());
        employeeDao = database.employeeDao();

        //employeeApi = RetrofitClient.getEmployeeApi();

        sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);
        String role = sharedPreferences.getString("userRole", "");

        if (!"Super Administrator".equalsIgnoreCase(role)) {

            Toast.makeText(requireContext(), "Access Denied", Toast.LENGTH_SHORT).show();

            getParentFragmentManager()
                    .popBackStack();
            return;
        }

        headerEmployeeAdd = view.findViewById(R.id.headerEmployeeAdd);

        recyclerEmployeeForm = view.findViewById(R.id.recyclerEmployeeAddForm);

        setupRecyclerView();

        employeeViewModel = new ViewModelProvider(this).get(EmployeeViewModel.class);

        recyclerEmployeeForm.addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView,
                            int dx,
                            int dy
                    ) {

                        super.onScrolled(recyclerView, dx, dy);

                        if (headerEmployeeAdd != null) {
                            if (recyclerView.canScrollVertically(-1)) {

                                headerEmployeeAdd.setBackgroundColor(android.graphics.Color.parseColor("#E6E6FA"));

                            } else {

                                headerEmployeeAdd.setBackgroundColor(android.graphics.Color.parseColor("#F4F7F5"));
                            }
                        }
                    }
                }
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupRecyclerView() {
        if (recyclerEmployeeForm == null) {
            return;
        }

        recyclerEmployeeForm.setLayoutManager(new LinearLayoutManager(requireContext()));

        employeeAddAdapter = new EmployeeAddAdapter(requireContext(), this::registerEmployee);

        recyclerEmployeeForm.setAdapter(employeeAddAdapter);

        SpringAnimation springY = new SpringAnimation(recyclerEmployeeForm, SpringAnimation.TRANSLATION_Y, 0f);

        springY.getSpring().setStiffness(SpringForce.STIFFNESS_LOW);

        springY.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

        recyclerEmployeeForm.setOnTouchListener(
                (v, event) -> {switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float deltaY = event.getRawY() - startY;
                            float translation = deltaY * 0.12f;
                            if (deltaY > 0 && !recyclerEmployeeForm.canScrollVertically(-1)) {
                                translation = Math.min(translation, 40f);
                                recyclerEmployeeForm.setTranslationY(translation);
                            }
                            else if (deltaY < 0 && !recyclerEmployeeForm.canScrollVertically(1)) {
                                translation = Math.max(translation, -50f);

                                recyclerEmployeeForm.setTranslationY(translation);
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            springY.start();
                            break;
                    }

                    return false;
                }
        );
    }

    private void registerEmployee() {

        EmployeeAddAdapter.EmployeeFormData data = employeeAddAdapter.getFormData();
        if (data == null) {
            Toast.makeText(requireContext(), "Form not loaded properly", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.name.isEmpty()) {
            Toast.makeText(requireContext(), "Employee Name Required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS
                .matcher(data.email)
                .matches()) {

            Toast.makeText(requireContext(), "Enter valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.phone.isEmpty() || !data.phone.matches("[0-9]{10}")) {
            Toast.makeText(requireContext(), "Enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!data.emergencyPhone.isEmpty() && !data.emergencyPhone.matches("[0-9]{10}")) {
            Toast.makeText(requireContext(), "Enter valid 10-digit emergency contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.empId.isEmpty()) {
            Toast.makeText(requireContext(), "Employee Id Required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.department.isEmpty() || data.department.equalsIgnoreCase("Select Department")) {
            Toast.makeText(requireContext(), "Please Select Department", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.role.isEmpty() || data.role.equalsIgnoreCase("Select Role")) {
            Toast.makeText(requireContext(), "Please Select Role", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.password.isEmpty() || data.password.length() < 6 || !data.password.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$")) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters with letters and numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!data.password.equals(data.confirmPassword)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.joiningDate.isEmpty()) {
            Toast.makeText(requireContext(), "Joining Date is required", Toast.LENGTH_SHORT).show();
            return;
        }


        EmployeeApiModel apiEmployee = new EmployeeApiModel();

        apiEmployee.setEmpID(data.empId);
        apiEmployee.setName(data.name);
        apiEmployee.setEmail(data.email);
        apiEmployee.setDepartment(data.department);
        apiEmployee.setRole(data.role);
        apiEmployee.setPhone(data.phone);
        apiEmployee.setJoiningDate(data.joiningDate);
        apiEmployee.setPassword(data.password);
        apiEmployee.setStatus("Active");
        apiEmployee.setEmergencyPhone(data.emergencyPhone);
        apiEmployee.setBloodGroup(data.bloodGroup);
        apiEmployee.setAddress(data.address);

        employeeViewModel.addEmployee(apiEmployee).observe(getViewLifecycleOwner(), state -> {

                    if (state == null) {
                        return;
                    }
                    if (state.isLoading()) {

                        // Loading

                    } else if (state.isSuccess()) {
                        Toast.makeText(requireContext(), "Employee added successfully", Toast.LENGTH_SHORT
                        ).show();

                        getParentFragmentManager()
                                .popBackStack();

                    } else if (state.isError()) {
                        Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_LONG
                        ).show();
                    }
                });

//        Employee employee =
//                new Employee(
//                        data.empId,
//                        data.name,
//                        data.email,
//                        data.role,
//                        data.department,
//                        data.phone,
//                        data.joiningDate,
//                        data.password,
//                        data.emergencyPhone,
//                        data.bloodGroup,
//                        data.address
//                );
//
//        Executors.newSingleThreadExecutor().execute(() -> {
//
//                    employeeDao.addEmployee(employee);
//
//                    if (getActivity() != null && isAdded()) {
//                        requireActivity().runOnUiThread(() -> {
//
//                                    Toast.makeText(requireContext(), "Registration Successful",
//                                            Toast.LENGTH_SHORT).show();
//                                    getParentFragmentManager()
//                                            .popBackStack();
//                        });
//                    }
//        });
    }
}
//package com.agribird.hrmsapp.ui.employee;
//
//import static android.content.Context.MODE_PRIVATE;
//
//import android.content.SharedPreferences;
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
//import com.agribird.hrmsapp.adapter.EmployeeAddAdapter;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//import java.util.concurrent.Executors;
//
//public class EmployeeAddFragment extends Fragment {
//
//    private RecyclerView recyclerEmployeeForm;
//    private EmployeeAddAdapter employeeAddAdapter;
//    private HRMSDatabase database;
//    private EmployeeDao employeeDao;
//    private SharedPreferences sharedPreferences;
//
//    public EmployeeAddFragment() {
//        // Required empty public constructor
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState) {
//
//        return inflater.inflate(R.layout.fragment_employee_add, container, false);
//    }
//
//    @Override
//    public void onViewCreated(
//            @NonNull View view,
//            @Nullable Bundle savedInstanceState) {
//
//        super.onViewCreated(view, savedInstanceState);
//
//        // Database
//        database = HRMSDatabase.getInstance(requireContext());
//        employeeDao = database.employeeDao();
//
//        // Shared Preferences
//        sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);
//
//        // Logged-in User Role Check
//        String role = sharedPreferences.getString("userRole", "");
//
//        if (!"Super Administrator".equalsIgnoreCase(role)) {
//            Toast.makeText(requireContext(), "Access Denied", Toast.LENGTH_SHORT).show();
//            getParentFragmentManager().popBackStack();
//            return;
//        }
//
//        // RecyclerView Setup
//        recyclerEmployeeForm = view.findViewById(R.id.recyclerEmployeeAddForm);
//        recyclerEmployeeForm.setLayoutManager(new LinearLayoutManager(requireContext()));
//
//        employeeAddAdapter = new EmployeeAddAdapter(
//                requireContext(),
//                this::registerEmployee
//        );
//
//        recyclerEmployeeForm.setAdapter(employeeAddAdapter);
//    }
//
//    private void registerEmployee() {
//
//        // Get Form Data
//        EmployeeAddAdapter.EmployeeFormData data = employeeAddAdapter.getFormData();
//
//        if (data == null) {
//            Toast.makeText(requireContext(), "Form not loaded properly", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // VALIDATIONS
//        if (data.name.isEmpty()) {
//            Toast.makeText(requireContext(), "Employee Name Required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.email.isEmpty()) {
//            Toast.makeText(requireContext(), "Email is required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(data.email).matches()) {
//            Toast.makeText(requireContext(), "Enter valid email address", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.phone.isEmpty()) {
//            Toast.makeText(requireContext(), "Phone Number is required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!data.phone.matches("[0-9]{10}")) {
//            Toast.makeText(requireContext(), "Enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.empId.isEmpty()) {
//            Toast.makeText(requireContext(), "Employee Id Required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.department.isEmpty() || data.department.equalsIgnoreCase("Select Department")) {
//            Toast.makeText(requireContext(), "Please Select Department", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.role.isEmpty() || data.role.equalsIgnoreCase("Select Role")) {
//            Toast.makeText(requireContext(), "Please Select Role", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.password.isEmpty()) {
//            Toast.makeText(requireContext(), "Password is Required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.password.length() < 6) {
//            Toast.makeText(requireContext(), "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!data.password.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$")) {
//            Toast.makeText(requireContext(), "Password must contain both letters and numbers", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.confirmPassword.isEmpty()) {
//            Toast.makeText(requireContext(), "Confirm Password is required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!data.password.equals(data.confirmPassword)) {
//            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data.joiningDate.isEmpty()) {
//            Toast.makeText(requireContext(), "Joining Date is required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // CREATE EMPLOYEE MODEL
//        Employee employee = new Employee(
//                data.empId,
//                data.name,
//                data.email,
//                data.role,
//                data.department,
//                data.phone,
//                data.joiningDate,
//                data.password
//        );
//
//        // DATABASE INSERT (THREAD-SAFE)
//        Executors.newSingleThreadExecutor().execute(() -> {
//            employeeDao.addEmployee(employee);
//
//            if (getActivity() != null && isAdded()) {
//                requireActivity().runOnUiThread(() -> {
//                    Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
//                    getParentFragmentManager().popBackStack();
//                });
//            }
//        });
//    }
//}