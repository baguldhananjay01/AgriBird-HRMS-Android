package com.agribird.hrmsapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdminLeaveAdapter
        extends RecyclerView.Adapter<AdminLeaveAdapter.AdminViewHolder> {

    private final Context context;

    private List<LeaveApiModel> leaveList;
    private List<LeaveApiModel> filterLeaveList;

    private final OnLeaveStatusChangeListener listener;

    public interface OnLeaveStatusChangeListener {
        void onStatusChanged(long leaveId, String newStatus);
    }

    public AdminLeaveAdapter(
            Context context,
            List<LeaveApiModel> leaveList,
            List<LeaveApiModel> filterLeaveList,
            OnLeaveStatusChangeListener listener) {

        this.context = context;
        this.leaveList = leaveList != null ? new ArrayList<>(leaveList) : new ArrayList<>();
        this.filterLeaveList = filterLeaveList != null ? new ArrayList<>(filterLeaveList) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.admin_leave_item, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AdminViewHolder holder,
            int position) {

        LeaveApiModel leave = filterLeaveList.get(position);

        if (leave == null) {
            return;
        }

        EmployeeApiModel employee = leave.getEmployee();

        String empName;
        String empId;

        if (employee != null) {

            if (employee.getName() != null && !employee.getName().trim().isEmpty()) {

                empName = employee.getName();
            } else {
                empName = "Unknown Employee";
            }

            if (employee.getEmpID() != null) {
                empId = employee.getEmpID();
            } else {
                empId = "";
            }
        } else {
            empId = "";
            empName = "Unknown Employee";
        }

        String leaveType = leave.getLeaveType() != null ? leave.getLeaveType() : "N/A";
        String startDate = leave.getStartDate() != null ? leave.getStartDate() : "";

        String endDate = leave.getEndDate() != null ? leave.getEndDate() : "";

        String reason = leave.getReason() != null ? leave.getReason() : "N/A";
        String status = leave.getStatus() != null ? leave.getStatus() : "Pending";
        holder.txtAdminEmpName.setText(empName);
        holder.txtAdminEmpId.setText("Emp ID: " + empId);
        holder.txtAdminLeaveType.setText(leaveType);
        holder.txtAdminLeaveDate.setText(startDate + " - " + endDate);
        holder.txtAdminLeaveStatus.setText(status);

        if ("Pending".equalsIgnoreCase(status)) {
            holder.viewAdminStatusIndicator.setBackgroundColor(Color.parseColor("#FF9800"));
            holder.cardAdminStatusContainer.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
            holder.txtAdminLeaveStatus.setTextColor(Color.parseColor("#FF9800"));

        } else if ("Approved".equalsIgnoreCase(status)) {
            holder.viewAdminStatusIndicator.setBackgroundColor(Color.parseColor("#2E7D32"));
            holder.cardAdminStatusContainer.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.txtAdminLeaveStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("Rejected".equalsIgnoreCase(status)) {
            holder.viewAdminStatusIndicator.setBackgroundColor(Color.parseColor("#C62828"));
            holder.cardAdminStatusContainer.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            holder.txtAdminLeaveStatus.setTextColor(Color.parseColor("#C62828"));

        } else {
            holder.viewAdminStatusIndicator.setBackgroundColor(Color.parseColor("#757575"));
            holder.cardAdminStatusContainer.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.txtAdminLeaveStatus.setTextColor(Color.parseColor("#757575"));
        }

        holder.btnViewDetails.setOnClickListener(v -> {

            if ("Pending".equalsIgnoreCase(status)) {

                showApprovalOptionsDialog(
                        leave,
                        empName,
                        empId,
                        leaveType,
                        reason,
                        startDate,
                        endDate
                );

            } else {

                showLeaveDetailsDialog(
                        empName,
                        empId,
                        status,
                        leaveType,
                        reason,
                        startDate,
                        endDate
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return filterLeaveList.size();
    }

    private void showApprovalOptionsDialog(
            LeaveApiModel leave,
            String empName,
            String empId,
            String leaveType,
            String reason,
            String startDate,
            String endDate) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Review Leave Request");

        builder.setMessage(
                "Employee Name: " + empName
                        + "\nEmp ID: " + empId
                        + "\nLeave Type: " + leaveType
                        + "\nReason: " + reason
                        + "\nDate: " + startDate
                        + " - " + endDate
        );

        builder.setPositiveButton("APPROVE",
                (dialog, which) -> {
                    showFinalConfirmation(leave.getId(), "Approved"
                    );
                }
        );

        builder.setNegativeButton("REJECT", (dialog, which) -> {
                    showFinalConfirmation(leave.getId(), "Rejected");
                }
        );
        builder.setNeutralButton("Cancel", null);

        builder.create().show();
    }

    private void showFinalConfirmation(long leaveId, String action) {

        new AlertDialog.Builder(context)

                .setTitle("Confirm Action")
                .setMessage("Are you sure you want to " + action.toLowerCase() + " this leave request?")
                .setPositiveButton("Yes, Confirm", (dialog, which) -> {
                            if (listener != null) {
                                listener.onStatusChanged(leaveId, action
                                );
                            }
                        }
                )
                .setNegativeButton("No", null).show();
    }

    private void showLeaveDetailsDialog(
            String empName,
            String empId,
            String status,
            String leaveType,
            String reason,
            String startDate,
            String endDate) {

        new AlertDialog.Builder(context)

                .setTitle("Leave Request Details")
                .setMessage(
                        "Employee: " + empName
                                + "\nEmp ID: " + empId
                                + "\nStatus: " + status
                                + "\nType: " + leaveType
                                + "\nReason: " + reason
                                + "\nDate: " + startDate
                                + " - " + endDate
                )
                .setPositiveButton("OK", null).show();
    }
    public void filterList(String query, String statusFilter) {

        filterLeaveList.clear();
        String lowercaseQuery = query != null ? query.toLowerCase().trim() : "";

        String selectedStatus = statusFilter != null ? statusFilter : "All";

        for (LeaveApiModel leave : leaveList) {

            if (leave == null) {
                continue;
            }

            EmployeeApiModel employee = leave.getEmployee();

            String empName = "";
            String empId = "";

            if (employee != null) {

                if (employee.getName() != null) {
                    empName = employee.getName().toLowerCase();
                }
                if (employee.getEmpID() != null) {
                    empId = employee.getEmpID().toLowerCase();
                }
            }

            String leaveType = leave.getLeaveType() != null ? leave.getLeaveType().toLowerCase() : "";
            String status = leave.getStatus() != null ? leave.getStatus() : "";

            boolean matchesSearch = lowercaseQuery.isEmpty() || empName.contains(lowercaseQuery) || empId.contains(lowercaseQuery) || leaveType.contains(lowercaseQuery);

            boolean matchesStatus = selectedStatus.equalsIgnoreCase("All") || status.equalsIgnoreCase(selectedStatus);

            if (matchesSearch && matchesStatus) {
                filterLeaveList.add(leave);
            }
        }

        notifyDataSetChanged();
    }



    public void updateData(List<LeaveApiModel> newList) {

        leaveList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();

        filterLeaveList = new ArrayList<>(leaveList);

        notifyDataSetChanged();
    }



    public static class AdminViewHolder
            extends RecyclerView.ViewHolder {

        View viewAdminStatusIndicator;
        View adminDivider;

        ImageView imgEmpIcon;

        TextView txtAdminEmpName;
        TextView txtAdminEmpId;
        TextView txtAdminLeaveStatus;
        TextView txtAdminLeaveType;
        TextView txtAdminLeaveDate;

        MaterialButton btnViewDetails;

        MaterialCardView cardAdminStatusContainer;

        public AdminViewHolder(
                @NonNull View itemView) {

            super(itemView);

            viewAdminStatusIndicator = itemView.findViewById(R.id.viewAdminStatusIndicator);
            adminDivider = itemView.findViewById(R.id.adminDivider);
            imgEmpIcon = itemView.findViewById(R.id.imgEmpIcon);
            txtAdminEmpName = itemView.findViewById(R.id.txtAdminEmpName);
            txtAdminEmpId = itemView.findViewById(R.id.txtAdminEmpId);
            txtAdminLeaveStatus = itemView.findViewById(R.id.txtAdminLeaveStatus);
            txtAdminLeaveType = itemView.findViewById(R.id.txtAdminLeaveType);
            txtAdminLeaveDate = itemView.findViewById(R.id.txtAdminLeaveDate);
            cardAdminStatusContainer = itemView.findViewById(R.id.cardAdminStatusContainer);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}