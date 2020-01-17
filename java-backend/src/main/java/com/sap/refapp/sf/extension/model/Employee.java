package com.sap.refapp.sf.extension.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "EMPLOYEE")
public class Employee implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(length = 10, name = "EMPLOYEE_ID", unique = true)
	private String employeeId;
	
	@Column(name = "EMPLOYEE_NAME", nullable = false)
	private String employeeName;
	
	@Column(length = 10, name = "MANAGER_ID", nullable = false)
	private String managerId;
	
	@Column(name = "MANAGER_NAME", nullable = false)
	private String managerName;

	@JsonIgnoreProperties("employees")
	@ManyToMany(mappedBy = "employees")
    private Set<Project> projects = new HashSet<>();
	
	@Transient
	private Set <String> skills = new HashSet<>();
	
	public Employee() {
		
	}
	
	public Employee(String employeeId, String name, String managerId, String managerName) {
		this.employeeId=employeeId;
		this.employeeName=name;
		this.managerId=managerId;
		this.managerName=managerName;
	}
		
	public String getEmployeeId() {
		return employeeId;
	}
	
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
		
	public String getemployeeName() {
		return employeeName;
	}
	
	public void setName(String name) {
		this.employeeName = name;
	}
		
	public String getManagerId() {
		return managerId;
	}
	
	public void setManagerId(String managerId) {
		this.managerId = managerId;
	}
		
	public String getManagerName() {
		return managerName;
	}
	
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
	
	public Set<Project> getProjects() {
		return projects;
	}

	public void setProjects(Set<Project> projects) {
		this.projects = projects;
	}
	
	public Set<String> getSkills() {
		return skills;
	}

	public void setSkills(Set<String> skills) {
		this.skills = skills;
	}
}
