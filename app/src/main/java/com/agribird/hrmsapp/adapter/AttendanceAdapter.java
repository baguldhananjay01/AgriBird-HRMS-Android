package com.agribird.hrmsapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.R;
import com.google.android.material.button.MaterialButton;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private String employeeName = "Employee";
    private String todayStatus = "Checking Security...";
    private String currentTime = "--:--";
    private String currentDate = "--";
    private String totalLoggedHours = "00h 00m";

    private String onlineTime = "00h 00m";
    private String breakTime = "0m";
    private String yesterdayStatus = "N/A";

    private boolean checkInEnabled = false;
    private boolean checkOutEnabled = false;

    private OnAttendanceActionListener listener;

    public interface OnAttendanceActionListener {
        void onCheckInClicked();

        void onCheckOutClicked();
    }

    public AttendanceAdapter(OnAttendanceActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_content, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        holder.txtEmpName.setText(
                employeeName != null ? employeeName : "Employee"
        );

        holder.txtTodayStatus.setText(
                todayStatus != null ? todayStatus : "N/A"
        );

        holder.txtTime.setText(
                currentTime != null ? currentTime : "--:--"
        );

        holder.txtDate.setText(
                currentDate != null ? currentDate : "--"
        );

        holder.txtTotalLoggedHours.setText(
                totalLoggedHours != null ? totalLoggedHours : "00h 00m"
        );

        holder.txtOnlineTime.setText(
                onlineTime != null ? onlineTime : "00h 00m"
        );

        holder.txtBreakTime.setText(
                breakTime != null ? breakTime : "0m"
        );

        holder.txtYesterdayStatus.setText(
                yesterdayStatus != null ? yesterdayStatus : "N/A"
        );

        updateStatusColor(holder);

        // ---------------- CHECK IN ----------------

        holder.btnCheckIn.setEnabled(checkInEnabled);

        holder.btnCheckIn.setOnClickListener(v -> {

            if (!checkInEnabled) {
                return;
            }

            // Immediately prevent double click
            holder.btnCheckIn.setEnabled(false);

            if (listener != null) {
                listener.onCheckInClicked();
            }
        });

        // ---------------- CHECK OUT ----------------

        holder.btnCheckOut.setEnabled(checkOutEnabled);

        holder.btnCheckOut.setOnClickListener(v -> {

            if (!checkOutEnabled) {
                return;
            }

            // Immediately prevent double click
            holder.btnCheckOut.setEnabled(false);

            if (listener != null) {
                listener.onCheckOutClicked();
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
        notifyDataSetChanged();
    }

    public void setTodayStatus(String status) {
        this.todayStatus = status;
        notifyDataSetChanged();
    }

    public void setCurrentTime(String time) {
        this.currentTime = time;
        notifyDataSetChanged();
    }

    public void setCurrentDate(String date) {
        this.currentDate = date;
        notifyDataSetChanged();
    }

    public void setTotalLoggedHours(String hours) {
        this.totalLoggedHours = hours;
        notifyDataSetChanged();
    }

    public void setOnlineTime(String time) {
        this.onlineTime = time;
        notifyDataSetChanged();
    }

    public void setBreakTime(String time) {
        this.breakTime = time;
        notifyDataSetChanged();
    }

    public void setYesterdayStatus(String status) {
        this.yesterdayStatus = status;
        notifyDataSetChanged();
    }

    public void setCheckInEnabled(boolean enabled) {
        this.checkInEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setCheckOutEnabled(boolean enabled) {
        this.checkOutEnabled = enabled;
        notifyDataSetChanged();
    }

    private void updateStatusColor(ViewHolder holder) {

        String status = todayStatus;

        if (status != null) {

            if (status.contains("Late")) {

                holder.txtTodayStatus.setTextColor(
                        Color.parseColor("#E65100")
                );

            } else if (status.contains("Completed")) {

                holder.txtTodayStatus.setTextColor(
                        Color.parseColor("#1976D2")
                );

            } else if (status.contains("Present")) {

                holder.txtTodayStatus.setTextColor(
                        Color.parseColor("#2E7D32")
                );

            } else if (
                    status.contains("Fake GPS") ||
                            status.contains("Authorization Failed") ||
                            status.contains("Out of Office")
            ) {

                holder.txtTodayStatus.setTextColor(Color.RED);

            } else {

                holder.txtTodayStatus.setTextColor(Color.GRAY);
            }
        }

        if (yesterdayStatus == null) {
            return;
        }

        if (
                yesterdayStatus.equalsIgnoreCase("Present") ||
                        yesterdayStatus.equalsIgnoreCase("Completed")
        ) {

            holder.txtYesterdayStatus.setTextColor(
                    Color.parseColor("#2E7D32")
            );

        } else if (
                yesterdayStatus.equalsIgnoreCase("Late")
        ) {

            holder.txtYesterdayStatus.setTextColor(
                    Color.parseColor("#EF6C00")
            );

        } else if (
                yesterdayStatus.equalsIgnoreCase("Absent") ||
                        yesterdayStatus.equalsIgnoreCase("N/A")
        ) {

            holder.txtYesterdayStatus.setTextColor(
                    Color.parseColor("#C62828")
            );

        } else {

            holder.txtYesterdayStatus.setTextColor(Color.GRAY);
        }
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtEmpName;
        TextView txtTodayStatus;
        TextView txtTime;
        TextView txtDate;
        TextView txtTotalLoggedHours;
        TextView txtOnlineTime;
        TextView txtBreakTime;
        TextView txtYesterdayStatus;

        MaterialButton btnCheckIn;
        MaterialButton btnCheckOut;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            txtEmpName =
                    itemView.findViewById(R.id.txtEmpName);

            txtTodayStatus =
                    itemView.findViewById(R.id.txtTodayStatus);

            txtTime =
                    itemView.findViewById(R.id.txtTime);

            txtDate =
                    itemView.findViewById(R.id.txtDate);

            txtTotalLoggedHours =
                    itemView.findViewById(R.id.txtTotalLoggedHours);

            txtOnlineTime =
                    itemView.findViewById(R.id.txtOnlineTime);

            txtBreakTime =
                    itemView.findViewById(R.id.txtBreakTime);

            txtYesterdayStatus =
                    itemView.findViewById(R.id.txtYesterdayStatus);

            btnCheckIn =
                    itemView.findViewById(R.id.btnCheckIn);

            btnCheckOut =
                    itemView.findViewById(R.id.btnCheckOut);
        }
    }
}
//package com.agribird.hrmsapp.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.Model.Attendance;
//import com.agribird.hrmsapp.R;
//
//import java.util.List;
//
//public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
//
//    private List<Attendance> attendanceList;
//
//    public AttendanceAdapter(List<Attendance> attendanceList) {
//        this.attendanceList = attendanceList;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_attendance_content, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Attendance item = attendanceList.get(position);
//
//        holder.txtEmpName.setText(item.getEmployeeName() != null ? item.getEmployeeName() : "Unknown");
//        holder.txtTime.setText(item.getCheckInTime() != null ? item.getCheckInTime() : "--:--");
//
//        String status = item.getStatus();
//        if (status != null) {
//            holder.txtTodayStatus.setText(status);
//            // Set color based on status
//            int color;
//            if (status.equalsIgnoreCase("Present") || status.equalsIgnoreCase("Completed")) {
//                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.green_primary);
//            } else if (status.equalsIgnoreCase("Late")) {
//                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.orange_leave);
//            } else if (status.equalsIgnoreCase("Absent")) {
//                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.red_checkout);
//            } else {
//                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_text);
//            }
//            holder.txtTodayStatus.setTextColor(color);
//        } else {
//            holder.txtTodayStatus.setText("N/A");
//            holder.txtTodayStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
//                    R.color.secondary_text));
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return attendanceList != null ? attendanceList.size() : 0;
//    }
//
//    // Call this method to update the data
//    public void setAttendanceList(List<Attendance> newList) {
//        this.attendanceList = newList;
//        notifyDataSetChanged();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView txtEmpName, txtTime, txtTodayStatus;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            txtEmpName = itemView.findViewById(R.id.txtEmpName);
//            txtTime = itemView.findViewById(R.id.txtTime);
//            txtTodayStatus = itemView.findViewById(R.id.txtTodayStatus);
//        }
//    }
//}
