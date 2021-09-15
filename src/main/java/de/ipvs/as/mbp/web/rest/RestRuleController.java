package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.domain.rules.RuleDTO;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.RuleActionRepository;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.repository.RuleTriggerRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.rules.RuleEngine;
import de.ipvs.as.mbp.service.rules.RuleExecutor;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller that exposes methods for the purpose of managing rules.
 */
@RestController()
@RequestMapping(Constants.BASE_PATH + "/rules")
@Api(tags = {"Rules"})
public class RestRuleController {

    @Autowired
    private RuleTriggerRepository ruleTriggerRepository;

    @Autowired
    private RuleActionRepository ruleActionRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private RuleExecutor ruleExecutor;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing rule entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Rule or requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<Rule>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding rules (includes access-control)
        List<Rule> rules = userEntityService.getPageWithAccessControlCheck(ruleRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(rules, selfLink, pageable));
    }

    @GetMapping(path = "/{ruleId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing rule entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the rule!"),
            @ApiResponse(code = 404, message = "Rule or requesting user not found!")})
    public ResponseEntity<EntityModel<Rule>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("ruleId") String ruleId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the corresponding rule (includes access-control)
        Rule rule = userEntityService.getForIdWithAccessControlCheck(ruleRepository, ruleId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(userEntityService.entityToEntityModel(rule));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing rule entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 409, message = "Rule already exists!")})
    public ResponseEntity<EntityModel<Rule>> create(
            @ApiParam(value = "Page parameters", required = true) Pageable pageable,
            @RequestBody RuleDTO ruleDTO) throws EntityAlreadyExistsException, EntityNotFoundException {
        // Create rule from request DTO
        Rule rule = (Rule) new Rule()
                .setName(ruleDTO.getName())
                .setTrigger(ruleDTO.getTrigger() == null ? null : userEntityService.getForId(ruleTriggerRepository, ruleDTO.getTrigger()))
                .setAccessControlPolicyIds(ruleDTO.getAccessControlPolicyIds());

        //Check whether rule actions types were provided
        if (ruleDTO.getActions() == null) {
            rule.setActions(new ArrayList<>());
        } else {
            //Resolve rule actions
            List<RuleAction> ruleActions = new ArrayList<>();
            for (String ruleActionId : ruleDTO.getActions()) {
                ruleActions.add(userEntityService.getForId(ruleActionRepository, ruleActionId));
            }
            rule.setActions(ruleActions);
        }

        // Save rule in the database
        Rule createdRule = userEntityService.create(ruleRepository, rule);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdRule));
    }

    @DeleteMapping(path = "/{ruleId}")
    @ApiOperation(value = "Deletes an existing rule entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the rule!"),
            @ApiResponse(code = 404, message = "Rule or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("ruleId") String ruleId) throws EntityNotFoundException, MissingPermissionException {
        // Delete the rule (includes access-control)
        userEntityService.deleteWithAccessControlCheck(ruleRepository, ruleId, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/enable/{id}")
    public ResponseEntity<Void> enableRule(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") String ruleId) throws MissingPermissionException, EntityNotFoundException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding rule (includes access-control)
        Rule rule = userEntityService.getForIdWithAccessControlCheck(ruleRepository, ruleId, ACAccessType.READ, accessRequest);

        // Check permission
        userEntityService.requirePermission(rule, ACAccessType.START, accessRequest);

        // Enable rule if necessary
        if (!rule.isEnabled() && !ruleEngine.enableRule(rule)) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Rule could not be enabled. Do all required components still exist?");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/disable/{id}")
    public ResponseEntity<Void> disableRule(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") String ruleId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding rule (includes access-control)
        Rule rule = userEntityService.getForIdWithAccessControlCheck(ruleRepository, ruleId, ACAccessType.READ, accessRequest);

        // Check permission
        userEntityService.requirePermission(rule, ACAccessType.STOP, accessRequest);

        // Enable rule if necessary
        if (rule.isEnabled()) {
            ruleEngine.disableRule(rule);
        }

        return ResponseEntity.ok().build();
    }
}
