package com.example.leave_service.leave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LeaveRequestCreateRequest(
        @NotNull LeaveType leaveType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Size(max = 1000) String reason
) {
}
