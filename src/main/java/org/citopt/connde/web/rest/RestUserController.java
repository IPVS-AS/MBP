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
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<List<User>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws URISyntaxException {
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
    public ResponseEntity<User> oneForUsername(@PathVariable @ApiParam(value = "Username of the user", example = "MyUser", required = true) String username) {
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
	public ResponseEntity<User> authenticate(@RequestBody @ApiParam(value = "Authentication data", required = true) UserAuthData authData) {
		// Retrieve user from database
		User user = userService.getForUsername(authData.getUsername().toLowerCase(Locale.ENGLISH));

		// Check password
		if (userService.checkPassword(user.getId(), authData.getPassword())) {
			return ResponseEntity.ok(user);
		} else {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid password!");
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
    		@RequestBody @ApiParam(value = "The user to update", required = true) User user) {
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
	public ResponseEntity<Void> delete(@PathVariable("username") @ApiParam(value = "Username of the user to delete", example = "MyUser", required = true) String username) {
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
    
    // - - -

    /**
     * POST /authenticate : Authenticates the received user.
     * Checks if the user is registered and if the password is correct.
     *
     * @param authData User authentication data to use
     * @return the ResponseEntity with status 200 (OK) if authentication
     * successful with the user object from database as body,
     * or with status 400 (Bad Request) if user not found or password incorrect
     */
//    @PostMapping("/authenticate")
//    @ApiOperation(value = "Authenticates a user", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User not found or password incorrect")})
//    public ResponseEntity<User> authenticate(@RequestBody @ApiParam(value = "Authentication data", required = true) UserAuthData authData) {
//        String lowercaseUsername = authData.getUsername().toLowerCase(Locale.ENGLISH);
//        Optional<User> dbUserOptional = userRepository.findByUsername(lowercaseUsername);
//
//        //Check if user was found
//        if (!dbUserOptional.isPresent()) {
//            return ResponseEntity.badRequest()
//                    .headers(HeaderUtil.createFailureAlert("User not found", authData.getUsername())).body(null);
//        }
//
//        //Get user object from database
//        User dbUser = dbUserOptional.get();
//
//        if (userService.passwordMatches(authData.getPassword(), dbUser.getPassword())) {
//            return ResponseEntity.ok()
//                    .headers(HeaderUtil.createAlert("Authentication successful", dbUser.getUsername())).body(dbUser);
//        } else {
//            return ResponseEntity.badRequest()
//                    .headers(HeaderUtil.createFailureAlert("Password incorrect", dbUser.getUsername())).body(null);
//        }
//    }

    /**
     * POST /users : Creates a new user.
     * <p>
     * Creates a new user if the username is not already used.
     *
     * @param user The user to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new user, or with status 400 (Bad Request) if the username is
     * already in use
     * @throws URISyntaxException If the Location URI syntax is incorrect
     */
//    @PostMapping("/users")
//    @ApiOperation(value = "Creates a new user entity", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "Username is already in use")})
//    public ResponseEntity<?> createUser(@Valid @RequestBody @ApiParam(value = "The user to create", required = true) User user) throws URISyntaxException {
//        // Lowercase the user username before comparing with database
//        if (userRepository.findByUsername(user.getUsername().toLowerCase()).isPresent()) {
//            return ResponseEntity.badRequest()
//                    .headers(HeaderUtil.createFailureAlert("Username already in use", user.getUsername())).body(null);
//        } else {
//            User newUser = userService.createUser(user);
//            return ResponseEntity.created(new URI("/api/users/" + newUser.getUsername()))
//                    .headers(HeaderUtil.createAlert("User registered successfully", newUser.getUsername()))
//                    .body(newUser);
//        }
//    }

    /**
     * PUT /users : Updates an existing User.
     *
     * @param user The user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * user, or with status 400 (Bad Request) if the username is already
     * in use, or with status 500 (Internal Server Error) if the user
     * couldn't be updated
     */
//    @PutMapping("/users")
//    @Secured(Constants.ADMIN)
//    @ApiOperation(value = "Updates an existing user entity", notes = "Requires admin privileges.", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "Username is already in use"), @ApiResponse(code = 500, message = "User could not be updated")})
//    public ResponseEntity<User> updateUser(@RequestBody @ApiParam(value = "The user to update", required = true) User user) {
//        Optional<User> existingUser = userRepository.findByUsername(user.getUsername().toLowerCase());
//        if (existingUser.isPresent() && (!existingUser.get().getId().equals(user.getId()))) {
//            return ResponseEntity.badRequest()
//                    .headers(HeaderUtil.createFailureAlert("Username already in use", user.getUsername())).body(null);
//        }
//        userService.updateUser(user.getId(), user.getUsername(), user.getPassword(), user.getFirstName(),
//                user.getLastName(), null
////                , user.getAuthorities()
//                );
//
//        return ResponseEntity.ok().headers(HeaderUtil.createAlert("User updated successfully", user.getUsername()))
//                .body(userService.getUserWithAuthorities(user.getId()));
//    }

//    /**
//     * GET /users : get all users.
//     *
//     * @param pageable The pagination information
//     * @return the ResponseEntity with status 200 (OK) and with body all users
//     * @throws URISyntaxException If the pagination headers couldn't be generated
//     */
//    @GetMapping("/users")
//    @Secured(Constants.ADMIN)
//    @ApiOperation(value = "Returns all existing user entities", notes = "Requires admin privileges.", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
//    public ResponseEntity<List<User>> getAllUsers(@ApiParam(value = "Pagination configuration") Pageable pageable) throws URISyntaxException {
//        Page<User> page = userRepository.findAll(pageable);
//        List<User> users = new ArrayList<>(page.getContent());
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
//        return new ResponseEntity<>(users, headers, HttpStatus.OK);
//    }
//
//    /**
//     * GET /users/:username : get the "username" user.
//     *
//     * @param username The username of the user to find
//     * @return the ResponseEntity with status 200 (OK) and with body the
//     * "username" user, or with status 404 (Not Found)
//     */
//    @GetMapping("/users/{username:" + Constants.USERNAME_REGEX + "}")
//    @Secured(Constants.ADMIN)
//    @ApiOperation(value = "Returns an user entity by its username", notes = "Requires admin privileges.", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "User not found")})
//    public ResponseEntity<User> getUser(@PathVariable @ApiParam(value = "Username of the user", example = "MyUser", required = true) String username) {
//        return userService.getUserWithAuthoritiesByUsername(username)
//                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
//                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }

    /**
     * DELETE /users/:username : delete the "username" user.
     *
     * @param username The username of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
//    @DeleteMapping("/users/{username:" + Constants.USERNAME_REGEX + "}")
//    @Secured(Constants.ADMIN)
//    @ApiOperation(value = "Deletes an user entity by its username", notes = "Requires admin privileges.", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success or user not found")})
//    public ResponseEntity<Void> deleteUser(@PathVariable @ApiParam(value = "Username of the user to delete", example = "MyUser", required = true) String username) {
//        userService.deleteUser(username);
//        return ResponseEntity.ok().headers(HeaderUtil.createAlert("User deleted successfully", username)).build();
//    }

    /**
     * Searches and returns a list of user excerpts for all users whose usernames contain a given query string. If the
     * provided query string is too short, an empty list is returned.
     *
     * @param queryString The query string for searching users
     * @return The list of user excerpts for all matching users
     */
//    @GetMapping("/users/contain")
//    @ApiOperation(value = "Searches and returns all users whose username contain a given query string ", notes = "Returns an empty list in case the query string is too short", produces = "application/json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Query result")})
//    public ResponseEntity<List<UserExcerpt>> searchUsersContaining(@RequestParam("query") @ApiParam(value = "Query string for searching users", example = "admin", required = true) String queryString) {
//        //Trim query
//        queryString = queryString.trim();
//
//        //Check if valid query string was provided
//        if (queryString.isEmpty()) {
//            //Return empty list
//            return ResponseEntity.ok(new ArrayList<>());
//        }
//
//        //Retrieve all users whose names contain the query string
//        List<UserExcerpt> users = userRepository.findByUsernameContains(queryString);
//
//        //Reply
//        return ResponseEntity.ok(users);
//    }
}
