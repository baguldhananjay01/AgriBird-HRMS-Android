package com.agribird.hrmsapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.R;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private List<ActivityItem> activityList;

    public RecentActivityAdapter(List<ActivityItem> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityItem item = activityList.get(position);
        holder.txtActivityTitle.setText(item.getTitle());
        holder.txtActivitySubtitle.setText(item.getSubtitle());
        holder.txtActivityTime.setText(item.getTime());
        holder.imgActivityIcon.setImageResource(item.getIconResId());

        // Icon background color
        holder.cardActivityIcon.setCardBackgroundColor(item.getIconColor());
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    // RecentActivityAdapter.java - ViewHolder मध्ये
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgActivityIcon;
        TextView txtActivityTitle, txtActivitySubtitle, txtActivityTime;
        com.google.android.material.card.MaterialCardView cardActivityIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgActivityIcon = itemView.findViewById(R.id.imgActivityIcon);
            txtActivityTitle = itemView.findViewById(R.id.txtActivityTitle);
            txtActivitySubtitle = itemView.findViewById(R.id.txtActivitySubtitle);
            txtActivityTime = itemView.findViewById(R.id.txtActivityTime);
            cardActivityIcon = itemView.findViewById(R.id.cardActivityIcon);
        }
    }

    // Model class for activity
    public static class ActivityItem {
        private String title;
        private String subtitle;
        private String time;
        private int iconResId;
        private int iconColor;

        public ActivityItem(String title, String subtitle, String time, int iconResId, int iconColor) {
            this.title = title;
            this.subtitle = subtitle;
            this.time = time;
            this.iconResId = iconResId;
            this.iconColor = iconColor;
        }

        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public String getTime() { return time; }
        public int getIconResId() { return iconResId; }
        public int getIconColor() { return iconColor; }
    }
}