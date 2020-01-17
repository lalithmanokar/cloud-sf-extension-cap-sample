package com.sap.refapp.sf.extension.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.sap.refapp.sf.extension.model.Employee;

/**
 * This is the employee repository interface 
 * which is responsible for communicating with database.
 *
 */
@Repository
public interface EmployeeRepository extends CrudRepository<Employee, String>{
	
	@Query(value = "SELECT * FROM Employee WHERE Employee_ID = ?1", nativeQuery = true)
	Employee findEmployeeById(String employeeId);

}