package com.example.employeeservice.gRPC;

import com.example.employeeservice.employee.Employe;
import com.example.employeeservice.employee.EmployeRepository;
import com.example.grpc.EmployeeForLeaveResponse;
import com.example.grpc.EmployeeGrpcServiceGrpc;
import com.example.grpc.EmployeeLookupRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class EmployeeGrpcServer extends EmployeeGrpcServiceGrpc.EmployeeGrpcServiceImplBase {

  private final EmployeRepository employeRepository;

  @Override
  public void getEmployeeForLeave(
          EmployeeLookupRequest request,
          StreamObserver<EmployeeForLeaveResponse> responseObserver
  ) {
    String employeeId = request.getEmployeeId().trim();

    if (employeeId.isEmpty()) {
      responseObserver.onError(
              Status.INVALID_ARGUMENT
                      .withDescription("employee_id est obligatoire.")
                      .asRuntimeException()
      );
      return;
    }

    Employe employe = employeRepository.findById(employeeId).orElse(null);

    if (employe == null) {
      responseObserver.onError(
              Status.NOT_FOUND
                      .withDescription("Employé introuvable : " + employeeId)
                      .asRuntimeException()
      );
      return;
    }

    EmployeeForLeaveResponse response = EmployeeForLeaveResponse.newBuilder()
            .setEmployeeId(employe.getId())
            .setEmploymentStatus(toProtoStatus(employe))
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  private static com.example.grpc.EmploymentStatus toProtoStatus(Employe employe) {
    if (employe.getEmploymentStatus() == null) {
      return com.example.grpc.EmploymentStatus.EMPLOYMENT_STATUS_UNSPECIFIED;
    }

    return switch (employe.getEmploymentStatus()) {
      case ACTIVE -> com.example.grpc.EmploymentStatus.EMPLOYMENT_STATUS_ACTIVE;
      case ON_LEAVE -> com.example.grpc.EmploymentStatus.EMPLOYMENT_STATUS_ON_LEAVE;
      case SUSPENDED -> com.example.grpc.EmploymentStatus.EMPLOYMENT_STATUS_SUSPENDED;
      case TERMINATED -> com.example.grpc.EmploymentStatus.EMPLOYMENT_STATUS_TERMINATED;
    };
  }

}
