package de.ipvs.as.mbp.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.EntityStillInUseException;
import de.ipvs.as.mbp.error.MissingOwnerPrivilegesException;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.util.C;
import de.ipvs.as.mbp.util.Pages;
import de.ipvs.as.mbp.domain.access_control.ACAbstractEffect;
import de.ipvs.as.mbp.domain.access_control.dto.ACEffectRequestDTO;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.service.access_control.ACEffectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(RestConfiguration.BASE_PATH + "/policy-effects")
@Api(tags = {"Access-Control Policy Effects"})
public class RestACEffectController {
	
	@Autowired
	private ACEffectService effectService;
    
	@Autowired
	private UserService userService;
	

	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing effects owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ACAbstractEffect>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		User user = userService.getLoggedInUser();
    	return ResponseEntity.ok(effectsToPagedModel(effectService.getAllForOwner(user.getId(), pageable), pageable));
    }
    
    @GetMapping(path = "/{effectId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing effect identified by its id if available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the effect!"), @ApiResponse(code = 404, message = "Effect or requesting user not found!") })
    public ResponseEntity<EntityModel<ACAbstractEffect>> one(@PathVariable("effectId") String effectId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingOwnerPrivilegesException {
    	User user = userService.getLoggedInUser();
    	return ResponseEntity.ok(effectToEntityModel(effectService.getForIdAndOwner(effectId, user.getId())));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new effect.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 201, message = "Effect successfully created!"), @ApiResponse(code = 404, message = "Requesting user not found!"), @ApiResponse(code = 409, message = "effect name already exists!") })
    public <T> ResponseEntity<EntityModel<ACAbstractEffect>> create(@Valid @RequestBody ACEffectRequestDTO requestDto, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityAlreadyExistsException {
    	User user = userService.getLoggedInUser();
    	return ResponseEntity.status(HttpStatus.CREATED).body(effectToEntityModel(effectService.create(requestDto, user.getId())));
    }
    
    @DeleteMapping(path = "/{effectId}")
    @ApiOperation(value = "Deletes an existing effect.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 204, message = "effect successfully deleted!"), @ApiResponse(code = 404, message = "Requesting user or effect not found!") })
    public ResponseEntity<Void> delete(@PathVariable("effectId") String effectId) throws EntityNotFoundException, EntityStillInUseException, MissingOwnerPrivilegesException {
    	User user = userService.getLoggedInUser();
    	effectService.delete(effectId, user.getId());
    	return ResponseEntity.noContent().build();
    }
    
    private EntityModel<ACAbstractEffect> effectToEntityModel(ACAbstractEffect effect) {
    	return EntityModel.of(effect, linkTo(getClass()).slash(effect.getId()).withSelfRel());
    }
    
    private PagedModel<EntityModel<ACAbstractEffect>> effectsToPagedModel(List<ACAbstractEffect> effects, Pageable pageable) {
    	// Extract requested page from all effects
    	List<ACAbstractEffect> page = Pages.page(effects, pageable);
    	
    	// Add self link to every effect
    	List<EntityModel<ACAbstractEffect>> effectEntityModels = page.stream().map(this::effectToEntityModel).collect(Collectors.toList());
    	
    	// Create self link
    	Link link = linkTo(methodOn(getClass()).all(pageable)).withSelfRel();
    	
    	// Create and return paged model
    	return PagedModel.of(effectEntityModels, Pages.metaDataOf(pageable, effectEntityModels.size()), C.listOf(link));
    }
    
}
