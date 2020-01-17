package com.sap.refapp.sf.extension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sap.refapp.sf.extension.config.SFExtensionApplicationContextInitializer;
import com.sap.refapp.sf.extension.model.Employee;
import com.sap.refapp.sf.extension.model.Project;
import com.sap.refapp.sf.extension.repository.EmployeeRepository;
import com.sap.refapp.sf.extension.repository.ProjectRepository;

@SpringBootApplication
@EnableScheduling
@Import({SFExtensionApplicationContextInitializer.class })
public class RunSmoothApplication implements CommandLineRunner {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public static void main(String[] args) {
		
		new SpringApplicationBuilder(RunSmoothApplication.class)
		.initializers(new SFExtensionApplicationContextInitializer())
		.run(args);
	}
	
	@Bean
	public RestTemplate getRestTemplate() {
		
		return new RestTemplate();
	}
	
	@Override
	public void run(String... args) throws Exception {
		loadInitialData();
	}
	
	public void loadInitialData() throws RestClientException, JSONException {
		
		Project p1 = new Project(1, "ESPM", "Enterprise sales and procurement model");
		Project p2 = new Project(2, "SHINE", "Sap hana interactive education");
		Project p3 = new Project(3, "HPA", "High performant application");
		Project p4 = new Project(4, "Objectstore sample", "Objectstore sample application");
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic c2ZhZG1pbkBTRlBBUlQwNDE4Mjk6cGFydDE4MTFEQzg=");
	    HttpEntity <String> entity = new HttpEntity<String>(headers);
	    String url = "https://apisalesdemo8.successfactors.com/odata/v2/User('106010')/directReports?&company_id=SFPART041829&$format=json&$top=5 ";
	       
	       
	    JSONObject result = new JSONObject(restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody());
	      
	    JSONArray reports = result.getJSONObject("d").getJSONArray("results");
	      
	    Employee e1= new Employee(reports.getJSONObject(0).getString("userId"),reports.getJSONObject(0).getString("defaultFullName"),"106010", "David Leal");
			    
	    Employee e2= new Employee(reports.getJSONObject(1).getString("userId"),reports.getJSONObject(1).getString("defaultFullName"),"106010", "David Leal");
		
	    Employee e3= new Employee(reports.getJSONObject(2).getString("userId"),reports.getJSONObject(2).getString("defaultFullName"),"106010", "David Lealn");
		
	    Employee e4= new Employee(reports.getJSONObject(3).getString("userId"),reports.getJSONObject(3).getString("defaultFullName"),"106010", "David Leal");
		
	    Employee e5 = new Employee("106010", "David Leal", "--MNGR--", "MANAGER");
	    
	    p1.getEmployees().add(e1);
		p1.getEmployees().add(e2);
		
		e1.getProjects().add(p1);
		e2.getProjects().add(p1);
		projectRepository.save(p1);
		
		p2.getEmployees().add(e2);
		p2.getEmployees().add(e3);
		
		e2.getProjects().add(p2);
		e3.getProjects().add(p2);
		projectRepository.save(p2);
		
	
		p3.getEmployees().add(e1);
		p3.getEmployees().add(e2);
		p3.getEmployees().add(e3);
		p3.getEmployees().add(e4);
		
		e1.getProjects().add(p3);
		e2.getProjects().add(p3);
		e3.getProjects().add(p3);
		e4.getProjects().add(p3);
		projectRepository.save(p3);
		
		projectRepository.save(p4);
		employeeRepository.save(e5);
	}
	
}


