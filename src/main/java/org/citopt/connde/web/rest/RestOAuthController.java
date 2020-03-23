package org.citopt.connde.web.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;

import org.citopt.connde.RestConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@PropertySource(value = "classpath:application.properties")
public class RestOAuthController {

	private static final Logger LOGGER = Logger.getLogger(RestOAuthController.class.getName());

	@Value("${security.oauth2.authorization.check-token-access}")
	private String checkTokenUri;

	@RequestMapping(value = "/getAccessCode", method = RequestMethod.GET)
	public String getDeviceCode(@RequestParam("code") String code) {
		return code;
	}

	@RequestMapping(value = "/checkOauthTokenUser", method = RequestMethod.POST)
	public ResponseEntity<?> checkOauthTokenUser(@RequestHeader("authorization") String authorizationHeader) {
		LOGGER.log(Level.INFO, "Check OAuth Token User");
		return checkToken(authorizationHeader);
	}

	@RequestMapping(value = "/checkOauthTokenSuperuser", method = RequestMethod.POST)
	public ResponseEntity<?> checkOauthTokenSuperuser(@RequestHeader("authorization") String authorizationHeader) {
		LOGGER.log(Level.INFO, "Check OAuth Token Superuser");
		return new ResponseEntity<>(HttpStatus.OK, HttpStatus.OK);
	}

	@RequestMapping(value = "/checkOauthTokenAcl", method = RequestMethod.POST)
	public ResponseEntity<?> checkOauthTokenAcl(@RequestHeader("authorization") String authorizationHeader) {
		LOGGER.log(Level.INFO, "Check OAuth Token Acl");
		return new ResponseEntity<>(HttpStatus.OK, HttpStatus.OK);
	}

	private ResponseEntity<?> checkToken(String authorizationHeader) {
		RestTemplate restTemplate = new RestTemplate();
		LOGGER.log(Level.INFO, "Checking with token uri " + checkTokenUri + " and " + authorizationHeader);
		ResponseEntity<Json> response = restTemplate.getForEntity(checkTokenUri +  "?token=" + authorizationHeader, Json.class);
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			LOGGER.log(Level.INFO, HttpStatus.OK.toString());
			return new ResponseEntity<>(HttpStatus.OK, HttpStatus.OK);
		}
		LOGGER.log(Level.INFO, HttpStatus.UNAUTHORIZED.toString());
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
	}
}
