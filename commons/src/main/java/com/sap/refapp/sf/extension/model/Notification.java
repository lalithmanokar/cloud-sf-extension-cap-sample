package com.sap.refapp.sf.extension.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Notification")
public class Notification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "NOTIFICATION_ID", unique = true, nullable = false)
	private Long notificationId;
	
	@Column(name = "MESSAGE", nullable = false)
	private String message;
	
	@Column(length = 10, name = "EMPLOYEE_ID", nullable = false )
	private String employeeId;
	
	@Column(length = 10, name = "MANAGER_ID", nullable = false )
	private String managerId;
	
	@Column(name="READ")
	private boolean readStatus = false;
	
	
	public Notification() {
		
	}
	
	public Notification(Long notificationId, String message, String employeeId, String managerId) {
	
		this.notificationId = notificationId;
		this.message = message;
		this.employeeId = employeeId;
		this.managerId = managerId;
	}
	
	public Long getNotificationId() {
		return notificationId;
	}
	
	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getManagerId() {
		return managerId;
	}
	
	public void setManagerId(String managerId) {
		this.managerId = managerId;
	}
	
	public String getEmployeeId() {
		return employeeId;
	}
	
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	} 
	
	public boolean getReadStatus() {
		return readStatus;
	}
	
	public void setReadStatus(boolean notificationStatus) {
		this.readStatus = notificationStatus;
	} 

}
