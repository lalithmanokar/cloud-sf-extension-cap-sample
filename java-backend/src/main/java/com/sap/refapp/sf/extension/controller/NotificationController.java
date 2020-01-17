package com.sap.refapp.sf.extension.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.refapp.sf.extension.model.Notification;
import com.sap.refapp.sf.extension.repository.NotificationRepository;

@RestController
@RequestMapping("/")
public class NotificationController {

	private final Logger logger = LoggerFactory.getLogger(NotificationController.class);

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	private NotificationRepository notificationRepo;

	String logonName;

	@MessageMapping("/notification")
	@SendTo("/topic/notificationByManager")
	@GetMapping("/runSmoothNotificationController/**")
	public void getNotificationByEmployeeId() {
		String managerId = logonName;

		if (managerId != null) {
			final Iterable<Notification> notifications = notificationRepo.findNotificationByManagerId(managerId);
			if (notifications != null) {
				List<Notification> unReadNotifications = new ArrayList<>();
				for (Notification notif : notifications) {
					if (!notif.getReadStatus()) {
						unReadNotifications.add(notif);
					}
				}
				this.template.convertAndSend("/topic/notificationByManager", unReadNotifications);
			}
		}

	}

	@GetMapping("/hello-token")
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
		logonName = token.getLogonName();
		return result;

	}

}