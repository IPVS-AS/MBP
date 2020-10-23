package org.citopt.connde.service;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.error.MBPException;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author Jakob Benz
 */
@Service
public class AdapterDeleteValidator implements IDeleteValidator<Adapter> {
	
	@Autowired
	private SensorRepository sensorRepository;
	
	@Autowired
	private ActuatorRepository actuatorRepository;

	@Override
	public void validateDeletable(Adapter adpater) {
		if (!sensorRepository.findAllByAdapterId(adpater.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Adapter '" + adpater.getName() + "' cannot be deleted since it is still used by one or more sensors.");
		} else if (!actuatorRepository.findAllByAdapterId(adpater.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Adapter '" + adpater.getName() + "' cannot be deleted since it is still used by one or more actuators.");
		}
	}

}
