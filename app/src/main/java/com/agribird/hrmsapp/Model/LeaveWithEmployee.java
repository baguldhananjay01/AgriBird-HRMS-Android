package com.agribird.hrmsapp.Model;

import androidx.room.Embedded;
import androidx.room.Relation;

public class LeaveWithEmployee {
    @Embedded
    public LeaveEntity leave;

    @Relation(
            parentColumn = "employeeId",
            entityColumn = "id"
    )

    public Employee employee;
}
