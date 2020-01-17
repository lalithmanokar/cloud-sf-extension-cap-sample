package com.sap.refapp.sf.extension.repository;

import com.sap.refapp.sf.extension.model.Notification;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import org.springframework.stereotype.Repository;

/**
 * This is the notification repository interface 
 * which is responsible for communicating with database.
 *
 */
@Repository
public interface NotificationRepository extends CrudRepository<Notification, String>{

	@Query(value = "SELECT * FROM NOTIFICATION WHERE MANAGER_ID = ?1", nativeQuery = true)
	Iterable<Notification> findNotificationByManagerId(String managerId);

}