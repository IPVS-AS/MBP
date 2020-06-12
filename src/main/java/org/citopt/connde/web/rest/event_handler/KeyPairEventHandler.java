package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.key_pair.KeyPair;
import org.citopt.connde.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Event handler for operations that are performed on devices.
 */
@Component
@RepositoryEventHandler
public class KeyPairEventHandler {

    @Autowired
    DeviceEventHandler deviceEventHandler;

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * Called in case a key pair is supposed to be deleted. This method then takes care of deleting
     * the devices which use this key pair.
     *
     * @param keyPair The key pair that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeKeyPairDelete(KeyPair keyPair) throws IOException {
        //Sanity check
        if (keyPair == null) {
            return;
        }

        //Find devices that use this key pair
        List<Device> affectedDevices = deviceRepository.findAllByKeyPairId(keyPair.getId());

        //Iterate over these devices
        for (Device device : affectedDevices) {
            //Clean up everything related to the device
            deviceEventHandler.beforeDeviceDelete(device);

            //Finally delete the device
            deviceRepository.delete(device);
        }
    }
}