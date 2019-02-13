package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Event handler for operations that are performed on sensors.
 *
 * @author Jan
 */
@Component
@RepositoryEventHandler(Sensor.class)
public class SensorEventHandler {
    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called, when a sensor is supposed to be deleted. This method then takes care of undeploying it before.
     *
     * @param sensor The sensor that is supposed to be deleted
     * @throws IOException In case of an I/O issue
     */
    @HandleBeforeDelete
    public void beforeSensorDelete(Sensor sensor) throws IOException {
        sshDeployer.undeployIfRunning(sensor);
    }
}
