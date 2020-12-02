package de.ipvs.as.mbp.service.access_control;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.access_control.ACPolicy;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingOwnerPrivilegesException;
import de.ipvs.as.mbp.repository.ACConditionRepository;
import de.ipvs.as.mbp.repository.ACEffectRepository;
import de.ipvs.as.mbp.repository.ACPolicyRepository;
import de.ipvs.as.mbp.domain.access_control.dto.ACPolicyRequestDTO;
import de.ipvs.as.mbp.domain.access_control.dto.ACPolicyResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
	
	public ACPolicy getForId(String id) throws EntityNotFoundException {
		return policyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Policy", id));
	}
	
	public ACPolicy getForIdAndOwner(String id, String ownerId) throws EntityNotFoundException, MissingOwnerPrivilegesException {
		ACPolicy policy = getForId(id);
		
		// Check whether the requesting user is the owner
		if (!policy.getOwnerId().equals(ownerId)) {
			throw new MissingOwnerPrivilegesException();
		}
		
		return policy;
	}
	
	public ACPolicy create(ACPolicyRequestDTO requestDto, String ownerId) throws EntityAlreadyExistsException, EntityNotFoundException {
		// Check whether a policy with the same name exists already
    	if (policyRepository.existsByName(requestDto.getName())) {
    		throw new EntityAlreadyExistsException("Policy", requestDto.getName());
    	}
		
    	// Check whether condition exists
		if (!conditionRepository.existsById(requestDto.getConditionId())) {
			throw new EntityNotFoundException("Policy condition", requestDto.getConditionId());
		}
		
		// Check whether effect exists
		if (requestDto.getEffectId() != null && !effectRepository.existsById(requestDto.getEffectId())) {
			throw new EntityNotFoundException("Policy effect", requestDto.getEffectId());
		}
		
		// Create policy
		List<ACAccessType> accessTypes = requestDto.getAccessTypes().stream().map(ACAccessType::valueOf).collect(Collectors.toList());
    	ACPolicy policy = new ACPolicy(requestDto.getName(), requestDto.getDescription(), accessTypes, requestDto.getConditionId(), requestDto.getEffectId(), ownerId);
    	return policyRepository.save(policy);
	}
	
	public void delete(String id, String ownerId) throws EntityNotFoundException, MissingOwnerPrivilegesException {
		// Retrieve policy to delete from the database (if it exists) (includes owner check)
		getForIdAndOwner(id, ownerId);
		
    	// Actually delete policy in the database
    	policyRepository.deleteById(id);
	}
	
	public List<ACPolicyResponseDTO> policiesToResponseDto(List<ACPolicy> policies, String userId) throws EntityNotFoundException, MissingOwnerPrivilegesException {
		List<ACPolicyResponseDTO> responseDtos = new ArrayList<>();
		for (ACPolicy p : policies) {
			responseDtos.add(policyToResponseDto(p, userId));
		}
		return responseDtos;
	}
	
	public ACPolicyResponseDTO policyToResponseDto(ACPolicy policy, String ownerId) throws EntityNotFoundException, MissingOwnerPrivilegesException {
		return new ACPolicyResponseDTO()
    			.setId(policy.getId())
    			.setName(policy.getName())
    			.setDescription(policy.getDescription())
    			.setAccessTypes(policy.getAccessTypes())
    			.setCondition(conditionService.getForIdAndOwner(policy.getConditionId(), ownerId))
    			.setEffect(policy.getEffectId() != null ? effectService.getForIdAndOwner(policy.getEffectId(), ownerId) : null);
	}

}
