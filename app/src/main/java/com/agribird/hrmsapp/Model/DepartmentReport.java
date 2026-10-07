package com.agribird.hrmsapp.Model;

public class DepartmentReport {

    private String departmentName;
    private int employeeCount;


    public DepartmentReport(String departmentName, int employeeCount) {

        this.departmentName=departmentName;
        this.employeeCount=employeeCount;
    }

    public String getDepartmentName() {
        return departmentName;
    }
    public int getEmployeeCount() {return employeeCount;}
}

