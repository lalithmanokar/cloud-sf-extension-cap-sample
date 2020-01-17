package com.sap.refapp.sf.extension.service;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This class is used to test the loading mock data from json files.
 *
 */
@ActiveProfiles(profiles = "test")
@RunWith(SpringRunner.class)
@ComponentScan({ "com.sap.refapp.sf.extension" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class EmployeeServiceTest {

	private static final String EMPLOYEE_TEST_DATA = "/employeeTestData.json";
	private static final String INVALID_PATH = "temp/testData.json";

	@Autowired
	private EmployeeService service;

	/**
	 * This method is used to test the loading of employee data from valid json
	 * path.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLoadEmployee() throws IOException {
		service.loadEmployee(EMPLOYEE_TEST_DATA);
	}

	/**
	 * This method is used to test the loading of employee data from invalid path.
	 * 
	 * @throws IOException
	 */
	@Test(expected = java.io.IOException.class)
	public void testLoadEmployeeFromInvalidPath() throws IOException {
		service.loadEmployee(INVALID_PATH);
	}

}
