package org.citopt.connde.web.rest;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.constants.Constants;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user.UserAuthData;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.InvalidPasswordException;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.repository.projection.UserExcerpt;
import org.citopt.connde.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for managing users.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/users")
@Api(tags = {"Users"})
public class RestUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieves all existing users.", notes = "Requires admin privileges.", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access users (admin privileges required)!"),
    		@ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<List<User>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws URISyntaxException, EntityNotFoundException {
    	// Check whether the requesting user has admin privileges
    	userService.requireAdmin();
    	
    	// Retrieve users from database
    	return ResponseEntity.ok(userService.getAll(pageable).toList());
    }

    @GetMapping("/{username:" + Constants.USERNAME_REGEX + "}")
    @ApiOperation(value = "Returns an user entity by its username", notes = "Requires admin privileges.", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    	@ApiResponse(code = 401, message = "Not authorized to access users (admin privileges required)!"),
    		@ApiResponse(code = 404, message = "User or requesting user not found!") })
    public ResponseEntity<User> oneForUsername(@PathVariable @ApiParam(value = "Username of the user", example = "MyUser", required = true) String username) throws EntityNotFoundException {
    	// Check whether the requesting user has admin privileges
    	userService.requireAdmin();
    	
    	// Retrieve user from database
    	return ResponseEntity.ok(userService.getForUsername(username));
    }
    
	@PostMapping("/authenticate")
	@ApiOperation(value = "Authenticates a user", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 403, message = "Invalid password!"),
			@ApiResponse(code = 404, message = "User or requesting user not found!") })
	public ResponseEntity<User> authenticate(@RequestBody @ApiParam(value = "Authentication data", required = true) UserAuthData authData) throws InvalidPasswordException {
		// Retrieve user from database
		User user = userService.getForUsername(authData.getUsername().toLowerCase(Locale.ENGLISH));

		// Check password
		if (userService.checkPassword(user.getId(), authData.getPassword())) {
			return ResponseEntity.ok(user);
		} else {
			throw new InvalidPasswordException();
		}
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Creates a new user.", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(code = 201, message = "User successfully created!"),
    		@ApiResponse(code = 409, message = "Username is already in use!") })
    public ResponseEntity<User> create(@Valid @RequestBody @ApiParam(value = "The user to create", required = true) User user) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(user));
	}
	
	@PutMapping("/{userId}")
    @ApiOperation(value = "Updates an existing user.", notes = "Requires admin privileges.", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "User successfully updated!"),
    		@ApiResponse(code = 401, message = "Not authorized to access users (admin privileges required)!"),
    		@ApiResponse(code = 404, message = "User to update or requesting user not found!"),
    		@ApiResponse(code = 409, message = "Username is already in use.") })
    public ResponseEntity<User> update(@PathVariable("userId") @ApiParam(value = "The id of the user", example = "5f218c7822424828a8275037") String userId,
    		@RequestBody @ApiParam(value = "The user to update", required = true) User user) throws EntityNotFoundException {
		// Check whether the requesting user has admin privileges
		userService.requireAdmin();
		
		// Update the user in the database
		return ResponseEntity.ok(userService.update(userId, user));
    }
    
	@DeleteMapping("/{username:" + Constants.USERNAME_REGEX + "}")
	@ApiOperation(value = "Deletes an existing user.", notes = "Requires admin privileges.")
	@ApiResponses({ @ApiResponse(code = 204, message = "User successfully deleted!"),
			@ApiResponse(code = 401, message = "Not authorized to access users (admin privileges required)!"),
			@ApiResponse(code = 404, message = "User to delete or requesting user not found!") })
	public ResponseEntity<Void> delete(@PathVariable("username") @ApiParam(value = "Username of the user to delete", example = "MyUser", required = true) String username) throws EntityNotFoundException {
		// Check whether the requesting user has admin privileges
		userService.requireAdmin();
		
		userService.deleteUser(username);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/searchByUsername")
    @ApiOperation(value = "Searches and returns all users whose username contain a given query string.", notes = "Returns an empty list in case the query is empty.", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Query result.") })
    public ResponseEntity<List<UserExcerpt>> searchByUsername(@RequestParam("query") @ApiParam(value = "Query string", example = "admin", required = true) String query) {
		// If query is empty -> return an empty result list, otherwise search for matching users
        return query.isEmpty() ? ResponseEntity.ok(new ArrayList<>()) : ResponseEntity.ok(userRepository.findByUsernameContains(query.trim()));
    }
    
}
