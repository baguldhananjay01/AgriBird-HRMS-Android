package com.agribird.hrmsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.Model.LoginResponseModel;
import com.agribird.hrmsapp.api.EmployeeApi;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tlEmail;
    private TextInputLayout tlPassword;

    private TextInputEditText hName;
    private TextInputEditText hPass;

    private Button btnLog;
    private CheckBox cbRemember;

    private SharedPreferences sharedPreferences;
    private EmployeeApi employeeApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        employeeApi = RetrofitClient.getEmployeeApi(this);

        tlEmail = findViewById(R.id.tlEmail);
        tlPassword = findViewById(R.id.tlPassword);

        hName = findViewById(R.id.hName);
        hPass = findViewById(R.id.hPass);

        btnLog = findViewById(R.id.btnLog);
        cbRemember = findViewById(R.id.cbRemember);

        sharedPreferences = getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);

        checkExistingSession();

        btnLog.setOnClickListener(v -> {

            if (!btnLog.isEnabled()) {
                return;
            }

            clearErrors();

            String email = getEmail();
            String password = getPassword();

            if (email.isEmpty()) {

                tlEmail.setError("Email cannot be empty");
                hName.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                tlEmail.setError("Enter a valid email address");
                hName.requestFocus();
                return;
            }

            if (password.isEmpty()) {

                tlPassword.setError("Password cannot be empty");
                hPass.requestFocus();
                return;
            }

            if (password.length() < 6) {
                tlPassword.setError("Password must be at least 6 characters");
                hPass.requestFocus();

                return;
            }



            loginEmployee(email, password);
        });
    }


    private void checkExistingSession() {

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        String jwtToken = sharedPreferences.getString("jwtToken", "");

        if (isLoggedIn && rememberMe && jwtToken != null && !jwtToken.trim().isEmpty()) {

            String userName = sharedPreferences.getString("userName", "");
            String userRole = sharedPreferences.getString("userRole", "");
            String userEmail = sharedPreferences.getString("userEmail", "");

            openMainActivity(userName, userRole, userEmail);
        }
    }

    private String getEmail() {

        if (hName.getText() == null) {
            return "";
        }

        return hName.getText().toString().trim();
    }

    private String getPassword() {

        if (hPass.getText() == null) {
            return "";
        }
        return hPass.getText().toString();
    }

    private void clearErrors() {

        tlEmail.setError(null);
        tlPassword.setError(null);
    }

    private void loginSuperAdmin() {

        String email =
                hName.getText().toString().trim();

        String password =
                hPass.getText().toString().trim();

        if (email.isEmpty()) {

            hName.setError("Enter email");
            hName.requestFocus();
            return;
        }

        if (password.isEmpty()) {

            hPass.setError("Enter password");
            hPass.requestFocus();
            return;
        }

        EmployeeApiModel employee =
                new EmployeeApiModel();

        employee.setEmail(email);
        employee.setPassword(password);

        employeeApi.loginEmployee(employee)
                .enqueue(
                        new Callback<ApiResponse<LoginResponseModel>>() {

                            @Override
                            public void onResponse(
                                    Call<ApiResponse<LoginResponseModel>> call,
                                    Response<ApiResponse<LoginResponseModel>> response) {

                                if (!response.isSuccessful()
                                        || response.body() == null
                                        || response.body().getData() == null) {

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Invalid Super Admin credentials",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                LoginResponseModel loginResponse =
                                        response.body().getData();

                                if (loginResponse.getToken() == null
                                        || loginResponse.getToken()
                                        .trim()
                                        .isEmpty()) {

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "JWT token not received",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                if (!"SUPER_ADMIN".equalsIgnoreCase(
                                        loginResponse.getRole())) {

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "You are not a Super Admin",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                saveLoginSession(loginResponse);
                            }

                            @Override
                            public void onFailure(
                                    Call<ApiResponse<LoginResponseModel>> call,
                                    Throwable t) {

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Server Error: " + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
    }

    private void loginEmployee(String email, String password) {

        btnLog.setEnabled(false);

        EmployeeApiModel loginRequest = new EmployeeApiModel();

        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        employeeApi.loginEmployee(loginRequest).enqueue(new Callback<ApiResponse<LoginResponseModel>>() {

                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<LoginResponseModel>> call,
                            @NonNull Response<ApiResponse<LoginResponseModel>> response) {

                        btnLog.setEnabled(true);

                        if (!response.isSuccessful()) {
                            handleHttpError(response);
                            return;
                        }

                        if (response.body() == null) {
                            Toast.makeText(LoginActivity.this, "Server returned empty response.", Toast.LENGTH_LONG).show();

                            return;
                        }

                        ApiResponse<LoginResponseModel> apiResponse = response.body();

                        if (apiResponse.getData() == null) {
                            String message = apiResponse.getMessage();

                            if (message == null || message.trim().isEmpty()) {
                                message = "Invalid Email or Password!";
                            }

                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

                            return;
                        }

                        LoginResponseModel loginResponse = apiResponse.getData();

                        if (loginResponse.getToken() == null || loginResponse.getToken().trim().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "JWT Token not received from server.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        saveLoginSession(loginResponse);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<LoginResponseModel>> call, @NonNull Throwable throwable) {

                        btnLog.setEnabled(true);
                        String errorMessage = throwable.getMessage();

                        if (errorMessage == null || errorMessage.trim().isEmpty()) {
                            errorMessage = "Unable to connect to server.";
                        }
                        Toast.makeText(LoginActivity.this, "API Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void handleHttpError(Response<ApiResponse<LoginResponseModel>> response) {

        int code = response.code();

        if (code == 401) {
            Toast.makeText(LoginActivity.this, "Invalid Email or Password!", Toast.LENGTH_LONG).show();

        } else if (code == 403) {
            Toast.makeText(LoginActivity.this, "Account access denied.", Toast.LENGTH_LONG).show();

        } else if (code == 404) {
            Toast.makeText(LoginActivity.this, "Login API not found.", Toast.LENGTH_LONG).show();
        } else if (code >= 500) {
            Toast.makeText(LoginActivity.this, "Server error. Please try again later.", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(LoginActivity.this, "Login failed. HTTP " + code, Toast.LENGTH_LONG).show();
        }
    }

    private void saveLoginSession(LoginResponseModel loginResponse) {

        boolean remember = cbRemember.isChecked();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", true);
        editor.putString("jwtToken", loginResponse.getToken());
        editor.putString("userId", String.valueOf(loginResponse.getEmployeeId()));
        editor.putString("userEmpId", loginResponse.getEmpID() != null ? loginResponse.getEmpID() : "N/A");
        editor.putString("userName", loginResponse.getName() != null ? loginResponse.getName() : "N/A");
        editor.putString("userEmail", loginResponse.getEmail() != null ? loginResponse.getEmail() : "N/A");
        editor.putString("userRole", loginResponse.getRole() != null ? loginResponse.getRole() : "N/A");

        editor.putBoolean("rememberMe", remember);

        editor.apply();
        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();

        openMainActivity(loginResponse.getName(), loginResponse.getRole(), loginResponse.getEmail());
    }

    private void openMainActivity(String name, String role, String email) {

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

        intent.putExtra("Name", name);
        intent.putExtra("Role", role);
        intent.putExtra("Email", email);

        startActivity(intent);

        finish();
    }
}

//package com.agribird.hrmsapp;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.agribird.hrmsapp.Model.EmployeeApiModel;
//import com.agribird.hrmsapp.api.EmployeeApi;
//import com.agribird.hrmsapp.dto.ApiResponse;
//import com.agribird.hrmsapp.network.RetrofitClient;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private TextInputLayout tlEmail, tlPassword;
//    private TextInputEditText hName, hPass;
//    private Button btnLog;
//    private CheckBox cbRemember;
//
//    private SharedPreferences sharedPreferences;
//    private EmployeeApi employeeApi;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_login);
//
//        employeeApi = RetrofitClient.getEmployeeApi();
//
//        tlEmail = findViewById(R.id.tlEmail);
//        tlPassword = findViewById(R.id.tlPassword);
//
//        hName = findViewById(R.id.hName);
//        hPass = findViewById(R.id.hPass);
//
//        btnLog = findViewById(R.id.btnLog);
//        cbRemember = findViewById(R.id.cbRemember);
//
//
//        sharedPreferences = getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);
//        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
//        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
//
//        if (isLoggedIn && rememberMe) {
//            String userName = sharedPreferences.getString("userName", "");
//            String userRole = sharedPreferences.getString("userRole", "");
//            String userEmail = sharedPreferences.getString("userEmail", "");
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//
//            intent.putExtra("Name", userName);
//            intent.putExtra("Role", userRole);
//            intent.putExtra("Email", userEmail);
//
//            startActivity(intent);
//            finish();
//
//            return;
//        }
//
//        btnLog.setOnClickListener(v -> {
//            tlEmail.setError(null);
//            tlPassword.setError(null);
//
//            String inputEmail = hName.getText() != null ? hName.getText().toString().trim() : "";
//            String inputPassword = hPass.getText() != null ? hPass.getText().toString().trim() : "";
//
//            if (inputEmail.isEmpty()) {
//                tlEmail.setError("Email cannot be empty");
//                hName.requestFocus();
//                return;
//            }
//
//            if (inputPassword.isEmpty()) {
//                tlPassword.setError("Password cannot be empty");
//                hPass.requestFocus();
//                return;
//            }
//
//            if (inputPassword.length() < 6) {
//                tlPassword.setError("Password must be at least 6 characters");
//                hPass.requestFocus();
//                return;
//            }
//
//            if (inputEmail.equalsIgnoreCase("superadmin@gmail.com") && inputPassword.equals("admin123")) {
//                loginSuperAdmin();
//
//            } else {
//
//                loginEmployee(inputEmail, inputPassword);
//            }
//        });
//    }
//
//    private void loginSuperAdmin() {
//
//        boolean remember = cbRemember.isChecked();
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        editor.putBoolean("isLoggedIn", true);
//        editor.putString("userName", "Sumit Sir");
//        editor.putString("userRole", "Super Administrator");
//        editor.putString("userEmail", "superadmin@gmail.com");
//        editor.putString("userId", "SUPER_ADMIN_ID");
//        editor.putString("userPhone", "9876543210");
//        editor.putString("userDept", "Management");
//        editor.putString("userEmpId", "ADM-001");
//        editor.putString("userJoiningDate", "01-01-2024");
//        editor.putString("userStatus", "ACTIVE");
//        editor.putString("userEmergencyPhone", "9876543210");
//        editor.putString("userAddress", "Pune, Maharashtra");
//        editor.putBoolean("rememberMe", remember);
//        editor.apply();
//
//
//        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//
//        intent.putExtra("Name", "Sumit Sir");
//        intent.putExtra("Role", "Super Administrator");
//        intent.putExtra("Email", "superadmin@gmail.com");
//
//        Toast.makeText(LoginActivity.this, "Super Admin Login Success!", Toast.LENGTH_SHORT).show();
//
//        startActivity(intent);
//        finish();
//    }
//    private void loginEmployee(
//            String email,
//            String password) {
//        EmployeeApiModel loginRequest = new EmployeeApiModel();
//        loginRequest.setEmail(email);
//        loginRequest.setPassword(password);
//
//        employeeApi.loginEmployee(loginRequest)
//                .enqueue(
//                        new Callback<ApiResponse<EmployeeApiModel>>() {
//
//                            @Override
//                            public void onResponse(
//                                    @NonNull Call<ApiResponse<EmployeeApiModel>> call,
//                                    @NonNull Response<ApiResponse<EmployeeApiModel>> response) {
//
//
//                                if (!response.isSuccessful()) {
//                                    Toast.makeText(LoginActivity.this, "Invalid Email or Password! HTTP " + response.code(), Toast.LENGTH_LONG).show();
//                                    return;
//                                }
//
//
//
//                                if (response.body() == null) {
//                                    Toast.makeText(LoginActivity.this, "Server returned empty response", Toast.LENGTH_LONG
//                                    ).show();
//                                    return;
//                                }
//
//                                if (response.body().getData() == null) {
//                                    Toast.makeText(LoginActivity.this, "Invalid Email or Password!", Toast.LENGTH_LONG).show();
//
//                                    return;
//                                }
//
//                                EmployeeApiModel employee = response.body().getData();
//                                saveEmployeeSession(employee);
//                            }
//
//                            @Override
//                            public void onFailure(
//                                    @NonNull Call<ApiResponse<EmployeeApiModel>> call,
//                                    @NonNull Throwable t) {
//                                Toast.makeText(LoginActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
//                            }
//                        }
//                );
//    }
//    private void saveEmployeeSession(EmployeeApiModel employee) {
//
//        boolean remember = cbRemember.isChecked();
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        editor.putBoolean("isLoggedIn", true);
//
//        editor.putString("userName", employee.getName());
//        editor.putString("userRole", employee.getRole());
//        editor.putString("userEmail", employee.getEmail());
//        editor.putString("userId", employee.getId());
//        editor.putString("userPhone", employee.getPhone() != null ? employee.getPhone() : "N/A");
//        editor.putString("userDept", employee.getDepartment() != null ? employee.getDepartment() : "N/A");
//        editor.putString("userEmpId", employee.getEmpID() != null ? employee.getEmpID() : "N/A");
//        editor.putString("userJoiningDate", employee.getJoiningDate() != null ? employee.getJoiningDate() : "N/A");
//        editor.putString("userStatus", employee.getStatus() != null ? employee.getStatus() : "ACTIVE");
//        editor.putString("userEmergencyPhone", employee.getEmergencyPhone() != null ? employee.getEmergencyPhone() : "N/A");
//        editor.putString("userAddress", employee.getAddress() != null ? employee.getAddress() : "N/A");
//        editor.putBoolean("rememberMe", remember);
//        editor.apply();
//
//        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//
//        intent.putExtra("Name", employee.getName());
//        intent.putExtra("Role", employee.getRole());
//        intent.putExtra("Email", employee.getEmail());
//
//        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
//
//        startActivity(intent);
//        finish();
//    }
//}
    //============================================================================================//
//package com.agribird.hrmsapp;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.api.EmployeeApi;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//import com.agribird.hrmsapp.network.RetrofitClient;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//public class LoginActivity extends AppCompatActivity {
//    TextInputLayout tlEmail, tlPassword;
//    TextInputEditText hName, hPass;
//    Button btnLog;
//    CheckBox cbRemember;
//
//    private SharedPreferences sharedPreferences;
//   // private HRMSDatabase database;
//    //private EmployeeDao employeeDao;
//
//    private EmployeeApi employeeApi;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_login);
//
//        //database = HRMSDatabase.getInstance(this);
//        //employeeDao = database.employeeDao();
//
//        employeeApi= RetrofitClient.getEmployeeApi();
//
//        tlEmail = findViewById(R.id.tlEmail);
//        tlPassword = findViewById(R.id.tlPassword);
//        hName = findViewById(R.id.hName);
//        hPass = findViewById(R.id.hPass);
//
//        btnLog = findViewById(R.id.btnLog);
//        cbRemember = findViewById(R.id.cbRemember);
//
//        sharedPreferences = getSharedPreferences("HRMS_SESSION", MODE_PRIVATE);
//
//        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
//        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
//
//        if (isLoggedIn && rememberMe) {
//            String userName = sharedPreferences.getString("userName", "");
//            String userRole = sharedPreferences.getString("userRole", "");
//            String userEmail = sharedPreferences.getString("userEmail", "");
//
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            intent.putExtra("Name", userName);
//            intent.putExtra("Role", userRole);
//            intent.putExtra("Email", userEmail);
//            startActivity(intent);
//            finish();
//        }
//
//        btnLog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                tlEmail.setError(null);
//                tlPassword.setError(null);
//
//                String inputEmail = hName.getText() != null ? hName.getText().toString().trim() : "";
//                String inputPassword = hPass.getText() != null ? hPass.getText().toString().trim() : "";
//
//                if (inputEmail.isEmpty()) {
//                    tlEmail.setError("Email cannot be empty");
//                    hName.requestFocus();
//                    return;
//                }
//
//                if (inputPassword.isEmpty()) {
//                    tlPassword.setError("Password cannot be empty");
//                    hPass.requestFocus();
//                    return;
//                }
//
//                if (inputPassword.length() < 6) {
//                    tlPassword.setError("Password must be at least 6 characters");
//                    hPass.requestFocus();
//                    return;
//                }
//
//                if (inputEmail.equalsIgnoreCase("superadmin@gmail.com") && inputPassword.equals("admin123")) {
//
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    boolean remember = cbRemember.isChecked();
//
//                    editor.putBoolean("isLoggedIn", true);
//                    editor.putString("userName", "Sumit Sir");
//                    editor.putString("userRole", "Super Administrator");
//                    editor.putString("userEmail", "superadmin@gmail.com");
//                    editor.putString("userId", "SUPER_ADMIN_ID");
//                    editor.putString("userPhone", "9876543210");
//                    editor.putString("userDept", "Management");
//                    editor.putString("userEmpId", "ADM-001");
//                    editor.putString("userJoiningDate", "01-01-2024");
//                    editor.putString("userStatus", "ACTIVE");
//
//                    // 🟢 सुपर ॲडसाठी इमर्जन्सी नंबर आणि ॲड्रेस सेव्ह करणे
//                    editor.putString("userEmergencyPhone", "9876543210");
//                    editor.putString("userAddress", "Pune, Maharashtra");
//
//                    editor.putBoolean("rememberMe", remember);
//                    editor.apply();
//
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    intent.putExtra("Name", "Sumit Sir");
//                    intent.putExtra("Role", "Super Administrator");
//                    intent.putExtra("Email", "superadmin@gmail.com");
//
//                    Toast.makeText(LoginActivity.this, "Super Admin Login Success!", Toast.LENGTH_SHORT).show();
//                    startActivity(intent);
//                    finish();
//
//                } else {
//
//                   Employee employee = employeeDao.loginEmployee(inputEmail, inputPassword);
//
//                    if (employee != null) {
//
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        boolean remember = cbRemember.isChecked();
//
//                        editor.putBoolean("isLoggedIn", true);
//                        editor.putString("userName", employee.getName());
//                        editor.putString("userRole", employee.getRole());
//                        editor.putString("userEmail", employee.getEmail());
//                        editor.putString("userId", employee.getId());
//
//                        editor.putString("userPhone", employee.getPhone() != null ? employee.getPhone() : "N/A");
//                        editor.putString("userDept", employee.getDepartment() != null ? employee.getDepartment() : "IT");
//                        editor.putString("userEmpId", employee.getEmpId() != null ? employee.getEmpId() : "EMP-" + employee.getEmpId());
//                        editor.putString("userJoiningDate", employee.getJoiningDate() != null ? employee.getJoiningDate() : "N/A");
//                        editor.putString("userStatus", employee.getStatus() != null ? employee.getStatus() : "ACTIVE");
//
//                        // 🟢 रेग्युलर कर्मचाऱ्यासाठी इमर्जन्सी नंबर आणि ॲड्रेस सेव्ह करणे
//                        editor.putString("userEmergencyPhone", employee.getEmergencyPhone() != null ? employee.getEmergencyPhone() : "N/A");
//                        editor.putString("userAddress", employee.getAddress() != null ? employee.getAddress() : "N/A");
//
//                        editor.putBoolean("rememberMe", remember);
//                        editor.apply();
//
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        intent.putExtra("Name", employee.getName());
//                        intent.putExtra("Role", employee.getRole());
//                        intent.putExtra("Email", employee.getEmail());
//
//                        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
//                        startActivity(intent);
//                        finish();
//
//                    } else {
//                        Toast.makeText(LoginActivity.this,
//                                "Invalid Email or Password!",
//                                Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        });
//    }
//}
//================================================================================================
//package com.agribird.hrmsapp;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.databasecon.HRMSDatabase;
//import com.agribird.hrmsapp.ui.dashboard.DashboardFragment;
//
//public class LoginActivity extends AppCompatActivity {
//
//    EditText hName, hPass;
//    //TextView hSignUp;
//    Button btnLog;
//
//    CheckBox cbRemember;
//
//    private SharedPreferences sharedPreferences;
//
//    private HRMSDatabase database;
//    private EmployeeDao employeeDao;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_login);
//
//        database=HRMSDatabase.getInstance(this);
//        employeeDao=database.employeeDao();
//
//        hName = findViewById(R.id.hName);
//        hPass = findViewById(R.id.hPass);
//        //hSignUp = findViewById(R.id.hSignUp);
//        btnLog = findViewById(R.id.btnLog);
//        cbRemember=findViewById(R.id.cbRemember);
////        hSignUp.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////
////                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
////                startActivity(intent);
////            }
////        });
//
//        sharedPreferences=getSharedPreferences("HRMS_SESSION",MODE_PRIVATE);
//
//        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
//        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
//
//        if (isLoggedIn && rememberMe) {
//
//            String userName = sharedPreferences.getString("userName", "");
//            String userRole = sharedPreferences.getString("userRole", "");
//            String userEmail=sharedPreferences.getString("userEmail","");
//            String userId=sharedPreferences.getString("userId","");
//
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            intent.putExtra("Name", userName);
//            intent.putExtra("Role", userRole);
//            intent.putExtra("Email", userEmail);
//            startActivity(intent);
//            finish();
//        }
//
//        btnLog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String inputEmail = hName.getText().toString().trim();
//                String inputPassword = hPass.getText().toString().trim();
//
//                if (inputEmail.isEmpty()) {
//
//                    hName.setError("Email cannot be empty");
//                    hName.requestFocus();
//                    return;
//                }
//
//                if(inputPassword.isEmpty()){
//                    hPass.setError("Password cannot be empty");
//                    hPass.requestFocus();
//                    return;
//                }
//
//                if(inputPassword.length()<6){
//                    hPass.setError("Password must be at least 6 characters");
//                    hPass.requestFocus();
//                    return;
//                }
//
//                if(inputEmail.equalsIgnoreCase("superadmin@gmail.com") && inputPassword.equals("admin123")){
//
//                    SharedPreferences.Editor editor=sharedPreferences.edit();
//                    boolean rememberMe = cbRemember.isChecked();
//
//                    editor.putBoolean("isLoggedIn", true);
//                    editor.putString("userName", "Sumit Sir");
//                    editor.putString("userRole", "Super Administrator");
//                    editor.putString("userEmail", "superadmin@gmail.com");
//                    editor.putString("userId", "SUPER_ADMIN_ID");
//                    editor.putString("userPhone", "9876543210");
//                    editor.putString("userDept", "Management");
//                    editor.putString("userEmpId", "ADM-001");
//                    editor.putString("userJoiningDate", "01-01-2024");
//                    editor.putString("userStatus", "ACTIVE");
//                    editor.putBoolean("rememberMe", rememberMe);
//
//                    editor.apply();
//
//                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
//                    intent.putExtra("Name","Sumit Sir");
//                    intent.putExtra("Role","Super Administrator");
//                    intent.putExtra("Email","superadmin@gmail.com");
//
//                    Toast.makeText(LoginActivity.this, "Super Admin Login Success!", Toast.LENGTH_SHORT).show();
//                    startActivity(intent);
//                    finish();
//                    return;
//
//                }else {
//
//                    Employee employee = employeeDao.loginEmployee(inputEmail, inputPassword);
//
//                    if (employee != null) {
//
//                        SharedPreferences.Editor editor=sharedPreferences.edit();
//
//                        boolean rememberMe = cbRemember.isChecked();
//
//                        editor.putBoolean("isLoggedIn", true);
//                        editor.putString("userName", employee.getName());
//                        editor.putString("userRole", employee.getRole());
//                        editor.putString("userEmail",employee.getEmail());
//                        editor.putString("userId",employee.getId());
//
//                        editor.putString("userPhone", employee.getPhone() != null ? employee.getPhone() : "N/A");
//                        editor.putString("userDept", employee.getDepartment() != null ? employee.getDepartment() : "IT");
//                        editor.putString("userEmpId", employee.getEmpId() != null ? employee.getEmpId() : "EMP-" + employee.getEmpId());
//                        editor.putString("userJoiningDate", employee.getJoiningDate() != null ? employee.getJoiningDate() : "N/A");
//                        editor.putString("userStatus", employee.getStatus() != null ? employee.getStatus() : "ACTIVE");
//
//                        editor.putBoolean("rememberMe",rememberMe);
//
//                        editor.apply();
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        intent.putExtra("Name", employee.getName());
//                        intent.putExtra("Role", employee.getRole());
//                        intent.putExtra("Email", employee.getEmail());
//
//                        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
//                        startActivity(intent);
//                        finish();
//
//                    } else {
//                        Toast.makeText(LoginActivity.this,
//                                "Invalid Email or Password!",
//                                Toast.LENGTH_LONG).show();
//                    }
//                }
//
//            }
//        });
//    }
//}