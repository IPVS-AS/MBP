package de.ipvs.as.mbp.web.rest.event_handler;

import java.io.IOException;

import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.service.deploy.SSHDeployer;
import de.ipvs.as.mbp.service.cep.trigger.CEPTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * Event handler for operations that are performed on sensors.
 */
@Component
@RepositoryEventHandler
public class SensorEventHandler {

    @Autowired
    private CEPTriggerService triggerService;

    @Autowired
    private SSHDeployer sshDeployer;


    /**
     * Called in case a sensor was created. This method then takes care of registering a corresponding
     * event type at the CEP engine.
     *
     * @param sensor The created sensor
     */
    @HandleAfterCreate
    public void afterSensorCreate(Sensor sensor) {
        triggerService.registerComponentEventType(sensor);
    }

    /**
     * Called in case a sensor is supposed to be deleted. This method then takes care of undeploying it before.
     *
     * @param sensor The sensor that is supposed to be deleted
     * @throws IOException In case of an I/O issue
     */
    @HandleBeforeDelete
    public void beforeSensorDelete(Sensor sensor) throws IOException {
        sshDeployer.undeployIfRunning(sensor);

    }

    /**
     * Called in case a sensor is supposed to be deleted. This method then takes care of deleting all
     * value logs that are associated with this sensor.
     *
     * @param sensor The sensor that is supposed to be deleted
     */
    @HandleAfterDelete
    public void afterSensorDelete(Sensor sensor) {
        //TODO Delete value logs with idref sensor.getId()
    }
}
