package com.agribird.hrmsapp.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.AttendanceApiModel;
import com.agribird.hrmsapp.R;

import java.util.ArrayList;
import java.util.List;

public class AttendanceHistoryAdapter
        extends RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder> {

    private List<AttendanceApiModel> attendanceList;
    private final List<AttendanceApiModel> attendanceListFull;

    public AttendanceHistoryAdapter(List<AttendanceApiModel> attendanceList) {
        this.attendanceList = new ArrayList<>(attendanceList);
        this.attendanceListFull = new ArrayList<>(attendanceList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_log, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AttendanceApiModel attendance = attendanceList.get(position);

        holder.txtHistoryDate.setText(attendance.getDate() != null ? attendance.getDate() : "--/--/----");
        holder.txtHistoryIn.setText("In: " + (attendance.getCheckInTime() != null ? attendance.getCheckInTime() : "--:--"));
        holder.txtHistoryOut.setText("Out: " + (attendance.getCheckOutTime() != null ? attendance.getCheckOutTime() : "--:--"));

        String status = attendance.getStatus() != null ? attendance.getStatus() : "Present";

        holder.txtHistoryStatus.setText(status.toUpperCase());

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(16f);

        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Present")) {

            holder.txtHistoryStatus.setTextColor(Color.parseColor("#15803D"));

            badgeBg.setColor(Color.parseColor("#DCFCE7"));

        } else if (status.equalsIgnoreCase("Late")) {

            holder.txtHistoryStatus.setTextColor(Color.parseColor("#A16207"));
            badgeBg.setColor(Color.parseColor("#FEF9C3"));

        } else if (status.equalsIgnoreCase("Absent")) {
            holder.txtHistoryStatus.setTextColor(Color.parseColor("#B91C1C"));
            badgeBg.setColor(Color.parseColor("#FEE2E2"));

        } else {
            holder.txtHistoryStatus.setTextColor(Color.parseColor("#1D4ED8"));
            badgeBg.setColor(Color.parseColor("#DBEAFE"));
        }

        holder.txtHistoryStatus.setBackground(badgeBg);
    }

    @Override
    public int getItemCount() {
        return attendanceList != null ? attendanceList.size() : 0;
    }

    public void filterList(List<AttendanceApiModel> filteredList) {
        this.attendanceList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }

    public List<AttendanceApiModel> getAttendanceListFull() {
        return attendanceListFull;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtHistoryDate;
        TextView txtHistoryIn;
        TextView txtHistoryOut;
        TextView txtHistoryStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtHistoryDate = itemView.findViewById(R.id.txtHistoryDate);
            txtHistoryIn = itemView.findViewById(R.id.txtHistoryIn);
            txtHistoryOut = itemView.findViewById(R.id.txtHistoryOut);
            txtHistoryStatus = itemView.findViewById(R.id.txtHistoryStatus);
        }
    }
}
//package com.agribird.hrmsapp.adapter;
//
//import android.graphics.Color;
//import android.graphics.drawable.GradientDrawable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.Attendance;
//import com.agribird.hrmsapp.R;
//
//import java.util.List;
//
//public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder> {
//
//    private final List<Attendance> attendanceList;
//
//    public AttendanceHistoryAdapter(List<Attendance> attendanceList) {
//        this.attendanceList = attendanceList;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_log, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Attendance attendance = attendanceList.get(position);
//
//        holder.txtHistoryDate.setText(attendance.getDate());
//        holder.txtHistoryIn.setText("In: " + (attendance.getCheckInTime() != null ? attendance.getCheckInTime() : "--:--"));
//        holder.txtHistoryOut.setText("Out: " + (attendance.getCheckOutTime() != null ? attendance.getCheckOutTime() : "--:--"));
//
//        String status = attendance.getStatus() != null ? attendance.getStatus() : "Present";
//        holder.txtHistoryStatus.setText(status);
//
//        GradientDrawable badgeBg = new GradientDrawable();
//        badgeBg.setCornerRadius(20f);
//
//        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Present")) {
//            holder.txtHistoryStatus.setTextColor(Color.parseColor("#2E7D32"));
//            badgeBg.setColor(Color.parseColor("#E8F5E9"));
//        } else if (status.equalsIgnoreCase("Late")) {
//            holder.txtHistoryStatus.setTextColor(Color.parseColor("#E65100"));
//            badgeBg.setColor(Color.parseColor("#FFF3E0"));
//        } else if (status.equalsIgnoreCase("Absent")) {
//            holder.txtHistoryStatus.setTextColor(Color.parseColor("#C62828"));
//            badgeBg.setColor(Color.parseColor("#FFEBEE"));
//        } else {
//            holder.txtHistoryStatus.setTextColor(Color.parseColor("#1976D2"));
//            badgeBg.setColor(Color.parseColor("#E3F2FD"));
//        }
//        holder.txtHistoryStatus.setBackground(badgeBg);
//    }
//
//    @Override
//    public int getItemCount() {
//        return attendanceList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView txtHistoryDate, txtHistoryIn, txtHistoryOut, txtHistoryStatus;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            txtHistoryDate = itemView.findViewById(R.id.txtHistoryDate);
//            txtHistoryIn = itemView.findViewById(R.id.txtHistoryIn);
//            txtHistoryOut = itemView.findViewById(R.id.txtHistoryOut);
//            txtHistoryStatus = itemView.findViewById(R.id.txtHistoryStatus);
//        }
//    }
//}