package org.citopt.connde.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.constants.Constants;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.service.UserService;
import org.citopt.connde.web.rest.util.HeaderUtil;
import org.citopt.connde.web.rest.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing users.
 * @author Imeri Amil
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestUserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	/**
	 * POST /authenticate : Authenticates the received user.
	 * 
	 * Checks if the user is registered and if the password is correct.
	 * 
	 * @param user
	 *            The user to authenticate
	 * @return the ResponseEntity with status 200 (OK) if authentication
	 *         successful, or with status 400 (Bad Request) if user not found or
	 *         password incorrect
	 */
	@PostMapping("/authenticate")
	public ResponseEntity<?> authenticate(@Valid @RequestBody User user) {
		String lowercaseUsername = user.getUsername().toLowerCase(Locale.ENGLISH);
		Optional<User> userFromDatabase = userRepository.findOneByUsername(lowercaseUsername);
		if (!userFromDatabase.isPresent()) {
			return ResponseEntity.badRequest()
					.headers(HeaderUtil.createFailureAlert("User not found", user.getUsername())).body(null);
		} else {
			if (userService.passwordMatches(user.getPassword(), userFromDatabase.get().getPassword())) {
				return ResponseEntity.ok()
						.headers(HeaderUtil.createAlert("Authentication successful", user.getUsername())).body(null);
			} else {
				return ResponseEntity.badRequest()
						.headers(HeaderUtil.createFailureAlert("Password incorrect", user.getUsername())).body(null);
			}
		}
	}

	/**
	 * POST /users : Creates a new user.
	 * 
	 * Creates a new user if the username is not already used.
	 * 
	 * @param user
	 *            The user to create
	 * @return the ResponseEntity with status 201 (Created) and with body the
	 *         new user, or with status 400 (Bad Request) if the username is
	 *         already in use
	 * @throws URISyntaxException
	 *             If the Location URI syntax is incorrect
	 */
	@PostMapping("/users")
	public ResponseEntity<?> createUser(@Valid @RequestBody User user) throws URISyntaxException {
		// Lowercase the user username before comparing with database
		if (userRepository.findOneByUsername(user.getUsername().toLowerCase()).isPresent()) {
			return ResponseEntity.badRequest()
					.headers(HeaderUtil.createFailureAlert("Username already in use", user.getUsername())).body(null);
		} else {
			User newUser = userService.createUser(user);
			return ResponseEntity.created(new URI("/api/users/" + newUser.getUsername()))
					.headers(HeaderUtil.createAlert("User registered successfully", newUser.getUsername()))
					.body(newUser);
		}
	}

	/**
	 * PUT /users : Updates an existing User.
	 *
	 * @param user
	 *            The user to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         user, or with status 400 (Bad Request) if the username is already
	 *         in use, or with status 500 (Internal Server Error) if the user
	 *         couldn't be updated
	 */
	@PutMapping("/users")
	@Secured(Constants.ADMIN)
	public ResponseEntity<User> updateUser(@RequestBody User user) {
		Optional<User> existingUser = userRepository.findOneByUsername(user.getUsername().toLowerCase());
		if (existingUser.isPresent() && (!existingUser.get().getId().equals(user.getId()))) {
			return ResponseEntity.badRequest()
					.headers(HeaderUtil.createFailureAlert("Username already in use", user.getUsername())).body(null);
		}
		userService.updateUser(user.getId(), user.getUsername(), user.getPassword(), user.getFirstName(),
				user.getLastName(), user.getAuthorities());

		return ResponseEntity.ok().headers(HeaderUtil.createAlert("User updated successfully", user.getUsername()))
				.body(userService.getUserWithAuthorities(user.getId()));
	}

	/**
	 * GET /users : get all users.
	 *
	 * @param pageable
	 *            The pagination information
	 * @return the ResponseEntity with status 200 (OK) and with body all users
	 * @throws URISyntaxException
	 *             If the pagination headers couldn't be generated
	 */
	@GetMapping("/users")
	@Secured(Constants.ADMIN)
	public ResponseEntity<List<User>> getAllUsers(Pageable pageable) throws URISyntaxException {
		Page<User> page = userRepository.findAll(pageable);
		List<User> users = new ArrayList<>(page.getContent());
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
		return new ResponseEntity<>(users, headers, HttpStatus.OK);
	}

	/**
	 * GET /users/:username : get the "username" user.
	 *
	 * @param username
	 *            The username of the user to find
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         "username" user, or with status 404 (Not Found)
	 */
	@GetMapping("/users/{username:" + Constants.USERNAME_REGEX + "}")
	@Secured(Constants.ADMIN)
	public ResponseEntity<User> getUser(@PathVariable String username) {
		return userService.getUserWithAuthoritiesByUsername(username)
				.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE /users/:username : delete the "username" user.
	 *
	 * @param username
	 *            The username of the user to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/users/{username:" + Constants.USERNAME_REGEX + "}")
	@Secured(Constants.ADMIN)
	public ResponseEntity<Void> deleteUser(@PathVariable String username) {
		userService.deleteUser(username);
		return ResponseEntity.ok().headers(HeaderUtil.createAlert("User deleted successfully", username)).build();
	}
}
