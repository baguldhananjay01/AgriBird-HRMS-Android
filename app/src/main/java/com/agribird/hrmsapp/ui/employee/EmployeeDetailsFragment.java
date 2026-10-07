package com.agribird.hrmsapp.ui.employee;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.agribird.hrmsapp.Model.Employee;
import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.EmployeeDetailsAdapter;
import com.agribird.hrmsapp.viewmodel.EmployeeViewModel;
import java.util.ArrayList;
import java.util.List;


public class EmployeeDetailsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmployeeDetailsAdapter adapter;
    private List<Employee> employeeList;

    private EmployeeViewModel employeeViewModel;

   // private HRMSDatabase database;
   // private EmployeeDao employeeDao;

    public EmployeeDetailsFragment() {
        super(R.layout.fragment_employee_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // DATABASE INITIALIZATION
       // database = HRMSDatabase.getInstance(requireContext());
      //  employeeDao = database.employeeDao();

        // RECYCLERVIEW INITIALIZATION
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        employeeViewModel = new ViewModelProvider(this)
                .get(EmployeeViewModel.class);

        employeeList = new ArrayList<>();

        adapter = new EmployeeDetailsAdapter(requireContext(), employeeList, new EmployeeDetailsAdapter.OnEmployeeActionListener() {
            @Override
            public void onCallClicked(Employee employee) {
                // CALL LOGIC
                String phoneNumber = employee.getPhone();
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                    intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEmailClicked(Employee employee) {
                // EMAIL LOGIC
                String email = employee.getEmail();
                if (email != null && !email.isEmpty()) {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                    intent.setData(android.net.Uri.parse("mailto:" + email));
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Regarding HRMS");
                    try {
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Email address not available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEditClicked(Employee employee) {

                EditEmployeeFragment editEmployeeFragment = new EditEmployeeFragment();
                Bundle editBundle = new Bundle();

                editBundle.putString("EMP_NAME", employee.getName());
                editBundle.putString("EMP_EMAIL", employee.getEmail());
                editBundle.putString("EMP_ROLE", employee.getRole());
                editBundle.putString("EMP_PHONE", employee.getPhone());
                editBundle.putString("EMP_EMERGENCY_PHONE", employee.getEmergencyPhone());
                editBundle.putString("EMP_BLOOD_GROUP", employee.getBloodGroup());
                editBundle.putString("EMP_ADDRESS", employee.getAddress());
                editBundle.putString("EMP_ID", employee.getEmpId());
                editBundle.putString("EMP_DEPARTMENT", employee.getDepartment());
                editBundle.putString("EMP_Date", employee.getJoiningDate());

                editEmployeeFragment.setArguments(editBundle);

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, editEmployeeFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClicked(Employee employee) {

                String employeeId = employee.getEmpId();
                String employeeName = employee.getName();

                if (employeeId == null || employeeId.trim().isEmpty()) {
                    Toast.makeText(requireContext(), "Employee ID is required to delete", Toast.LENGTH_SHORT).show();
                    return;
                }
                String displayName = (employeeName == null || employeeName.trim().isEmpty()) ? "this employee" : employeeName;

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

                builder.setTitle("Delete Employee");
                builder.setMessage("Are you sure you want to delete " + displayName + " (ID: " + employeeId + ")?");

                builder.setCancelable(false);

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.setPositiveButton("Delete", (dialog, which) -> {
                            deleteEmployeeFromBackend(employeeId);
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

            @Override
            public void onItemClick(Employee employee) {
            }
        });

        recyclerView.setAdapter(adapter);
        loadEmployeeData();
    }

    //room connection method
//    private void loadEmployeeData() {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            List<Employee> list = employeeDao.getAllEmployee();
//            if (getActivity() != null && isAdded()) {
//                requireActivity().runOnUiThread(() -> {
//                    employeeList.clear();
//                    if (list != null) {
//                        employeeList.addAll(list);
//                    }
//                    adapter.updateData(employeeList);
//                });
//            }
//        });
//    }

    private void loadEmployeeData() {

        Bundle bundle = getArguments();

        if (bundle == null) {
            Toast.makeText(requireContext(), "Employee ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String empID = bundle.getString("EMP_ID");

        if (empID == null || empID.isEmpty()) {

            Toast.makeText(requireContext(), "Employee ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        employeeViewModel.getEmployeeByEmpId(empID).observe(getViewLifecycleOwner(),state ->{

            if(!isAdded()  || state==null){
                return;
            }

            switch (state.getStatus()){
                
                case LOADING:
                    Toast.makeText(requireContext(), "Loding Employee", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    EmployeeApiModel employeeApiModel=state.getData();

                    if(employeeApiModel ==null){
                        return;
                    }

                    Employee employee=new Employee(
                            employeeApiModel.getEmpID(),
                            employeeApiModel.getName(),
                            employeeApiModel.getEmail(),
                            employeeApiModel.getRole(),
                            employeeApiModel.getDepartment(),
                            employeeApiModel.getPhone(),
                            employeeApiModel.getJoiningDate(),
                            employeeApiModel.getPassword(),
                            employeeApiModel.getEmergencyPhone(),
                            employeeApiModel.getBloodGroup(),
                            employeeApiModel.getAddress()
                    );
                    employee.setStatus(employeeApiModel.getStatus());
                    employeeList.clear();
                    employeeList.add(employee);

                    if(adapter != null){
                        adapter.updateData(employeeList);
                    }
                    break;

                case ERROR:
                    String message = state.getMessage();

                    if(message == null || message.isEmpty()){
                        message = "Failed to load employee";
                    }
                    Toast.makeText(requireContext(),message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

    }

    private void deleteEmployeeFromBackend(String employeeId) {

        employeeViewModel.deleteEmployee(employeeId).observe(getViewLifecycleOwner(),apiState ->{

            if(apiState ==null){
                return;
            }
            switch (apiState.getStatus()){
                case LOADING:
                    Toast.makeText(requireContext(), "Deleting Employee...", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    Toast.makeText(requireContext(), "Employee deleted successfully", Toast.LENGTH_SHORT).show();
                    loadEmployeeData();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(),apiState.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

    }
}

//package com.agribird.hrmsapp.ui.employee;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
//import androidx.fragment.app.Fragment;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//import java.util.concurrent.Executors;
//
//public class EmployeeDetailsFragment extends Fragment {
//
//    // Views Declaration
//    private TextView eName, eEmail, eRole, eDEPARTMENT, eID, eJoiningDate, ePhone;
//    private TextView eEmergencyPhone, eBloodGroup, eAddress; // Added new fields
//    private Button btnCall, btnEmail, btnBack, btnEdit, btnDelete;
//
//    private HRMSDatabase database;
//    private EmployeeDao employeeDao;
//
//    // Data Variables
//    private String name, email, role, id, department, date, phone, emergencyPhone, bloodGroup, address;
//    private int position = -1;
//
//    public EmployeeDetailsFragment() {
//        super(R.layout.fragment_employee_details);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // DATABASE INITIALIZATION
//        database = HRMSDatabase.getInstance(requireContext());
//        employeeDao = database.employeeDao();
//
//        // VIEWS INITIALIZATION
//        eName = view.findViewById(R.id.eName);
//        eEmail = view.findViewById(R.id.eEmail);
//        eRole = view.findViewById(R.id.eRole);
//        ePhone = view.findViewById(R.id.ePhone);
//        eEmergencyPhone = view.findViewById(R.id.eEmergencyPhone);
//        eBloodGroup = view.findViewById(R.id.eBloodGroup);
//        eAddress = view.findViewById(R.id.eAddress);
//        eDEPARTMENT = view.findViewById(R.id.eDepartment);
//        eID = view.findViewById(R.id.eID);
//        eJoiningDate = view.findViewById(R.id.eJoiningDate);
//
//        btnCall = view.findViewById(R.id.btnCALL);
//        btnEmail = view.findViewById(R.id.btnEmail);
//        btnBack = view.findViewById(R.id.btnBack);
//        btnEdit = view.findViewById(R.id.btnEdit);
//        btnDelete = view.findViewById(R.id.btnDelete);
//
//        // RECEIVE BUNDLE DATA
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            extractBundleData(bundle);
//            updateUI();
//        }
//
//        // RESULT LISTENER FROM EditEmployeeFragment
//        getParentFragmentManager().setFragmentResultListener(
//                "EDIT_EMPLOYEE_RESULT",
//                getViewLifecycleOwner(),
//                (requestKey, result) -> {
//                    extractBundleData(result);
//                    updateUI();
//                });
//
//        // DYNAMIC CALL INTENT
//        btnCall.setOnClickListener(v -> {
//            String phoneNumber = ePhone.getText().toString().trim();
//            if (!phoneNumber.isEmpty()) {
//                Intent intent = new Intent(Intent.ACTION_DIAL);
//                intent.setData(Uri.parse("tel:" + phoneNumber));
//                startActivity(intent);
//            } else {
//                Toast.makeText(requireContext(), "Phone number not available", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // EMAIL INTENT
//        btnEmail.setOnClickListener(v -> {
//            String employeeEmail = eEmail.getText().toString().trim();
//            if (!employeeEmail.isEmpty()) {
//                Intent intent = new Intent(Intent.ACTION_SENDTO);
//                intent.setData(Uri.parse("mailto:" + employeeEmail));
//                startActivity(intent);
//            } else {
//                Toast.makeText(requireContext(), "Email address not available", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // EDIT BUTTON
//        btnEdit.setOnClickListener(v -> {
//            EditEmployeeFragment editEmployeeFragment = new EditEmployeeFragment();
//            Bundle editBundle = new Bundle();
//
//            editBundle.putString("EMP_NAME", eName.getText().toString());
//            editBundle.putString("EMP_EMAIL", eEmail.getText().toString());
//            editBundle.putString("EMP_ROLE", eRole.getText().toString());
//            editBundle.putString("EMP_PHONE", ePhone.getText().toString());
//            editBundle.putString("EMP_EMERGENCY_PHONE", eEmergencyPhone.getText().toString());
//            editBundle.putString("EMP_BLOOD_GROUP", eBloodGroup.getText().toString());
//            editBundle.putString("EMP_ADDRESS", eAddress.getText().toString());
//            editBundle.putString("EMP_ID", eID.getText().toString());
//            editBundle.putString("EMP_DEPARTMENT", eDEPARTMENT.getText().toString());
//            editBundle.putString("EMP_Date", eJoiningDate.getText().toString());
//            editBundle.putInt("POSITION", position);
//
//            editEmployeeFragment.setArguments(editBundle);
//
//            getParentFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, editEmployeeFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        btnDelete.setOnClickListener(v -> {
//            String employeeId = eID.getText().toString().trim();
//            String employeeName = eName.getText().toString().trim();
//
//            if (employeeId.isEmpty()) {
//                eID.setError("Employee ID is required to delete");
//                eID.requestFocus();
//                return;
//            }
//
//            String displayName = employeeName.isEmpty() ? "this employee" : employeeName;
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//            builder.setTitle("Delete Employee");
//            builder.setMessage("Are you sure you want to delete " + displayName + " (ID: " + employeeId + ")?");
//            builder.setCancelable(false);
//
//            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
//
//            builder.setPositiveButton("Delete", (dialog, which) -> {
//                Executors.newSingleThreadExecutor().execute(() -> {
//                    Employee employeeToDelete = employeeDao.getEmployeeById(employeeId);
//
//                    if (employeeToDelete != null) {
//                        employeeDao.deleteEmployee(employeeToDelete);
//
//                        if (getActivity() != null && isAdded()) {
//                            requireActivity().runOnUiThread(() -> {
//                                Toast.makeText(requireContext(), "Employee Deleted Successfully", Toast.LENGTH_SHORT).show();
//
//                                Bundle result = new Bundle();
//                                result.putBoolean("DELETE_EMPLOYEE", true);
//                                result.putInt("POSITION", position);
//
//                                getParentFragmentManager().setFragmentResult("EMPLOYEE_DETAILS_RESULT", result);
//                                getParentFragmentManager().popBackStack();
//                            });
//                        }
//                    } else {
//                        if (getActivity() != null && isAdded()) {
//                            requireActivity().runOnUiThread(() ->
//                                    Toast.makeText(requireContext(), "Error: Employee not found in database!", Toast.LENGTH_SHORT).show()
//                            );
//                        }
//                    }
//                });
//            });
//
//            AlertDialog alertDialog = builder.create();
//            alertDialog.show();
//        });
//
//        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
//    }
//
//    private void extractBundleData(Bundle bundle) {
//        name = bundle.getString("EMP_NAME", "");
//        email = bundle.getString("EMP_EMAIL", "");
//        role = bundle.getString("EMP_ROLE", "");
//        phone = bundle.getString("EMP_PHONE", "");
//        emergencyPhone = bundle.getString("EMP_EMERGENCY_PHONE", "");
//        bloodGroup = bundle.getString("EMP_BLOOD_GROUP", "");
//        address = bundle.getString("EMP_ADDRESS", "");
//        id = bundle.getString("EMP_ID", "");
//        department = bundle.getString("EMP_DEPARTMENT", "");
//        date = bundle.getString("EMP_Date", "");
//        position = bundle.getInt("POSITION", position);
//    }
//
//    private void updateUI() {
//        eName.setText(name);
//        eEmail.setText(email);
//        eRole.setText(role);
//        ePhone.setText(phone);
//        eEmergencyPhone.setText(emergencyPhone);
//        eBloodGroup.setText(bloodGroup);
//        eAddress.setText(address);
//        eDEPARTMENT.setText(department);
//        eID.setText(id);
//        eJoiningDate.setText(date);
//    }
//}

//package com.agribird.hrmsapp.ui.employee;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
//import androidx.fragment.app.Fragment;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//import java.util.concurrent.Executors;
//
//public class EmployeeDetailsFragment extends Fragment {
//
//    private TextView eName, eEmail, eRole, eDEPARTMENT, eID, eJoiningDate, ePhone;
//    private Button btnCall, btnEmail, btnBack, btnEdit, btnDelete;
//    private HRMSDatabase database;
//    private EmployeeDao employeeDao;
//
//    private String name, email, role, id, department, date, phone;
//    private int position = -1;
//
//    public EmployeeDetailsFragment() {
//        super(R.layout.fragment_employee_details);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // DATABASE INITIALIZATION
//        database = HRMSDatabase.getInstance(requireContext());
//        employeeDao = database.employeeDao();
//
//        // VIEWS INITIALIZATION
//        eName = view.findViewById(R.id.eName);
//        eEmail = view.findViewById(R.id.eEmail);
//        eRole = view.findViewById(R.id.eRole);
//        ePhone = view.findViewById(R.id.ePhone);
//        eDEPARTMENT = view.findViewById(R.id.eDepartment);
//        eID = view.findViewById(R.id.eID);
//        eJoiningDate = view.findViewById(R.id.eJoiningDate);
//
//        btnCall = view.findViewById(R.id.btnCALL);
//        btnEmail = view.findViewById(R.id.btnEmail);
//        btnBack = view.findViewById(R.id.btnBack);
//        btnEdit = view.findViewById(R.id.btnEdit);
//        btnDelete = view.findViewById(R.id.btnDelete);
//
//        // RECEIVE BUNDLE DATA
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            name = bundle.getString("EMP_NAME", "");
//            email = bundle.getString("EMP_EMAIL", "");
//            role = bundle.getString("EMP_ROLE", "");
//            phone = bundle.getString("EMP_PHONE", "");
//            id = bundle.getString("EMP_ID", "");
//            department = bundle.getString("EMP_DEPARTMENT", "");
//            date = bundle.getString("EMP_Date", "");
//            position = bundle.getInt("POSITION", -1);
//
//            updateUI();
//        }
//
//        // RESULT LISTENER FROM EditEmployeeFragment
//        getParentFragmentManager().setFragmentResultListener(
//                "EDIT_EMPLOYEE_RESULT",
//                getViewLifecycleOwner(),
//                (requestKey, result) -> {
//                    name = result.getString("EMP_NAME", "");
//                    email = result.getString("EMP_EMAIL", "");
//                    role = result.getString("EMP_ROLE", "");
//                    phone = result.getString("EMP_PHONE", "");
//                    id = result.getString("EMP_ID", "");
//                    department = result.getString("EMP_DEPARTMENT", "");
//                    date = result.getString("EMP_Date", "");
//                    position = result.getInt("POSITION", position);
//
//                    updateUI();
//                });
//
//        // DYNAMIC CALL INTENT (FIXED)
//        btnCall.setOnClickListener(v -> {
//            String phoneNumber = ePhone.getText().toString().trim();
//            if (!phoneNumber.isEmpty()) {
//                Intent intent = new Intent(Intent.ACTION_DIAL);
//                intent.setData(Uri.parse("tel:" + phoneNumber));
//                startActivity(intent);
//            } else {
//                Toast.makeText(requireContext(), "Phone number not available", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // EMAIL INTENT
//        btnEmail.setOnClickListener(v -> {
//            String employeeEmail = eEmail.getText().toString().trim();
//            if (!employeeEmail.isEmpty()) {
//                Intent intent = new Intent(Intent.ACTION_SENDTO);
//                intent.setData(Uri.parse("mailto:" + employeeEmail));
//                startActivity(intent);
//            } else {
//                Toast.makeText(requireContext(), "Email address not available", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // EDIT BUTTON
//        btnEdit.setOnClickListener(v -> {
//            EditEmployeeFragment editEmployeeFragment = new EditEmployeeFragment();
//            Bundle editBundle = new Bundle();
//
//            editBundle.putString("EMP_NAME", eName.getText().toString());
//            editBundle.putString("EMP_EMAIL", eEmail.getText().toString());
//            editBundle.putString("EMP_ROLE", eRole.getText().toString());
//            editBundle.putString("EMP_PHONE", ePhone.getText().toString());
//            editBundle.putString("EMP_ID", eID.getText().toString());
//            editBundle.putString("EMP_DEPARTMENT", eDEPARTMENT.getText().toString());
//            editBundle.putString("EMP_Date", eJoiningDate.getText().toString());
//            editBundle.putInt("POSITION", position);
//
//            editEmployeeFragment.setArguments(editBundle);
//
//            getParentFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, editEmployeeFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        // DELETE BUTTON (FIXED BACKGROUND THREAD ISSUE)
//        btnDelete.setOnClickListener(v -> {
//            String employeeId = eID.getText().toString().trim();
//            String employeeName = eName.getText().toString().trim();
//
//            if (employeeId.isEmpty()) {
//                eID.setError("Employee ID is required to delete");
//                eID.requestFocus();
//                return;
//            }
//
//            String displayName = employeeName.isEmpty() ? "this employee" : employeeName;
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//            builder.setTitle("Delete Employee");
//            builder.setMessage("Are you sure you want to delete " + displayName + " (ID: " + employeeId + ")?");
//            builder.setCancelable(false);
//
//            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
//
//            builder.setPositiveButton("Delete", (dialog, which) -> {
//                // Background thread for Database operations
//                Executors.newSingleThreadExecutor().execute(() -> {
//                    Employee employeeToDelete = employeeDao.getEmployeeById(employeeId);
//
//                    if (getActivity() == null || !isAdded()) return;
//
//                    requireActivity().runOnUiThread(() -> {
//                        if (employeeToDelete != null) {
//                            // Delete on background thread
//                            Executors.newSingleThreadExecutor().execute(() -> {
//                                employeeDao.deleteEmployee(employeeToDelete);
//
//                                if (getActivity() == null || !isAdded()) return;
//
//                                requireActivity().runOnUiThread(() -> {
//                                    Toast.makeText(requireContext(), "Employee Deleted Successfully", Toast.LENGTH_SHORT).show();
//
//                                    Bundle result = new Bundle();
//                                    result.putBoolean("DELETE_EMPLOYEE", true);
//                                    result.putInt("POSITION", position);
//
//                                    getParentFragmentManager().setFragmentResult("EMPLOYEE_DETAILS_RESULT", result);
//                                    getParentFragmentManager().popBackStack();
//                                });
//                            });
//                        } else {
//                            Toast.makeText(requireContext(), "Error: Employee not found in database!", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                });
//            });
//
//            AlertDialog alertDialog = builder.create();
//            alertDialog.show();
//        });
//
//        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
//    }
//
//    private void updateUI() {
//        eName.setText(name);
//        eEmail.setText(email);
//        eRole.setText(role);
//        ePhone.setText(phone);
//        eDEPARTMENT.setText(department);
//        eID.setText(id);
//        eJoiningDate.setText(date);
//    }
//}