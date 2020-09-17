package org.citopt.connde.service;

import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;


	public Page<User> getAll(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	public User getLoggedInUser() {
		return getForUsername(SecurityUtils.getCurrentUserUsername());
	}
	
	public User getForId(String id) {
		return userRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"User with id '" + id + "' does not exist!"));
	}

	public User getForUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"User with username '" + username + "' does not exist!"));
	}

	public User create(User user) {
		// Check whether a user with the same username exists already
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"A user with username '" + user.getUsername() + "' exists already!");
		}

		// Create user in the database
		return userRepository
				.save(new User()
						.setUsername(user.getUsername())
						.setPassword(passwordEncoder.encode(user.getPassword()))
						.setFirstName(user.getFirstName())
						.setLastName(user.getLastName()));
	}

	public User update(String userId, User user) {
		// Check whether user exists (based on the id)
		if (!userRepository.existsById(userId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"User with id '" + userId + "' does not exist!");
		}

		// Check whether a user with the same username exists already
		if (userRepository.existsOtherByUsername(userId, user.getUsername())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, 
					"Username '" + user.getUsername() + "' exists already!");
		}

		return userRepository.save(new User()
				.setId(userId)
				.setUsername(user.getUsername())
				.setPassword(passwordEncoder.encode(user.getPassword()))
				.setFirstName(user.getFirstName())
				.setLastName(user.getLastName()));
	}

	public void deleteUser(String userId) {
		// Check whether user exists (based on the id)
		if (!userRepository.existsById(userId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id '" + userId + "' does not exist!");
		}

		userRepository.deleteById(userId);
	}
	
	public boolean checkPassword(String userId, String passwordToCheck) {
		return passwordEncoder.matches(passwordToCheck, getForId(userId).getPassword());
	}
	
	/**
	 * Checks whether the currently logged in user has admin privileges and
	 * throws a {@link ResponseStatusException} with HTTP status code {@link HttpStatus#NOT_FOUND}
	 * in case the user does <b>not</b> have admin privileges.
	 */
	public void requireAdmin() {
    	User user = userRepository.findByUsername(SecurityUtils.getCurrentUserUsername()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	if (!user.isAdmin()) {
    		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin privileges required!");
    	}
    }

	// - - -

//    public User createUser(String username, String password, String firstName, String lastName) {
//        User newUser = new User();
////        Authority authority = authorityRepository.findByName(Constants.USER).get();
////        Set<Authority> authorities = new HashSet<>();
//        String encryptedPassword = passwordEncoder.encode(password);
//        newUser.setUsername(username);
//        newUser.setPassword(encryptedPassword);
//        newUser.setFirstName(firstName);
//        newUser.setLastName(lastName);
////        authorities.add(authority);
////        newUser.setAuthorities(authorities);
//        userRepository.save(newUser);
//        return newUser;
//    }

//	public boolean passwordMatches(String userPassword, String userFromDatabasePassword) {
//		return passwordEncoder.matches(userPassword, userFromDatabasePassword);
//	}

//    public User createUser(User user) {
//        Authority authority = authorityRepository.findByName(Constants.USER).get();
//        Set<Authority> authorities = new HashSet<>();
//        authorities.add(authority);
////        user.setAuthorities(authorities);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        userRepository.save(user);
//        return user;
//    }
//
//    public void updateUser(String firstName, String lastName) {
//        userRepository.findByUsername(SecurityUtils.getCurrentUserUsername()).ifPresent(user -> {
//            user.setFirstName(firstName);
//            user.setLastName(lastName);
//            userRepository.save(user);
//        });
//    }

//    public void updateUser(String id, String username, String password, String firstName, String lastName, Set<Authority> authorities) {
//        userRepository
//                .findById(id)
//                .ifPresent(user -> {
//                    user.setUsername(username);
//                    user.setPassword(passwordEncoder.encode(password));
//                    user.setFirstName(firstName);
//                    user.setLastName(lastName);
////                    Set<Authority> managedAuthorities = user.getAuthorities();
////                    managedAuthorities.clear();
////                    authorities.forEach(
////                            authority -> managedAuthorities.add(authorityRepository.findByName(authority.getName()).get())
////                    );
//                    userRepository.save(user);
//                });
//    }

//    public void deleteUser(String username) {
//        userRepository.findByUsername(username).ifPresent(user -> {
//            userRepository.delete(user);
//        });
//    }

//    public void changePassword(String password) {
//        userRepository.findByUsername(SecurityUtils.getCurrentUserUsername()).ifPresent(user -> {
//            String encryptedPassword = passwordEncoder.encode(password);
//            user.setPassword(encryptedPassword);
//            userRepository.save(user);
//        });
//    }
//
//    public Optional<User> getUserWithAuthoritiesByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//
//    public User getUserWithAuthorities(String id) {
//        return userRepository.findById(id).get();
//    }
//
//    public User getUserWithAuthorities() {
//        Optional<User> optionalUser = userRepository.findByUsername(SecurityUtils.getCurrentUserUsername());
//        User user = null;
//        if (optionalUser.isPresent()) {
//            user = optionalUser.get();
//        }
//        return user;
//    }
}