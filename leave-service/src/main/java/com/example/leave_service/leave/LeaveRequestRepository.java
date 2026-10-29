package com.example.leave_service.leave;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);

    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveStatus status);
}
