package org.citopt.connde.web.rest;

import javax.ws.rs.core.Response;

import org.citopt.connde.RestConfiguration;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestOAuthController {

    @RequestMapping(value = "/addNewDevice", method = RequestMethod.GET)
    public String getDeviceCode(@RequestParam("code") String code) {
        System.out.println("Device Code is " + code);
        return code;
    }
}
