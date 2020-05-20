package org.citopt.connde.service.authentication;

import java.util.logging.Logger;

import javax.json.Json;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OAuth2AuthenticationService {

	private final Logger LOGGER = Logger.getLogger(OAuth2AuthenticationService.class.getName());

	@Value("${security.oauth2.authorization.check-token-access}")
	private String checkTokenUri;

	@Value("${security.oauth2.resource.jwt.key-value}")
	private String signingKey;

	/**
	 * Check an access token for its validity. Validity = it must not be expired!
	 * @param authorizationHeader is the oauth2 access token (jwt)
	 * @return @{@link ResponseEntity} with @{@link HttpStatus}. 200 (OK) if token is valid, 401 (Unauthorized) otherwise.
	 */
	public ResponseEntity<Void> checkToken(String authorizationHeader) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Json> response = restTemplate.getForEntity(checkTokenUri +  "?token=" + authorizationHeader, Json.class);
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			JwtHelper.decodeAndVerify(authorizationHeader, new MacSigner(signingKey));
			return ResponseEntity.ok().build();
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Check the request of a client (for publish/subscribe). The MBP client has only read (subscribe) access.
	 * @param token is the OAuth2 access token.
	 * @param clientId is the Id of the requesting client.
	 * @param topic is the Id of the topic, for which the client is requesting access.
	 * @param acc is the access level identifier: 0 = NONE, 1 = READ, 2 = WRITE, 4 = SUBSCRIBE
	 * @return @{@link ResponseEntity} with @{@link HttpStatus}. 200 (OK) if access is granted for the given token, client ID, topic, and access level. 401 (Unauthorized) otherwise.
	 */
	public ResponseEntity<Void> checkAccess(String token, String clientId, String topic, String acc) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Json> response = restTemplate.getForEntity(checkTokenUri +  "?token=" + token, Json.class);
		Jwt jwt = JwtHelper.decodeAndVerify(token, new MacSigner(signingKey));
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			if (clientId.contains("mbp") && (acc.equals("4") || acc.equals("1"))) {
				LOGGER.info("Authorized MBP for Level " + acc);
				return ResponseEntity.ok().build();
			} else if (jwt.getClaims().contains("device-client") && acc.equals("2")) {
				LOGGER.info("Authorized Device for level " + acc);
				return ResponseEntity.ok().build();
			} else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
}
