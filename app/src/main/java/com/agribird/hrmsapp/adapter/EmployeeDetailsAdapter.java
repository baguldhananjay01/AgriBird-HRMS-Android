package com.agribird.hrmsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.Employee;
import com.agribird.hrmsapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class EmployeeDetailsAdapter extends RecyclerView.Adapter<EmployeeDetailsAdapter.EmployeeViewHolder>{

    private Context context;
    private List<Employee> employeeList;
    private OnEmployeeActionListener actionListener;

    // Interface for button actions
    public interface OnEmployeeActionListener {
        void onCallClicked(Employee employee);
        void onEmailClicked(Employee employee);
        void onEditClicked(Employee employee);
        void onDeleteClicked(Employee employee);
        void onItemClick(Employee employee); // card click (optional)
    }

    public EmployeeDetailsAdapter (Context context, List<Employee> employeeList, OnEmployeeActionListener listener) {
        this.context = context;
        this.employeeList = employeeList != null ? employeeList : new ArrayList<>();
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_employee_details, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee employee = employeeList.get(position);

        // Set data to all views
        holder.eName.setText(employee.getName());
        holder.eRole.setText(employee.getRole() != null ? employee.getRole() : "N/A");
        holder.eEmail.setText(employee.getEmail() != null ? employee.getEmail() : "N/A");
        holder.ePhone.setText(employee.getPhone() != null ? employee.getPhone() : "N/A");
        holder.eEmergencyPhone.setText(employee.getEmergencyPhone() != null ? employee.getEmergencyPhone() : "N/A");
        holder.eBloodGroup.setText(employee.getBloodGroup() != null ? employee.getBloodGroup() : "N/A");
        holder.eAddress.setText(employee.getAddress() != null ? employee.getAddress() : "N/A");
        holder.eDepartment.setText(employee.getDepartment() != null ? employee.getDepartment() : "N/A");
        holder.eID.setText(employee.getEmpId() != null ? employee.getEmpId() : "N/A");
        holder.eJoiningDate.setText(employee.getJoiningDate() != null ? employee.getJoiningDate() : "N/A");

        // Button actions
        holder.btnCall.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCallClicked(employee);
            } else {
                // Default action: dial
                String phone = employee.getPhone();
                if (phone != null && !phone.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.btnEmail.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEmailClicked(employee);
            } else {
                String email = employee.getEmail();
                if (email != null && !email.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Email address not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditClicked(employee);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClicked(employee);
            }
        });

        // Whole card click (optional)
        holder.cardView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(employee);
            }
        });

        // Note: btnBack is not used in the adapter; it's hidden or you can remove it from the layout.
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    // Update data method
    public void updateData(List<Employee> newList) {
        this.employeeList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView eName, eRole, eEmail, ePhone, eEmergencyPhone, eBloodGroup, eAddress, eDepartment, eID, eJoiningDate;
        Button btnCall, btnEmail, btnEdit, btnDelete;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardProfileContainer);
            eName = itemView.findViewById(R.id.eName);
            eRole = itemView.findViewById(R.id.eRole);
            eEmail = itemView.findViewById(R.id.eEmail);
            ePhone = itemView.findViewById(R.id.ePhone);
            eEmergencyPhone = itemView.findViewById(R.id.eEmergencyPhone);
            eBloodGroup = itemView.findViewById(R.id.eBloodGroup);
            eAddress = itemView.findViewById(R.id.eAddress);
            eDepartment = itemView.findViewById(R.id.eDepartment);
            eID = itemView.findViewById(R.id.eID);
            eJoiningDate = itemView.findViewById(R.id.eJoiningDate);
            btnCall = itemView.findViewById(R.id.btnCALL);
            btnEmail = itemView.findViewById(R.id.btnEmail);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            // btnBack = itemView.findViewById(R.id.btnBack); // not used
        }
    }
}

