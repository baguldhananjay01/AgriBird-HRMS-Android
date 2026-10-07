package com.agribird.hrmsapp.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;

public class ApplyLeaveAdapter extends RecyclerView.Adapter<ApplyLeaveAdapter.LeaveViewHolder> {

    private final Context context;
    private final OnLeaveActionListener listener;
    private final String employeeName;
    private final String employeeId;

    public interface OnLeaveActionListener {

        void onSubmitLeave(
                int position,
                String employeeName,
                String employeeId,
                String leaveType,
                String startDate,
                String endDate,
                String reason
        );
    }

    public ApplyLeaveAdapter(
            Context context,
            OnLeaveActionListener listener,
            String employeeName,
            String employeeId) {

        this.context = context;
        this.listener = listener;
        this.employeeName = employeeName;
        this.employeeId = employeeId;
    }

    @NonNull
    @Override
    public LeaveViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_apply_leave, parent, false);

        return new LeaveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LeaveViewHolder holder,
            int position) {

        holder.edtUser.setText(employeeName);
        holder.edtId.setText(employeeId);

        holder.edtUser.setEnabled(false);
        holder.edtId.setEnabled(false);

        ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(context, R.array.leave_types, android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        holder.spinnerLeaveType.setAdapter(spinnerAdapter);

        // START DATE
        holder.edtStartDate.setOnClickListener(v -> showDatePicker(holder.edtStartDate));

        holder.edtStartDate.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        showDatePicker(holder.edtStartDate);
                    }
                });

        // END DATE
        holder.editEndDate.setOnClickListener(v -> showDatePicker(holder.editEndDate));

        holder.editEndDate.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        showDatePicker(holder.editEndDate);
                    }
                }
        );

        // SUBMIT
        holder.btnSubmitLeave.setOnClickListener(v -> {
            String selectedLeave = holder.spinnerLeaveType.getSelectedItem().toString().trim();
            String startDate = holder.edtStartDate.getText().toString().trim();
            String endDate = holder.editEndDate.getText().toString().trim();
            String reason = holder.edtReason.getText().toString().trim();

            if (selectedLeave.isEmpty() || selectedLeave.equalsIgnoreCase("Select Leave Type")) {

                Toast.makeText(context, "Please select a leave type", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDate.isEmpty()) {
                Toast.makeText(context, "Please select start date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endDate.isEmpty()) {

                Toast.makeText(context, "Please select end date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reason.isEmpty()) {
                Toast.makeText(context, "Please enter reason", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {

                listener.onSubmitLeave(
                        position,
                        employeeName,
                        employeeId,
                        selectedLeave,
                        startDate,
                        endDate,
                        reason
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private void showDatePicker(EditText editText) {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        context,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String date = String.format(
                                            java.util.Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                            editText.setText(date);
                        }, year, month, day);

        datePickerDialog.show();
    }

    public static class LeaveViewHolder
            extends RecyclerView.ViewHolder {

        MaterialCardView cardEmployeeForm;

        TextView txtLeaveTitle;
        TextView txtLeaveSubtitle;

        TextView lblUser;
        TextView lblId;
        TextView lblLeaveType;
        TextView lblStartDate;
        TextView lblEndDate;
        TextView lblReason;

        EditText edtUser;
        EditText edtId;
        EditText edtStartDate;
        EditText editEndDate;
        EditText edtReason;

        Spinner spinnerLeaveType;

        MaterialButton btnSubmitLeave;

        public LeaveViewHolder(@NonNull View itemView) {
            super(itemView);

            cardEmployeeForm = itemView.findViewById(R.id.cardEmployeeForm);
            txtLeaveTitle = itemView.findViewById(R.id.txtLeaveTitle);
            txtLeaveSubtitle = itemView.findViewById(R.id.txtLeaveSubtitle);
            lblUser = itemView.findViewById(R.id.lblUser);
            lblId = itemView.findViewById(R.id.lblId);
            lblLeaveType = itemView.findViewById(R.id.lblLeaveType);
            lblStartDate = itemView.findViewById(R.id.lblStartDate);
            lblEndDate = itemView.findViewById(R.id.lblEndDate);
            lblReason = itemView.findViewById(R.id.lblReason);
            edtUser = itemView.findViewById(R.id.edtUser);
            edtId = itemView.findViewById(R.id.edtId);
            edtStartDate = itemView.findViewById(R.id.edtStartDate);
            editEndDate = itemView.findViewById(R.id.editEndDate);
            edtReason = itemView.findViewById(R.id.edtReason);
            spinnerLeaveType = itemView.findViewById(R.id.spinnerLeaveType);
            btnSubmitLeave = itemView.findViewById(R.id.btnSubmitLeave);
        }
    }
}
//package com.agribird.hrmsapp.adapter;
//
//import android.app.DatePickerDialog;
//import android.content.Context;
//import android.content.SharedPreferences;
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
//import com.agribird.hrmsapp.R;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.card.MaterialCardView;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Locale;
//
//public class ApplyLeaveAdapter extends RecyclerView.Adapter<ApplyLeaveAdapter.LeaveViewHolder> {
//
//    private final Context context;
//    private final OnLeaveActionListener listener;
//    private final String employeeName;
//    private final String employeeId;
//
//    // Listener interface
//    public interface OnLeaveActionListener {
//        void onSubmitLeave(
//                int position,
//                String employeeName,
//                String employeeId,
//                String leaveType,
//                String startDate,
//                String endDate,
//                String reason
//        );
//    }
//
//    public ApplyLeaveAdapter(Context context, OnLeaveActionListener listener, String employeeName, String employeeId) {
//        this.context = context;
//        this.listener = listener;
//        this.employeeName = employeeName;
//        this.employeeId = employeeId;
//    }
//
//    @NonNull
//    @Override
//    public LeaveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_apply_leave, parent, false);
//        return new LeaveViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull LeaveViewHolder holder, int position) {
//        // Set default values
//        holder.edtUser.setText(employeeName);
//        holder.edtId.setText(employeeId);
//
//        // Setup Spinner with leave types from array
//        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
//                context,
//                R.array.leave_types,
//                android.R.layout.simple_spinner_item
//        );
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        holder.spinnerLeaveType.setAdapter(spinnerAdapter);
//
//        // Date Pickers for Start Date
//        holder.edtStartDate.setOnClickListener(v -> showDatePicker(holder.edtStartDate));
//        holder.edtStartDate.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) showDatePicker(holder.edtStartDate);
//        });
//
//        // Date Pickers for End Date
//        holder.editEndDate.setOnClickListener(v -> showDatePicker(holder.editEndDate));
//        holder.editEndDate.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) showDatePicker(holder.editEndDate);
//        });
//
//        // Submit button
//        holder.btnSubmitLeave.setOnClickListener(v -> {
//            String selectedLeave = holder.spinnerLeaveType.getSelectedItem().toString();
//            String startDate = holder.edtStartDate.getText().toString().trim();
//            String endDate = holder.editEndDate.getText().toString().trim();
//            String reason = holder.edtReason.getText().toString().trim();
//
//            // Basic validation (though fragment does more)
//            if (selectedLeave.equals("Select Leave Type") || selectedLeave.isEmpty()) {
//                Toast.makeText(context, "Please select a leave type", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (startDate.isEmpty()) {
//                Toast.makeText(context, "Please select start date", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (endDate.isEmpty()) {
//                Toast.makeText(context, "Please select end date", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (reason.isEmpty()) {
//                Toast.makeText(context, "Please enter reason", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Pass data to fragment via listener
//            if (listener != null) {
//                listener.onSubmitLeave(
//                        position,
//                        employeeName,
//                        employeeId,
//                        selectedLeave,
//                        startDate,
//                        endDate,
//                        reason
//                );
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return 1; // Only one form item
//    }
//
//    // Helper to show DatePickerDialog
//    private void showDatePicker(final EditText editText) {
//        final Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                context,
//                (view, selectedYear, selectedMonth, selectedDay) -> {
//                    // Format date as d/M/yyyy (matching fragment's parse format)
//                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
//                    editText.setText(date);
//                },
//                year, month, day
//        );
//        datePickerDialog.show();
//    }
//
//    // ViewHolder class
//    public static class LeaveViewHolder extends RecyclerView.ViewHolder {
//        MaterialCardView cardEmployeeForm;
//        TextView txtLeaveTitle, txtLeaveSubtitle, lblUser, lblId, lblLeaveType, lblStartDate, lblEndDate, lblReason;
//        EditText edtUser, edtId, edtStartDate, editEndDate, edtReason;
//        Spinner spinnerLeaveType;
//        MaterialButton btnSubmitLeave;
//
//        public LeaveViewHolder(@NonNull View itemView) {
//            super(itemView);
//            cardEmployeeForm = itemView.findViewById(R.id.cardEmployeeForm);
//            txtLeaveTitle = itemView.findViewById(R.id.txtLeaveTitle);
//            txtLeaveSubtitle = itemView.findViewById(R.id.txtLeaveSubtitle);
//            lblUser = itemView.findViewById(R.id.lblUser);
//            lblId = itemView.findViewById(R.id.lblId);
//            lblLeaveType = itemView.findViewById(R.id.lblLeaveType);
//            lblStartDate = itemView.findViewById(R.id.lblStartDate);
//            lblEndDate = itemView.findViewById(R.id.lblEndDate);
//            lblReason = itemView.findViewById(R.id.lblReason);
//            edtUser = itemView.findViewById(R.id.edtUser);
//            edtId = itemView.findViewById(R.id.edtId);
//            edtStartDate = itemView.findViewById(R.id.edtStartDate);
//            editEndDate = itemView.findViewById(R.id.editEndDate);
//            edtReason = itemView.findViewById(R.id.edtReason);
//            spinnerLeaveType = itemView.findViewById(R.id.spinnerLeaveType);
//            btnSubmitLeave = itemView.findViewById(R.id.btnSubmitLeave);
//        }
//    }
//}