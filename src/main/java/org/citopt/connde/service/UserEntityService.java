package org.citopt.connde.service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.access_control.IACRequestedEntity;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.repository.ACPolicyRepository;
import org.citopt.connde.repository.UserEntityRepository;
import org.citopt.connde.service.access_control.ACPolicyEvaluationService;
import org.citopt.connde.util.C;
import org.citopt.connde.util.Pages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
	 * @param <E> the type of the {@link UserEntity}.
	 * @param repository the repository to retrieve the user entities from.
	 * @param accessType the {@link ACAccessType} to check.
	 * @param accessRequest the {@link ACAccessRequest} containing the contextual information
	 * 		  of the requesting user required to evaluate the policies.
	 * @return the list of (filtered) user entities.
	 */
	public <E extends UserEntity> List<E> getAllWithPolicyCheck(UserEntityRepository<E> repository, ACAccessType accessType, ACAccessRequest accessRequest) {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();
		
		// Retrieve the entities from the database
		List<E> entities = repository.findAll(DEFAULT_SORT);
		
		// Filter devices (according to owner and policies)
    	return entities
    			.stream()
				// Filter devices that have an owner
				.filter(UserEntity::hasOwner)
    			// Filter devices that are owned by the requesting user (all access granted)
    			.filter(e -> e.getOwner().getId().equals(user.getId()))
    			// Filter devices with policies with non-matching access-types
    			.filter(e -> policyRepository.existsByIdAnyAndAccessTypeAll(e.getAccessControlPolicyIds(), C.listOf(accessType.toString())))
    			// Filter devices with policies that deny access
    			.filter(e -> {
    				// Create the corresponding access object
    	    		ACAccess access = new ACAccess(accessType, user, e);
    	    		// Retrieve the policies from the database
    	    		List<ACPolicy> policies = getPoliciesForEntity(e);
    	    		// Evaluate the policies
    	    		return policies.stream().anyMatch(p -> policyEvaluationService.evaluate(p, access, accessRequest));
    	    	})
    			.collect(Collectors.toList());
	}
	
	/**
	 * Retrieves a page of user entities from the database. Note that only those entities are returned,
	 * that are either owned by the requesting user or for which a policy grants reading access
	 * to the requesting user.
	 * 
	 * @param <E> the type of the {@link UserEntity}.
	 * @param repository the repository to retrieve the user entities from.
	 * @param accessType the {@link ACAccessType} to check.
	 * @param accessRequest the {@link ACAccessRequest} containing the contextual information
	 * 		  of the requesting user required to evaluate the policies.
	 * @param pageable the {@link Pageable} to configure the resulting list.
	 * @return the page of (filtered) user entities.
	 */
	public <E extends UserEntity> List<E> getPageWithPolicyCheck(UserEntityRepository<E> repository, ACAccessType accessType, ACAccessRequest accessRequest, Pageable pageable) {
		// Extract requested page from all entities
    	return Pages.page(getAllWithPolicyCheck(repository, accessType, accessRequest), pageable);
	}
    
    /**
     * Retrieves a user entity from the database.
     * 
     * @param <E> the type of the {@link UserEntity}.
     * @param repository the repository to retrieve the user entity from.
     * @param entityId the id of the {@link UserEntity}.
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
     * @param <E> the type of the {@link UserEntity}.
     * @param repository the repository to retrieve the user entity from.
     * @param entityId the id of the {@link UserEntity}.
     * @param accessType the {@link ACAccessType} to check.
	 * @param accessRequest the {@link ACAccessRequest} containing the contextual information
	 * 		  of the requesting user required to evaluate the policies.
     * @return the {@link UserEntity} if it exists and the user is either the owner or has been granted reading access
     * 		   to it via a corresponding {@link ACPolicy}.
     * @throws EntityNotFoundException 
     */
    public <E extends UserEntity> E getForIdWithPolicyCheck(UserEntityRepository<E> repository, String entityId, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();

		// Retrieve the entity from the database
		E entity = repository.findById(entityId).orElseThrow(() -> new EntityNotFoundException("Entity", entityId));

		// Check whether the requesting user is allowed to access the entity
    	ACAccess access = new ACAccess(accessType, user, entity);
    	List<ACPolicy> policies = getPoliciesForEntity(entity);
    	if (!entity.getOwner().getId().equals(user.getId()) && !policies.stream().anyMatch(p -> policyEvaluationService.evaluate(p, access, accessRequest))) {
    		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User '" + user.getUsername() + "' is not allowed to access entity with id '" + entityId + "'!");
    	}

		return entity;
    }
    
    public <E extends UserEntity> E create(UserEntityRepository<E> repository, E entity) throws EntityNotFoundException {
    	// Retrieve the currently logged in user from the database
    	User user = userService.getLoggedInUser();
    	
    	// Set owner user manually
    	entity.setOwner(user);
    	
    	// Save (create) entity
    	return repository.save(entity);
    }
    
    /**
     * Deletes a user entity in the database.
     * 
     * @param <E> the type of the {@link UserEntity}.
     * @param repository the repository to retrieve the user entity from.
     * @param entityId the id of the {@link UserEntity}.
     * @param accessType the {@link ACAccessType} to check.
	 * @param accessRequest the {@link ACAccessRequest} containing the contextual information
	 * 		  of the requesting user required to evaluate the policies.
     * @throws EntityNotFoundException 
     */
    public <E extends UserEntity> void deleteWithPolicyCheck(UserEntityRepository<E> repository, String entityId, ACAccessRequest accessRequest) throws EntityNotFoundException {
    	// Retrieve the currently logged in user from the database
    	User user = userService.getLoggedInUser();
    	
    	// Retrieve the entity from the database
    	E entity = getForIdWithPolicyCheck(repository, entityId, ACAccessType.READ, accessRequest);
    	
    	// Check whether the requesting user is allowed to delete the entity
    	ACAccess access = new ACAccess(ACAccessType.DELETE, user, entity);
    	List<ACPolicy> policies = getPoliciesForEntity(entity);
    	if (!entity.getOwner().getId().equals(user.getId()) && !policies.stream().anyMatch(p -> policyEvaluationService.evaluate(p, access, accessRequest))) {
    		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User '" + user.getUsername() + "' is not allowed to delete entity with id '" + entityId + "'!");
    	}
    	
    	// Everything checks out (user is owner or a policy grants the delete permission) -> delete the entity in the database
    	repository.deleteById(entityId);
    }
    
    public <E extends IACRequestedEntity> List<E> filterforOwnerAndPolicies(Supplier<List<E>> entitiesSupplier, ACAccessType accessType, ACAccessRequest accessRequest) {
    	return filterforOwnerAndPolicies(entitiesSupplier.get(), accessType, accessRequest);
    }
    
    public <E extends IACRequestedEntity> List<E> filterforOwnerAndPolicies(List<E> entities, ACAccessType accessType, ACAccessRequest accessRequest) {
    	// Retrieve the currently logged in user from the database
    	User user = userService.getLoggedInUser();
    	
    	// Filter entities (according to owner and policies)
    	return entities
    			.stream()
    			// Filter devices that have an owner // TODO: Add check
    			// Filter devices that are owned by the requesting user (all access granted)
    			.filter(e -> e.getOwner().getId().equals(user.getId()))
    			// Filter devices with policies with non-matching access-types
    			.filter(e -> policyRepository.existsByIdAnyAndAccessTypeAll(e.getAccessControlPolicyIds(), C.listOf(accessType.toString())))
    			// Filter devices with policies that deny access
    			.filter(e -> {
    				// Create the corresponding access object
    	    		ACAccess access = new ACAccess(accessType, user, e);
    	    		// Retrieve the policies from the database
    	    		List<ACPolicy> policies = getPoliciesForEntity(e);
    	    		// Evaluate the policies
    	    		return policies.stream().anyMatch(p -> policyEvaluationService.evaluate(p, access, accessRequest));
    	    	})
    			.collect(Collectors.toList());
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
    
    public <E extends UserEntity> void requirePermission(UserEntityRepository<E> repository, String entityId, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
    	E entity = getForIdWithPolicyCheck(repository, entityId, ACAccessType.READ, accessRequest);
		requirePermission(entity, accessType, accessRequest);
	}
    
    public <E extends IACRequestedEntity> void requirePermission(E entity, ACAccessType accessType, ACAccessRequest accessRequest) throws MissingPermissionException {
		if (!checkPermission(entity, accessType, accessRequest)) {
			throw new MissingPermissionException("Entity", entity.getId(), accessType);
		}
	}
	
    public <E extends UserEntity> boolean checkPermission(UserEntityRepository<E> repository, String entityId, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException {
    	E entity = getForIdWithPolicyCheck(repository, entityId, ACAccessType.READ, accessRequest);
		return checkPermission(entity, accessType, accessRequest);
	}
	
    public <E extends IACRequestedEntity> boolean checkPermission(E entity, ACAccessType accessType, ACAccessRequest accessRequest) {
		List<ACPolicy> policies = getPoliciesForEntity(entity);
		for (ACPolicy policy : policies) {
			if (!policyEvaluationService.evaluate(policy, new ACAccess(accessType, userService.getLoggedInUser(), entity), accessRequest)) {
				return false;
			}
		}
		return true;
	}
    
//    public <E extends UserEntity> void requireUniqueName(UserEntityRepository<E> repository, String entityName) throws EntityAlreadyExistsException {
//    	if (repository.existsByName(entityName)) {
//    		throw new EntityAlreadyExistsException("Entity", entityName);
//    	}
//    }
    
    public <E extends UserEntity> PagedModel<EntityModel<E>> entitiesToPagedModel(List<E> entities, Link selfLink, Pageable pageable) {
    	List<EntityModel<E>> deviceEntityModels = entities.stream().map(this::entityToEntityModel).collect(Collectors.toList());
    	return new PagedModel<>(deviceEntityModels, Pages.metaDataOf(pageable, deviceEntityModels.size()), C.listOf(selfLink));
    }
    
    public <E extends UserEntity> EntityModel<E> entityToEntityModel(E entity) {
    	return new EntityModel<E>(entity, linkTo(getClass()).slash(entity.getId()).withSelfRel());
    }
    
}