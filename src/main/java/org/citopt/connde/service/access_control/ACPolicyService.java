package org.citopt.connde.service.access_control;

import java.util.List;
import java.util.stream.Collectors;

import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.access_control.dto.ACPolicyRequestDTO;
import org.citopt.connde.domain.access_control.dto.ACPolicyResponseDTO;
import org.citopt.connde.repository.ACConditionRepository;
import org.citopt.connde.repository.ACEffectRepository;
import org.citopt.connde.repository.ACPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for {@link ACPolicy policies}.
 * 
 * @author Jakob Benz
 */
@Service
public class ACPolicyService {
	
	@Autowired
	private ACPolicyRepository policyRepository;
	
	@Autowired
	private ACConditionService conditionService;
	
	@Autowired
	private ACEffectService effectService;
	
	@Autowired
	private ACConditionRepository conditionRepository;
	
	@Autowired
	private ACEffectRepository effectRepository;
	
	// - - -
	
	public List<ACPolicy> getAll(Pageable pageable) {
		return policyRepository.findAll(pageable).stream().collect(Collectors.toList());
	}
	
	public List<ACPolicy> getAllForOwner(String ownerId, Pageable pageable) {
		return policyRepository.findAllByOwner(ownerId, pageable);
	}
	
	public List<ACPolicy> getAllForOwnerAndCondition(String ownerId, String conditionId, Pageable pageable) {
		return policyRepository.findByOwnerAndCondition(ownerId, conditionId, pageable);
	}
	
	public List<ACPolicy> getAllForOwnerAndEffect(String ownerId, String effectId, Pageable pageable) {
		return policyRepository.findByOwnerAndEffect(ownerId, effectId, pageable);
	}
	
	public ACPolicy getForId(String id) {
		return policyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Access control policy with id '" + id + "' does not exist!"));
	}
	
	public ACPolicy getForIdAndOwner(String id, String ownerId) {
		ACPolicy policy = getForId(id);
		
		// Check whether the requesting user is the owner
		if (!policy.getOwnerId().equals(ownerId)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner privileges required!");
		}
		
		return policy;
	}
	
	public ACPolicy create(ACPolicyRequestDTO requestDto, String ownerId) {
		// Check whether a policy with the same name exists already
    	if (policyRepository.existsByName(requestDto.getName())) {
    		throw new ResponseStatusException(HttpStatus.CONFLICT, "An access control policy with name '" + requestDto.getName() + "' exists already!");
    	}
		
    	// Check whether condition exists
		if (!conditionRepository.existsById(requestDto.getConditionId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Access control policy condition with id '" + requestDto.getConditionId() + "' does not exist!");
		}
		
		// Check whether effect exists
		if (requestDto.getEffectId() != null && !effectRepository.existsById(requestDto.getEffectId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Access control policy effect with id '" + requestDto.getEffectId() + "' does not exist!");
		}
		
		// Create policy
		List<ACAccessType> accessTypes = requestDto.getAccessTypes().stream().map(ACAccessType::valueOf).collect(Collectors.toList());
    	ACPolicy policy = new ACPolicy(requestDto.getName(), requestDto.getDescription(), accessTypes, requestDto.getConditionId(), requestDto.getEffectId(), ownerId);
    	return policyRepository.save(policy);
	}
	
	public void delete(String id, String ownerId) {
		// Retrieve policy to delete from the database (if it exists) (includes owner check)
		getForIdAndOwner(id, ownerId);
		
    	// Actually delete policy in the database
    	policyRepository.deleteById(id);
	}
	
	public List<ACPolicyResponseDTO> policiesToResponseDto(List<ACPolicy> policies, String userId) {
		return policies.stream().map(p -> policyToResponseDto(p, userId)).collect(Collectors.toList());
	}
	
	public ACPolicyResponseDTO policyToResponseDto(ACPolicy policy, String ownerId) {
		return new ACPolicyResponseDTO()
    			.setId(policy.getId())
    			.setName(policy.getName())
    			.setDescription(policy.getDescription())
    			.setAccessTypes(policy.getAccessTypes())
    			.setCondition(conditionService.getForIdAndOwner(policy.getConditionId(), ownerId))
    			.setEffect(policy.getEffectId() != null ? effectService.getForIdAndOwner(policy.getEffectId(), ownerId) : null);
	}

}
