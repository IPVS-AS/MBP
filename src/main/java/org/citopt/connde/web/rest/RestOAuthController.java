package org.citopt.connde.web.rest;

import java.util.logging.Logger;

import javax.json.Json;

import groovy.util.logging.Slf4j;
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
@Slf4j
public class RestOAuthController {

	private static final Logger LOGGER = Logger.getLogger(RestOAuthController.class.getName());

	@Value("${security.oauth2.authorization.check-token-access}")
	private String checkTokenUri;

	@RequestMapping(value = "/auth_code", method = RequestMethod.GET)
	public String getDeviceCode(@RequestParam("code") String code) {
		return code;
	}

	@RequestMapping(value = "/checkOauthTokenUser", method = RequestMethod.POST)
	public ResponseEntity<?> checkOauthTokenUser(@RequestHeader("authorization") String authorizationHeader) {
		if (!authorizationHeader.isEmpty())  {
			return checkToken(authorizationHeader);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/checkOauthTokenSuperuser", method = RequestMethod.POST)
	public ResponseEntity<?> checkOauthTokenSuperuser(@RequestHeader("authorization") String authorizationHeader) {
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	@RequestMapping(value = "/checkOauthTokenAcl", method = RequestMethod.POST)
	public ResponseEntity<?> checkOauthTokenAcl(@RequestHeader("authorization") String authorizationHeader) {
		if (!authorizationHeader.isEmpty()) {
			return checkToken(authorizationHeader);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	private ResponseEntity<?> checkToken(String authorizationHeader) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Json> response = restTemplate.getForEntity(checkTokenUri +  "?token=" + authorizationHeader, Json.class);
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
}
