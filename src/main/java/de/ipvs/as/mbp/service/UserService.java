package de.ipvs.as.mbp.service;

import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.repository.UserRepository;
import de.ipvs.as.mbp.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
				() -> new MBPException(HttpStatus.NOT_FOUND,
						"User with id '" + id + "' does not exist!"));
	}

	public User getForUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(
				() -> new MBPException(HttpStatus.NOT_FOUND,
						"User with username '" + username + "' does not exist!"));
	}

	public User create(User user) {
		// Check whether a user with the same username exists already
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new MBPException(HttpStatus.CONFLICT,
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
			throw new MBPException(HttpStatus.NOT_FOUND,
					"User with id '" + userId + "' does not exist!");
		}

		// Check whether a user with the same username exists already
		if (userRepository.existsOtherByUsername(userId, user.getUsername())) {
			throw new MBPException(HttpStatus.CONFLICT, 
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
			throw new MBPException(HttpStatus.NOT_FOUND, "User with id '" + userId + "' does not exist!");
		}

		userRepository.deleteById(userId);
	}
	
	public boolean checkPassword(String userId, String passwordToCheck) {
		return passwordEncoder.matches(passwordToCheck, getForId(userId).getPassword());
	}
	
	/**
	 * Checks whether the currently logged in user has admin privileges.
	 * @throws EntityNotFoundException
	 */
	public void requireAdmin() throws EntityNotFoundException {
    	User user = userRepository.findByUsername(SecurityUtils.getCurrentUserUsername()).orElseThrow(() -> new EntityNotFoundException("User", SecurityUtils.getCurrentUserUsername()));
    	if (!user.isAdmin()) {
    		throw new MBPException(HttpStatus.UNAUTHORIZED, "Admin privileges required!");
    	}
    }

}