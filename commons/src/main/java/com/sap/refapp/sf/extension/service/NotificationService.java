package com.sap.refapp.sf.extension.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sap.refapp.sf.extension.model.Notification;
import com.sap.refapp.sf.extension.repository.NotificationRepository;

@Repository
@Transactional
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;
	
	
	public Iterable<Notification> getAllNotification() throws DataAccessException {

		Iterable<Notification> notification = notificationRepository.findAll();
		return notification;
	}

	public Notification saveNotification(Notification notification) {
		return notificationRepository.save(notification);
	}
	
}