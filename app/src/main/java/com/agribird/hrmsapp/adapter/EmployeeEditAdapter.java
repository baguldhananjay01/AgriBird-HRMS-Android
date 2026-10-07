package com.agribird.hrmsapp.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.Employee;
import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.api.EmployeeApi;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeEditAdapter extends RecyclerView.Adapter<EmployeeEditAdapter.EditViewHolder> {

    private final Context context;
    private final Employee employee;
    private final int employeePosition;
    private final OnEmployeeEditListener listener;
    //private final EmployeeApi employeeApi;
    public interface OnEmployeeEditListener {

        void onUpdateEmployee(
                String employeeId,
                EmployeeApiModel apiEmployee
        );

        void onCancel();
    }

    public EmployeeEditAdapter(
            Context context,
            Employee employee,
            int employeePosition,
            OnEmployeeEditListener listener) {

        this.context = context;
        this.employee = employee;
        this.employeePosition = employeePosition;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee_edit_form, parent, false);
        return new EditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditViewHolder holder, int position) {
        holder.bindEmployee(employee);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class EditViewHolder extends RecyclerView.ViewHolder {
        EditText editName, editEmail, editPhone, eEmergencyPhone, eAddress, editID, eJoiningDate;
        TextView txtEmployeeName, txtEmployeeId, txtEmployeeRole;
        Spinner emList, emRoleList, spBloodGroup;
        View btnSaveChanges, btnCancel;
        public EditViewHolder(@NonNull View itemView) {
            super(itemView);

            editName = itemView.findViewById(R.id.editName);
            editEmail = itemView.findViewById(R.id.editEmail);
            editPhone = itemView.findViewById(R.id.editPhone);
            eEmergencyPhone = itemView.findViewById(R.id.eEmergencyPhone);
            eAddress = itemView.findViewById(R.id.eAddress);
            editID = itemView.findViewById(R.id.editID);
            eJoiningDate = itemView.findViewById(R.id.eJoiningDate);

            txtEmployeeName = itemView.findViewById(R.id.txtEmployeeName);
            txtEmployeeId = itemView.findViewById(R.id.txtEmployeeId);
            txtEmployeeRole = itemView.findViewById(R.id.txtEmployeeRole);

            emList = itemView.findViewById(R.id.emList);
            emRoleList = itemView.findViewById(R.id.emRoleList);
            spBloodGroup = itemView.findViewById(R.id.spBloodGroup);

            btnSaveChanges = itemView.findViewById(R.id.btnSaveChanges);
            btnCancel = itemView.findViewById(R.id.btnCancel);

            setupSpinners();

            eJoiningDate.setOnClickListener(v -> openDatePicker());

            btnSaveChanges.setOnClickListener(v -> updateEmployee());

            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancel();
                }

            });
        }
        private void setupSpinners() {

            ArrayAdapter<CharSequence> departmentAdapter =
                    ArrayAdapter.createFromResource(context, R.array.departments_array, android.R.layout.simple_spinner_item);
            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            emList.setAdapter(departmentAdapter);

            ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(context, R.array.blood_groups_array,android.R.layout.simple_spinner_item);

            bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spBloodGroup.setAdapter(bloodAdapter);

            emList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            int roleArrayId;
                            switch (position) {
                                case 1: roleArrayId = R.array.roles_management;
                                break;
                                case 2: roleArrayId = R.array.roles_sales;
                                break;
                                case 3: roleArrayId = R.array.roles_it;
                                break;
                                case 4: roleArrayId = R.array.roles_finance;
                                break;
                                case 5: roleArrayId = R.array.roles_hr;
                                break;
                                case 6: roleArrayId = R.array.roles_operations;
                                break;
                                case 7: roleArrayId = R.array.roles_quality;break;
                                case 8: roleArrayId = R.array.roles_data_mis;
                                break;
                                case 9: roleArrayId = R.array.roles_csr;
                                break;
                                case 10: roleArrayId = R.array.roles_logistics;
                                break;
                                default: roleArrayId = -1;
                                break;
                            }

                            if (roleArrayId != -1) {

                                ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(context, roleArrayId, android.R.layout.simple_spinner_item);
                                roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                emRoleList.setAdapter(roleAdapter);

                                selectSpinnerItemByValue(emRoleList, employee.getRole());

                            } else {

                                ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new String[]{"Select Role"});

                                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                emRoleList.setAdapter(emptyAdapter);
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );
        }

        private void bindEmployee(Employee employee) {

            txtEmployeeName.setText(employee.getName());
            txtEmployeeId.setText(employee.getEmpId());
            txtEmployeeRole.setText(employee.getRole());
            editName.setText(employee.getName());
            editEmail.setText(employee.getEmail());
            editPhone.setText(employee.getPhone());
            eEmergencyPhone.setText(employee.getEmergencyPhone());
            eAddress.setText(employee.getAddress());
            editID.setText(employee.getEmpId());
            eJoiningDate.setText(employee.getJoiningDate());
            selectSpinnerItemByValue(emList, employee.getDepartment());
            selectSpinnerItemByValue(spBloodGroup, employee.getBloodGroup());
        }

        private void selectSpinnerItemByValue(Spinner spinner, String value) {

            if (spinner.getAdapter() == null ||
                    value == null) {
                return;
            }

            for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
                Object item = spinner.getAdapter().getItem(i);

                if (item != null && item.toString().equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        private void openDatePicker() {

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view,selectedYear, selectedMonth, selectedDay) -> {
                                String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                eJoiningDate.setText(selectedDate);
                            }, year, month, day);
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        }
        private void updateEmployee() {

            String updateName = editName.getText().toString().trim();
            String updateEmail = editEmail.getText().toString().trim();
            String updatePhone = editPhone.getText().toString().trim();
            String updateEmergencyPhone = eEmergencyPhone.getText().toString().trim();
            String updateAddress = eAddress.getText().toString().trim();
            String updateId = editID.getText().toString().trim();
            String updateJoiningDate = eJoiningDate.getText().toString().trim();
            String updateDepartment = emList.getSelectedItem() != null ? emList.getSelectedItem().toString().trim() : "";
            String updateRole = emRoleList.getSelectedItem() != null ? emRoleList.getSelectedItem().toString().trim() : "";
            String updateBloodGroup = spBloodGroup.getSelectedItem() != null ? spBloodGroup.getSelectedItem().toString().trim() : "";

            if (TextUtils.isEmpty(updateName)) {
                editName.setError("Name is required");
                return;
            }

            if (TextUtils.isEmpty(updateEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(updateEmail).matches()) {
                editEmail.setError("Valid email is required");
                return;
            }
            if (TextUtils.isEmpty(updatePhone) || !updatePhone.matches("[0-9]{10}")) {
                editPhone.setError("Enter valid 10-digit number");
                return;
            }
            if (!updateEmergencyPhone.isEmpty() && !updateEmergencyPhone.matches("[0-9]{10}")) {
                eEmergencyPhone.setError("Enter valid 10-digit emergency number");
                return;
            }
            if (TextUtils.isEmpty(updateId)) {
                editID.setError("Employee ID is required");
                return;
            }


            if (TextUtils.isEmpty(updateDepartment) || updateDepartment.equalsIgnoreCase("Select Department")) {

                Toast.makeText(context, "Please select department", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(updateRole) || updateRole.equalsIgnoreCase("Select Role")) {
                Toast.makeText(context, "Please select role", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(updateJoiningDate)) {
                eJoiningDate.setError("Joining date is required");
                return;
            }

            EmployeeApiModel apiEmployee = new EmployeeApiModel();
            apiEmployee.setEmpID(updateId);
            apiEmployee.setName(updateName);
            apiEmployee.setEmail(updateEmail);
            apiEmployee.setDepartment(updateDepartment);
            apiEmployee.setRole(updateRole);
            apiEmployee.setPhone(updatePhone);
            apiEmployee.setJoiningDate(updateJoiningDate);
            apiEmployee.setPassword(employee.getPassword());
            apiEmployee.setStatus(employee.getStatus());
            apiEmployee.setEmergencyPhone(updateEmergencyPhone);
            apiEmployee.setBloodGroup(updateBloodGroup);
            apiEmployee.setAddress(updateAddress);

            if (listener != null) {
                listener.onUpdateEmployee(
                        employee.getEmpId(),
                        apiEmployee
                );
            }
        }
    }
}

//package com.agribird.hrmsapp.adapter;
//
//import android.app.DatePickerDialog;
//import android.content.Context;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//
//import java.util.Calendar;
//
//public class EmployeeEditAdapter extends RecyclerView.Adapter<EmployeeEditAdapter.EditViewHolder> {
//
//    private final Context context;
//    private final EmployeeDao employeeDao;
//    private final Employee employee;
//    private final int employeePosition;
//    private final OnEmployeeEditListener listener;
//
//    public interface OnEmployeeEditListener {
//        void onEmployeeUpdated(Employee updatedEmployee, int position);
//        void onCancel();
//    }
//
//    public EmployeeEditAdapter(
//            Context context,
//            EmployeeDao employeeDao,
//            Employee employee,
//            int employeePosition,
//            OnEmployeeEditListener listener) {
//
//        this.context = context;
//        this.employeeDao = employeeDao;
//        this.employee = employee;
//        this.employeePosition = employeePosition;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public EditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee_edit_form, parent, false);
//        return new EditViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull EditViewHolder holder, int position) {
//        holder.bindEmployee(employee);
//    }
//
//    @Override
//    public int getItemCount() {
//        return 1;
//    }
//
//    public class EditViewHolder extends RecyclerView.ViewHolder {
//
//        EditText editName, editEmail, editPhone, eEmergencyPhone, eAddress, editID, eJoiningDate;
//        TextView txtEmployeeName, txtEmployeeId, txtEmployeeRole;
//        Spinner emList, emRoleList, spBloodGroup;
//        View btnSaveChanges, btnCancel;
//
//        public EditViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            editName = itemView.findViewById(R.id.editName);
//            editEmail = itemView.findViewById(R.id.editEmail);
//            editPhone = itemView.findViewById(R.id.editPhone);
//            eEmergencyPhone = itemView.findViewById(R.id.eEmergencyPhone);
//            eAddress = itemView.findViewById(R.id.eAddress);
//            editID = itemView.findViewById(R.id.editID);
//            eJoiningDate = itemView.findViewById(R.id.eJoiningDate);
//
//            txtEmployeeName = itemView.findViewById(R.id.txtEmployeeName);
//            txtEmployeeId = itemView.findViewById(R.id.txtEmployeeId);
//            txtEmployeeRole = itemView.findViewById(R.id.txtEmployeeRole);
//
//            emList = itemView.findViewById(R.id.emList);
//            emRoleList = itemView.findViewById(R.id.emRoleList);
//            spBloodGroup = itemView.findViewById(R.id.spBloodGroup);
//
//            btnSaveChanges = itemView.findViewById(R.id.btnSaveChanges);
//            btnCancel = itemView.findViewById(R.id.btnCancel);
//
//            setupSpinners();
//
//            eJoiningDate.setOnClickListener(v -> openDatePicker());
//            btnSaveChanges.setOnClickListener(v -> updateEmployee());
//            btnCancel.setOnClickListener(v -> {
//                if (listener != null) listener.onCancel();
//            });
//        }
//
//        private void setupSpinners() {
//            // Department Spinner Setup
//            ArrayAdapter<CharSequence> departmentAdapter = ArrayAdapter.createFromResource(
//                    context, R.array.departments_array, android.R.layout.simple_spinner_item);
//            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            emList.setAdapter(departmentAdapter);
//
//            // Blood Group Spinner Setup
//            ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(
//                    context, R.array.blood_groups_array, android.R.layout.simple_spinner_item);
//            bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spBloodGroup.setAdapter(bloodAdapter);
//
//            // Dynamic Role mapping based on Department Selection
//            emList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    int roleArrayId;
//                    switch (position) {
//                        case 1: roleArrayId = R.array.roles_management; break;
//                        case 2: roleArrayId = R.array.roles_sales; break;
//                        case 3: roleArrayId = R.array.roles_it; break;
//                        case 4: roleArrayId = R.array.roles_finance; break;
//                        case 5: roleArrayId = R.array.roles_hr; break;
//                        case 6: roleArrayId = R.array.roles_operations; break;
//                        case 7: roleArrayId = R.array.roles_quality; break;
//                        case 8: roleArrayId = R.array.roles_data_mis; break;
//                        case 9: roleArrayId = R.array.roles_csr; break;
//                        case 10: roleArrayId = R.array.roles_logistics; break;
//                        default: roleArrayId = -1; break;
//                    }
//
//                    if (roleArrayId != -1) {
//                        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
//                                context, roleArrayId, android.R.layout.simple_spinner_item);
//                        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        emRoleList.setAdapter(roleAdapter);
//
//                        // Set existing role selection if matching
//                        selectSpinnerItemByValue(emRoleList, employee.getRole());
//                    } else {
//                        ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(
//                                context, android.R.layout.simple_spinner_item, new String[]{"Select Role"});
//                        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        emRoleList.setAdapter(emptyAdapter);
//                    }
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {}
//            });
//        }
//
//        private void bindEmployee(Employee employee) {
//            txtEmployeeName.setText(employee.getName());
//            txtEmployeeId.setText(employee.getEmpId());
//            txtEmployeeRole.setText(employee.getRole());
//
//            editName.setText(employee.getName());
//            editEmail.setText(employee.getEmail());
//            editPhone.setText(employee.getPhone());
//            eEmergencyPhone.setText(employee.getEmergencyPhone());
//            eAddress.setText(employee.getAddress());
//            editID.setText(employee.getId());
//            eJoiningDate.setText(employee.getJoiningDate());
//
//            // Set Department
//            selectSpinnerItemByValue(emList, employee.getDepartment());
//
//            // Set Blood Group
//            selectSpinnerItemByValue(spBloodGroup, employee.getBloodGroup());
//        }
//
//        private void selectSpinnerItemByValue(Spinner spinner, String value) {
//            if (spinner.getAdapter() != null && value != null) {
//                for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
//                    if (spinner.getAdapter().getItem(i).toString().equalsIgnoreCase(value)) {
//                        spinner.setSelection(i);
//                        break;
//                    }
//                }
//            }
//        }
//
//        private void openDatePicker() {
//            Calendar calendar = Calendar.getInstance();
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
//                    (view, selectedYear, selectedMonth, selectedDay) -> {
//                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
//                        eJoiningDate.setText(selectedDate);
//                    }, year, month, day);
//
//            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
//            datePickerDialog.show();
//        }
//
//        private void updateEmployee() {
//            String updateName = editName.getText().toString().trim();
//            String updateEmail = editEmail.getText().toString().trim();
//            String updatePhone = editPhone.getText().toString().trim();
//            String updateEmergencyPhone = eEmergencyPhone.getText().toString().trim();
//            String updateAddress = eAddress.getText().toString().trim();
//            String updateId = editID.getText().toString().trim();
//            String updateJoiningDate = eJoiningDate.getText().toString().trim();
//
//            String updateDepartment = emList.getSelectedItem() != null ? emList.getSelectedItem().toString().trim() : "";
//            String updateRole = emRoleList.getSelectedItem() != null ? emRoleList.getSelectedItem().toString().trim() : "";
//            String updateBloodGroup = spBloodGroup.getSelectedItem() != null ? spBloodGroup.getSelectedItem().toString().trim() : "";
//
//            // Validations
//            if (TextUtils.isEmpty(updateName)) {
//                editName.setError("Name is required");
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(updateEmail).matches()) {
//                editEmail.setError("Valid email is required");
//                return;
//            }
//
//            if (TextUtils.isEmpty(updatePhone) || !updatePhone.matches("[0-9]{10}")) {
//                editPhone.setError("Enter valid 10-digit number");
//                return;
//            }
//
//            if (!updateEmergencyPhone.isEmpty() && !updateEmergencyPhone.matches("[0-9]{10}")) {
//                eEmergencyPhone.setError("Enter valid 10-digit emergency number");
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateId)) {
//                editID.setError("Employee ID is required");
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateDepartment) || updateDepartment.equalsIgnoreCase("Select Department")) {
//                Toast.makeText(context, "Please select department", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateRole) || updateRole.equalsIgnoreCase("Select Role")) {
//                Toast.makeText(context, "Please select role", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateJoiningDate)) {
//                eJoiningDate.setError("Joining date is required");
//                return;
//            }
//
//            // DB Update Thread
//            new Thread(() -> {
//                Employee employeeUpdate = employeeDao.getEmployeeById(employee.getEmpId());
//
//                if (employeeUpdate == null) {
//                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
//                    mainHandler.post(() -> Toast.makeText(context, "Employee not found!", Toast.LENGTH_SHORT).show());
//                    return;
//                }
//
//                // Construct Employee with all fields
//                Employee updatedEmployee = new Employee(
//                        updateId,
//                        updateName,
//                        updateEmail,
//                        updateRole,
//                        updateDepartment,
//                        updatePhone,
//                        updateJoiningDate,
//                        employeeUpdate.getPassword(),
//                        updateEmergencyPhone,
//                        updateBloodGroup,
//                        updateAddress
//                );
//
//                employeeDao.updateEmployee(updatedEmployee);
//
//                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
//                mainHandler.post(() -> {
//                    Toast.makeText(context, "Employee Updated Successfully", Toast.LENGTH_SHORT).show();
//                    if (listener != null) {
//                        listener.onEmployeeUpdated(updatedEmployee, employeePosition);
//                    }
//                });
//            }).start();
//        }
//    }
//}

/// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//package com.agribird.hrmsapp.adapter;
//
//import android.app.DatePickerDialog;
//import android.content.Context;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//
//import java.util.Calendar;
//
//public class EmployeeEditAdapter extends RecyclerView.Adapter<EmployeeEditAdapter.EditViewHolder> {
//
//    private final Context context;
//    private final EmployeeDao employeeDao;
//    private final Employee employee;
//    private final int employeePosition;
//
//    private final OnEmployeeEditListener listener;
//
//    public interface OnEmployeeEditListener {
//        void onEmployeeUpdated(Employee updatedEmployee, int position);
//
//        void onCancel();
//    }
//
//    public EmployeeEditAdapter(
//            Context context,
//            EmployeeDao employeeDao,
//            Employee employee,
//            int employeePosition,
//            OnEmployeeEditListener listener) {
//
//        this.context = context;
//        this.employeeDao = employeeDao;
//        this.employee = employee;
//        this.employeePosition = employeePosition;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public EditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee_edit_form, parent, false);
//        return new EditViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull EditViewHolder holder, int position) {
//        holder.bindEmployee(employee);
//    }
//
//    @Override
//    public int getItemCount() {
//        return 1;
//    }
//
//    public class EditViewHolder extends RecyclerView.ViewHolder {
//
//        EditText editName;
//        EditText editEmail;
//        EditText editPhone;
//        EditText editRole;
//        EditText editID;
//        EditText eJoiningDate;
//        TextView txtEmployeeName, txtEmployeeId, txtEmployeeRole;
//
//        Spinner emList;
//
//        View btnSaveChanges;
//        View btnCancel;
//
//        public EditViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            editName = itemView.findViewById(R.id.editName);
//            editEmail = itemView.findViewById(R.id.editEmail);
//            editPhone = itemView.findViewById(R.id.editPhone);
//            editRole = itemView.findViewById(R.id.editRole);
//            editID = itemView.findViewById(R.id.editID);
//            eJoiningDate = itemView.findViewById(R.id.eJoiningDate);
//            txtEmployeeName = itemView.findViewById(R.id.txtEmployeeName);
//            txtEmployeeId = itemView.findViewById(R.id.txtEmployeeId);
//            txtEmployeeRole = itemView.findViewById(R.id.txtEmployeeRole);
//
//            emList = itemView.findViewById(R.id.emList);
//
//            btnSaveChanges = itemView.findViewById(R.id.btnSaveChanges);
//            btnCancel = itemView.findViewById(R.id.btnCancel);
//
//            setupDepartmentSpinner();
//
//            eJoiningDate.setOnClickListener(v -> openDatePicker());
//
//            btnSaveChanges.setOnClickListener(v -> updateEmployee());
//
//            btnCancel.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onCancel();
//                }
//            });
//        }
//
//        private void bindEmployee(Employee employee) {
//
//            txtEmployeeName.setText(employee.getName());
//            txtEmployeeId.setText(employee.getEmpId());
//            txtEmployeeRole.setText(employee.getRole());
//
//            editName.setText(employee.getName());
//            editEmail.setText(employee.getEmail());
//            editPhone.setText(employee.getPhone());
//            editRole.setText(employee.getRole());
//            editID.setText(employee.getId());
//            eJoiningDate.setText(employee.getJoiningDate());
//
//            // FIX: Safe Spinner Mapping
//            if (emList.getAdapter() != null) {
//                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) emList.getAdapter();
//                for (int i = 0; i < adapter.getCount(); i++) {
//                    if (adapter.getItem(i).toString().equalsIgnoreCase(employee.getDepartment())) {
//                        emList.setSelection(i);
//                        break;
//                    }
//                }
//            }
//        }
//
//        private void setupDepartmentSpinner() {
//
//            ArrayAdapter<CharSequence> departmentAdapter = ArrayAdapter.createFromResource(context, R.array.departments_array, android.R.layout.simple_spinner_item);
//
//            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            emList.setAdapter(departmentAdapter);
//        }
//
//        private void openDatePicker() {
//
//            Calendar calendar = Calendar.getInstance();
//
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;eJoiningDate.setText(selectedDate);}, year, month, day);
//
//            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
//            datePickerDialog.show();
//        }
//
//        private void updateEmployee() {
//
//            String updateName = editName.getText().toString().trim();
//            String updateEmail = editEmail.getText().toString().trim();
//            String updatePhone = editPhone.getText().toString().trim();
//            String updateRole = editRole.getText().toString().trim();
//            String updateId = editID.getText().toString().trim();
//            String updateDepartment = emList.getSelectedItem() != null ? emList.getSelectedItem().toString().trim() : "";
//            String updateJoiningDate = eJoiningDate.getText().toString().trim();
//
//            if (TextUtils.isEmpty(updateName)) {
//                editName.setError("Name is required");
//                editName.requestFocus();
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateEmail)) {
//                editEmail.setError("Email is required");
//                editEmail.requestFocus();
//                return;
//            }
//
//            if (!android.util.Patterns.EMAIL_ADDRESS
//                    .matcher(updateEmail)
//                    .matches()) {
//
//                editEmail.setError("Enter valid email");
//                editEmail.requestFocus();
//                return;
//            }
//
//            if (TextUtils.isEmpty(updatePhone)) {
//                editPhone.setError("Phone number is required");
//                editPhone.requestFocus();
//                return;
//            }
//
//            if (!updatePhone.matches("[0-9]{10}")) {
//                editPhone.setError("Enter valid 10-digit number");
//                editPhone.requestFocus();
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateRole)) {
//                editRole.setError("Role is required");
//                editRole.requestFocus();
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateId)) {
//                editID.setError("Employee ID is required");
//                editID.requestFocus();
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateDepartment) || updateDepartment.equals("Select Department")) {
//
//                Toast.makeText(context, "Please select department", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (TextUtils.isEmpty(updateJoiningDate)) {
//
//                eJoiningDate.setError("Joining date is required");
//                eJoiningDate.requestFocus();
//
//                return;
//            }
//
//
//            // ==========================
//            // DATABASE UPDATE
//            // ==========================
//
//            new Thread(() -> {
//
//                Employee employeeUpdate = employeeDao.getEmployeeById(employee.getEmpId());
//
//                if (employeeUpdate == null) {
//
//                    if (listener != null) {
//
//                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
//                        mainHandler.post(() -> Toast.makeText(context, "Employee not found!", Toast.LENGTH_SHORT).show());
//                    }
//                    return;
//                }
//
//                // FIX: Employee Constructor Sequence Updated (id, name, email, role, department, phone, joiningDate, password)
//                Employee updatedEmployee =
//                        new Employee(
//                                updateId,
//                                updateName,
//                                updateEmail,
//                                updateRole,
//                                updateDepartment,
//                                updatePhone,
//                                updateJoiningDate,
//                                employeeUpdate.getPassword()
//                        );
//
//
//                employeeDao.updateEmployee(updatedEmployee);
//
//                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
//
//                mainHandler.post(() -> {
//
//                    Toast.makeText(context, "Employee Updated Successfully", Toast.LENGTH_SHORT).show();
//
//                    if (listener != null) {
//                        listener.onEmployeeUpdated(updatedEmployee, employeePosition);
//                    }
//
//                });
//
//            }).start();
//        }
//    }
//}