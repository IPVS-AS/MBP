package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.security.SecurityUtils;
import org.citopt.connde.service.access_control.ACPolicyEvaluationService;
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
@RequestMapping(RestConfiguration.BASE_PATH + "/device")
@Api(tags = {"Devices"})
public class RestDeviceController {
	
//	private static final Logger LOGGER = Logger.getLogger(RestDeviceController.class.getName());

    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ACPolicyEvaluationService policyEvaluationService;
    
    
	@GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing device entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the entity!") })
    public ResponseEntity<PagedModel<EntityModel<Device>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable, @Valid @RequestBody ACAccessRequest accessRequest) {
    	// Retrieve requesting user from the database (if it can be identified and is present)
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Retrieve all devices owned by the user (no paging yet)
    	List<Device> devices = deviceRepository.findByOwner(user.getId(), Pages.ALL);
    	
//    	// Filter policies with non-matching access-types -> less policies to actually evaluate (no paging yet)
//    	devices = devices.stream().filter(d -> d.getAccessControlPolicies().stream().anyMatch(p -> p.getAccessTypes().contains(ACAccessType.READ))).collect(Collectors.toList());
    	
//    	// Filter devices the user is not permitted to access (still no paging yet)
//    	devices = devices.stream().filter(d -> {
//    		ACAccess access = new ACAccess(ACAccessType.READ, user, d);
//    		return d.getOwner().getId().equals(user.getId())
//    				|| d.getAccessControlPolicies().stream().anyMatch(p -> policyEvaluationService.evaluate(p, access, accessRequest));
//    	}).collect(Collectors.toList());
    	
    	// Extract requested page from all devices
    	List<Device> page = Pages.page(devices, pageable);
    	
    	// Add self link to every device
    	List<EntityModel<Device>> deviceEntityModels = page.stream().map(this::deviceToEntityModel).collect(Collectors.toList());
    	
    	// Create self link
    	Link link = linkTo(methodOn(getClass()).all(pageable, accessRequest)).withSelfRel();
    	
    	return ResponseEntity.ok(new PagedModel<>(deviceEntityModels, Pages.metaDataOf(pageable, deviceEntityModels.size()), C.listOf(link)));
//    	return ResponseEntity.ok(PagedModel.of(deviceEntityModels, Pages.metaDataOf(pageable, devices.size()), C.listOf(link)));
    }
    
    @GetMapping(path = "/{deviceId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the device!"), @ApiResponse(code = 404, message = "Device not found!") })
    public ResponseEntity<EntityModel<Device>> one(@PathVariable("deviceId") String deviceId, @ApiParam(value = "Page parameters", required = true) Pageable pageable, @Valid ACAccessRequest accessRequest) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Retrieve the requested device from the database (if it exists)
    	Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
    	if (!deviceOptional.isPresent()) {
    		return ResponseEntity.notFound().build();
    	}
    	Device device = deviceOptional.get();
    	
//    	// Check whether the requesting user is authorized to access the device
//    	ACAccess access = new ACAccess(ACAccessType.READ, user, device);
//    	if (!device.getOwner().getId().equals(user.getId()) && !device.getAccessControlPolicies().stream().anyMatch(p -> policyEvaluationService.evaluate(p, access, accessRequest))) {
//    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    	}
    			
    	// Add self link to device
    	EntityModel<Device> deviceEntityModel = deviceToEntityModel(device);
    	
    	return ResponseEntity.ok(deviceEntityModel);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Device created successfully!"), @ApiResponse(code = 409, message = "Device already exists!") })
    public ResponseEntity<EntityModel<Device>> create(@PathVariable("deviceId") String deviceId, @ApiParam(value = "Page parameters", required = true) Pageable pageable, @Valid ACAccessRequest accessRequest, @RequestBody Device device) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
//    	// Retrieve the requested device from the database (if it exists)
//    	Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
//    	if (!deviceOptional.isPresent()) {
//    		return ResponseEntity.notFound().build();
//    	}
//    	Device device = deviceOptional.get();
//    	
//    	// Check whether the requesting user is authorized to access the device
//    	ACAccess access = new ACAccess(ACAccessType.READ, user, device);
//    	if (!device.getOwner().getId().equals(user.getId()) && !device.getAccessControlPolicies().stream().anyMatch(p -> policyEvaluationService.evaluate(p, access, accessRequest))) {
//    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    	}
    			
    	// Add self link to device
    	EntityModel<Device> deviceEntityModel = deviceToEntityModel(device);
    	
    	System.out.println(device.getName());
    	System.err.println(accessRequest.getContext().size());
    	
    	
    	return ResponseEntity.ok(deviceEntityModel);
    }
    
//    @PostMapping(path = "/test1")
//    public ResponseEntity<EntityModel<Device>> test1() {
//    	Device device = deviceRepository.findById("5f30fe9f8bdd050a72e9f636").get();
//    	User user = userRepository.findOneByUsername("admin").get();
//		
//		List<ACAccessType> acs = new ArrayList<>();
//		acs.add(ACAccessType.READ);
//		IACCondition c = new ACSimpleCondition<String>("C1", ACArgumentFunction.EQUALS, new ACConditionSimpleAttributeArgument<>(ACEntityType.REQUESTING_ENTITY, "a1"), new ACConditionSimpleValueArgument<String>("v1"));
//		List<IACEffect<?>> effects = new ArrayList<>();
////		effects.add(new ACDoubleAccuracyEffect("Effect 1", 10, 5));
//		ACPolicy p = new ACPolicy("P1", 1, acs, c, effects, user);
//		p = policyRepository.save(p);
//		device.getAccessControlPolicies().add(p);
//		device = deviceRepository.save(device);
//    	
//    	return ResponseEntity.ok(deviceToEntityModel(device));
//    }
    
    private EntityModel<Device> deviceToEntityModel(Device device) {
    	return new EntityModel<Device>(device, linkTo(getClass()).slash(device.getId()).withSelfRel());
//    	return EntityModel.of(device).add(linkTo(getClass()).slash(device.getId()).withSelfRel());
    }

    
//  public static void main(String[] args) throws JsonProcessingException {
//  	List<ACAttribute<? extends Comparable<?>>> attributes = new ArrayList<>();
//  	attributes.add(new ACAttribute<String>(ACDataType.ALPHABETIC, "firstName", "Jakob"));
//  	attributes.add(new ACAttribute<String>(ACDataType.ALPHABETIC, "lastName", "Benz"));
//		ACAccessRequest r = new ACAccessRequest(attributes);
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(r));
//	}
    
}
