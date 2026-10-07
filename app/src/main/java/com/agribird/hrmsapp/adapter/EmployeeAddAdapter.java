package com.agribird.hrmsapp.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.R;

import java.util.Calendar;

public class EmployeeAddAdapter extends RecyclerView.Adapter<EmployeeAddAdapter.EmployeeFormViewHolder> {

    private final Context context;
    private final OnRegisterClickListener onRegisterClickListener;
    private EmployeeFormViewHolder formViewHolder;

    // Callback Interface
    public interface OnRegisterClickListener {
        void onRegisterClick();
    }

    // Constructor
    public EmployeeAddAdapter(Context context, OnRegisterClickListener onRegisterClickListener) {
        this.context = context;
        this.onRegisterClickListener = onRegisterClickListener;
    }

    @NonNull
    @Override
    public EmployeeFormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_add_employee_form, parent, false);
        return new EmployeeFormViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeFormViewHolder holder, int position) {
        formViewHolder = holder;

        // ==========================================
        // DEPARTMENT SPINNER
        // ==========================================
        ArrayAdapter<CharSequence> deptAdapter = ArrayAdapter.createFromResource(
                context, R.array.departments_array, android.R.layout.simple_spinner_item);
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.emList.setAdapter(deptAdapter);

        // ==========================================
        // BLOOD GROUP SPINNER (NEW)
        // ==========================================
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(
                context, R.array.blood_groups_array, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spBloodGroup.setAdapter(bloodAdapter);

        // ==========================================
        // DEPARTMENT -> ROLE
        // ==========================================
        holder.emList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int roleArrayId;
                switch (position) {
                    case 1: roleArrayId = R.array.roles_management; break;
                    case 2: roleArrayId = R.array.roles_sales; break;
                    case 3: roleArrayId = R.array.roles_it; break;
                    case 4: roleArrayId = R.array.roles_finance; break;
                    case 5: roleArrayId = R.array.roles_hr; break;
                    case 6: roleArrayId = R.array.roles_operations; break;
                    case 7: roleArrayId = R.array.roles_quality; break;
                    case 8: roleArrayId = R.array.roles_data_mis; break;
                    case 9: roleArrayId = R.array.roles_csr; break;
                    case 10: roleArrayId = R.array.roles_logistics; break;
                    default: roleArrayId = -1; break;
                }

                if (roleArrayId != -1) {
                    ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
                            context, roleArrayId, android.R.layout.simple_spinner_item);
                    roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.emRoleList.setAdapter(roleAdapter);
                } else {
                    ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(
                            context, android.R.layout.simple_spinner_item, new String[]{"Select Role"});
                    emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.emRoleList.setAdapter(emptyAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ==========================================
        // JOINING DATE PICKER
        // ==========================================
        holder.eJoiningDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                    (datePickerView, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        holder.eJoiningDate.setText(formattedDate);
                    }, year, month, day);

            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });

        // ==========================================
        // REGISTER BUTTON
        // ==========================================
        holder.btnRegi.setOnClickListener(v -> {
            if (onRegisterClickListener != null) {
                onRegisterClickListener.onRegisterClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    // ==========================================
    // GET FORM DATA
    // ==========================================
    public EmployeeFormData getFormData() {
        if (formViewHolder == null) {
            return null;
        }

        String name = formViewHolder.eName.getText().toString().trim();
        String email = formViewHolder.eEmail.getText().toString().trim();
        String phone = formViewHolder.ePhone.getText().toString().trim();
        String emergencyPhone = formViewHolder.eEmergencyPhone.getText().toString().trim();
        String address = formViewHolder.eAddress.getText().toString().trim();
        String empId = formViewHolder.eEmpID.getText().toString().trim();

        String selectedRole = "";
        if (formViewHolder.emRoleList != null && formViewHolder.emRoleList.getAdapter() != null) {
            int selectedPos = formViewHolder.emRoleList.getSelectedItemPosition();
            if (selectedPos != Spinner.INVALID_POSITION && selectedPos < formViewHolder.emRoleList.getAdapter().getCount()) {
                Object item = formViewHolder.emRoleList.getAdapter().getItem(selectedPos);
                if (item != null) selectedRole = item.toString().trim();
            }
        }

        String selectedDept = "";
        if (formViewHolder.emList != null && formViewHolder.emList.getSelectedItem() != null) {
            selectedDept = formViewHolder.emList.getSelectedItem().toString().trim();
        }

        String selectedBloodGroup = "";
        if (formViewHolder.spBloodGroup != null && formViewHolder.spBloodGroup.getSelectedItem() != null) {
            selectedBloodGroup = formViewHolder.spBloodGroup.getSelectedItem().toString().trim();
        }

        String pass = formViewHolder.ePass.getText().toString().trim();
        String passCon = formViewHolder.ePassCon.getText().toString().trim();
        String date = formViewHolder.eJoiningDate.getText().toString().trim();

        return new EmployeeFormData(
                name, email, phone, emergencyPhone, selectedBloodGroup,
                address, empId, selectedRole, selectedDept, pass, passCon, date
        );
    }

    // ==========================================
    // VIEW HOLDER
    // ==========================================
    public static class EmployeeFormViewHolder extends RecyclerView.ViewHolder {
        EditText eName, eEmail, ePhone, eEmergencyPhone, eAddress, eEmpID, ePass, ePassCon, eJoiningDate;
        Spinner emList, emRoleList, spBloodGroup;
        Button btnRegi;

        public EmployeeFormViewHolder(@NonNull View itemView) {
            super(itemView);
            eName = itemView.findViewById(R.id.eName);
            eEmail = itemView.findViewById(R.id.eEmail);
            ePhone = itemView.findViewById(R.id.ePhone);
            eEmergencyPhone = itemView.findViewById(R.id.eEmergencyPhone);
            spBloodGroup = itemView.findViewById(R.id.spBloodGroup);
            eAddress = itemView.findViewById(R.id.eAddress);
            eEmpID = itemView.findViewById(R.id.eEmpID);
            ePass = itemView.findViewById(R.id.ePass);
            ePassCon = itemView.findViewById(R.id.ePassCon);
            eJoiningDate = itemView.findViewById(R.id.eJoiningDate);
            emList = itemView.findViewById(R.id.emList);
            emRoleList = itemView.findViewById(R.id.emRoleList);
            btnRegi = itemView.findViewById(R.id.btnRegi);
        }
    }
    public static class EmployeeFormData {
        public String name, email, phone, emergencyPhone, bloodGroup, address, empId, role, department, password, confirmPassword, joiningDate;

        public EmployeeFormData(String name, String email, String phone, String emergencyPhone,
                                String bloodGroup, String address, String empId, String role,
                                String department, String password, String confirmPassword, String joiningDate) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.emergencyPhone = emergencyPhone;
            this.bloodGroup = bloodGroup;
            this.address = address;
            this.empId = empId;
            this.role = role;
            this.department = department;
            this.password = password;
            this.confirmPassword = confirmPassword;
            this.joiningDate = joiningDate;
        }
    }
}

//package com.agribird.hrmsapp.adapter;
//
//import android.app.DatePickerDialog;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Spinner;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.R;
//
//import java.util.Calendar;
//
//public class EmployeeAddAdapter extends RecyclerView.Adapter<EmployeeAddAdapter.EmployeeFormViewHolder> {
//
//    private final Context context;
//
//    // Fragment मधून Register logic Adapter ला मिळण्यासाठी
//    private final OnRegisterClickListener onRegisterClickListener;
//
//    private EmployeeFormViewHolder formViewHolder;
//
//
//    // Callback Interface
//    public interface OnRegisterClickListener {
//        void onRegisterClick();
//    }
//
//
//    // Constructor
//    public EmployeeAddAdapter(Context context, OnRegisterClickListener onRegisterClickListener) {
//        this.context = context;
//        this.onRegisterClickListener = onRegisterClickListener;
//    }
//
//
//    @NonNull
//    @Override
//    public EmployeeFormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_add_employee_form, parent, false);
//
//        return new EmployeeFormViewHolder(view);
//    }
//
//
//    @Override
//    public void onBindViewHolder(@NonNull EmployeeFormViewHolder holder, int position) {
//        // Current Form ViewHolder save करणे
//        formViewHolder = holder;
//        // ==========================================
//        // DEPARTMENT SPINNER
//        // ==========================================
//
//        ArrayAdapter<CharSequence> deptAdapter = ArrayAdapter.createFromResource(context, R.array.departments_array, android.R.layout.simple_spinner_item);
//        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        holder.emList.setAdapter(deptAdapter);
//
//
//        // ==========================================
//        // DEPARTMENT -> ROLE
//        // ==========================================
//
//        holder.emList.setOnItemSelectedListener(
//                new AdapterView.OnItemSelectedListener() {
//
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//                        int roleArrayId;
//
//                        switch (position) {
//
//                            case 1:
//                                roleArrayId = R.array.roles_management;
//                                break;
//                            case 2:
//                                roleArrayId = R.array.roles_sales;
//                                break;
//                            case 3:
//                                roleArrayId = R.array.roles_it;
//                                break;
//                            case 4: roleArrayId = R.array.roles_finance;
//                                break;
//                            case 5: roleArrayId = R.array.roles_hr;
//                                break;
//                            case 6: roleArrayId = R.array.roles_operations;
//                                break;
//                            case 7: roleArrayId = R.array.roles_quality;
//                                break;
//                            case 8: roleArrayId = R.array.roles_data_mis;
//                                break;
//                            case 9: roleArrayId = R.array.roles_csr;
//                                break;
//                            case 10: roleArrayId = R.array.roles_logistics;
//                                break;
//                            default:
//                                roleArrayId = -1;
//                                break;
//                        }
//
//
//                        if (roleArrayId != -1) {
//
//                            ArrayAdapter<CharSequence> roleAdapter =
//                                    ArrayAdapter.createFromResource(context,
//                                            roleArrayId,
//                                            android.R.layout.simple_spinner_item);
//                            roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                            holder.emRoleList.setAdapter(roleAdapter);
//
//                        } else {
//                            // Default empty adapter
//                            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(context,
//                                    android.R.layout.simple_spinner_item,
//                                    new String[]{"Select Role"}
//                            );
//                            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                            holder.emRoleList.setAdapter(emptyAdapter);
//                        }
//                    }
//
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//                    }
//                }
//        );
//
//
//        // ==========================================
//        // JOINING DATE PICKER
//        // ==========================================
//
//        holder.eJoiningDate.setOnClickListener(v -> {
//
//            Calendar calendar = Calendar.getInstance();
//
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//
//            DatePickerDialog datePickerDialog =
//                    new DatePickerDialog(context, (datePickerView, selectedYear, selectedMonth, selectedDay) -> {
//                                String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
//                                holder.eJoiningDate.setText(formattedDate);
//                            }, year, month, day);
//
//
//            // Future date select करू नये
//            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
//
//            datePickerDialog.show();
//        });
//
//
//        // ==========================================
//        // REGISTER / ADD EMPLOYEE BUTTON
//        // ==========================================
//
//        holder.btnRegi.setOnClickListener(v -> {
//
//            if (onRegisterClickListener != null) {
//
//                onRegisterClickListener
//                        .onRegisterClick();
//            }
//        });
//    }
//
//
//    @Override
//    public int getItemCount() {
//
//        return 1;
//    }
//
//
//    // ==========================================
//    // GET FORM DATA (UPDATED & FIXED)
//    // ==========================================
//
//    public EmployeeFormData getFormData() {
//
//        if (formViewHolder == null) {
//            return null;
//        }
//
//        String name = formViewHolder.eName
//                        .getText()
//                        .toString()
//                        .trim();
//
//        String email = formViewHolder.eEmail
//                        .getText()
//                        .toString()
//                        .trim();
//
//        String phone = formViewHolder.ePhone
//                        .getText()
//                        .toString()
//                        .trim();
//
//        String empId = formViewHolder.eEmpID
//                        .getText()
//                        .toString()
//                        .trim();
//
//
//        // Selected Role get करणे (Safe approach)
//        String selectedRole = "";
//        if (formViewHolder.emRoleList != null && formViewHolder.emRoleList.getAdapter() != null) {
//            int selectedPos = formViewHolder.emRoleList.getSelectedItemPosition();
//            if (selectedPos != Spinner.INVALID_POSITION && selectedPos < formViewHolder.emRoleList.getAdapter().getCount()) {
//                Object item = formViewHolder.emRoleList.getAdapter().getItem(selectedPos);
//                if (item != null) {
//                    selectedRole = item.toString().trim();
//                }
//            }
//        }
//
//
//        // Selected Department get करणे
//        String selectedDept = "";
//        if (formViewHolder.emList != null && formViewHolder.emList.getSelectedItem() != null) {
//            selectedDept = formViewHolder.emList
//                            .getSelectedItem()
//                            .toString()
//                            .trim();
//        }
//
//        String pass = formViewHolder.ePass
//                        .getText()
//                        .toString()
//                        .trim();
//
//        String passCon = formViewHolder.ePassCon
//                        .getText()
//                        .toString()
//                        .trim();
//
//        String date = formViewHolder.eJoiningDate
//                        .getText()
//                        .toString()
//                        .trim();
//
//
//        return new EmployeeFormData(
//                name,
//                email,
//                phone,
//                empId,
//                selectedRole,
//                selectedDept,
//                pass,
//                passCon,
//                date
//        );
//    }
//
//
//    // ==========================================
//    // VIEW HOLDER
//    // ==========================================
//
//    public static class EmployeeFormViewHolder
//            extends RecyclerView.ViewHolder {
//
//        EditText eName;
//        EditText eEmail;
//        EditText ePhone;
//        EditText eEmpID;
//        EditText ePass;
//        EditText ePassCon;
//        EditText eJoiningDate;
//        Spinner emList;
//        Spinner emRoleList;
//
//        Button btnRegi;
//
//
//        public EmployeeFormViewHolder(
//                @NonNull View itemView) {
//
//            super(itemView);
//
//
//            eName = itemView.findViewById(R.id.eName);
//            eEmail = itemView.findViewById(R.id.eEmail);
//            ePhone =itemView.findViewById(R.id.ePhone);
//            eEmpID = itemView.findViewById(R.id.eEmpID);
//            ePass = itemView.findViewById(R.id.ePass);
//            ePassCon = itemView.findViewById(R.id.ePassCon);
//            eJoiningDate = itemView.findViewById(R.id.eJoiningDate);
//            emList = itemView.findViewById(R.id.emList);
//            emRoleList = itemView.findViewById(R.id.emRoleList);
//            btnRegi = itemView.findViewById(R.id.btnRegi);
//        }
//    }
//
//
//    // ==========================================
//    // FORM DATA MODEL
//    // ==========================================
//
//    public static class EmployeeFormData {
//
//        public String name;
//
//        public String email;
//
//        public String phone;
//
//        public String empId;
//
//        public String role;
//
//        public String department;
//
//        public String password;
//
//        public String confirmPassword;
//
//        public String joiningDate;
//
//
//        public EmployeeFormData(
//                String name,
//                String email,
//                String phone,
//                String empId,
//                String role,
//                String department,
//                String password,
//                String confirmPassword,
//                String joiningDate) {
//
//            this.name = name;
//            this.email = email;
//            this.phone = phone;
//            this.empId = empId;
//            this.role = role;
//            this.department = department;
//            this.password = password;
//            this.confirmPassword = confirmPassword;
//            this.joiningDate = joiningDate;
//        }
//    }
//}