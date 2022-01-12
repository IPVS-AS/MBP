package de.ipvs.as.mbp.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingOwnerPrivilegesException;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.util.C;
import de.ipvs.as.mbp.util.Pages;
import org.assertj.core.util.Arrays;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.access_control.dto.ACPolicyRequestDTO;
import de.ipvs.as.mbp.domain.access_control.dto.ACPolicyResponseDTO;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.service.access_control.ACPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for managing {@link Device devices}.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(Constants.BASE_PATH + "/policies")
@Api(tags = {"Access-Control Policies"})
public class RestACPolicyController {
	
	@Autowired
	private ACPolicyService policyService;
	
    @Autowired
    private UserService userService;
    
    @PreAuthorize("isFullyAuthenticated()")
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing policies owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ACPolicyResponseDTO>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingOwnerPrivilegesException {
		User user = userService.getLoggedInUser();
    	return ResponseEntity.ok(policiesToPagedModel(policyService.policiesToResponseDto(policyService.getAllForOwner(user.getId(), pageable), user.getId()), pageable));
    }

	@PreAuthorize("isFullyAuthenticated()")
	@GetMapping(path = "/byCondition/{conditionId}", produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing policies owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ACPolicyResponseDTO>>> byCondition(@PathVariable("conditionId") String conditionId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingOwnerPrivilegesException {
		User user = userService.getLoggedInUser();
    	return ResponseEntity.ok(policiesToPagedModel(policyService.policiesToResponseDto(policyService.getAllForOwnerAndCondition(user.getId(), conditionId, pageable), user.getId()), pageable));
    }
	
	@GetMapping(path = "/byEffect/{effectId}", produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing policies owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ACPolicyResponseDTO>>> byEffect(@PathVariable("effectId") String effectId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingOwnerPrivilegesException {
		User user = userService.getLoggedInUser();
		return ResponseEntity.ok(policiesToPagedModel(policyService.policiesToResponseDto(policyService.getAllForOwnerAndEffect(user.getId(), effectId, pageable), user.getId()), pageable));
    }

	@PreAuthorize("isFullyAuthenticated()")
    @GetMapping(path = "/{policyId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing policy identified by its id if available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the policy!"), @ApiResponse(code = 404, message = "Policy or requesting user not found!") })
    public ResponseEntity<EntityModel<ACPolicyResponseDTO>> one(@PathVariable("policyId") String policyId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingOwnerPrivilegesException {
    	User user = userService.getLoggedInUser();
    	return ResponseEntity.ok(policyToEntityModel(policyService.policyToResponseDto(policyService.getForIdAndOwner(policyId, user.getId()), user.getId())));
    }

	@PreAuthorize("isFullyAuthenticated()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new policy.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 201, message = "Policy successfully created!"), @ApiResponse(code = 404, message = "Requesting user, condition, or effect not found!"), @ApiResponse(code = 409, message = "Policy name already exists!") })
    public ResponseEntity<EntityModel<ACPolicyResponseDTO>> create(@Valid @RequestBody ACPolicyRequestDTO requestDto, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, EntityAlreadyExistsException, MissingOwnerPrivilegesException {
    	User user = userService.getLoggedInUser();
    	return ResponseEntity.status(HttpStatus.CREATED).body(policyToEntityModel(policyService.policyToResponseDto(policyService.create(requestDto, user.getId()), user.getId()))); 
    }

	@PreAuthorize("isFullyAuthenticated()")
    @DeleteMapping(path = "/{policyId}")
    @ApiOperation(value = "Deletes an existing policy.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 204, message = "Policy successfully deleted!"), @ApiResponse(code = 404, message = "Requesting user or policy not found!") })
    public ResponseEntity<Void> delete(@PathVariable("policyId") String policyId) throws EntityNotFoundException, MissingOwnerPrivilegesException {
    	User user = userService.getLoggedInUser();
    	policyService.delete(policyId, user.getId());
    	return ResponseEntity.noContent().build();
    }

	@PreAuthorize("isFullyAuthenticated()")
    @GetMapping(path = "/accessTypes", produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing policy access types.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!") })
    public ResponseEntity<List<String>> accessTypes() {
    	return ResponseEntity.ok(Arrays.asList(ACAccessType.values()).stream().map(Object::toString).collect(Collectors.toList()));
    }
    
    private EntityModel<ACPolicyResponseDTO> policyToEntityModel(ACPolicyResponseDTO policy) {
    	return EntityModel.of(policy, linkTo(getClass()).slash(policy.getId()).withSelfRel());
    }
    
    private PagedModel<EntityModel<ACPolicyResponseDTO>> policiesToPagedModel(List<ACPolicyResponseDTO> policies, Pageable pageable) throws EntityNotFoundException, MissingOwnerPrivilegesException {
    	// Extract requested page from all policies
    	List<ACPolicyResponseDTO> page = Pages.page(policies, pageable);
    	
    	// Add self link to every policy
    	List<EntityModel<ACPolicyResponseDTO>> policyEntityModels = page.stream().map(this::policyToEntityModel).collect(Collectors.toList());
    	
    	// Create self link
    	Link link = linkTo(methodOn(getClass()).all(pageable)).withSelfRel();
    	
    	// Create and return paged model
    	return PagedModel.of(policyEntityModels, Pages.metaDataOf(pageable, policyEntityModels.size()), C.listOf(link));
    }

}
