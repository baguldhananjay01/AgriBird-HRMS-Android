package com.agribird.hrmsapp.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Entity
public class Employee implements Serializable {
    @NotNull
    @PrimaryKey
    private String id;
    private String name;
    private String email;
    private String role;
    private String department;
    private String phone;
    private String joiningDate;
    private String password;
    private String status;

    // NEW FIELDS ADDED
    private String emergencyPhone;
    private String bloodGroup;
    private String address;

    // PRIMARY CONSTRUCTOR (Includes All Fields)
    public Employee(@NotNull String id, String name, String email, String role,
                    String department, String phone, String joiningDate, String password,
                    String emergencyPhone, String bloodGroup, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.department = department;
        this.phone = phone;
        this.joiningDate = joiningDate;
        this.password = password;
        this.emergencyPhone = emergencyPhone;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.status = "Active";
    }

    // SECONDARY CONSTRUCTOR (Backward Compatibility / Optional Use)
    @Ignore
    public Employee(@NotNull String id, String name, String email, String role,
                    String department, String phone, String joiningDate, String password) {
        this(id, name, email, role, department, phone, joiningDate, password, "", "", "");
    }

    // GETTERS & SETTERS FOR EXISTING FIELDS
    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // GETTERS & SETTERS FOR NEW FIELDS
    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // HELPER METHODS FOR LOGIN / PROFILE
    public String getEmpId() {
        return id;
    }

    public String getAccountStatus() {
        return status;
    }
}