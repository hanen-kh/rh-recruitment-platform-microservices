package com.example.leave_service.leave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeaveDecisionRequest(
        @NotNull LeaveStatus status,
        @Size(max = 1000) String reviewComment
) {
}
