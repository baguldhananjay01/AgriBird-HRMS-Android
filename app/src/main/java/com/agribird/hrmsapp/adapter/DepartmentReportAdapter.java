package com.agribird.hrmsapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.DepartmentReport;
import com.agribird.hrmsapp.R;

import java.util.ArrayList;

public class DepartmentReportAdapter extends RecyclerView.Adapter<DepartmentReportAdapter.ReportViewHolder> {

    private ArrayList<DepartmentReport> departmentReports;

    public DepartmentReportAdapter(ArrayList<DepartmentReport> departmentReports){
        this.departmentReports=departmentReports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_department_report,parent,false);

        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {

        DepartmentReport departmentReport= departmentReports.get(position);

        holder.txtDepartmentName.setText(String.valueOf(departmentReport.getDepartmentName()));
        holder.txtEmployeeCount.setText(String.valueOf(departmentReport.getEmployeeCount()));
    }

    @Override
    public int getItemCount() {
        return departmentReports.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder{

        TextView txtDepartmentName,txtEmployeeCount;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDepartmentName=itemView.findViewById(R.id.txtDepartmentName);
            txtEmployeeCount=itemView.findViewById(R.id.txtEmployeeCount);
        }
    }
}
