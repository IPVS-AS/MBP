package org.citopt.connde.service;

import org.citopt.connde.domain.operator.Operator;
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
public class OperatorDeleteValidator implements IDeleteValidator<Operator> {
	
	@Autowired
	private SensorRepository sensorRepository;
	
	@Autowired
	private ActuatorRepository actuatorRepository;

	@Override
	public void validateDeletable(Operator adpater) {
		if (!sensorRepository.findAllByOperatorId(adpater.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Operator '" + adpater.getName() + "' cannot be deleted since it is still used by one or more sensors.");
		} else if (!actuatorRepository.findAllByOperatorId(adpater.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Operator '" + adpater.getName() + "' cannot be deleted since it is still used by one or more actuators.");
		}
	}

}
