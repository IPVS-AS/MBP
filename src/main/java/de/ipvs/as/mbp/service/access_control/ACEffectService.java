package de.ipvs.as.mbp.service.access_control;

import java.util.List;
import java.util.stream.Collectors;

import de.ipvs.as.mbp.domain.access_control.ACAbstractEffect;
import de.ipvs.as.mbp.repository.ACEffectRepository;
import de.ipvs.as.mbp.repository.ACPolicyRepository;
import de.ipvs.as.mbp.domain.access_control.dto.ACEffectRequestDTO;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.EntityStillInUseException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingOwnerPrivilegesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
	
	public List<ACAbstractEffect> getAll(Pageable pageable) {
		return effectRepository.findAll(pageable).stream().collect(Collectors.toList());
	}
	
	public List<ACAbstractEffect> getAllForOwner(String ownerId, Pageable pageable) {
		return effectRepository.findAllByOwner(ownerId, pageable);
	}
	
	public ACAbstractEffect getForId(String id) throws EntityNotFoundException {
		return effectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Policy effect", id));
	}
	
	public ACAbstractEffect getForIdAndOwner(String id, String ownerId) throws EntityNotFoundException, MissingOwnerPrivilegesException {
		ACAbstractEffect effect = getForId(id);
		
		// Check whether the requesting user is the owner
		if (!effect.getOwnerId().equals(ownerId)) {
			throw new MissingOwnerPrivilegesException();
		}
		
		return effect;
	}
	
	public ACAbstractEffect create(ACEffectRequestDTO requestDto, String ownerId) throws EntityAlreadyExistsException {
		// Check whether a condition with the same name exists already
    	if (effectRepository.existsByName(requestDto.getName())) {
    		throw new EntityAlreadyExistsException("Policy effect", requestDto.getName());
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
			throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Effect could not be created!");
		}
		
		return effectRepository.save(effect);
	}
	
	public void delete(String id, String ownerId) throws EntityNotFoundException, MissingOwnerPrivilegesException, EntityStillInUseException {
		// Retrieve effect to delete from the database (if it exists) (includes owner check)
		getForIdAndOwner(id, ownerId);
		
		// Check whether effect is used by a policy
		if (policyRepository.countUsingEffect(id) > 0) {
			throw new EntityStillInUseException("Policy effect", id);
		}
		
    	// Actually delete effect in the database
		effectRepository.deleteById(id);
	}

}
