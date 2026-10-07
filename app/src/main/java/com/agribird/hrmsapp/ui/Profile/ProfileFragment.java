package com.agribird.hrmsapp.ui.Profile;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.agribird.hrmsapp.Model.ProfileModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.ProfileAdapter;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private SharedPreferences sharedPreferences;
    private View cardHeader;
    private ProfileModel profileModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardHeader = view.findViewById(R.id.cardHeader);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (cardHeader != null) {
                    if (recyclerView.canScrollVertically(-1)) {
                        cardHeader.setBackgroundColor(android.graphics.Color.parseColor("#E6E6FA"));
                    } else {
                        cardHeader.setBackgroundColor(android.graphics.Color.parseColor("#F4F7F5"));
                    }
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);

        loadProfileData();

        getParentFragmentManager().setFragmentResultListener("PROFILE_UPDATE_KEY", getViewLifecycleOwner(), (requestKey, result) -> {
            String newName = result.getString("Name");
            String newEmail = result.getString("Email");
            String newPhone = result.getString("Phone");
            String newEmergencyPhone = result.getString("EmergencyPhone");
            String newAddress = result.getString("Address");

            if (profileModel != null) {
                profileModel.setName(newName);
                profileModel.setEmail(newEmail);
                profileModel.setPhone(newPhone);
                profileModel.setEmergencyPhone(newEmergencyPhone);
                profileModel.setAddress(newAddress);

                if (profileAdapter != null) {
                    profileAdapter.notifyDataSetChanged(); // UI रिफ्रेश होईल
                }
            }
        });
    }

    private void loadProfileData() {
        Bundle args = getArguments();

        String name = (args != null && args.containsKey("Name")) ? args.getString("Name") : sharedPreferences.getString("userName", "N/A");
        String role = (args != null && args.containsKey("Role")) ? args.getString("Role") : sharedPreferences.getString("userRole", "N/A");
        String email = (args != null && args.containsKey("Email")) ? args.getString("Email") : sharedPreferences.getString("userEmail", "N/A");
        String phone = (args != null && args.containsKey("Phone")) ? args.getString("Phone") : sharedPreferences.getString("userPhone", "N/A");

        String empId = (args != null && args.containsKey("EmpID")) ? args.getString("EmpID") : sharedPreferences.getString("userId", "N/A");
        String department = (args != null && args.containsKey("Department")) ? args.getString("Department") : sharedPreferences.getString("userDept", "N/A");
        String joiningDate = (args != null && args.containsKey("JoiningDate")) ? args.getString("JoiningDate") : sharedPreferences.getString("userJoiningDate", "N/A");
        String accountStatus = (args != null && args.containsKey("AccountStatus")) ? args.getString("AccountStatus") : sharedPreferences.getString("userAccountStatus", "ACTIVE");

        String emergencyPhone = (args != null && args.containsKey("EmergencyPhone")) ? args.getString("EmergencyPhone") : sharedPreferences.getString("userEmergencyPhone", "N/A");
        String address = (args != null && args.containsKey("Address")) ? args.getString("Address") : sharedPreferences.getString("userAddress", "N/A");

        profileModel = new ProfileModel(
                name,
                role,
                email,
                phone,
                empId,
                department,
                joiningDate,
                accountStatus,
                emergencyPhone,
                address
        );

        profileAdapter = new ProfileAdapter(requireContext(), profileModel);
        recyclerView.setAdapter(profileAdapter);
    }
}

//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.agribird.hrmsapp.R;
//import com.google.android.material.snackbar.Snackbar;
//
//public class ProfileFragment extends Fragment {
//
//    private TextView txtWelcome, txtRole, txtEmail;
//    private Button btnBac, btnCall, btnEmail, btnMap, btnEdit;
//
//    private String name;
//    private String role;
//    private String email;
//
//    public ProfileFragment() {
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_profile, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        txtWelcome = view.findViewById(R.id.txtWelcome);
//        txtRole = view.findViewById(R.id.txtRole);
//        txtEmail = view.findViewById(R.id.txtEmail);
//        btnBac = view.findViewById(R.id.btnBack);
//        btnCall = view.findViewById(R.id.btnCall);
//        btnEmail = view.findViewById(R.id.btnEmail);
//        btnMap = view.findViewById(R.id.btnMap);
//        btnEdit = view.findViewById(R.id.btnEdit);
//
//        Bundle args = getArguments();
//        if (args != null) {
//            name = args.getString("Name", "User");
//            role = args.getString("Role", "Employee");
//            email = args.getString("Email", "email@example.com");
//        } else {
//            name = "User";
//            role = "Employee";
//            email = "email@example.com";
//        }
//
//        txtWelcome.setText(name);
//        txtRole.setText(role);
//        txtEmail.setText(email);
//
//        Toast.makeText(requireContext(),
//                "Employee Verified!\nName: " + name + "\nEmail: " + email,
//                Toast.LENGTH_LONG).show();
//
//        getParentFragmentManager().setFragmentResultListener("PROFILE_UPDATE_KEY", getViewLifecycleOwner(), (requestKey, result) -> {
//            name = result.getString("Name", name);
//            email = result.getString("Email", email);
//
//            txtWelcome.setText(name);
//            txtEmail.setText(email);
//
//            if (getView() != null) {
//                Snackbar.make(getView(), "Profile Updated Successfully!", Snackbar.LENGTH_SHORT).show();
//            }
//        });
//
//        btnEdit.setOnClickListener(v -> {
//            EditProfileFragment editProfileFragment = new EditProfileFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString("Name", name);
//            bundle.putString("Email", email);
//            editProfileFragment.setArguments(bundle);
//
//            getParentFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragmentContainer, editProfileFragment) // तुमच्या Activity मधील FrameLayout ID वापरा
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        btnCall.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_DIAL);
//            intent.setData(Uri.parse("tel:9876543210"));
//            startActivity(intent);
//        });
//
//        btnEmail.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_SENDTO);
//            intent.setData(Uri.parse("mailto:hr@agribird.com"));
//            startActivity(intent);
//        });
//
//        btnMap.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse("geo:0,0?q=AgriBird Services Private Limited, City Vista, Kharadi, Pune"));
//            startActivity(intent);
//        });
//
//        btnBac.setOnClickListener(v -> getParentFragmentManager().popBackStack());
//    }
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
//        String currentName = sharedPreferences.getString("userName", name);
//        String currentEmail = sharedPreferences.getString("userEmail", email);
//
//        txtWelcome.setText(currentName);
//        txtEmail.setText(currentEmail);
//    }
//}