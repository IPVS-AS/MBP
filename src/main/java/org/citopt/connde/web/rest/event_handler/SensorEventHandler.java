package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Event handler for operations that are performed on sensors.
 */
@Component
@RepositoryEventHandler
public class SensorEventHandler {
    @Autowired
    private ValueLogRepository valueLogRepository;

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
