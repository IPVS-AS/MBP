package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.key.KeyPair;
import org.citopt.connde.domain.key.KeyPairValidator;
import org.citopt.connde.repository.KeyPairRepository;
import org.citopt.connde.service.UserService;
import org.citopt.connde.service.crypto.SSHKeyPairGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        //TODO accept passphrase and comment as parameters
        String passphrase = "";
        String comment = "";

        //Sanity check for passphrase
        if (passphrase == null) {
            passphrase = "";
        }

        //Sanity check for comment
        if (comment == null) {
            comment = "";
        }

        //Check for name
        if((name == null) || name.isEmpty() || (keyPairRepository.findByName(name) != null)){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        //Generate key pair
        KeyPair keyPair = keyPairGenerator.generateKeyPair(passphrase, comment);

        //Add name to key pair
        keyPair.setName(name);

        //Set owner of key pair
        keyPair.setOwner(userService.getUserWithAuthorities());

        //Store key pair in repository
        keyPairRepository.insert(keyPair);

        //Respond with generated key pair
        return new ResponseEntity<>(keyPair, HttpStatus.CREATED);
    }
}
