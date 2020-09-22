package org.citopt.connde.service.access_control;

import java.util.List;
import java.util.stream.Collectors;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.dto.ACConditionRequestDTO;
import org.citopt.connde.repository.ACConditionRepository;
import org.citopt.connde.repository.ACPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Service for all {@link ACAbstractCondition} implementation.
 * 
 * @author Jakob Benz
 */
@Service
public class ACConditionService {
	
	@Autowired
	private ACPolicyRepository policyRepository;
	
	@Autowired
	private ACConditionRepository conditionRepository;
	
	// - - -
	
	public List<ACAbstractCondition> getAll(Pageable pageable) {
		return conditionRepository.findAll(pageable)
				.stream()
				.map(ACAbstractCondition::computeAndSetHumanReadableDescription)
				.collect(Collectors.toList());
	}
	
	public List<ACAbstractCondition> getAllForOwner(String ownerId, Pageable pageable) {
		return conditionRepository.findAllByOwner(ownerId, pageable)
				.stream()
				.filter(p -> p.getOwnerId().equals(ownerId))
				.map(ACAbstractCondition::computeAndSetHumanReadableDescription)
				.collect(Collectors.toList());
	}
	
	public ACAbstractCondition getForId(String id) {
		return conditionRepository.findById(id)
				.map(ACAbstractCondition::computeAndSetHumanReadableDescription)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Access control policy with id '" + id + "' does not exist!"));
	}
	
	public ACAbstractCondition getForIdAndOwner(String id, String ownerId) {
		ACAbstractCondition condition = getForId(id);
		
		// Check whether the requesting user is the owner
		if (!condition.getOwnerId().equals(ownerId)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner privileges required!");
		}
		
		return condition;
	}
	
	public ACAbstractCondition create(ACConditionRequestDTO requestDto, String ownerId) {
		// Check whether a condition with the same name exists already
    	if (conditionRepository.existsByName(requestDto.getName())) {
    		throw new ResponseStatusException(HttpStatus.CONFLICT, "An access control policy condition with name '" + requestDto.getName() + "' exists already!");
    	}
		
		// Create condition
    	ACAbstractCondition condition = null;
		try {
			condition = (ACAbstractCondition) ACAbstractCondition.forJQBOutput(requestDto.getCondition())
					.setName(requestDto.getName())
					.setDescription(requestDto.getDescription())
					.setOwnerId(ownerId);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Condition could not be parsed!");
		}
		
		return conditionRepository.save(condition);
	}
	
	public void delete(String id, String ownerId) {
		// Retrieve condition to delete from the database (if it exists) (includes owner check)
		getForIdAndOwner(id, ownerId);
		
		// Check whether condition is used by a policy
		if (policyRepository.countUsingCondition(id) > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This condition is being used by at least one policy an cannot be deleted.");
		}
		
    	// Actually delete condition in the database
    	conditionRepository.deleteById(id);
	}

}
