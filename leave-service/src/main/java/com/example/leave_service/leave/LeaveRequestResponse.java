package com.example.leave_service.leave;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveRequestResponse(
        Long id,
        String employeeId,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal requestedDays,
        String reason,
        LeaveStatus status,
        String reviewedById,
        LocalDateTime reviewedAt,
        String reviewComment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LeaveRequestResponse fromEntity(LeaveRequest request) {
        return new LeaveRequestResponse(
                request.getId(),
                request.getEmployeeId(),
                request.getLeaveType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getRequestedDays(),
                request.getReason(),
                request.getStatus(),
                request.getReviewedById(),
                request.getReviewedAt(),
                request.getReviewComment(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
