package de.ipvs.as.mbp.web.rest;

import java.util.List;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.repository.projection.OperatorExcerpt;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.repository.projection.ComponentExcerpt;
import de.ipvs.as.mbp.service.UserEntityService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller that exposes methods that allow the filtering for certain
 * components, e.g. by operator/device id.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Component filter"})
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

	@Autowired
	private OperatorRepository operatorRepository;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    /**
     * Retrieves all rules that use a given rule trigger.
     *
     * @param ruleTriggerId the id of the {@link RuleTrigger}.
     * @return the list of {@link Rule}s.
     * @throws EntityNotFoundException
     * @throws MissingPermissionException
     */
    @GetMapping("/rules/by-ruleTrigger/{id}")
    @ApiOperation(value = "Retrieves the rules which use a certain rule trigger and for which the user is authorized.", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the rule trigger!"),
            @ApiResponse(code = 404, message = "Rule trigger or requesting user not found!")})
    public ResponseEntity<List<Rule>> getRulesByRuleTriggerId(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the rule trigger", example = "5c97dc2583aeb6078c5ab672", required = true) String ruleTriggerId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Make sure the requesting user is allowed to access the rule trigger
        userEntityService.getForIdWithAccessControlCheck(ruleTriggerRepository, ruleTriggerId, ACAccessType.READ, accessRequest);

        // Retrieve all rules using the trigger from the database
        List<Rule> rules = ruleRepository.findAllByTriggerId(ruleTriggerId);

        // Filter based on owner and policies
        rules = userEntityService.filterForAdminOwnerAndPolicies(rules, ACAccessType.READ, accessRequest);
        return ResponseEntity.ok(rules);
    }

    /**
     * Retrieves all rules that use a given rule action.
     *
     * @param ruleActionId the id of the {@link RuleAction}.
     * @return the list of {@link Rule}s.
     * @throws EntityNotFoundException
     * @throws MissingPermissionException
     */
    @GetMapping("/rules/by-ruleAction/{id}")
    @ApiOperation(value = "Retrieves the rules which use a certain rule action and for which the user is authorized.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the rule action!"),
            @ApiResponse(code = 404, message = "Rule action or requesting user not found!")})
    public ResponseEntity<List<Rule>> getRulesByRuleActionId(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the rule action", example = "5c97dc2583aeb6078c5ab672", required = true) String ruleActionId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Make sure the requesting user is allowed to access the rule action
        userEntityService.getForIdWithAccessControlCheck(ruleActionRepository, ruleActionId, ACAccessType.READ, accessRequest);

        // Retrieve all rules using the action from the database
        List<Rule> rules = ruleRepository.findAllByActionId(ruleActionId);

        // Filter based on owner and policies
        rules = userEntityService.filterForAdminOwnerAndPolicies(rules, ACAccessType.READ, accessRequest);
        return ResponseEntity.ok(rules);
    }

    /**
     * Retrieves all components that use a given operator.
     *
     * @param operatorId the id of the {@link Operator}.
     * @return the list of {@link Component}s.
     */
    @GetMapping("/components/by-operator/{id}")
    @ApiOperation(value = "Retrieves the components which use a certain operator and for which the user is authorized.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the operator!"),
            @ApiResponse(code = 404, message = "Operator or requesting user not found!")})
    public ResponseEntity<List<ComponentExcerpt>> getComponentsByOperatorId(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the operator", example = "5c97dc2583aeb6078c5ab672", required = true) String operatorId) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve actuator and sensor excerpts from the database
        List<ComponentExcerpt> componentExcerpts = userEntityService.filterForAdminOwnerAndPolicies(() -> actuatorRepository.findAllByOperatorId(operatorId), ACAccessType.READ, accessRequest);
        componentExcerpts.addAll(userEntityService.filterForAdminOwnerAndPolicies(() -> sensorRepository.findAllByOperatorId(operatorId), ACAccessType.READ, accessRequest));
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
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the operator!"),
            @ApiResponse(code = 404, message = "Operator or requesting user not found!")})
    public ResponseEntity<List<ComponentExcerpt>> getComponentsByDeviceID(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve actuator and sensor excerpts from the database
        List<ComponentExcerpt> componentExcerpts = userEntityService.filterForAdminOwnerAndPolicies(() -> actuatorRepository.findAllByOperatorId(deviceId), ACAccessType.READ, accessRequest);
        componentExcerpts.addAll(userEntityService.filterForAdminOwnerAndPolicies(() -> sensorRepository.findAllByOperatorId(deviceId), ACAccessType.READ, accessRequest));
        return ResponseEntity.ok(componentExcerpts);
    }

	/**
	 * Retrieves all operators that use a given data model.
	 *
	 * @param dataModelId the id of the {@link de.ipvs.as.mbp.domain.data_model.DataModel}.
	 * @return the list of {@link Operator}s.
	 */
	@GetMapping("/components/by-data-model/{id}")
	@ApiOperation(value = "Retrieves the operators which use a certain data model and for which the user is authorized.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the data model!"),
			@ApiResponse(code = 404, message = "Data model or requesting user not found!") })
	public ResponseEntity<List<OperatorExcerpt>> getOperatorsByDataModelId(
			@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the data model", example = "5c97dc2583aeb6078c5ab672", required = true) String dataModelId) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

		// Retrieve operator excerpts from the database
		List<OperatorExcerpt> operatorExcerpts = userEntityService.filterForAdminOwnerAndPolicies(() -> operatorRepository.findAllByDataModelId(dataModelId), ACAccessType.READ, accessRequest);
		return ResponseEntity.ok(operatorExcerpts);
	}


    /**
     * Retrieves all tests that use a given sensor.
     *
     * @param sensorId the id of the sensor
     * @return the list of tests
     */
    @GetMapping("/test/by-sensor/{id}")
    @ApiOperation(value = "Retrieves the tests which use a certain sensor and for which the user is authorized.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the sensor!"),
            @ApiResponse(code = 404, message = "Operator or requesting user not found!")})
    public ResponseEntity<List<TestDetails>> getTestsBySensorId(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId) throws MissingPermissionException, EntityNotFoundException {
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
        //Retrieve sensor from repository with access control check (to check permissions)
        userEntityService.getForIdWithAccessControlCheck(sensorRepository, sensorId, ACAccessType.READ, accessRequest);

        //Retrieve all test details that use the given sensor
        List<TestDetails> affectedTestDetails = userEntityService.filterForAdminOwnerAndPolicies(() -> testDetailsRepository.findAllBySensorId(sensorId), ACAccessType.READ, accessRequest);

        //Return result
        return ResponseEntity.ok(affectedTestDetails);
    }
}
