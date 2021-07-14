package de.ipvs.as.mbp.service.user;

import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingAdminPrivilegesException;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.security.SecurityUtils;
import de.ipvs.as.mbp.service.testing.TestEngine;
import de.ipvs.as.mbp.service.testing.rerun.TestRerunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private ACPolicyRepository policyRepository;

    @Autowired
    private ACConditionRepository conditionRepository;

    @Autowired
    private ACEffectRepository effectRepository;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestEngine testEngine;

    @Autowired
    private TestRerunService testRerunService;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private KeyPairRepository keyPairRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private MonitoringOperatorRepository monitoringOperatorRepository;

    @Autowired
    private ValueLogRepository valueLogRepository;

    @Autowired
    private SensorTypeRepository sensorTypeRepository;

    @Autowired
    private ActuatorTypeRepository actuatorTypeRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private EnvironmentModelRepository environmentModelRepository;

    @Autowired
    private RuleTriggerRepository ruleTriggerRepository;

    @Autowired
    private RuleActionRepository ruleActionRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private ExceptionLogRepository exceptionLogRepository;

    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getLoggedInUser() {
        return getForUsername(SecurityUtils.getCurrentUserUsername());
    }

    public User getForId(String id) {
        return userRepository.findById(id).orElseThrow(() -> new MBPException(HttpStatus.NOT_FOUND, "User with ID '" +
                id + "' does not exist!"));
    }

    public User getForUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new MBPException(HttpStatus.NOT_FOUND,
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
        //Retrieve the user from repository
        User user = this.getForId(userId);

        //Check whether user is a system user
        if (user.isSystemUser()) {
            throw new MBPException(HttpStatus.FORBIDDEN, "User with id '" + userId + "' is a system user and thus cannot be deleted.");
        }

        // Delete the corresponding data. The deletion order is important as the entities depend on each other.
        deleteTests(userId);
        deleteSensors(userId);
        deleteActuators(userId);
        deleteDevicesAndMonitoringOperators(userId);
        deleteKeyPairs(userId);
        deleteOperators(userId);
        deleteEnvironmentModels(userId);
        deleteEntityTypes(userId);
        deleteRules(userId);
        deletePolicies(userId);
        deleteExceptionLogs(user.getUsername());

        // Delete the user
        userRepository.deleteById(userId);
    }

    private void deleteTests(String userId) {
        userEntityService.getAllForOwnerId(testDetailsRepository, userId).forEach(entity -> {
            // Check whether entity actually can be deleted (may still be in use)
            userEntityService.requireDeletable(entity);
            // Delete test-related data
            testRerunService.deleteRerunComponents(entity);
            testEngine.deleteAllReports(entity.getId());
            // Everything checks out -> delete the entity in the database
            testDetailsRepository.deleteById(entity.getId());
        });
    }

    private void deleteSensors(String userId) {
        userEntityService.getAllForOwnerId(sensorRepository, userId).forEach(entity -> {
            // Check whether entity is used by a test
            if (testDetailsRepository.findOneBySensorId(entity.getId()).isPresent()) {
                throw new MBPException(HttpStatus.CONFLICT, "The sensor '" + entity.getName() + "' is still used by at least one test and thus cannot be deleted.");
            }
            // Check whether entity actually can be deleted (may still be in use)
            userEntityService.requireDeletable(entity);
            // Delete value logs of this sensor
            valueLogRepository.deleteByIdRef(entity.getId());
            // Everything checks out -> delete the entity in the database
            sensorRepository.deleteById(entity.getId());
        });
    }

    private void deleteActuators(String userId) {
        userEntityService.getAllForOwnerId(actuatorRepository, userId).forEach(entity -> {
            // Check whether entity actually can be deleted (may still be in use)
            userEntityService.requireDeletable(entity);
            // Delete value logs of this actuator
            valueLogRepository.deleteByIdRef(entity.getId());
            // Everything checks out -> delete the entity in the database
            actuatorRepository.deleteById(entity.getId());
        });
    }

    private void deleteDevicesAndMonitoringOperators(String userId) {
        List<Device> devices = new ArrayList<>();
        userEntityService.getAllForOwnerId(deviceRepository, userId).forEach(entity -> {
            // Check whether entity actually can be deleted (may still be in use)
            userEntityService.requireDeletable(entity);
            // Everything checks out -> delete the entity in the database
            deviceRepository.deleteById(entity.getId());
            devices.add(entity);
        });
        userEntityService.getAllForOwnerId(monitoringOperatorRepository, userId).forEach(entity -> {
            // Check whether entity actually can be deleted (may still be in use)
            userEntityService.requireDeletable(entity);
            // Everything checks out -> delete the entity in the database
            monitoringOperatorRepository.deleteById(entity.getId());
            devices.forEach(device -> {
                // Delete value logs
                valueLogRepository.deleteByIdRef(new MonitoringComponent(entity, device).getId());
            });
        });
    }

    private void deleteKeyPairs(String userId) {
        deleteEntities(keyPairRepository, userId);
    }

    private void deleteOperators(String userId) {
        deleteEntities(operatorRepository, userId);
    }

    private void deleteEnvironmentModels(String userId) {
        deleteEntities(environmentModelRepository, userId);
    }

    private void deleteEntityTypes(String userId) {
        deleteEntities(sensorTypeRepository, userId);
        deleteEntities(actuatorTypeRepository, userId);
        deleteEntities(deviceTypeRepository, userId);
    }

    private void deleteRules(String userId) {
        deleteEntities(ruleRepository, userId);
        deleteEntities(ruleActionRepository, userId);
        deleteEntities(ruleTriggerRepository, userId);
    }

    private void deletePolicies(String userId) {
        policyRepository.deleteByOwnerId(userId);
        effectRepository.findByOwnerId(userId).forEach(entity -> {
            if (policyRepository.countUsingEffect(entity.getId()) > 0) {
                throw new MBPException(HttpStatus.CONFLICT, "The effect '" + entity.getName() + "' is still used by at least one policy and thus cannot be deleted.");
            }
            // Actually delete condition in the database
            effectRepository.deleteById(entity.getId());
        });
        conditionRepository.findByOwnerId(userId).forEach(entity -> {
            if (policyRepository.countUsingCondition(entity.getId()) > 0) {
                throw new MBPException(HttpStatus.CONFLICT, "The condition '" + entity.getName() + "' is still used by at least one policy and thus cannot be deleted.");
            }
            // Actually delete condition in the database
            conditionRepository.deleteById(entity.getId());
        });
    }

    private void deleteExceptionLogs(String username) {
        exceptionLogRepository.deleteByUsername(username);
    }

    private <E extends UserEntity> void deleteEntities(UserEntityRepository<E> repository, String userId) {
        userEntityService.getAllForOwnerId(repository, userId).forEach(entity -> {
            // Check whether entity actually can be deleted (may still be in use)
            userEntityService.requireDeletable(entity);
            // Everything checks out -> delete the entity in the database
            repository.deleteById(entity.getId());
        });
    }

    /**
     * Promotes a user, given by its user ID, to an administrator.
     *
     * @param userId The ID of the user to promote
     * @return The updated user object
     */
    public User promoteUser(String userId) {
        //Retrieve the user from repository
        User user = this.getForId(userId);

        //Check whether user is a system user
        if (user.isSystemUser()) {
            throw new MBPException(HttpStatus.FORBIDDEN, "User with id \"" + userId + "\" is a system user and cannot be altered.");
        }

        //Check whether user is already admin
        if (user.isAdmin()) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "User with id \"" + userId + "\" is already an administrator.");
        }

        //Make user admin
        user.setAdmin(true);

        //Update repository
        userRepository.save(user);

        //Return modified user
        return user;
    }

    /**
     * Degrades an administrator, given by its user ID, to a non-administrator.
     *
     * @param userId The ID of the user to degrade
     * @return The updated user object
     */
    public User degradeUser(String userId) {
        //Retrieve the user from repository
        User user = this.getForId(userId);

        //Check whether user is a system user
        if (user.isSystemUser()) {
            throw new MBPException(HttpStatus.FORBIDDEN, "User with id \"" + userId + "\" is a system user and cannot be altered.");
        }

        //Check whether user is no admin
        if (!user.isAdmin()) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "User with id \"" + userId + "\" is not an administrator.");
        }

        //Make user non-admin
        user.setAdmin(false);

        //Update repository
        userRepository.save(user);

        //Return modified user
        return user;
    }

    /**
     * Changes the password of an existing user, given by its user ID, to a new one. The new password has to be passed
     * in plain text and will then be hashed.
     *
     * @param userId      The ID of the user for which the password is supposed to be changed
     * @param newPassword The new password to set (in plain text)
     * @return The updated user object
     */
    public User changePassword(String userId, String newPassword) {
        //Retrieve the user from repository
        User user = this.getForId(userId);

        //Set password
        user.setPassword(passwordEncoder.encode(newPassword));

        //Update repository
        userRepository.save(user);

        //Return modified user
        return user;
    }

    public boolean checkPassword(String userId, String passwordToCheck) {
        return passwordEncoder.matches(passwordToCheck, getForId(userId).getPassword());
    }

    /**
     * Checks whether the currently logged in user has admin privileges.
     */
    public void requireAdmin() throws MissingAdminPrivilegesException {
        User user = userRepository.findByUsername(SecurityUtils.getCurrentUserUsername()).orElseThrow(MissingAdminPrivilegesException::new);
        if (!user.isAdmin()) {
            throw new MissingAdminPrivilegesException();
        }
    }

}