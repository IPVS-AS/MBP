package org.citopt.connde.service;

import org.citopt.connde.domain.device.Device;
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
public class DeviceDeleteValidator implements IDeleteValidator<Device> {
	
	@Autowired
	private SensorRepository sensorRepository;
	
	@Autowired
	private ActuatorRepository actuatorRepository;

	@Override
	public void validateDeletable(Device device) {
		if (!sensorRepository.findAllByDeviceId(device.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Device '" + device.getName() + "' cannot be deleted since it is still used by one or more sensors.");
		} else if (!actuatorRepository.findAllByDeviceId(device.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Device '" + device.getName() + "' cannot be deleted since it is still used by one or more actuators.");
		}
	}

}
