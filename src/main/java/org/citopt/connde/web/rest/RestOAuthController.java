package org.citopt.connde.web.rest;

import java.util.logging.Logger;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.service.authentication.OAuth2AuthenticationService;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@PropertySource(value = "classpath:application.properties")
public class RestOAuthController {

	private final OAuth2AuthenticationService oAuth2AuthenticationService;

	private final Logger LOGGER = Logger.getLogger(RestOAuthController.class.getName());

	public RestOAuthController(OAuth2AuthenticationService oAuth2AuthenticationService) {
		this.oAuth2AuthenticationService = oAuth2AuthenticationService;
	}

	@RequestMapping(value = "/auth_code", method = RequestMethod.GET)
	public String getDeviceCode(@RequestParam("code") @ApiParam(value = "Authorization code", required = true) String code) {
		return code;
	}

	@RequestMapping(value = "/checkOauthTokenUser", method = RequestMethod.POST)
	@ApiOperation("Check an access token if it is valid and if the client is authorized to connect.")
	@ApiResponses({@ApiResponse(code = 200, message = "Access token is valid."), @ApiResponse(code = 401, message = "Access token is invalid/expired.")})
	public ResponseEntity<Void> checkOauthTokenUser(@RequestHeader("authorization") @ApiParam(value = "OAuth2 Access Token", required = true) String authorizationHeader) {
		if (!authorizationHeader.isEmpty())  {
			return oAuth2AuthenticationService.checkToken(authorizationHeader);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/checkOauthTokenSuperuser", method = RequestMethod.POST)
	@ApiResponse(code = 401, message = "Superuser is currently not supported.")
	public ResponseEntity<Void> checkOauthTokenSuperuser(@RequestHeader("authorization") @ApiParam(value = "OAuth2 Access Token", required = true) String authorizationHeader) {
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	@RequestMapping(value = "/checkOauthTokenAcl", method = RequestMethod.POST)
	@ApiOperation("Check an access token if it is valid and if the client is authorized to publish/subscribe.")
	@ApiResponses({@ApiResponse(code = 200, message = "Access token is valid."), @ApiResponse(code = 401, message = "Access token is invalid/expired.")})
	public ResponseEntity<Void> checkOauthTokenAcl(
			@RequestHeader("authorization") @ApiParam(value = "OAuth2 Access Token", required = true) String authorizationHeader,
			@RequestParam("clientid") String clientid,
			@RequestParam("topic") String topic,
			@RequestParam("acc") String acc) {
		LOGGER.info("Access request from " + clientid + " | Topic: " + topic + " | Level " + acc);
		if (!authorizationHeader.isEmpty()) {
			return oAuth2AuthenticationService.checkAccess(authorizationHeader, clientid, topic, acc);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
}
