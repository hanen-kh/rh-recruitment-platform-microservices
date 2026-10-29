package com.example.leave_service;

import com.example.grpc.EmployeeGrpcServiceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.grpc.client.ImportGrpcClients;

@SpringBootApplication
@EnableDiscoveryClient
@ImportGrpcClients(
		target = "employee-service",
		types = EmployeeGrpcServiceGrpc.EmployeeGrpcServiceBlockingStub.class
)
public class LeaveServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeaveServiceApplication.class, args);
	}

}
