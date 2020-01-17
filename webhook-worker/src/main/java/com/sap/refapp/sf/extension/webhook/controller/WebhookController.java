package com.sap.refapp.sf.extension.webhook.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.refapp.sf.extension.model.Notification;
import com.sap.refapp.sf.extension.service.NotificationService;

@RestController
public class WebhookController {

	private static List<String> ALLOWED_REQUEST_ORIGINS;

	final HttpHeaders headers = new HttpHeaders();

	private final Logger logger = LoggerFactory.getLogger(WebhookController.class);

	final static String finalDestination = "RunSmoothApp";

	final static String DESTINATION_PATH = "/destination-configuration/v1/destinations/";

	private final RestTemplate restTemplate;

	final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private NotificationService notificationService;

	@Autowired
	public WebhookController(Environment env, final RestTemplate rest) {
		String allowedRequestOrigins = env.getProperty("ALLOWED_REQUEST_ORIGINS"); // comma separated origins to
																					// whitelist
		if (allowedRequestOrigins != null) {
			ALLOWED_REQUEST_ORIGINS = Stream.of(allowedRequestOrigins.trim().split("\\s*,\\s*"))
					.collect(Collectors.toList()); // store in a List for easy lookup
		}
		this.restTemplate = rest;

	}

	/**
	 * Method that receives messages posted by EM
	 * 
	 * @param request HttpServletRequest just for logging
	 * @param body    The message from queue
	 */
	@RequestMapping(value = "/webhook/**", method = RequestMethod.POST)
	public void onMessage(HttpServletRequest request, @RequestBody String body) {
		String path = request.getRequestURI();
		logger.info("Received message '%s' on path '%s'", body, path);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Notification notification = mapper.readValue(body, Notification.class);
			notificationService.saveNotification(notification);

			callNotificationControllerFromDestinationService();

		} catch (JsonParseException e) {
			logger.error(e.getMessage());
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Method that receives OPTIONS call for handshake
	 * 
	 * @param request                    HttpServletRequest just for logging
	 * @param webhookRequestOriginHeader webhook-request-origin header that EM sends
	 *                                   in handshake call
	 */
	@RequestMapping(value = "/webhook/**", method = RequestMethod.OPTIONS)
	public ResponseEntity<Object> onHandshake(HttpServletRequest request,
			@RequestHeader("webhook-request-origin") String webhookRequestOriginHeader,
			@RequestHeader Map<String, String> headers) {
		String path = request.getRequestURI();

		HttpHeaders responseHeaders = new HttpHeaders();

		headers.forEach((key, value) -> {
			logger.info(String.format("Header '%s' = %s", key, value)); // to discover webhook-request-callback
		});

		if (ALLOWED_REQUEST_ORIGINS == null || ALLOWED_REQUEST_ORIGINS.isEmpty()) {
			responseHeaders.add("WebHook-Allowed-Origin", "*"); // allow any origin if no whitelist is defined
			logger.info(String.format("No whitelist is defined - allow all. Handshake completed on path '%s'", path));
		} else if (ALLOWED_REQUEST_ORIGINS.contains(webhookRequestOriginHeader)) {
			responseHeaders.add("WebHook-Allowed-Origin", webhookRequestOriginHeader); // allow request origin only if
																						// it is whitelisted
			logger.info(
					String.format("webhook-request-origin found in whitelist. Handshake completed on path '%s'", path));
		} else {
			// do not allow origin which is not whitelisted
			logger.error(String.format("Handshake failed on path '%s'", path));
		}

		return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
	}

	/**
	 * Method to catch all GET requests for testing
	 */
	@RequestMapping(value = "/webhook/**", method = RequestMethod.GET)
	public String onGet(HttpServletRequest request, HttpServletResponse response) {
		String path = request.getRequestURI();

		String patternString = ".*/response/(\\d+)/?.*"; // send desired response status in URL

		Pattern pattern = Pattern.compile(patternString);

		Matcher matcher = pattern.matcher(path);
		while (matcher.find()) {
			String group = matcher.group(1);
			int code = Integer.parseInt(group);
			response.setStatus(code); // status from request path
			return "Here's your " + code + " response for path " + path;
		}

		return "Hello " + path;
	}

	/** methods to call runsmooth app with auth **/

	private void callNotificationControllerFromDestinationService() {
		try {
			final DestinationService destination = getDestinationServiceDetails();
			final String accessToken = getOAuthToken(destination);
			headers.set("Authorization", "Bearer " + accessToken);
			HttpEntity entity = new HttpEntity(headers);

			final String notificationUrl = destination.uri + DESTINATION_PATH + finalDestination;
			final ResponseEntity<String> response = restTemplate.exchange(notificationUrl, HttpMethod.GET, entity,
					String.class);

			final JsonNode root = mapper.readTree(response.getBody());

			final String destinationURlFromService = root.path("destinationConfiguration").path("URL").asText();

			final JsonNode authTokens = root.path("authTokens");

			String token = null;

			for (JsonNode node : authTokens) {
				token = node.path("value").asText();

			}

			headers.set("Authorization", "Bearer " + token);

			HttpEntity entity2 = new HttpEntity(headers);

			final String URLToPost = destinationURlFromService + "/runSmoothNotificationController";

			/*
			 * final ResponseEntity<String> response2 = restTemplate.exchange(URLToPost,
			 * HttpMethod.GET, entity2, String.class);
			 */
			restTemplate.exchange(URLToPost, HttpMethod.GET, entity2, String.class);

		} catch (IOException e) {
			logger.error("No proper destination Service available: {}", e.getMessage());

		}

	}

	private String getOAuthToken(final DestinationService destination) throws IOException {
		final String auth = destination.clientid + ":" + destination.clientsecret;
		final byte[] basicToken = Base64.getEncoder().encode(auth.getBytes());
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Authorization", "Basic " + new String(basicToken));
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("client_id", destination.clientid);
		map.add("grant_type", "client_credentials");
		final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
				headers);
		ResponseEntity<String> response = restTemplate.postForEntity(destination.url + "/oauth/token", request,
				String.class);
		final String responseString = response.getBody();
		JsonNode root = mapper.readTree(responseString);
		final String accessToken = root.get("access_token").asText();
		return accessToken;

	}

	private DestinationService getDestinationServiceDetails() throws IOException {
		final String destinationService = System.getenv("VCAP_SERVICES");
		final JsonNode root = mapper.readTree(destinationService);
		final JsonNode destinations = root.get("destination").get(0).get("credentials");
		final DestinationService destination = mapper.treeToValue(destinations, DestinationService.class);
		return destination;

	}

}

@JsonIgnoreProperties(ignoreUnknown = true)
class DestinationService {

	public String clientid;
	public String clientsecret;
	public String uri;
	public String url;

}
