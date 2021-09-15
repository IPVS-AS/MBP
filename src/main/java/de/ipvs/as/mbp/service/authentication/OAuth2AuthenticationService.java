//package de.ipvs.as.mbp.service.authentication;
//
//import java.util.logging.Logger;
//
//import javax.json.Json;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.jwt.Jwt;
//import org.springframework.security.jwt.JwtHelper;
//import org.springframework.security.jwt.crypto.sign.MacSigner;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//@Service
//public class OAuth2AuthenticationService {
//
//	private final Logger LOGGER = Logger.getLogger(OAuth2AuthenticationService.class.getName());
//
//	@Value("${security.oauth2.authorization.check-token-access}")
//	private String checkTokenUri;
//
//	@Value("${security.oauth2.resource.jwt.key-value}")
//	private String signingKey;
//
//	/**
//	 * Check an access token for its validity. Validity = it must not be expired and the signature must be verified.
//	 * @param accessToken is the oauth2 access token (jwt)
//	 * @return @{@link ResponseEntity} with @{@link HttpStatus}. 200 (OK) if token is valid, 401 (Unauthorized) otherwise.
//	 */
//	public ResponseEntity<Void> checkToken(String accessToken) {
//		accessToken = accessToken.split(" ")[1];
//		RestTemplate restTemplate = new RestTemplate();
//		ResponseEntity<Json> response = restTemplate.getForEntity(checkTokenUri +  "?token=" + accessToken, Json.class);
//		if (response.getStatusCode().equals(HttpStatus.OK)) {
//			JwtHelper.decodeAndVerify(accessToken, new MacSigner(signingKey));
//			return ResponseEntity.ok().build();
//		}
//		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//	}
//
//	/**
//	 * Check the request of a client (for publish/subscribe). The MBP client has only read (subscribe) access.
//	 * @param accessToken is the OAuth2 access accessToken.
//	 * @param clientId is the Id of the requesting client.
//	 * @param topic is the Id of the topic, for which the client is requesting access.
//	 * @param acc is the access level identifier: 0 = NONE, 1 = READ, 2 = WRITE, 4 = SUBSCRIBE
//	 * @return @{@link ResponseEntity} with @{@link HttpStatus}. 200 (OK) if access is granted for the given accessToken, client ID, topic, and access level. 401 (Unauthorized) otherwise.
//	 */
//	public ResponseEntity<Void> checkAccess(String accessToken, String clientId, String topic, String acc) {
//		accessToken = accessToken.split(" ")[1];
//		RestTemplate restTemplate = new RestTemplate();
//		ResponseEntity<Json> response = restTemplate.getForEntity(checkTokenUri +  "?token=" + accessToken, Json.class);
//		Jwt jwt = JwtHelper.decodeAndVerify(accessToken, new MacSigner(signingKey));
//		if (response.getStatusCode().equals(HttpStatus.OK)) {
//			if (clientId.contains("mbp") && (acc.equals("4") || acc.equals("1"))) {
//				LOGGER.info("Authorized MBP for Level " + acc);
//				return ResponseEntity.ok().build();
//			} else if (jwt.getClaims().contains("device-client") && acc.equals("2")) {
//				LOGGER.info("Authorized Device for level " + acc);
//				return ResponseEntity.ok().build();
//			} else {
//				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//			}
//		} else {
//			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//		}
//	}
//}
