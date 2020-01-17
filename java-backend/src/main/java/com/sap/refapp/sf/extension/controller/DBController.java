package com.sap.refapp.sf.extension.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.refapp.sf.extension.model.Employee;
import com.sap.refapp.sf.extension.model.Notification;
import com.sap.refapp.sf.extension.model.Project;
import com.sap.refapp.sf.extension.repository.NotificationRepository;
import com.sap.refapp.sf.extension.service.EmployeeService;
import com.sap.refapp.sf.extension.service.ProjectService;

@RestController
@RequestMapping("/sf-extension.svc/api/v1")

public class DBController {

	private final Logger logger = LoggerFactory.getLogger(DBController.class);

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private NotificationRepository notificationRepo;
	
	/*
	 * Return the list of all employees
	 */
	@GetMapping("/employees")
	public ResponseEntity<Iterable<Employee>> getEmployees() {

		final Iterable<Employee> employees;
		try {
			employees = employeeService.getAllEmployees();
			if (employees != null)
				return new ResponseEntity<>(employees, HttpStatus.OK);
			return errorMessage("Not found", HttpStatus.NOT_FOUND);
		} catch (DataAccessException e) {
			logger.error("Database is down");
			return errorMessage("Database service is temporarily down. Please try again later",
					HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/*
	 * To generate a profile summary of the Employee when the Termination Event is
	 * triggered
	 */
	@GetMapping("employees/{employeeId}")
	public ResponseEntity<Employee> getProfileSummary(@PathVariable(value = "employeeId") String employeeId) {

		Employee employee = employeeService.getEmployeeById(employeeId);
		if (employee != null) {

			try {
				employee.getSkills().addAll(getSkillsFromSF(employeeId));

			} catch (RestClientException e) {
				logger.error(e.getMessage());
			} catch (JSONException e) {
				logger.error(e.getMessage());
			}

			return new ResponseEntity<>(employee, HttpStatus.OK);

		}

		return errorMessage("Not found", HttpStatus.NOT_FOUND);

	}

	/*
	 * Return the list of all Projects with its details like Description and
	 * Employees participating in the project
	 */
	@GetMapping("/projects")
	public ResponseEntity<Iterable<Project>> getProjectDetails() throws InterruptedException {

		final Iterable<Project> projects;
		try {
			projects = projectService.getAllProjects();
			if (projects != null)
				return new ResponseEntity<>(projects, HttpStatus.OK);
			return errorMessage("Not found", HttpStatus.NOT_FOUND);
		} catch (DataAccessException e) {
			logger.error(e.getMessage());
			return errorMessage("Database service is temporarily down. Please try again later",
					HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/*
	 * Returns the details of the project with id which is passed as parameter
	 */
	@GetMapping("/projects/{projId}")
	public ResponseEntity<Project> getProjectDetails(@PathVariable(value = "projId") int projId) {

		Project project = projectService.getProjectById(projId);
		if (project != null)
			return new ResponseEntity<>(project, HttpStatus.OK);
		return errorMessage("Not found", HttpStatus.NOT_FOUND);
	}

	/*
	 * method to get token
	 */
	@GetMapping("/token")
	public ResponseEntity<String> getToken() {

		return new ResponseEntity<>("ok", HttpStatus.OK);
	}

	/*
	 * Creates a new Project with its details
	 */
	@PostMapping("projects")
	public ResponseEntity<String> addProject(@RequestBody final Project project) {

		projectService.saveProject(project);
		return new ResponseEntity<>(String.valueOf(project.getProjectId()), HttpStatus.CREATED);

	}

	/*
	 * Add employee to project
	 */

	@PutMapping("/projects/{projId}/{empId}")
	public ResponseEntity AddEmployeeToProject(@PathVariable(value = "projId") String projId,
			@PathVariable(value = "empId") String empId) {

		Project project = projectService.getProjectById(Integer.parseInt(projId));
		Employee emp = employeeService.getEmployeeById(empId);
		
		Set<Employee> employees = project.getEmployees();
		employees.add(emp);
		project.setEmployees(employees);
		projectService.saveProject(project);
		
		Set<Project> projects = emp.getProjects();
		projects.add(project);
		emp.setProjects(projects);
		employeeService.saveEmployee(emp);
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

	}

	/*
	 * Returns the list of notifications for the logged in user
	 */
	@GetMapping("notificationsForManager/{managerId}")
	public ResponseEntity<Iterable<Notification>> getNotificationByEmployeeId(
			@PathVariable(value = "managerId") String managerId) {

		final Iterable<Notification> notifications = notificationRepo.findNotificationByManagerId(managerId);
		if (notifications != null)
			return new ResponseEntity<>(notifications, HttpStatus.OK);
		return errorMessage("Not found", HttpStatus.NOT_FOUND);

	}

	/*
	 * To mark a notification as Read after Summary is generated
	 * 
	 */
	@PutMapping("markNotificationAsRead/{notificationId}")
	public boolean markNotificationAsRead(@PathVariable(value = "notificationId") String notificationId) {
		Optional<Notification> notif = notificationRepo.findById(notificationId);
		if(notif.isPresent()) {
			notif.get().setReadStatus(true);
			notificationRepo.save(notif.get());
			return true;
		}
		return false;
	}

	public static ResponseEntity errorMessage(String message, HttpStatus status) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);

		return ResponseEntity.status(status).headers(headers).body(message);
	}

	@GetMapping(value = "/hello-token")
	public Map<String, String> sayHello(@AuthenticationPrincipal Token token) {

		logger.info("Got the Xsuaa token: " + token);
		logger.info(token.toString());

		Map<String, String> result = new HashMap<>();
		result.put("grant type", token.getGrantType());
		result.put("client id", token.getClientId());
		result.put("subaccount id", token.getSubaccountId());
		result.put("logon name", token.getLogonName());
		result.put("family name", token.getFamilyName());
		result.put("given name", token.getGivenName());
		result.put("email", token.getEmail());
		result.put("authorities", String.valueOf(token.getAuthorities()));
		result.put("scopes", String.valueOf(token.getScopes()));
		return result;
	}

	public static ArrayList<String> getSkillsFromSF(String employeeId) throws RestClientException, JSONException {
		final String uri = "https://apisalesdemo2.successfactors.eu/odata/v2/SkillProfile(externalCode='" + employeeId
				+ "')?&$format=json&$expand=externalCodeNav,ratedSkills/skillNav&$select=ratedSkills/skillNav/name_en_US";

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Basic c2ZhZG1pbkBTRlBBUlQwMzUyMjQ6cGFydDE4MTFEQzI=");

		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		JSONObject result;
		JSONArray skills = null;
		ArrayList<String> skillNames = new ArrayList<String>();
		try {
			result = new JSONObject(restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody());
			skills = result.getJSONObject("d").getJSONObject("ratedSkills").getJSONArray("results");

			for (int i = 0; i < skills.length(); i++) {

				JSONObject oSkill;
				oSkill = skills.getJSONObject(i);
				String skill = (String) ((JSONObject) oSkill.get("skillNav")).get("name_en_US");
				skillNames.add(skill);

			}

		} catch (Exception e) {
			throw e;
		}

		return skillNames;

	}

}
