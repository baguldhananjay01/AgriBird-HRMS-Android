package com.agribird.hrmsapp.ui.Profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.EditProfileAdapter;
import com.agribird.hrmsapp.api.EmployeeApi;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private RecyclerView recyclerViewEditProfile;
    private ImageView btnBack;
    private View cardHeader;

    private String name = "";
    private String email = "";
    private String phone = "";
    private String emergencyPhone = "";
    private String address = "";

    private EmployeeApi employeeApi;

    public EditProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerViewEditProfile = view.findViewById(R.id.recyclerViewEditProfile);
        btnBack = view.findViewById(R.id.btnBack);
        cardHeader = view.findViewById(R.id.cardHeader);


        employeeApi = RetrofitClient.getEmployeeApi(requireContext());


        recyclerViewEditProfile.addOnScrollListener(
                new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(
                            @NonNull RecyclerView recyclerView,
                            int dx,
                            int dy) {

                        super.onScrolled(recyclerView, dx, dy);

                        if (cardHeader != null) {

                            if (recyclerView.canScrollVertically(-1)) {
                                cardHeader.setBackgroundColor(android.graphics.Color.parseColor("#E6E6FA"));
                            } else {
                                cardHeader.setBackgroundColor(android.graphics.Color.parseColor("#F4F7F5"));
                            }
                        }
                    }
                }
        );

        Bundle args = getArguments();

        if (args != null) {

            name = args.getString("Name", "");
            email = args.getString("Email", "");
            phone = args.getString("Phone", "");
            emergencyPhone = args.getString("EmergencyPhone", "");
            address = args.getString("Address", "");
        }

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        recyclerViewEditProfile.setLayoutManager(new LinearLayoutManager(requireContext()));


        EditProfileAdapter adapter =
                new EditProfileAdapter(
                        name,
                        email,
                        phone,
                        emergencyPhone,
                        address,
                        (
                                updatedName,
                                updatedEmail,
                                updatedPhone,
                                updatedEmergency,
                                updatedAddress
                        ) -> {

                            if (updatedName.isEmpty()) {
                                Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (updatedEmail.isEmpty()) {
                                Toast.makeText(requireContext(), "Email is required", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {

                                Toast.makeText(requireContext(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (!updatedPhone.isEmpty() && !updatedPhone.matches("[0-9]{10}")) {
                                Toast.makeText(requireContext(), "Enter valid 10-digit phone number", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (!updatedEmergency.isEmpty()
                                    && !updatedEmergency.matches("[0-9]{10}")) {

                                Toast.makeText(requireContext(), "Enter valid 10-digit emergency number", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (!isAdded()) {
                                return;
                            }
                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
                            String empId = sharedPreferences.getString("userEmpId", "");
                            String userId = sharedPreferences.getString("userId", "");

                            if (empId == null || empId.trim().isEmpty()) {

                                Toast.makeText(requireContext(), "Employee ID not found in session", Toast.LENGTH_LONG).show();

                                return;
                            }

                            EmployeeApiModel apiEmployee = new EmployeeApiModel();

                            apiEmployee.setEmpID(empId);
                            apiEmployee.setName(updatedName);
                            apiEmployee.setEmail(updatedEmail);
                            apiEmployee.setPhone(updatedPhone);
                            apiEmployee.setEmergencyPhone(updatedEmergency);
                            apiEmployee.setAddress(updatedAddress);

                            apiEmployee.setDepartment(sharedPreferences.getString("userDept", "N/A"));
                            apiEmployee.setRole(sharedPreferences.getString("userRole", "N/A"));
                            apiEmployee.setJoiningDate(sharedPreferences.getString("userJoiningDate", "N/A"));
                            apiEmployee.setStatus(sharedPreferences.getString("userStatus", "ACTIVE"));


                            Toast.makeText(requireContext(), "Updating profile...", Toast.LENGTH_SHORT).show();

                            employeeApi.updateEmployee(empId, apiEmployee).enqueue(
                                    new Callback<ApiResponse<EmployeeApiModel>>() {

                                        @Override
                                        public void onResponse(
                                                @NonNull Call<ApiResponse<EmployeeApiModel>> call,
                                                @NonNull Response<ApiResponse<EmployeeApiModel>> response) {

                                            if (!isAdded()) {
                                                return;
                                            }

                                            if (!response.isSuccessful()) {
                                                Toast.makeText(requireContext(), "Backend update failed. HTTP " + response.code(), Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            if (response.body() == null) {
                                                Toast.makeText(requireContext(), "Server returned empty response", Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            ApiResponse<EmployeeApiModel> apiResponse = response.body();

                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("userName", updatedName);
                                            editor.putString("userEmail", updatedEmail);
                                            editor.putString("userPhone", updatedPhone);
                                            editor.putString("userEmergencyPhone", updatedEmergency);
                                            editor.putString("userAddress", updatedAddress);

                                            editor.apply();

                                            Bundle result = new Bundle();

                                            result.putString("Name", updatedName);
                                            result.putString("Email", updatedEmail);
                                            result.putString("Phone", updatedPhone);
                                            result.putString("EmergencyPhone", updatedEmergency);
                                            result.putString("Address", updatedAddress);

                                            getParentFragmentManager().setFragmentResult("PROFILE_UPDATE_KEY", result);

                                            String message = apiResponse.getMessage();

                                            if (message == null || message.trim().isEmpty()) {
                                                message = "Profile Updated Successfully";
                                           }
                                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                            getParentFragmentManager().popBackStack();}

                                        @Override
                                        public void onFailure(
                                                @NonNull Call<ApiResponse<EmployeeApiModel>> call,
                                                @NonNull Throwable t) {
                                            if (!isAdded()) {
                                                return;
                                            }
                                            Toast.makeText(requireContext(), "API Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }, v -> getParentFragmentManager().popBackStack());
        recyclerViewEditProfile.setAdapter(adapter);
    }
}
//package com.agribird.hrmsapp.ui.Profile;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.agribird.hrmsapp.R;
//import com.agribird.hrmsapp.adapter.EditProfileAdapter;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//
//public class EditProfileFragment extends Fragment {
//
//    private RecyclerView recyclerViewEditProfile;
//    private ImageView btnBack;
//    private View cardHeader;
//    private String name = "";
//    private String email = "";
//    private String phone = "";
//    private String emergencyPhone = "";
//    private String address = "";
//
//    public EditProfileFragment() {
//        // Required empty public constructor
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        recyclerViewEditProfile = view.findViewById(R.id.recyclerViewEditProfile);
//        btnBack = view.findViewById(R.id.btnBack);
//        cardHeader = view.findViewById(R.id.cardHeader);
//
//        recyclerViewEditProfile.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                if (cardHeader != null) {
//                    if (recyclerView.canScrollVertically(-1)) {
//                        cardHeader.setBackgroundColor(android.graphics.Color.parseColor("#E6E6FA"));
//                    } else {
//                        cardHeader.setBackgroundColor(android.graphics.Color.parseColor("#F4F7F5"));
//                    }
//                }
//            }
//        });
//
//        Bundle args = getArguments();
//        if (args != null) {
//            name = args.getString("Name", "");
//            email = args.getString("Email", "");
//            phone = args.getString("Phone", "");
//            emergencyPhone = args.getString("EmergencyPhone", "");
//            address = args.getString("Address", "");
//        }
//
//        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
//
//        recyclerViewEditProfile.setLayoutManager(new LinearLayoutManager(requireContext()));
//
//        EditProfileAdapter adapter = new EditProfileAdapter(
//                name,
//                email,
//                phone,
//                emergencyPhone,
//                address,
//                (updatedName, updatedEmail, updatedPhone, updatedEmergency, updatedAddress) -> {
//
//                    if (updatedName.isEmpty()) {
//                        Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if (updatedEmail.isEmpty()) {
//                        Toast.makeText(requireContext(), "Email is required", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
//                        Toast.makeText(requireContext(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    if (isAdded() && getActivity() != null) {
//                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
//
//                        String empId = sharedPreferences.getString("userId", "");
//                        try {
//                            HRMSDatabase.getInstance(requireContext())
//                                    .employeeDao()
//                                    .updateEmployeeProfile(empId, updatedName, updatedEmail, updatedPhone, updatedEmergency, updatedAddress);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString("userName", updatedName);
//                        editor.putString("userEmail", updatedEmail);
//                        editor.putString("userPhone", updatedPhone);
//                        editor.putString("userEmergencyPhone", updatedEmergency);
//                        editor.putString("userAddress", updatedAddress);
//                        editor.apply();
//
//
//                        Bundle result = new Bundle();
//                        result.putString("Name", updatedName);
//                        result.putString("Email", updatedEmail);
//                        result.putString("Phone", updatedPhone);
//                        result.putString("EmergencyPhone", updatedEmergency);
//                        result.putString("Address", updatedAddress);
//                        getParentFragmentManager().setFragmentResult("PROFILE_UPDATE_KEY", result);
//
//                        Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
//                        getParentFragmentManager().popBackStack();
//                    }
//                },
//                v -> getParentFragmentManager().popBackStack() // Cancel Button Click
//        );
//
//        recyclerViewEditProfile.setAdapter(adapter);
//    }
//}

//============================================================================================================
//package com.agribird.hrmsapp.ui.Profile;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.agribird.hrmsapp.R;
//
//public class EditProfileFragment extends Fragment {
//
//    private EditText editName, editEmail;
//    private Button btnCancel, btnSave;
//    private String name;
//    private String email;
//
//    public EditProfileFragment() {
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        editName = view.findViewById(R.id.editName);
//        editEmail = view.findViewById(R.id.editEmail);
//        btnSave = view.findViewById(R.id.btnSave);
//        btnCancel = view.findViewById(R.id.btnCancel);
//
//        Bundle args = getArguments();
//        if (args != null) {
//            name = args.getString("Name", "");
//            email = args.getString("Email", "");
//        }
//
//        editName.setText(name);
//        editEmail.setText(email);
//
//        btnSave.setOnClickListener(v -> {
//            String updatedName = editName.getText().toString().trim();
//            String updatedEmail = editEmail.getText().toString().trim();
//
//            if (updatedName.isEmpty()) {
//                editName.setError("Required");
//                editName.requestFocus();
//                return;
//            }
//            if (updatedEmail.isEmpty()) {
//                editEmail.setError("Required");
//                editEmail.requestFocus();
//                return;
//            }
//            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
//                editEmail.setError("Invalid Email Address");
//                editEmail.requestFocus();
//                return;
//            }
//            if (isAdded() && getActivity() != null) {
//                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("HRMS_SESSION", Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString("userName", updatedName);
//                editor.putString("userEmail", updatedEmail);
//                editor.apply();
//
//                // Fragment Result पाठवा
//                Bundle result = new Bundle();
//                result.putString("Name", updatedName);
//                result.putString("Email", updatedEmail);
//                getParentFragmentManager().setFragmentResult("PROFILE_UPDATE_KEY", result);
//
//                Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
//
//                getParentFragmentManager().popBackStack();
//            }
//        });
//        btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
//    }
//}