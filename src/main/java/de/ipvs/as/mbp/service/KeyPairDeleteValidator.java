package de.ipvs.as.mbp.service;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.repository.DeviceRepository;
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
