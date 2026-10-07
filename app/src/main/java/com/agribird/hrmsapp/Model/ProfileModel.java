package com.agribird.hrmsapp.Model;

public class ProfileModel {
    private String name, role, email, phone, empId, department, joiningDate, accountStatus,emergencyPhone,address;

    public ProfileModel(String name, String role, String email, String phone, String empId, String department, String joiningDate, String accountStatus,
                        String emergencyPhone,String address) {
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.empId = empId;
        this.department = department;
        this.joiningDate = joiningDate;
        this.accountStatus = accountStatus;
        this.emergencyPhone=emergencyPhone;
        this.address=address;
    }

    // Getters
    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }
    public String getRole() { return role; }
    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() { return phone; }
    public String getEmpId() { return empId; }
    public String getDepartment() { return department; }
    public String getJoiningDate() { return joiningDate; }
    public String getAccountStatus() { return accountStatus; }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setPhone(String phone) { this.phone = phone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
    public void setAddress(String address) { this.address = address; }
}