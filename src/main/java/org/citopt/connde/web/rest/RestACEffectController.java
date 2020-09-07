package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAbstractEffect;
import org.citopt.connde.domain.access_control.dto.ACEffectRequestDTO;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.ACEffectRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.security.SecurityUtils;
import org.citopt.connde.util.C;
import org.citopt.connde.util.Pages;
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
import org.springframework.web.server.ResponseStatusException;

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
	private ACEffectRepository effectRepository;
	
	@Autowired
	private UserRepository userRepository;
	

	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing effects owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ACAbstractEffect>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	// Retrieve requesting user from the database (if it can be identified and is present)
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Retrieve all effects owned by the user (no paging yet)
    	List<ACAbstractEffect> effectsOwnedByUser = effectRepository.findByOwner(user.getId(), Pages.ALL);
    	
    	// Extract requested page from all effects
    	List<ACAbstractEffect> page = Pages.page(effectsOwnedByUser, pageable);
    	
    	// Add self link to every effect
    	List<EntityModel<ACAbstractEffect>> effectEntityModels = page.stream().map(this::effectToEntityModel).collect(Collectors.toList());
    	
    	// Create self link
    	Link link = linkTo(methodOn(getClass()).all(pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(new PagedModel<>(effectEntityModels, Pages.metaDataOf(pageable, effectEntityModels.size()), C.listOf(link)));
    }
    
    @GetMapping(path = "/{effectId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing effect identified by its id if available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the effect!"), @ApiResponse(code = 404, message = "Effect or requesting user not found!") })
    public ResponseEntity<EntityModel<ACAbstractEffect>> one(@PathVariable("effectId") String effectId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Retrieve the requested effect from the database (if it exists)
    	Optional<ACAbstractEffect> effectOptional = effectRepository.findById(effectId);
    	if (!effectOptional.isPresent()) {
    		return ResponseEntity.notFound().build();
    	}
    	ACAbstractEffect effect = effectOptional.get();
    	
    	// Check whether the requesting user is the owner of the effect
    	if (!effect.getOwner().getId().equals(user.getId())) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    			
    	// Add self link to effect
    	EntityModel<ACAbstractEffect> effectEntityModel = effectToEntityModel(effect);
    	
    	return ResponseEntity.ok(effectEntityModel);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new effect.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 201, message = "Effect successfully created!"), @ApiResponse(code = 404, message = "Requesting user not found!"), @ApiResponse(code = 409, message = "effect name already exists!") })
    public <T> ResponseEntity<EntityModel<ACAbstractEffect>> create(@Valid @RequestBody ACEffectRequestDTO requestDto, @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Check whether a policy with the same name exists already
    	if (effectRepository.existsByName(requestDto.getName())) {
    		return ResponseEntity.status(HttpStatus.CONFLICT).build();
    	}    	
    	
    	// Create new effect and save it in the database
    	ACAbstractEffect effect = null;
		try {
			effect = (ACAbstractEffect) ACAbstractEffect.forType(requestDto.getType())
					.setParameters(requestDto.getParameters())
					.setName(requestDto.getName())
					.setDescription(requestDto.getDescription())
					.setOwner(user);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    	effect = effectRepository.save(effect);
    	
    	// Add self link to policy
    	EntityModel<ACAbstractEffect> effectEntityModel = effectToEntityModel(effect);
    	
    	return ResponseEntity.status(HttpStatus.CREATED).body(effectEntityModel);
    }
    
    @DeleteMapping(path = "/{effectId}")
    @ApiOperation(value = "Deletes an existing effect.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 204, message = "effect successfully deleted!"), @ApiResponse(code = 404, message = "Requesting user or effect not found!") })
    public ResponseEntity<Void> delete(@PathVariable("effectId") String effectId) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));

    	// Retrieve the effect to delete from the database (if it exists)
    	Optional<ACAbstractEffect> effectOptional = effectRepository.findById(effectId);
    	if (!effectOptional.isPresent()) {
    		return ResponseEntity.notFound().build();
    	}
    	ACAbstractEffect effect = effectOptional.get();
    	
    	// Check whether the requesting user is the owner of the effect
    	if (!effect.getOwner().getId().equals(user.getId())) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	
    	// Actually delete effect in the database
    	effectRepository.deleteById(effectId);
    	
    	return ResponseEntity.noContent().build();
    }
    
    private EntityModel<ACAbstractEffect> effectToEntityModel(ACAbstractEffect effect) {
    	return new EntityModel<ACAbstractEffect>(effect, linkTo(getClass()).slash(effect.getId()).withSelfRel());
    }
    
}
