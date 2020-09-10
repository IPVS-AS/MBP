package org.citopt.connde.service.access_control;

import java.util.List;
import java.util.stream.Collectors;

import org.citopt.connde.domain.access_control.ACAbstractEffect;
import org.citopt.connde.domain.access_control.dto.ACEffectRequestDTO;
import org.citopt.connde.repository.ACEffectRepository;
import org.citopt.connde.repository.ACPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for all {@link ACAbstractEffect} implementation.
 * 
 * @author Jakob Benz
 */
@Service
public class ACEffectService {
	
	@Autowired
	private ACPolicyRepository policyRepository;
	
	@Autowired
	private ACEffectRepository effectRepository;
	
	// - - -
	
	public List<ACAbstractEffect> getAll() {
		return effectRepository.findAll();
	}
	
	public List<ACAbstractEffect> getAllForOwner(String ownerId) {
		return effectRepository.findAll().stream().filter(p -> p.getOwnerId().equals(ownerId)).collect(Collectors.toList());
	}
	
	public ACAbstractEffect getForId(String id) {
		return effectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Access control policy with id '" + id + "' does not exist!"));
	}
	
	public ACAbstractEffect getForIdAndOwner(String id, String ownerId) {
		ACAbstractEffect effect = getForId(id);
		
		// Check whether the requesting user is the owner
		if (!effect.getOwnerId().equals(ownerId)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner privileges required!");
		}
		
		return effect;
	}
	
	public ACAbstractEffect create(ACEffectRequestDTO requestDto, String ownerId) {
		// Check whether a condition with the same name exists already
    	if (effectRepository.existsByName(requestDto.getName())) {
    		throw new ResponseStatusException(HttpStatus.CONFLICT, "An access control policy condition with name '" + requestDto.getName() + "' exists already!");
    	}
		
		// Create effect
    	ACAbstractEffect effect = null;
		try {
			effect = (ACAbstractEffect) ACAbstractEffect.forType(requestDto.getType())
					.setParameters(requestDto.getParameters())
					.setName(requestDto.getName())
					.setDescription(requestDto.getDescription())
					.setOwnerId(ownerId);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Effect could not be created!");
		}
		
		return effectRepository.save(effect);
	}
	
	public void delete(String id, String ownerId) {
		// Retrieve effect to delete from the database (if it exists) (includes owner check)
		getForIdAndOwner(id, ownerId);
		
		// Check whether effect is used by a policy
		if (policyRepository.countUsingEffect(id) > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This effect is being used by at least one policy an cannot be deleted.");
		}
		
    	// Actually delete effect in the database
		effectRepository.deleteById(id);
	}

}
