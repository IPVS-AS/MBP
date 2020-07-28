package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.key_pair.KeyPair;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.KeyPairRepository;
import org.citopt.connde.service.UserService;
import org.citopt.connde.service.crypto.SSHKeyPairGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for functions related to RSA key pairs that may be used for establishing SSH connections.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Key pairs"}, description = "Functions related to RSA key pairs for SSH")
public class RestKeyPairController {

    @Autowired
    private KeyPairRepository keyPairRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SSHKeyPairGenerator keyPairGenerator;

    /**
     * Generates and registers a new SSH key pair with a given name.
     *
     * @return A response entity containing the generated key pair
     */
    @PostMapping("/key-pairs/generate")
    @ApiOperation(value = "Generates and registers a new SSH key pair with a given name.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 400, message = "Invalid name or key pair with this name already exists")})
    public ResponseEntity<KeyPair> generateKeyPair(@RequestBody @ApiParam(value = "Name of the new key par") String name) {
        //Check for name
        if ((name == null) || name.isEmpty() || (keyPairRepository.findByName(name) != null)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        //Generate key pair
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //Add name to key pair
        keyPair.setName(name);

        //Set owner of key pair
        keyPair.setOwner(userService.getUserWithAuthorities());

        //Store key pair in repository
        keyPairRepository.insert(keyPair);

        //Respond with generated key pair
        return new ResponseEntity<>(keyPair, HttpStatus.CREATED);
    }

    /**
     * Returns a list of devices that make use of a certain key pair.
     *
     * @param keyPairId The id of the key pair for which using devices should be found
     * @return A list of all devices that make use of the key pair
     */
    @GetMapping("/devices/by-key/{id}")
    @ApiOperation(value = "Retrieves the devices which make use of a certain key pair and for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Key pair not found or not authorized to access the key pair")})
    public ResponseEntity<List<Device>> getDevicesByKeyPair(@PathVariable(value = "id") @ApiParam(value = "ID of the key pair", example = "5c97dc2583aeb6078c5ab672", required = true) String keyPairId) {
        // Get key pair object by its ID
        KeyPair keyPair = keyPairRepository.findById(keyPairId).get();

        //Check if key pair could be found
        if (keyPair == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get all devices from the repository
        List<Device> devices = deviceRepository.findAll();

        //List of using devices
        List<Device> dependentDevices = new ArrayList<>();

        //Iterate over all devices
        for (Device device : devices) {
            //Check if device uses a key pair
            if (!device.hasRSAKey()) {
                continue;
            }

            //Check if current device uses the key pair of interest
            if (device.getKeyPair().getId().equals(keyPair.getId())) {
                dependentDevices.add(device);
            }
        }

        return new ResponseEntity<>(dependentDevices, HttpStatus.OK);
    }
}
