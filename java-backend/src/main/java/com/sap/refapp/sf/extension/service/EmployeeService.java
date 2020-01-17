package com.sap.refapp.sf.extension.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.refapp.sf.extension.model.Employee;
import com.sap.refapp.sf.extension.repository.EmployeeRepository;

@Repository
@Transactional
public class EmployeeService {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

	@Autowired
	private EmployeeRepository employeeRepository;

	public Iterable<Employee> getAllEmployees() throws DataAccessException {

		Iterable<Employee> employees = employeeRepository.findAll();
		return employees;
	}

	public Employee getEmployeeById(String employeeId) {
		return employeeRepository.findEmployeeById(employeeId);
	}

	public Employee saveEmployee(Employee employee) {
		return employeeRepository.save(employee);
	}

	public void saveEmployee(List<Employee> employees) {
		employeeRepository.saveAll(employees);
	}

	public void loadEmployee(String location) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<List<Employee>> typeReference = new TypeReference<List<Employee>>() {
		};
		InputStream inputStream = null;
		try {
			inputStream = TypeReference.class.getResourceAsStream(location);
			List<Employee> listOfEmployees = mapper.readValue(inputStream, typeReference);
			logger.info("emp no:" + listOfEmployees.size());
			saveEmployee(listOfEmployees);
		} catch (IOException e) {
			throw e;
		} 
	}

}