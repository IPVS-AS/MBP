package org.citopt.connde.web.rest;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;

import org.apache.commons.codec.binary.Base64;
import org.citopt.connde.RestConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestOAuthController {

	private static final Logger LOGGER = Logger.getLogger(RestOAuthController.class.getName());

	@RequestMapping(value = "/getAccessCode", method = RequestMethod.GET)
	public String getDeviceCode(@RequestParam("code") String code) {
		return code;
	}

	@RequestMapping(value = "/testOauth", method = RequestMethod.GET)
	public String testOAuth() {
		return randomNumeric(4);
	}

	@RequestMapping(value = "/checkOauthTokenUser", method = RequestMethod.POST)
	public HttpStatus checkOauthTokenUser(@RequestHeader("authorization") String authorizationHeader) {
		LOGGER.log(Level.INFO, "############################### Authorization header looks like: " + authorizationHeader);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Json> response = restTemplate.getForEntity("http://192.168.209.207:8080/MBP/oauth/check_token?token=" + getBearerTokenFromAuthHeader(authorizationHeader), Json.class);
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			LOGGER.log(Level.INFO, "CHECK OAUTH TOKEN FOR USER RETURNED OK ################");
			return HttpStatus.OK;
		}
		return HttpStatus.FORBIDDEN;
	}

	@RequestMapping(value = "/checkOauthTokenSuperuser", method = RequestMethod.POST)
	public HttpStatus checkOauthTokenSuperuser(@RequestHeader("Authorization") String authorizationHeader) {
		LOGGER.log(Level.INFO, "CHECK OAUTH TOKEN FOR SUPERUSER RETURNED 403 ################");
		return HttpStatus.FORBIDDEN;
	}

	@RequestMapping(value = "/checkOauthTokenAcl", method = RequestMethod.POST)
	public HttpStatus checkOauthTokenAcl(@RequestHeader("Authorization") String authorizationHeader) {
		LOGGER.log(Level.INFO, "CHECK OAUTH TOKEN FOR ACL RETURNED 403 ################");
		return HttpStatus.FORBIDDEN;
	}

	private String getBearerTokenFromAuthHeader(String authorizationHeader) {
		if (!authorizationHeader.isEmpty() && authorizationHeader.startsWith("Bearer")) {
			String[] authorizationHeaderSplit = authorizationHeader.split(" ");
			return authorizationHeaderSplit[1];
		}
		return null;
	}

	private HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {{
			String auth = username + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(
					auth.getBytes(StandardCharsets.US_ASCII));
			String authHeader = "Basic " + new String(encodedAuth);
			set("Authorization", authHeader);
		}};
	}
}
