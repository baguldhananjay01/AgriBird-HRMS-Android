package com.agribird.hrmsapp.ui.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.Employee;
import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.EmployeeAdapter;
import com.agribird.hrmsapp.viewmodel.EmployeeViewModel;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ConstraintLayout layoutNoData;
    private TextView txtEmployeeCount;
    private SearchView searchView;
    private View cardRecyclerContainer;

    private ArrayList<Employee> employeeList;

    //private EmployeeDao employeeDao;

    private EmployeeViewModel employeeViewModel;
    private EmployeeAdapter adapter;

    private String loggedInRole;

    public EmployeeListFragment() {super(R.layout.fragment_employee_list);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_employee_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       // HRMSDatabase database = HRMSDatabase.getInstance(requireContext());
       // employeeDao = database.employeeDao();



        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        layoutNoData = view.findViewById(R.id.layoutNoData);
        txtEmployeeCount = view.findViewById(R.id.txtEmployeeCount);

        cardRecyclerContainer = (View) recyclerView.getParent();

        employeeViewModel = new ViewModelProvider(this)
                .get(EmployeeViewModel.class);

        if (getArguments() != null) {
            loggedInRole = getArguments().getString("LOGGED_IN_ROLE");
        }
        employeeList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EmployeeAdapter(employeeList, loggedInRole,
                (employee, position) -> {
                    EmployeeDetailsFragment employeeDetailsFragment = new EmployeeDetailsFragment();

                    Bundle bundle = new Bundle();

                    bundle.putInt("POSITION", position);
                    bundle.putString("EMP_ID", employee.getEmpId());
                    bundle.putString("EMP_NAME", employee.getName());
                    bundle.putString("EMP_EMAIL", employee.getEmail());
                    bundle.putString("EMP_PHONE", employee.getPhone());
                    bundle.putString("EMP_ROLE", employee.getRole());
                    bundle.putString("EMP_DEPARTMENT", employee.getDepartment());
                    bundle.putString("EMP_Date", employee.getJoiningDate());
                    bundle.putString("EMP_EMERGENCY_PHONE", employee.getEmergencyPhone());
                    bundle.putString("EMP_BLOOD_GROUP", employee.getBloodGroup());
                    bundle.putString("EMP_ADDRESS", employee.getAddress());

                    employeeDetailsFragment.setArguments(bundle);

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, employeeDetailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
        );

        recyclerView.setAdapter(adapter);

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filter(newText);
                    return true;
                }
            });
        }

        getParentFragmentManager().setFragmentResultListener("EMPLOYEE_DETAILS_RESULT", getViewLifecycleOwner(),
                (requestKey, result) -> {
                    boolean deleted = result.getBoolean("DELETE_EMPLOYEE", false);
                    boolean edited = result.getBoolean("EDIT_EMPLOYEE", false);

                    if (deleted || edited) {
                        loadEmployeeData();
                    }
                }
        );

        loadEmployeeData();
    }

    @Override
    public void onResume() {
        super.onResume();
       // loadEmployeeData();
    }

    private void loadEmployeeData() {

        employeeViewModel.getAllEmployees()
                .observe(getViewLifecycleOwner(), state -> {

                    if (!isAdded() || state == null) {
                        return;
                    }

                    switch (state.getStatus()) {

                        case LOADING:

                            Toast.makeText(requireContext(), "Loading employees...", Toast.LENGTH_SHORT).show();
                            break;

                        case SUCCESS:

                            List<EmployeeApiModel> apiEmployees = state.getData();

                            employeeList.clear();

                            if (apiEmployees != null) {

                                for (EmployeeApiModel apiEmployee : apiEmployees) {

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
                                    employeeList.add(employee);
                                }
                            }

                            if (adapter != null) {
                                adapter.filterList(new ArrayList<>(employeeList));
                            }

                            checkEmptyState(employeeList);
                            updateEmployeeCount(employeeList.size());
                            break;

                        case ERROR:

                            String message = state.getMessage();

                            if (message == null || message.isEmpty()) {
                                message = "Something went wrong";
                            }

                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                            break;
                    }
                });
    }

    private void filter(String text) {
        ArrayList<Employee> filteredList = new ArrayList<>();

        String searchText = text == null ? "" : text.toLowerCase().trim();

        for (Employee emp : employeeList) {

            String name = emp.getName() == null ? "" : emp.getName();
            String email = emp.getEmail() == null ? "" : emp.getEmail();
            String department = emp.getDepartment() == null ? "" : emp.getDepartment();

            if (name.toLowerCase().contains(searchText) ||
                    email.toLowerCase().contains(searchText) ||
                    department.toLowerCase().contains(searchText)) {

                filteredList.add(emp);
            }
        }

        checkEmptyState(filteredList);

        if (adapter != null) {
            adapter.filterList(filteredList);
        }

        updateEmployeeCount(filteredList.size());
    }

//    private void loadEmployeeData() {
//
//        employeeViewModel.getAllEmployees().observe(getViewLifecycleOwner(),
//                apiEmployees -> {
//
//                            if (!isAdded()) {
//                                return;
//                            }
//
//                            employeeList.clear();
//
//                            if (apiEmployees != null) {
//
//                                for (EmployeeApiModel apiEmployee : apiEmployees) {
//
//                                    Employee employee = new Employee(
//                                            apiEmployee.getEmpID(),
//                                            apiEmployee.getName(),
//                                            apiEmployee.getEmail(),
//                                            apiEmployee.getRole(),
//                                            apiEmployee.getDepartment(),
//                                            apiEmployee.getPhone(),
//                                            apiEmployee.getJoiningDate(),
//                                            apiEmployee.getPassword(),
//                                            apiEmployee.getEmergencyPhone(),
//                                            apiEmployee.getBloodGroup(),
//                                            apiEmployee.getAddress()
//                                    );
//
//                                    employee.setStatus(apiEmployee.getStatus()
//                                    );
//
//                                    employeeList.add(employee);
//                                }
//                            }
//
//                            if (adapter != null) {
//
//                                adapter.filterList(new ArrayList<>(employeeList)
//                                );
//                            }
//
//                            checkEmptyState(employeeList);
//                            updateEmployeeCount(employeeList.size());
//                        }
//                );
//    }
//
//    // Room Database operations
////    private void loadEmployeeData() {
////        if (employeeDao == null) {
////            return;
////        }
////
////        Executors.newSingleThreadExecutor().execute(() -> {
////            List<Employee> freshList = employeeDao.getAllEmployee();
////
////            if (getActivity() == null || !isAdded()) return;
////
////            requireActivity().runOnUiThread(() -> {
////                if (employeeList == null) {
////                    employeeList = new ArrayList<>();
////                }
////
////                employeeList.clear();
////
////                if (freshList != null) {
////                    employeeList.addAll(freshList);
////                }
////
////                if (adapter != null) {
////                    adapter.filterList(new ArrayList<>(employeeList));
////                }
////
////                checkEmptyState(employeeList);
////                updateEmployeeCount(employeeList.size());
////            });
////        });
////    }
//
//    private void filter(String text) {
//        ArrayList<Employee> filteredList = new ArrayList<>();
//        String searchText = text == null ? "" : text.toLowerCase().trim();
//
//        for (Employee emp : employeeList) {
//            String name = emp.getName() == null ? "" : emp.getName();
//            String email = emp.getEmail() == null ? "" : emp.getEmail();
//            String department = emp.getDepartment() == null ? "" : emp.getDepartment();
//
//            if (name.toLowerCase().contains(searchText) ||
//                    email.toLowerCase().contains(searchText) ||
//                    department.toLowerCase().contains(searchText)) {
//                filteredList.add(emp);
//            }
//        }
//
//        checkEmptyState(filteredList);
//
//        if (adapter != null) {
//            adapter.filterList(filteredList);
//        }
//        updateEmployeeCount(filteredList.size());
//    }


    private void checkEmptyState(List<Employee> list) {
        if (list == null || list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            cardRecyclerContainer.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            cardRecyclerContainer.setVisibility(View.VISIBLE);
            layoutNoData.setVisibility(View.GONE);
        }
    }

    private void updateEmployeeCount(int count) {
        if (txtEmployeeCount != null) {
            txtEmployeeCount.setText("Total Employees: " + count);
        }
    }
}
//package com.agribird.hrmsapp.ui.employee;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.widget.SearchView;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.adapter.EmployeeAdapter;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Executors;
//
//public class EmployeeListFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private ConstraintLayout layoutNoData;
//    private TextView txtEmployeeCount;
//    private SearchView searchView;
//
//    private ArrayList<Employee> employeeList;
//
//    private EmployeeDao employeeDao;
//    private EmployeeAdapter adapter;
//
//    private String loggedInRole;
//
//    public EmployeeListFragment() {
//        super(R.layout.fragment_employee_list);
//    }
//
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState) {
//
//        return inflater.inflate(
//                R.layout.fragment_employee_list,
//                container,
//                false
//        );
//    }
//
//    @Override
//    public void onViewCreated(
//            @NonNull View view,
//            @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        HRMSDatabase database = HRMSDatabase.getInstance(requireContext());
//        employeeDao = database.employeeDao();
//
//        recyclerView = view.findViewById(R.id.recyclerView);
//        searchView = view.findViewById(R.id.searchView);
//        layoutNoData = view.findViewById(R.id.layoutNoData);
//        txtEmployeeCount = view.findViewById(R.id.txtEmployeeCount);
//
//        if (getArguments() != null) {
//            loggedInRole = getArguments().getString("LOGGED_IN_ROLE");
//        }
//        employeeList = new ArrayList<>();
//        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//
//        adapter = new EmployeeAdapter(employeeList, loggedInRole,
//                (employee, position) -> {
//                    EmployeeDetailsFragment employeeDetailsFragment = new EmployeeDetailsFragment();
//
//                    Bundle bundle = new Bundle();
//
//                    bundle.putInt("POSITION", position);
//
//                    // FIX: Pass employee.getEmpId() instead of employee.getId()
//                    bundle.putString("EMP_ID", employee.getEmpId());
//                    bundle.putString("EMP_NAME", employee.getName());
//                    bundle.putString("EMP_EMAIL", employee.getEmail());
//                    bundle.putString("EMP_PHONE", employee.getPhone());
//                    bundle.putString("EMP_ROLE", employee.getRole());
//                    bundle.putString("EMP_DEPARTMENT", employee.getDepartment());
//                    bundle.putString("EMP_Date", employee.getJoiningDate());
//                    bundle.putString("EMP_EMERGENCY_PHONE", employee.getEmergencyPhone());
//                    bundle.putString("EMP_BLOOD_GROUP", employee.getBloodGroup());
//                    bundle.putString("EMP_ADDRESS", employee.getAddress());
//
//                    employeeDetailsFragment.setArguments(bundle);
//
//                    getParentFragmentManager()
//                            .beginTransaction()
//                            .replace(
//                                    R.id.fragmentContainer,
//                                    employeeDetailsFragment
//                            )
//                            .addToBackStack(null)
//                            .commit();
//                }
//        );
//
//        recyclerView.setAdapter(adapter);
//
//        if (searchView != null) {
//            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    return false;
//                }
//
//                @Override
//                public boolean onQueryTextChange(String newText) {
//                    filter(newText);
//                    return true;
//                }
//            });
//        }
//
//        getParentFragmentManager().setFragmentResultListener("EMPLOYEE_DETAILS_RESULT", getViewLifecycleOwner(),
//                (requestKey, result) -> {
//                    boolean deleted = result.getBoolean("DELETE_EMPLOYEE", false);
//                    boolean edited = result.getBoolean("EDIT_EMPLOYEE", false);
//
//                    if (deleted || edited) {
//                        loadEmployeeData();
//                    }
//                }
//        );
//
//        loadEmployeeData();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        loadEmployeeData();
//    }
//
//    // FIX: Room Database operations moved to Background Thread
//    private void loadEmployeeData() {
//        if (employeeDao == null) {
//            return;
//        }
//
//        Executors.newSingleThreadExecutor().execute(() -> {
//            List<Employee> freshList = employeeDao.getAllEmployee();
//
//            if (getActivity() == null || !isAdded()) return;
//
//            requireActivity().runOnUiThread(() -> {
//                if (employeeList == null) {
//                    employeeList = new ArrayList<>();
//                }
//
//                employeeList.clear();
//
//                if (freshList != null) {
//                    employeeList.addAll(freshList);
//                }
//
//                if (adapter != null) {
//                    adapter.filterList(new ArrayList<>(employeeList));
//                }
//
//                checkEmptyState(employeeList);
//                updateEmployeeCount(employeeList.size());
//            });
//        });
//    }
//
//    private void filter(String text) {
//        ArrayList<Employee> filteredList = new ArrayList<>();
//        String searchText = text == null ? "" : text.toLowerCase().trim();
//
//        for (Employee emp : employeeList) {
//            String name = emp.getName() == null ? "" : emp.getName();
//            String email = emp.getEmail() == null ? "" : emp.getEmail();
//            String department = emp.getDepartment() == null ? "" : emp.getDepartment();
//
//            if (name.toLowerCase().contains(searchText) ||
//                    email.toLowerCase().contains(searchText) ||
//                    department.toLowerCase().contains(searchText)) {
//                filteredList.add(emp);
//            }
//        }
//
//        checkEmptyState(filteredList);
//
//        if (adapter != null) {
//            adapter.filterList(filteredList);
//        }
//        updateEmployeeCount(filteredList.size());
//    }
//
//    private void checkEmptyState(List<Employee> list) {
//        if (list == null || list.isEmpty()) {
//            recyclerView.setVisibility(View.GONE);
//            layoutNoData.setVisibility(View.VISIBLE);
//        } else {
//            recyclerView.setVisibility(View.VISIBLE);
//            layoutNoData.setVisibility(View.GONE);
//        }
//    }
//
//    private void updateEmployeeCount(int count) {
//        if (txtEmployeeCount != null) {
//            txtEmployeeCount.setText("Total Employees: " + count);
//        }
//    }
//}