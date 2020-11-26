package org.citopt.connde.service;

import org.citopt.connde.domain.key_pair.KeyPair;
import org.citopt.connde.error.MBPException;
import org.citopt.connde.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author Jakob Benz
 */
@Service
public class KeyPairDeleteValidator implements IDeleteValidator<KeyPair> {
	
	@Autowired
	private DeviceRepository deviceRepository;

	@Override
	public void validateDeletable(KeyPair keyPair) {
		if (!deviceRepository.findAllByKeyPairId(keyPair.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Key pair '" + keyPair.getName() + "' cannot be deleted since it is still used by a device.");
		}
	}

}
