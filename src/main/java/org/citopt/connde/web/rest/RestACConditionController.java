package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.IACCondition;
import org.citopt.connde.domain.device.Device;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * REST Controller for managing {@link Device devices}.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/policy-conditions")
@Api(tags = {"Access-Control Policies"})
public class RestACConditionController {
    
	@GetMapping(produces = "application/hal+json")
    public ResponseEntity<PagedModel<EntityModel<IACCondition>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	return ResponseEntity.ok().build();
    }
    
}
