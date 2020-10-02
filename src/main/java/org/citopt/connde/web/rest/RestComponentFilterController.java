package org.citopt.connde.web.rest;

import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.RuleActionRepository;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.repository.RuleTriggerRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.projection.ComponentExcerpt;
import org.citopt.connde.service.UserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller that exposes methods that allow the filtering for certain
 * components, e.g. by adapter/device id.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = { "Component filter" })
public class RestComponentFilterController {

	@Autowired
	private ActuatorRepository actuatorRepository;

	@Autowired
	private SensorRepository sensorRepository;

	@Autowired
	private RuleActionRepository ruleActionRepository;

	@Autowired
	private RuleTriggerRepository ruleTriggerRepository;

	@Autowired
	private RuleRepository ruleRepository;
	
	@Autowired
	private UserEntityService userEntityService;

	/**
	 * Retrieves all rules that use a given rule trigger.
	 *
	 * @param ruleTriggerId the id of the {@link RuleTrigger}.
	 * @return the list of {@link Rule}s.
	 * @throws EntityNotFoundException 
	 */
	@GetMapping("/rules/by-ruleTrigger/{id}")
	@ApiOperation(value = "Retrieves the rules which use a certain rule trigger and for which the user is authorized.", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the rule trigger!"),
			@ApiResponse(code = 404, message = "Rule trigger or requesting user not found!") })
	public ResponseEntity<List<Rule>> getRulesByRuleTriggerId(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the rule trigger", example = "5c97dc2583aeb6078c5ab672", required = true) String ruleTriggerId) throws EntityNotFoundException {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
		// Make sure the requesting user is allowed to access the rule trigger
		userEntityService.getForIdWithPolicyCheck(ruleTriggerRepository, ruleTriggerId, ACAccessType.READ, accessRequest);

		// Retrieve all rules using the trigger from the database
    	List<Rule> rules = ruleRepository.findAllByTriggerId(ruleTriggerId);
    	
    	// Filter based on owner and policies
    	rules = userEntityService.filterforOwnerAndPolicies(rules, ACAccessType.READ, accessRequest);
    	return ResponseEntity.ok(rules);
	}

	/**
	 * Retrieves all rules that use a given rule action.
	 *
	 * @param ruleActionId the id of the {@link RuleAction}.
	 * @return the list of {@link Rule}s.
	 * @throws EntityNotFoundException 
	 */
	@GetMapping("/rules/by-ruleAction/{id}")
	@ApiOperation(value = "Retrieves the rules which use a certain rule action and for which the user is authorized.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the rule action!"),
			@ApiResponse(code = 404, message = "Rule action or requesting user not found!") })
	public ResponseEntity<List<Rule>> getRulesByRuleActionId(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the rule action", example = "5c97dc2583aeb6078c5ab672", required = true) String ruleActionId) throws EntityNotFoundException {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
		// Make sure the requesting user is allowed to access the rule action
		userEntityService.getForIdWithPolicyCheck(ruleActionRepository, ruleActionId, ACAccessType.READ, accessRequest);
		
		// Retrieve all rules using the action from the database
    	List<Rule> rules = ruleRepository.findAllByActionId(ruleActionId);
    	
    	// Filter based on owner and policies
    	rules = userEntityService.filterforOwnerAndPolicies(rules, ACAccessType.READ, accessRequest);
    	return ResponseEntity.ok(rules);
	}

	/**
	 * Retrieves all components that use a given adapter.
	 *
	 * @param adapterId the id of the {@link Adapter}.
	 * @return the list of {@link Component}s.
	 */
	@GetMapping("/components/by-adapter/{id}")
	@ApiOperation(value = "Retrieves the components which use a certain adapter and for which the user is authorized.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the adapter!"),
			@ApiResponse(code = 404, message = "Adapter or requesting user not found!") })
	public ResponseEntity<List<ComponentExcerpt>> getComponentsByAdapterId(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
		// Retrieve actuator and sensor excerpts from the database
		List<ComponentExcerpt> componentExcerpts = userEntityService.filterforOwnerAndPolicies(() -> actuatorRepository.findAllByAdapterId(adapterId), ACAccessType.READ, accessRequest);
		componentExcerpts.addAll(userEntityService.filterforOwnerAndPolicies(() -> sensorRepository.findAllByAdapterId(adapterId), ACAccessType.READ, accessRequest));
		return ResponseEntity.ok(componentExcerpts);
	}

	/**
	 * Retrieves all components that use a given device.
	 *
	 * @param deviceId the id of the {@link Device}.
	 * @return the list of {@link Component}s.
	 */
	@GetMapping("/components/by-device/{id}")
	@ApiOperation(value = "Retrieves the components which make use of a certain device and for which the user is authorized", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the adapter!"),
			@ApiResponse(code = 404, message = "Adapter or requesting user not found!") })
	public ResponseEntity<List<ComponentExcerpt>> getComponentsByDeviceID(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
		// Retrieve actuator and sensor excerpts from the database
		List<ComponentExcerpt> componentExcerpts = userEntityService.filterforOwnerAndPolicies(() -> actuatorRepository.findAllByAdapterId(deviceId), ACAccessType.READ, accessRequest);
		componentExcerpts.addAll(userEntityService.filterforOwnerAndPolicies(() -> sensorRepository.findAllByAdapterId(deviceId), ACAccessType.READ, accessRequest));
		return ResponseEntity.ok(componentExcerpts);
	}

}
