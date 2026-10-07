package com.agribird.hrmsapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.Employee;
import com.agribird.hrmsapp.R;

import java.util.ArrayList;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private ArrayList<Employee> employeeList;
    private String loggedInRole;
    private OnEmployeeClickListener listener;

    public EmployeeAdapter(ArrayList<Employee> employeeList, String loggedInRole, OnEmployeeClickListener listener) {
        this.employeeList = employeeList;
        this.loggedInRole = loggedInRole;
        this.listener=listener;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);

        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {

        Employee employee = employeeList.get(position);

        holder.tvEmployeeName.setText(employee.getName());
        holder.tvEmployeeEmail.setText(employee.getEmail());
        holder.tvEmployeeRole.setText(employee.getRole());

        holder.itemView.setOnClickListener(v->{
            listener.onEmployeeClick(employee,position);
        });

        if (employee.getRole().equalsIgnoreCase("Super Admin")) {
            holder.tvEmployeeName.setTextColor(Color.RED);
            holder.tvEmployeeRole.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.tvEmployeeName.setTextColor(Color.BLACK);
            holder.tvEmployeeRole.setTextColor(Color.parseColor("#2E7D32"));
        }
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEmployee;
        TextView tvEmployeeName, tvEmployeeEmail, tvEmployeeRole;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);

            imgEmployee = itemView.findViewById(R.id.imgEmployee);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeEmail = itemView.findViewById(R.id.tvEmployeeEmail);
            tvEmployeeRole = itemView.findViewById(R.id.tvEmployeeRole);
        }
    }

    public interface OnEmployeeClickListener {
        void onEmployeeClick(Employee employee,int position);
    }
    public void filterList(ArrayList<Employee> filteredList){
        employeeList = filteredList;
        notifyDataSetChanged();
    }

}
