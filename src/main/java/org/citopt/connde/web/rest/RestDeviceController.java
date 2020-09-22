package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.service.UserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for managing {@link Device}s.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/devices")
@Api(tags = { "Devices" })
public class RestDeviceController {
	
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing device entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Device or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<Device>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable, @Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Retrieve the corresponding devices (includes access-control)
    	List<Device> devices = userEntityService.getPageWithPolicyCheck(deviceRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	try {
    		new File("/Users/jakob/Desktop/temp.txt").delete();
    		File file = new File("/Users/jakob/Desktop/temp.txt");
    		FileWriter fw = new FileWriter(file);
			fw.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(accessRequest));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(pageable, accessRequest)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(devices, selfLink, pageable));
    }
    
    @GetMapping(path = "/{deviceId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the device!"),
    		@ApiResponse(code = 404, message = "Device or requesting user not found!") })
    public ResponseEntity<EntityModel<Device>> one(@PathVariable("deviceId") String deviceId, @ApiParam(value = "Page parameters", required = true) Pageable pageable, @Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Retrieve the corresponding device (includes access-control)
    	Device device = userEntityService.getForIdWithPolicyCheck(deviceRepository, deviceId, ACAccessType.READ, accessRequest);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(device));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 409, message = "Device already exists!") })
    public ResponseEntity<EntityModel<Device>> create(@PathVariable("deviceId") String deviceId, @ApiParam(value = "Page parameters", required = true) Pageable pageable, @RequestBody Device device) {
    	// Check whether a device with the same name already exists in the database
    	userEntityService.requireUniqueName(deviceRepository, device.getName());

    	// Save device in the database
    	Device createdDevice = deviceRepository.save(device);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdDevice));
    }
    
    @DeleteMapping(path = "/{deviceId}")
    @ApiOperation(value = "Deletes an existing device entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the device!"),
    		@ApiResponse(code = 404, message = "Device or requesting user not found!") })
    public ResponseEntity<Void> delete(@PathVariable("deviceId") String deviceId, @Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Delete the device (includes access-control) 
    	userEntityService.deleteWithPolicyCheck(deviceRepository, deviceId, accessRequest);
    	return ResponseEntity.noContent().build();
    }
    
    // =====================================================================================================

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
    
    
//  public static void main(String[] args) throws JsonProcessingException {
//  	List<ACAttribute<? extends Comparable<?>>> attributes = new ArrayList<>();
//  	attributes.add(new ACAttribute<String>(ACDataType.ALPHABETIC, "firstName", "Jakob"));
//  	attributes.add(new ACAttribute<String>(ACDataType.ALPHABETIC, "lastName", "Benz"));
//		ACAccessRequest r = new ACAccessRequest(attributes);
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(r));
//	}
    
}
