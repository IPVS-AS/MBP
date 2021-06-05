package de.ipvs.as.mbp.service;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.access_control.*;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import de.ipvs.as.mbp.error.*;
import de.ipvs.as.mbp.repository.ACPolicyRepository;
import de.ipvs.as.mbp.repository.UserEntityRepository;
import de.ipvs.as.mbp.service.access_control.ACPolicyEvaluationService;
import de.ipvs.as.mbp.service.event_handler.ICreateEventHandler;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.service.validation.IDeleteValidator;
import de.ipvs.as.mbp.util.C;
import de.ipvs.as.mbp.util.Pages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@Service
public class UserEntityService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "name");

    @Autowired
    private UserService userService;

    @Autowired
    private ACPolicyRepository policyRepository;

    @Autowired
    private ACPolicyEvaluationService policyEvaluationService;


    /**
     * Retrieves all user entities from the database. Note that only those entities are returned,
     * that are either owned by the requesting user or for which a policy grants reading access
     * to the requesting user.
     *
     * @param <E>           the type of the {@link UserEntity}.
     * @param repository    the repository to retrieve the user entities from.
     * @param accessType    the {@link ACAccessType} to check.
     * @param accessRequest the {@link ACAccessRequest} containing the contextual information
     *                      of the requesting user required to evaluate the policies.
     * @return the list of (filtered) user entities.
     */
    public <E extends UserEntity> List<E> getAllWithAccessControlCheck(UserEntityRepository<E> repository, ACAccessType accessType, ACAccessRequest accessRequest) {
        return filterForAdminOwnerAndPolicies(() -> repository.findAll(DEFAULT_SORT), accessType, accessRequest);
    }

    /**
     * Retrieves a page of user entities from the database. Note that only those entities are returned,
     * that are either owned by the requesting user or for which a policy grants reading access
     * to the requesting user.
     *
     * @param <E>           the type of the {@link UserEntity}.
     * @param repository    the repository to retrieve the user entities from.
     * @param accessType    the {@link ACAccessType} to check.
     * @param accessRequest the {@link ACAccessRequest} containing the contextual information
     *                      of the requesting user required to evaluate the policies.
     * @param pageable      the {@link Pageable} to configure the resulting list.
     * @return the page of (filtered) user entities.
     */
    public <E extends UserEntity> List<E> getPageWithAccessControlCheck(UserEntityRepository<E> repository, ACAccessType accessType, ACAccessRequest accessRequest, Pageable pageable) {
        // Extract requested page from all entities
        return Pages.page(getAllWithAccessControlCheck(repository, accessType, accessRequest), pageable);
    }

    /**
     * Retrieves a user entity from the database.
     *
     * @param <E>        the type of the {@link UserEntity}.
     * @param repository the repository to retrieve the user entity from.
     * @param entityId   the id of the {@link UserEntity}.
     * @return the {@link UserEntity} if it exists.
     * @throws EntityNotFoundException
     */
    public <E extends UserEntity> E getForId(UserEntityRepository<E> repository, String entityId) throws EntityNotFoundException {
        // Retrieve the entity from the database
        return repository.findById(entityId).orElseThrow(() -> new EntityNotFoundException("Entity", entityId));
    }

    /**
     * Retrieves a user entity from the database.
     *
     * @param <E>           the type of the {@link UserEntity}.
     * @param repository    the repository to retrieve the user entity from.
     * @param entityId      the id of the {@link UserEntity}.
     * @param accessType    the {@link ACAccessType} to check.
     * @param accessRequest the {@link ACAccessRequest} containing the contextual information
     *                      of the requesting user required to evaluate the policies.
     * @return the {@link UserEntity} if it exists and the user is either the owner or has been granted reading access
     * to it via a corresponding {@link ACPolicy}.
     * @throws EntityNotFoundException
     * @throws MissingPermissionException
     */
    public <E extends UserEntity> E getForIdWithAccessControlCheck(UserEntityRepository<E> repository, String entityId, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the entity from the database
        E entity = repository.findById(entityId).orElseThrow(() -> new EntityNotFoundException("Entity", entityId));

        //User must be loginable
        requireLoginable();

        // Check owner
        if (!checkAdmin() && !checkOwner(entity)) {
            // Not the owner -> check policies
            requirePermission(repository, entityId, accessType, accessRequest);
        }

        return entity;
    }

    /**
     * Retrieves the first applicable policy that grant the requested access. For
     * example, used for applying effects (constraints) where we need the
     * {@link ACAbstractEffect} associated with a certain policy.
     *
     * @param entity        the {@link UserEntity} access is requested for.
     * @param accessType    the {@link ACAccessType}.
     * @param accessRequest the {@link ACAccessRequest} containing the contextual
     *                      information of the requesting user required to evaluate
     *                      the policies.
     * @return the first policy granting access if there is any wrapped in an
     * {@link Optional}; an empty {@link Optional} otherwise.
     */
    public <E extends UserEntity> Optional<ACPolicy> getFirstPolicyGrantingAccess(E entity, ACAccessType accessType, ACAccessRequest accessRequest) {
        for (ACPolicy policy : getPoliciesForEntity(entity)) {
            if (policyEvaluationService.evaluate(policy, new ACAccess(accessType, userService.getLoggedInUser(), entity), accessRequest)) {
                return Optional.of(policy);
            }
        }
        return Optional.empty();
    }

    public <E extends UserEntity> E create(UserEntityRepository<E> repository, E entity) throws EntityNotFoundException
    {
        System.out.println("Create before User checking");
        System.out.println("User in create" + userService.getLoggedInUser());
        //Retrieve the currently logged in user from the database
        User user = userService.getLoggedInUser();

        //User must be loginable
        requireLoginable(user);

        //Set owner user manually
        entity.setOwner(user);

        //Get all create validators that are associated with this entity type
        MBPEntity[] annotations = entity.getClass().getAnnotationsByType(MBPEntity.class);
        List<ICreateValidator<E>> validators = new ArrayList<>();
        for (MBPEntity annotation : annotations) {
            for (Class<? extends ICreateValidator<?>> c : annotation.createValidator()) {
                validators.add((ICreateValidator<E>) DynamicBeanProvider.get(c));
            }
        }

        //Execute validation and throw exception if validation fails
        validators.forEach(v -> v.validateCreatable(entity));

        //Save (create) entity
        E createdEntity = repository.save(entity);

        //Get creation event handlers if available
        List<ICreateEventHandler<E>> createEventHandlers = new ArrayList<>();
        for (MBPEntity annotation : annotations) {
            for (Class<? extends ICreateEventHandler<?>> c : annotation.createEventHandler()) {
                createEventHandlers.add((ICreateEventHandler<E>) DynamicBeanProvider.get(c));
            }
        }

        //Call all found event handlers
        createEventHandlers.forEach(v -> v.onCreate(entity));

        //Return created entity
        return createdEntity;
    }

    /**
     * Deletes a user entity in the database.
     *
     * @param <E>           the type of the {@link UserEntity}.
     * @param repository    the repository to retrieve the user entity from.
     * @param entityId      the id of the {@link UserEntity}.
     * @param accessRequest the {@link ACAccessRequest} containing the contextual information
     *                      of the requesting user required to evaluate the policies.
     * @throws EntityNotFoundException
     * @throws MissingPermissionException
     */
    public <E extends UserEntity> void deleteWithAccessControlCheck(UserEntityRepository<E> repository, String entityId, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the entity from the database
        E entity = getForId(repository, entityId);

        //User must be loginable
        requireLoginable();

        if ((!checkAdmin() && !checkOwner(entity))) {
            // Not the owner -> check policies
            requirePermission(repository, entityId, ACAccessType.DELETE, accessRequest);
        }

        // Check whether entity actually can be deleted (may still be in use)
        requireDeletable(entity);

        // Everything checks out (user is owner or a policy grants the delete permission) -> delete the entity in the database
        repository.deleteById(entityId);
    }

    /**
     * Checks whether the entity can be deleted from an integrity point-of-view.
     * For example, a key-pair cannot be deleted if it is currently being used
     * by a device.
     *
     * @param entity the {@link UserEntity} to delete.
     */
    @SuppressWarnings("unchecked")
    public <E extends UserEntity> void requireDeletable(E entity) {
        MBPEntity[] annotations = entity.getClass().getAnnotationsByType(MBPEntity.class);
        List<IDeleteValidator<E>> validators = new ArrayList<>();
        for (MBPEntity annotation : annotations) {
            for (Class<? extends IDeleteValidator<?>> c : annotation.deleteValidator()) {
                validators.add((IDeleteValidator<E>) DynamicBeanProvider.get(c));
            }
        }

        // Throws exception if entity is not deletable
        validators.forEach(v -> v.validateDeletable(entity));
    }

    public <E extends IACRequestedEntity> List<E> filterForAdminOwnerAndPolicies(Supplier<List<E>> entitiesSupplier, ACAccessType accessType, ACAccessRequest accessRequest)  {
        return filterForAdminOwnerAndPolicies(entitiesSupplier.get(), accessType, accessRequest);
    }

    public <E extends IACRequestedEntity> List<E> filterForAdminOwnerAndPolicies(List<E> entities, ACAccessType accessType, ACAccessRequest accessRequest)  {
        // Retrieve the currently logged in user from the database
        User user = userService.getLoggedInUser();

        //User must be loginable
        requireLoginable(user);

        // Admin users are allowed to access everything
        if (user.isAdmin()) {
            return entities;
        }

        // Requesting user is a non-admin user
        List<E> filteredEntities = new ArrayList<>();
        // Add all entities without owner or owned by the requesting user
        filteredEntities.addAll(entities.stream()
                .filter(e -> checkOwner(user.getId(), e))
                .collect(Collectors.toList()));
        // Add all entities with a policy that grants access to the requesting user (not owned by the user)
        filteredEntities.addAll(entities.stream()
                .filter(e -> !checkOwner(user.getId(), e))
                .filter(e -> checkPermission(e, accessType, accessRequest))
                .collect(Collectors.toList()));

        return filteredEntities;
    }

    public <E extends IACRequestedEntity> List<ACPolicy> getPoliciesForEntity(E entity) {
        List<ACPolicy> policies = new ArrayList<>();
        entity.getAccessControlPolicyIds().forEach(policyId -> policyRepository.findById(policyId).ifPresent(policies::add));
        return policies;
    }

    public <E extends IACRequestedEntity> List<ACPolicy> getPoliciesForEntityAndAccessType(E entity, ACAccessType accessType) {
        List<ACPolicy> policies = new ArrayList<>();
        entity.getAccessControlPolicyIds().forEach(policyId -> policyRepository.findByIdAndAccessTypeAll(policyId, C.listOf(accessType.toString())).ifPresent(policies::add));
        return policies;
    }

    public void requireAdmin() throws MissingAdminPrivilegesException {
        requireAdmin(userService.getLoggedInUser());
    }

    public void requireAdmin(String userId) throws MissingAdminPrivilegesException {
        requireAdmin(userService.getForId(userId));
    }

    public void requireAdmin(User user) throws MissingAdminPrivilegesException {
        if (!user.isAdmin()) {
            throw new MissingAdminPrivilegesException();
        }
    }

    public <E extends IACRequestedEntity> boolean checkAdmin() {
        return checkAdmin(userService.getLoggedInUser());
    }

    public <E extends IACRequestedEntity> boolean checkAdmin(String userId) {
        return checkAdmin(userService.getForId(userId));
    }

    public <E extends IACRequestedEntity> boolean checkAdmin(User user) {
        return user.isAdmin();
    }

    public <E extends IACRequestedEntity> boolean checkOwner(E entity) {
        return checkOwner(userService.getLoggedInUser(), entity);
    }

    public <E extends IACRequestedEntity> boolean checkOwner(String userId, E entity) {
        return entity.getOwner() == null || entity.getOwner().getId().equals(userId);
    }

    public <E extends IACRequestedEntity> boolean checkOwner(User user, E entity) {
        return checkOwner(user.getId(), entity);
    }

    public void requireSystemUser() throws NoSystemUserException {
        requireSystemUser(userService.getLoggedInUser());
    }

    public void requireSystemUser(String userId) throws NoSystemUserException {
        requireSystemUser(userService.getForId(userId));
    }

    public void requireSystemUser(User user) throws NoSystemUserException {
        if (!user.isSystemUser()) {
            throw new NoSystemUserException();
        }
    }

    public void requireLoginable()  {
        requireLoginable(userService.getLoggedInUser());
    }

    public void requireLoginable(String userId)  {
        requireLoginable(userService.getForId(userId));
    }

    public void requireLoginable(User user){
        if (!user.isLoginable()) {
            throw new UserNotLoginableException();
        }
    }

    public <E extends UserEntity> void requirePermission(UserEntityRepository<E> repository, String entityId, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
        E entity = getForId(repository, entityId);
        requirePermission(entity, accessType, accessRequest);
    }

    public <E extends IACRequestedEntity> void requirePermission(E entity, ACAccessType accessType, ACAccessRequest accessRequest) throws MissingPermissionException {
        if (!checkAdmin() && !checkOwner(entity) && !checkPermission(entity, accessType, accessRequest)) {
            throw new MissingPermissionException("Entity", entity.getId(), accessType);
        }
    }

    public <E extends UserEntity> boolean checkPermission(UserEntityRepository<E> repository, String entityId, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
        E entity = getForIdWithAccessControlCheck(repository, entityId, ACAccessType.READ, accessRequest);
        return checkPermission(entity, accessType, accessRequest);
    }

    public <E extends IACRequestedEntity> boolean checkPermission(E entity, ACAccessType accessType, ACAccessRequest accessRequest) {
        if (policyRepository.existsByIdAnyAndAccessTypeAll(entity.getAccessControlPolicyIds(), C.listOf(accessType.toString()))) {
            return getPoliciesForEntity(entity)
                    .stream()
                    .anyMatch(p -> policyEvaluationService.evaluate(p, new ACAccess(accessType, userService.getLoggedInUser(), entity), accessRequest));
        }
        return false;
    }

    public <E extends UserEntity> PagedModel<EntityModel<E>> entitiesToPagedModel(List<E> entities, Link selfLink, Pageable pageable) {
        List<EntityModel<E>> deviceEntityModels = entities.stream().map(this::entityToEntityModel).collect(Collectors.toList());
        return new PagedModel<>(deviceEntityModels, Pages.metaDataOf(pageable, deviceEntityModels.size()), C.listOf(selfLink));
    }

    public <E extends UserEntity> EntityModel<E> entityToEntityModel(E entity) {
        return new EntityModel<E>(entity, linkTo(getClass()).slash(entity.getId()).withSelfRel());
    }

}