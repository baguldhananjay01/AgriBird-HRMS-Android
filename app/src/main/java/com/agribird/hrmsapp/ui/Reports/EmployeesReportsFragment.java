package com.agribird.hrmsapp.ui.Reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.Model.DepartmentReport;
import com.agribird.hrmsapp.Model.Employee;
import com.agribird.hrmsapp.R;
import com.agribird.hrmsapp.adapter.DepartmentReportAdapter;
import com.agribird.hrmsapp.dao.EmployeeDao;
import com.agribird.hrmsapp.databasecon.HRMSDatabase;

import java.util.ArrayList;
import java.util.List;

public class EmployeesReportsFragment extends Fragment {

    private TextView txtTotalCount;
    private Button btnBackFromRep;
    private RecyclerView recyclerDepartmentReport;

    private DepartmentReportAdapter adapter;
    private ArrayList<DepartmentReport> reportList = new ArrayList<>();

    private HRMSDatabase database;
    private EmployeeDao employeeDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_employees_reports, container, false);

        txtTotalCount = view.findViewById(R.id.txtTotalCount);
        btnBackFromRep = view.findViewById(R.id.btnBackFromRep);
        recyclerDepartmentReport = view.findViewById(R.id.recyclerDepartmentReport);

        database = HRMSDatabase.getInstance(requireContext());
        employeeDao = database.employeeDao();

        recyclerDepartmentReport.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnBackFromRep.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        loadReportData();

        return view;
    }

    private void loadReportData() {
        new Thread(() -> {

            List<Employee> receivedList = employeeDao.getAllEmployee();

            if (receivedList == null) {
                receivedList = new ArrayList<>();
            }

            int management = 0, sales = 0, it = 0, finance = 0, hr = 0;
            int operations = 0, qa = 0, mis = 0, csr = 0, logistics = 0;

            for (Employee employee : receivedList) {
                String department = employee.getDepartment();

                if (department != null) {
                    if (department.equals("Management & Admin")) {
                        management++;
                    } else if (department.equals("Sales & Business Development")) {
                        sales++;
                    } else if (department.equals("IT & Development")) {
                        it++;
                    } else if (department.equals("Finance & Accounts")) {
                        finance++;
                    } else if (department.equals("Human Resources (HR)")) {
                        hr++;
                    } else if (department.equals("Operations & Lead Generation")) {
                        operations++;
                    } else if (department.equals("Quality Assurance (QA)")) {
                        qa++;
                    } else if (department.equals("Data Research & MIS")) {
                        mis++;
                    } else if (department.equals("Customer Support (CSR)")) {
                        csr++;
                    } else if (department.equals("Logistics & Marketplace")) {
                        logistics++;
                    }
                }
            }

            reportList.clear();
            reportList.add(new DepartmentReport("Management & Admin", management));
            reportList.add(new DepartmentReport("Sales & Business Development", sales));
            reportList.add(new DepartmentReport("IT & Development", it));
            reportList.add(new DepartmentReport("Finance & Accounts", finance));
            reportList.add(new DepartmentReport("Human Resources (HR)", hr));
            reportList.add(new DepartmentReport("Operations & Lead Generation", operations));
            reportList.add(new DepartmentReport("Quality Assurance (QA)", qa));
            reportList.add(new DepartmentReport("Data Research & MIS", mis));
            reportList.add(new DepartmentReport("Customer Support (CSR)", csr));
            reportList.add(new DepartmentReport("Logistics & Marketplace", logistics));

            final int totalSize = receivedList.size();

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    txtTotalCount.setText(String.valueOf(totalSize));
                    adapter = new DepartmentReportAdapter(reportList);
                    recyclerDepartmentReport.setAdapter(adapter);
                });
            }
        }).start();
    }
}