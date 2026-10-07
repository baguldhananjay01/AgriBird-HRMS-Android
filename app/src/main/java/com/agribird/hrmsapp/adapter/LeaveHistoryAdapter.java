package com.agribird.hrmsapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class LeaveHistoryAdapter extends RecyclerView.Adapter<LeaveHistoryAdapter.ViewHolder> {

    private ArrayList<LeaveApiModel> leaveList;

    public LeaveHistoryAdapter(List<LeaveApiModel> leaveList) {
        this.leaveList = new ArrayList<>();

        if (leaveList != null) {
            this.leaveList.addAll(leaveList);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leave_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        LeaveApiModel leave = leaveList.get(position);

        // Leave Type
        holder.txtLeaveType.setText(leave.getLeaveType() != null ? leave.getLeaveType() : "Leave");

        // Dates
        String startDate = leave.getStartDate() != null ? leave.getStartDate() : "";
        String endDate = leave.getEndDate() != null ? leave.getEndDate() : "";
        holder.txtLeaveDates.setText(startDate + " - " + endDate);

        // Status
        String status = leave.getStatus();

        if (status != null && status.equalsIgnoreCase("Approved")) {

            holder.cardStatusContainer.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.txtLeaveStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#2E7D32"));
            holder.txtLeaveStatus.setText("Approved");

        } else if (status != null && status.equalsIgnoreCase("Rejected")) {
            holder.cardStatusContainer.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            holder.txtLeaveStatus.setTextColor(Color.parseColor("#C62828"));
            holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#C62828"));
            holder.txtLeaveStatus.setText("Rejected");

        } else {
            holder.cardStatusContainer.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
            holder.txtLeaveStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#FF9800"));
            holder.txtLeaveStatus.setText("Pending");
        }
    }

    @Override
    public int getItemCount() {
        return leaveList != null ? leaveList.size() : 0;
    }

    public void updateList(List<LeaveApiModel> newList) {

        leaveList.clear();
        if (newList != null) {
            leaveList.addAll(newList);
        }

        notifyDataSetChanged();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        View viewStatusIndicator;
        MaterialCardView cardStatusContainer;

        TextView txtLeaveType;
        TextView txtLeaveDates;
        TextView txtLeaveStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
            cardStatusContainer = itemView.findViewById(R.id.cardStatusContainer);
            txtLeaveType = itemView.findViewById(R.id.txtLeaveType);
            txtLeaveDates = itemView.findViewById(R.id.txtLeaveDates);
            txtLeaveStatus = itemView.findViewById(R.id.txtLeaveStatus);
        }
    }
}
//package com.agribird.hrmsapp.adapter;
//
//import android.graphics.Color;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.LeaveEntity;
//import com.agribird.hrmsapp.R;
//import com.google.android.material.card.MaterialCardView;
//
//import java.util.ArrayList;
//
//public class LeaveHistoryAdapter extends RecyclerView.Adapter<LeaveHistoryAdapter.ViewHolder> {
//
//    private ArrayList<LeaveEntity> leaveList;
//
//    public LeaveHistoryAdapter(ArrayList<LeaveEntity> leaveList) {
//        this.leaveList = leaveList;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leave_history_item, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        LeaveEntity leave = leaveList.get(position);
//
//        holder.txtLeaveType.setText(leave.getLeaveType());
//
//        String dateRange = leave.getStartDate() + " - " + leave.getEndDate();
//        holder.txtLeaveDates.setText(dateRange);
//
//        String status = leave.getStatus();
//
//        if (status != null && status.equalsIgnoreCase("Approved")) {
//            holder.cardStatusContainer.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
//            holder.txtLeaveStatus.setTextColor(Color.parseColor("#2E7D32"));
//            holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#2E7D32"));
//            holder.txtLeaveStatus.setText("Approved");
//        } else if (status != null && status.equalsIgnoreCase("Rejected")) {
//            holder.cardStatusContainer.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
//            holder.txtLeaveStatus.setTextColor(Color.parseColor("#C62828"));
//            holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#C62828"));
//            holder.txtLeaveStatus.setText("Rejected");
//        } else {
//            holder.cardStatusContainer.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
//            holder.txtLeaveStatus.setTextColor(Color.parseColor("#FF9800"));
//            holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#FF9800"));
//            holder.txtLeaveStatus.setText("Pending");
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return leaveList != null ? leaveList.size() : 0;
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//
//        View viewStatusIndicator;
//        MaterialCardView cardStatusContainer;
//        TextView txtLeaveType;
//        TextView txtLeaveDates;
//        TextView txtLeaveStatus;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
//            cardStatusContainer = itemView.findViewById(R.id.cardStatusContainer);
//            txtLeaveType = itemView.findViewById(R.id.txtLeaveType);
//            txtLeaveDates = itemView.findViewById(R.id.txtLeaveDates);
//            txtLeaveStatus = itemView.findViewById(R.id.txtLeaveStatus);
//        }
//    }
//}
