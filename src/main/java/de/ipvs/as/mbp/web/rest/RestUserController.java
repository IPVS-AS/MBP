package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user.UserLoginData;
import de.ipvs.as.mbp.error.*;
import de.ipvs.as.mbp.repository.UserRepository;
import de.ipvs.as.mbp.repository.projection.UserExcerpt;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.service.user.UserSessionService;
import de.ipvs.as.mbp.util.Pages;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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

    @Autowired
    private UserSessionService userSessionService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing users.", notes = "Requires admin privileges.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access users (admin privileges required)!")})
    public ResponseEntity<PagedModel<EntityModel<User>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws MissingAdminPrivilegesException, EntityNotFoundException {
        // Check whether the requesting user has admin privileges
        userService.requireAdmin();

        //Retrieve all users and create entity models for them
        List<EntityModel<User>> entityModels = userService.getAll(pageable).toList().stream().map(user -> new EntityModel<User>(user, linkTo(getClass()).slash(user.getId()).withSelfRel())).collect(Collectors.toList());

        //Create paged model from users
        PagedModel<EntityModel<User>> pagedModel = new PagedModel<>(entityModels, Pages.metaDataOf(pageable, entityModels.size()));

        //Create and return response
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping(value = "/{username:" + Constants.USERNAME_REGEX + "}", produces = "application/hal+json")
    @ApiOperation(value = "Returns an user entity by its username", notes = "Requires admin privileges.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access users (admin privileges required)!"),
            @ApiResponse(code = 404, message = "User or requesting user not found!")})
    public ResponseEntity<User> oneForUsername(@PathVariable @ApiParam(value = "Username of the user", example = "MyUser", required = true) String username) throws EntityNotFoundException, MissingAdminPrivilegesException {
        // Check whether the requesting user has admin privileges
        userService.requireAdmin();

        // Retrieve user from database
        return ResponseEntity.ok(userService.getForUsername(username));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new user.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "User successfully created!"),
            @ApiResponse(code = 409, message = "Username is already in use!")})
    public ResponseEntity<User> create(@Valid @RequestBody @ApiParam(value = "The user to create", required = true) User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(user));
    }

    @GetMapping(value = "/searchByUsername", produces = "application/hal+json")
    @ApiOperation(value = "Searches and returns all users whose username contain a given query string.", notes = "Returns an empty list in case the query is empty.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Query result.")})
    public ResponseEntity<List<UserExcerpt>> searchByUsername(@RequestParam("query") @ApiParam(value = "Query string", example = "admin", required = true) String query) {
        // If query is empty -> return an empty result list, otherwise search for matching users
        return query.isEmpty() ? ResponseEntity.ok(new ArrayList<>()) : ResponseEntity.ok(userRepository.findByUsernameContains(query.trim()));
    }

    @PostMapping(value = "/login")
    @ApiOperation(value = "Performs login for a user", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 403, message = "Invalid password!"),
            @ApiResponse(code = 404, message = "User or requesting user not found!")})
    public ResponseEntity<User> login(@RequestBody @ApiParam(value = "Login data", required = true) UserLoginData loginData) throws InvalidPasswordException, UserNotLoginableException {
        // Retrieve user from database
        User user = userService.getForUsername(loginData.getUsername().toLowerCase(Locale.ENGLISH));

        //Check if login into user is possible
        if (!user.isLoginable()) {
            throw new UserNotLoginableException("This user is a system user and thus login is impossible.");
        }

        // Check password
        if (!userService.checkPassword(user.getId(), loginData.getPassword())) {
            throw new InvalidPasswordException();
        }

        //Create new session and retrieve corresponding cookie
        ResponseCookie sessionCookie = userSessionService.createSessionCookie(user);

        //Build response from cookie
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, sessionCookie.toString()).body(user);
    }

    @DeleteMapping(path = "/{userId}")
    @ApiOperation(value = "Deletes an existing user identified by its ID.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the user!"),
            @ApiResponse(code = 404, message = "User not found!")})
    public ResponseEntity<Void> delete(@PathVariable("userId") String userId) throws MissingAdminPrivilegesException, EntityNotFoundException {
        // Check whether the requesting user has admin privileges
        userService.requireAdmin();

        // Try to delete the user
        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{userId}/promote")
    @ApiOperation(value = "Promotes an existing user to an administrator.")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to promote users (admin privileges required)!")})
    public ResponseEntity<EntityModel<User>> promoteUser(@PathVariable("userId") String userId) throws MissingAdminPrivilegesException, EntityNotFoundException {
        // Check whether the requesting user has admin privileges
        userService.requireAdmin();

        // Update user
        User updatedUser = userService.promoteUser(userId);

        //Return entity model of the updated user
        return ResponseEntity.ok(new EntityModel<User>(updatedUser, linkTo(getClass()).slash(updatedUser.getId()).withSelfRel()));
    }

    @PostMapping(path = "/{userId}/degrade")
    @ApiOperation(value = "Degrades an existing administrator to a non-administrator.")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to degrade users (admin privileges required)!"), @ApiResponse(code = 403, message = "Cannot degrade yourself.")})
    public ResponseEntity<EntityModel<User>> degradeUser(@PathVariable("userId") String userId) throws MissingAdminPrivilegesException, EntityNotFoundException {
        // Check whether the requesting user has admin privileges
        userService.requireAdmin();

        //Check if current user is the same as the affected user
        if (userService.getLoggedInUser().getId().equals(userId)) {
            throw new MBPException(HttpStatus.FORBIDDEN, "To prevent lock-outs, you cannot degrade yourself.");
        }

        // Update user
        User updatedUser = userService.degradeUser(userId);

        //Return entity model of the updated user
        return ResponseEntity.ok(new EntityModel<User>(updatedUser, linkTo(getClass()).slash(updatedUser.getId()).withSelfRel()));
    }

    @PostMapping(path = "/{userId}/change_password")
    @ApiOperation(value = "Updates the password of an existing user.")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to change the password (admin privileges required)!")})
    public ResponseEntity<EntityModel<User>> changePassword(@PathVariable("userId") String userId, @RequestBody @ApiParam(value = "The new password to set (plain text)", required = true) User newPassword) throws MissingAdminPrivilegesException, EntityNotFoundException {
        // Check whether the requesting user has admin privileges
        userService.requireAdmin();

        // Update user
        User updatedUser = userService.changePassword(userId, newPassword.getPassword());

        //Return entity model of the updated user
        return ResponseEntity.ok(new EntityModel<User>(updatedUser, linkTo(getClass()).slash(updatedUser.getId()).withSelfRel()));
    }
}
