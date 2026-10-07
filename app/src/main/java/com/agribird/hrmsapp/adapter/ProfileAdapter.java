package com.agribird.hrmsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.LoginActivity;
import com.agribird.hrmsapp.Model.ProfileModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.ui.Profile.EditProfileFragment;
import com.google.android.material.card.MaterialCardView;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private final Context context;
    private final ProfileModel profileModel;
    private final SharedPreferences sharedPreferences;

    public ProfileAdapter(Context context, ProfileModel profileModel) {
        this.context = context;
        this.profileModel = profileModel;
        this.sharedPreferences = context.getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        if (profileModel == null) return;

        holder.txtUserName.setText(profileModel.getName() != null ? profileModel.getName().toUpperCase() : "");
        holder.txtPhone.setText(profileModel.getPhone() != null ? profileModel.getPhone() : "N/A");
        holder.txtWelcomeName.setText(profileModel.getName() != null ? profileModel.getName() : "User");
        holder.txtRole.setText(profileModel.getRole() != null ? profileModel.getRole() : "N/A");
        holder.txtEmail.setText(profileModel.getEmail() != null ? profileModel.getEmail() : "N/A");
        holder.txtDepartment.setText(profileModel.getDepartment() != null ? profileModel.getDepartment() : "N/A");
        holder.txtEmployeeId.setText(profileModel.getEmpId() != null ? profileModel.getEmpId() : "N/A");
        holder.txtJoiningDate.setText(profileModel.getJoiningDate() != null ? profileModel.getJoiningDate() : "N/A");

        String status = profileModel.getAccountStatus();
        if (status != null && !status.trim().isEmpty()) {
            holder.txtAccountStatus.setText(status.toUpperCase());
            if (status.equalsIgnoreCase("ACTIVE")) {
                holder.txtAccountStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else if (status.equalsIgnoreCase("INACTIVE") || status.equalsIgnoreCase("BLOCKED") || status.equalsIgnoreCase("SUSPENDED")) {
                holder.txtAccountStatus.setTextColor(Color.parseColor("#C62828"));
            } else if (status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("ON_LEAVE")) {
                holder.txtAccountStatus.setTextColor(Color.parseColor("#EF6C00"));
            } else {
                holder.txtAccountStatus.setTextColor(Color.parseColor("#424242"));
            }
        } else {
            holder.txtAccountStatus.setText("N/A");
            holder.txtAccountStatus.setTextColor(Color.parseColor("#757575"));
        }

        if (holder.btnChangePhoto != null) {
            holder.btnChangePhoto.setOnClickListener(v -> {
                if (context instanceof FragmentActivity) {
                    FragmentActivity activity = (FragmentActivity) context;
                    EditProfileFragment editProfileFragment = new EditProfileFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("Name", profileModel.getName());
                    bundle.putString("Email", profileModel.getEmail());
                    bundle.putString("Phone", profileModel.getPhone());

                    bundle.putString("EmergencyPhone", profileModel.getEmergencyPhone());
                    bundle.putString("Address", profileModel.getAddress());

                    editProfileFragment.setArguments(bundle);

                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, editProfileFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (holder.bgHRCall != null) {
            holder.bgHRCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:9876543210"));
                context.startActivity(intent);
            });
        }

        if (holder.bgEmailSupport != null) {
            holder.bgEmailSupport.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:hr@agribird.com"));
                context.startActivity(intent);
            });
        }

        if (holder.bgShippingAddress != null) {
            holder.bgShippingAddress.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=AgriBird Services Private Limited, Pune"));
                context.startActivity(intent);
            });
        }

        if (holder.cardLogout != null) {
            holder.cardLogout.setOnClickListener(v -> new AlertDialog.Builder(context)
                    .setTitle("🚪 Logout")
                    .setMessage("Are you sure you want to logout from AgriBird HRMS?")
                    .setCancelable(false)
                    .setPositiveButton("Yes, Logout", (dialog, which) -> {
                        if (sharedPreferences != null) {
                            sharedPreferences.edit().clear().apply();
                        }
                        Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);

                        if (context instanceof FragmentActivity) {
                            ((FragmentActivity) context).finishAffinity();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show());
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtPhone, txtWelcomeName, txtRole, txtEmail;
        TextView txtDepartment, txtEmployeeId, txtJoiningDate, txtAccountStatus;
        //ImageView btnEditPersonalDetails;
        MaterialCardView btnChangePhoto;
        View bgHRCall, bgEmailSupport, bgShippingAddress;
        MaterialCardView cardLogout;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtWelcomeName = itemView.findViewById(R.id.txtWelcomeName);
            txtRole = itemView.findViewById(R.id.txtRole);
            txtEmail = itemView.findViewById(R.id.txtemail);
            //btnEditPersonalDetails = itemView.findViewById(R.id.btnEditPersonalDetails);
            btnChangePhoto=itemView.findViewById(R.id.btnChangePhoto);

            txtDepartment = itemView.findViewById(R.id.txtDepartment);
            txtEmployeeId = itemView.findViewById(R.id.txtEmployeeId);
            txtJoiningDate = itemView.findViewById(R.id.txtJoiningDate);
            txtAccountStatus = itemView.findViewById(R.id.txtAccountStatus);

            bgHRCall = itemView.findViewById(R.id.bgHRCall);
            bgEmailSupport = itemView.findViewById(R.id.bgEmailSupport);
            bgShippingAddress = itemView.findViewById(R.id.bgShippingAddress);
            cardLogout = itemView.findViewById(R.id.cardLogout);
        }
    }
}

//package com.agribird.hrmsapp.adapter;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.fragment.app.FragmentActivity;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.LoginActivity;
//import com.agribird.hrmsapp.Model.ProfileModel;
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.ui.Profile.EditProfileFragment;
//import com.google.android.material.card.MaterialCardView;
//
//public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
//
//    private final Context context;
//    private final ProfileModel profileModel;
//    private final SharedPreferences sharedPreferences;
//
//    public ProfileAdapter(Context context, ProfileModel profileModel) {
//        this.context = context;
//        this.profileModel = profileModel;
//        // Shared Preferences Initialization Fix
//        this.sharedPreferences = context.getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
//    }
//
//    @NonNull
//    @Override
//    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
//        return new ProfileViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
//        if (profileModel == null) return;
//
//        // Dynamic Text Set Logic
//        holder.txtUserName.setText(profileModel.getName() != null ? profileModel.getName().toUpperCase() : "");
//        holder.txtPhone.setText(profileModel.getPhone() != null ? profileModel.getPhone() : "N/A");
//        holder.txtWelcomeName.setText(profileModel.getName() != null ? profileModel.getName() : "User");
//        holder.txtRole.setText(profileModel.getRole() != null ? profileModel.getRole() : "N/A");
//        holder.txtEmail.setText(profileModel.getEmail() != null ? profileModel.getEmail() : "N/A");
//        holder.txtDepartment.setText(profileModel.getDepartment() != null ? profileModel.getDepartment() : "N/A");
//        holder.txtEmployeeId.setText(profileModel.getEmpId() != null ? profileModel.getEmpId() : "N/A");
//        holder.txtJoiningDate.setText(profileModel.getJoiningDate() != null ? profileModel.getJoiningDate() : "N/A");
//
//        // =========================================================
//        // DYNAMIC ACCOUNT STATUS & COLOR LOGIC
//        // =========================================================
//        String status = profileModel.getAccountStatus();
//        if (status != null && !status.trim().isEmpty()) {
//            holder.txtAccountStatus.setText(status.toUpperCase());
//
//            if (status.equalsIgnoreCase("ACTIVE")) {
//                holder.txtAccountStatus.setTextColor(Color.parseColor("#2E7D32")); // Dark Green
//            } else if (status.equalsIgnoreCase("INACTIVE") || status.equalsIgnoreCase("BLOCKED") || status.equalsIgnoreCase("SUSPENDED")) {
//                holder.txtAccountStatus.setTextColor(Color.parseColor("#C62828")); // Red
//            } else if (status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("ON_LEAVE")) {
//                holder.txtAccountStatus.setTextColor(Color.parseColor("#EF6C00")); // Orange
//            } else {
//                holder.txtAccountStatus.setTextColor(Color.parseColor("#424242")); // Default Gray
//            }
//        } else {
//            holder.txtAccountStatus.setText("N/A");
//            holder.txtAccountStatus.setTextColor(Color.parseColor("#757575"));
//        }
//
//        // Edit Profile Click Listener
//        if (holder.btnEditPersonalDetails != null) {
//            holder.btnEditPersonalDetails.setOnClickListener(v -> {
//                if (context instanceof FragmentActivity) {
//                    FragmentActivity activity = (FragmentActivity) context;
//                    EditProfileFragment editProfileFragment = new EditProfileFragment();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("Name", profileModel.getName());
//                    bundle.putString("Email", profileModel.getEmail());
//                    bundle.putString("Phone", profileModel.getPhone());
//                    editProfileFragment.setArguments(bundle);
//
//                    activity.getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.fragmentContainer, editProfileFragment)
//                            .addToBackStack(null)
//                            .commit();
//                }
//            });
//        }
//
//        // HR Call Action
//        if (holder.bgHRCall != null) {
//            holder.bgHRCall.setOnClickListener(v -> {
//                Intent intent = new Intent(Intent.ACTION_DIAL);
//                intent.setData(Uri.parse("tel:9876543210"));
//                context.startActivity(intent);
//            });
//        }
//
//        // Email Support Action
//        if (holder.bgEmailSupport != null) {
//            holder.bgEmailSupport.setOnClickListener(v -> {
//                Intent intent = new Intent(Intent.ACTION_SENDTO);
//                intent.setData(Uri.parse("mailto:hr@agribird.com"));
//                context.startActivity(intent);
//            });
//        }
//
//        // Map Location Action
//        if (holder.bgShippingAddress != null) {
//            holder.bgShippingAddress.setOnClickListener(v -> {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("geo:0,0?q=AgriBird Services Private Limited, Pune"));
//                context.startActivity(intent);
//            });
//        }
//
//        // Logout Action (FIXED LOGOUT & BACKSTACK LOGIC)
//        if (holder.cardLogout != null) {
//            holder.cardLogout.setOnClickListener(v -> {
//                new AlertDialog.Builder(context)
//                        .setTitle("\uD83D\uDEAA Logout")
//                        .setMessage("Are you sure you want to logout from AgriBird HRMS?")
//                        .setCancelable(false)
//                        .setPositiveButton("Yes, Logout", (dialog, which) -> {
//
//                            // 1. CLEAR SPECIFIC USER SESSION
//                            if (sharedPreferences != null) {
//                                sharedPreferences.edit().clear().apply();
//                            }
//
//                            // 2. CLEAR DEFAULT SHARED PREFERENCES (जर अॅपमध्ये इतर कुठे वापरले असेल तर)
//                            context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE)
//                                    .edit().clear().apply();
//
//                            Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show();
//
//                            // 3. LAUNCH LOGIN ACTIVITY
//                            Intent intent = new Intent(context, LoginActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            context.startActivity(intent);
//
//                            // 4. KILL ALL RUNNING ACTIVITIES / FRAGMENTS
//                            if (context instanceof FragmentActivity) {
//                                FragmentActivity activity = (FragmentActivity) context;
//                                activity.finishAffinity(); // 👈 पूर्ण Activity Stack / Dashboard किल करेल
//                            }
//                        })
//                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                        .show();
//            });
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return 1;
//    }
//
//    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
//
//        TextView txtUserName, txtPhone, txtWelcomeName, txtRole, txtEmail;
//        TextView txtDepartment, txtEmployeeId, txtJoiningDate, txtAccountStatus;
//        ImageView btnEditPersonalDetails;
//        View bgHRCall, bgEmailSupport, bgShippingAddress;
//        MaterialCardView cardLogout;
//
//        public ProfileViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            txtUserName = itemView.findViewById(R.id.txtUserName);
//            txtPhone = itemView.findViewById(R.id.txtPhone);
//            txtWelcomeName = itemView.findViewById(R.id.txtWelcomeName);
//            txtRole = itemView.findViewById(R.id.txtRole);
//            txtEmail = itemView.findViewById(R.id.txtemail);
//            btnEditPersonalDetails = itemView.findViewById(R.id.btnEditPersonalDetails);
//
//            txtDepartment = itemView.findViewById(R.id.txtDepartment);
//            txtEmployeeId = itemView.findViewById(R.id.txtEmployeeId);
//            txtJoiningDate = itemView.findViewById(R.id.txtJoiningDate);
//            txtAccountStatus = itemView.findViewById(R.id.txtAccountStatus);
//
//            bgHRCall = itemView.findViewById(R.id.bgHRCall);
//            bgEmailSupport = itemView.findViewById(R.id.bgEmailSupport);
//            bgShippingAddress = itemView.findViewById(R.id.bgShippingAddress);
//            cardLogout = itemView.findViewById(R.id.cardLogout);
//        }
//    }
//}