package com.sap.refapp.sf.extension.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * This is the DB Controller Test class
 *
 */
@ActiveProfiles(profiles = "test")
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ComponentScan({ "com.sap.refapp.sf.extension" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Sql("classpath:employeeTestData.sql")
@Transactional
public class DBControllerTest {

	protected static final String EMPLOYEE_API = "/sf-extension.svc/api/v1/employees";
	protected static final String PROJECT_API = "/sf-extension.svc/api/v1/projects";
	protected static final String NOTIFICATION_API = "/sf-extension.svc/api/v1/notificationsForManager";

	@Autowired
	WebApplicationContext context;

	private MockMvc mockMvc;
	private MockHttpServletRequestBuilder requestBuilder;

	private final static String EMPLOYEE_JSON = "{\"employeeId\":\"106003\",\"employeeName\":\"Simon Rampal\","
			+ "\"managerId\":\"106010\",\"managerName\":\"David Leal\",\"projects\":"
			+ "[{\"projectId\":1,\"projectName\":\"ESPM\",\"description\":"
			+ "\"Enterprise sales and procurement model\"},{\"projectId\":3,\"projectName\""
			+ ":\"HPA\",\"description\":\"High performant application\"},"
			+ "{\"projectId\":2,\"projectName\":\"SHINE\",\"description\":\"Sap hana interactive education\"}]"
			+ ",\"skills\":[\"Engineering Best Practices\",\"Process Improvement\",\"Quality Assurance\",\"Analytical Methods\"]}\r\n";
	private final static String PROJECT_JSON = "{\"projectId\":1,\"projectName\":\"ESPM\","
			+ "\"description\":\"Enterprise sales and procurement model\",\"employees\""
			+ ":[{\"employeeId\":\"106002\",\"employeeName\":\"Jamie King\",\"managerId\""
			+ ":\"106010\",\"managerName\":\"David Leal\",\"skills\":[]},{\"employeeId\":"
			+ "\"106003\",\"employeeName\":\"Simon Rampal\",\"managerId\":\"106010\","
			+ "\"managerName\":\"David Leal\",\"skills\":[]}]}\r\n";

	private final static String NOTIFICATION_JSON = "[{\"notificationId\":1," + "\"message\":\"Terminated\","
			+ "\"employeeId\":\"1234\",\"managerId\":\"106010\"," + "\"readStatus\":false}]";

	@Before
	public void create() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	/**
	 * It is used to test the getEmployeeById() by providing valid employee id.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getEmployeeByValidId() throws Exception {
		requestBuilder = buildGetRequest(EMPLOYEE_API + "/106002");
		
		  mockMvc.perform(requestBuilder).andExpect(status().isOk())
		  .andExpect(content().contentType(APPLICATION_JSON_UTF8));
	}

	/**
	 * It is used to test the getEmployeeById() by providing invalid employee id.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getEmployeeByInvalidId() throws Exception {
		requestBuilder = buildGetRequest(EMPLOYEE_API + "/3");
		mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
	}

	/**
	 * It is used to test the get stock by valid product id.
	 * 
	 * @throws Exception
	 */

	@Test
	public void getByValidProjectId() throws Exception {
		requestBuilder = buildGetRequest(PROJECT_API + "/1");

		
		  mockMvc.perform(requestBuilder).andExpect(status().isOk())
		  .andExpect(content().contentType(APPLICATION_JSON_UTF8));
		 
			
	}

	/**
	 * It is used to test the get stock by invalid product id.
	 * 
	 * @throws Exception
	 */

	@Test
	public void getByInValidProjectId() throws Exception {
		requestBuilder = buildGetRequest(PROJECT_API + "/33");
		mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
	}

	@Test
	public void addProjectTest() throws Exception {
		String PROJECT_JSON2 = "{\"projectName\": \"one Inbox\",\r\n"
				+ "		\"description\": \"central inbox for multiple solutions\"\r\n" + "	}";
		requestBuilder = buildPostRequest(PROJECT_API);

		mockMvc.perform(requestBuilder.contentType(APPLICATION_JSON_UTF8).content(PROJECT_JSON2))
				.andExpect(status().isCreated());
	}

	/*
	 * @Test public void addEmployeeToProjectTest() throws Exception {
	 * requestBuilder = buildPutRequest(PROJECT_API + "/4/106003"); String
	 * PROJECT_JSON2 =
	 * "{\"projectId\":4,\"projectName\":\"Objectstore sample\",\"description\":\"Objectstore sample application\","
	 * +
	 * "\"employees\":[{\"employeeId\":\"106003\",\"employeeName\":\"Simon Rampal\","
	 * +
	 * "\"managerId\":\"106010\",\"managerName\":\"David Leal\",\"skills\":[]}]}\r\n"
	 * ;
	 * 
	 * String EMPLOYEE_JSON2 =
	 * "{\"employeeId\":\"106003\",\"employeeName\":\"Simon Rampal\"," +
	 * "\"managerId\":\"106010\",\"managerName\":\"David Leal\",\"projects\":" +
	 * "[{\"projectId\":4,\"projectName\":\"Objectstore sample\",\"description\":\"Objectstore sample application\"},"
	 * +
	 * "{\"projectId\":2,\"projectName\":\"SHINE\",\"description\":\"Sap hana interactive education\"},"
	 * +
	 * "{\"projectId\":3,\"projectName\":\"HPA\",\"description\":\"High performant application\"},"
	 * +
	 * "{\"projectId\":1,\"projectName\":\"ESPM\",\"description\":\"Enterprise sales and procurement model\"}]"
	 * +
	 * ",\"skills\":[\"Engineering Best Practices\",\"Process Improvement\",\"Quality Assurance\",\"Analytical Methods\"]}"
	 * ;
	 * 
	 * mockMvc.perform(requestBuilder).andExpect(status().isNoContent());
	 * 
	 * requestBuilder = buildGetRequest(PROJECT_API + "/4");
	 * mockMvc.perform(requestBuilder).andExpect(status().isOk())
	 * .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(content().
	 * json(PROJECT_JSON2));
	 * 
	 * requestBuilder = buildGetRequest(EMPLOYEE_API + "/106003");
	 * mockMvc.perform(requestBuilder).andExpect(status().isOk())
	 * .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(content().
	 * json(EMPLOYEE_JSON2));
	 * 
	 * }
	 */

	/**
	 * It is used to test getAllEmployees
	 * 
	 * @throws Exception
	 */

	@Test
	public void getAllEmployeesTest() throws Exception {
		requestBuilder = buildGetRequest(EMPLOYEE_API);
		MvcResult result = mockMvc.perform(requestBuilder).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8)).andReturn();

		JSONArray jsonArr = new JSONArray(result.getResponse().getContentAsString());
		System.out.println(jsonArr);
		assertEquals(jsonArr.length(), 5);
	}

	/**
	 * It is used to test the getNotificationByEmployeeId() by providing valid
	 * employee id.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getNotificationByEmployeeIdTest() throws Exception {
		requestBuilder = buildGetRequest(NOTIFICATION_API + "/106010");
		mockMvc.perform(requestBuilder).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8));
	}

	/**
	 * It is used to build mock GET request.
	 * 
	 * @param path
	 * @return MockHttpServletRequestBuilder
	 */
	private MockHttpServletRequestBuilder buildGetRequest(final String path) {
		return get(path);
	}

	/**
	 * It is used to build mock PUT request.
	 * 
	 * @param path
	 * @return MockHttpServletRequestBuilder
	 */
	private MockHttpServletRequestBuilder buildPostRequest(final String path) {
		return post(path);
	}

	/**
	 * It is used to build mock PUT request.
	 * 
	 * @param path
	 * @return MockHttpServletRequestBuilder
	 */
	private MockHttpServletRequestBuilder buildPutRequest(final String path) {
		return put(path);
	}

}
