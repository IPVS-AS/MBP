package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestOAuthController {

	private final Logger log = LoggerFactory.getLogger(RestOAuthController.class);

	@RequestMapping(value = "/getAccessCode", method = RequestMethod.GET)
	public String getDeviceCode(@RequestParam("code") String code) {
		/*
		System.out.println("Device Code is " + code);
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject().put("code", code);
			return new ResponseEntity<>(jsonObject, HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
			return new ResponseEntity<>(new JSONObject(), HttpStatus.INTERNAL_SERVER_ERROR);
		}*/
		return code;
	}

	@RequestMapping(value = "/testOauth", method = RequestMethod.GET)
	public String testOAuth() {
		return randomNumeric(4);
	}

	@RequestMapping(value = "/verifyPermission", method = RequestMethod.POST)
	public HttpStatus verifyPermission() {
		log.debug("VERIFY PERMISSION WAS OK ################");
		return HttpStatus.OK;
	}
}
