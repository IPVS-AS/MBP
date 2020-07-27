package org.citopt.connde.web.rest;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping(RestConfiguration.BASE_PATH + "/device")
@Api(tags = {"Devices"})
public class RestDeviceController {
	
	private static final Logger LOGGER = Logger.getLogger(RestDeviceController.class.getName());

    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing device entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<Page<Device>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	Optional<User> user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername());
    	
    	
    	
    	
    	
    	return ResponseEntity.ok(deviceRepository.findByOwnerOrPolicyAccessTypeMatchAll(ownerId, accessTypes, pageable));
    }
    
//    @GetMapping(path = "/{deviceName}", produces = "application/hal+json")
//    @ApiOperation(value = "Retrieves a device entity by its name.", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
//    public ResponseEntity<String> oneForName(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
//    	return ResponseEntity.ok("Test 1");
//    }
    
    // - - - TODO: TO BE OUTSOURCED
    
    private List<UserEntity> filterUserEntitiesTODONamingTODO(List<UserEntity> userEntities) {
    	// TODO: Implement
    	return userEntities;
    }

}
