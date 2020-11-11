package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.operator.Operator;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.projection.ComponentExcerpt;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Event handler for operations that are performed on operators.
 */
@Component
@RepositoryEventHandler
public class OperatorEventHandler {
    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called in case an operator is supposed to be deleted. This method then takes care of deleting
     * the components which use this operator and the associated value logs.
     *
     * @param operator The operator that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeOperatorDelete(Operator operator) throws IOException {
        String operatorId = operator.getId();

        //Find actuators that use this operator and iterate over them
        List<ComponentExcerpt> affectedActuators = actuatorRepository.findAllByOperatorId(operatorId);
        for (ComponentExcerpt projection : affectedActuators) {
            Actuator actuator = actuatorRepository.findById(projection.getId()).get();

            //Undeploy actuator if running
            sshDeployer.undeployIfRunning(actuator);

            //TODO Delete value logs with idref actuator.getId()

            //Delete actuator
            actuatorRepository.deleteById(projection.getId());
        }

        //Find sensors that use this operator and iterate over them
        List<ComponentExcerpt> affectedSensors = sensorRepository.findAllByOperatorId(operatorId);
        for (ComponentExcerpt projection : affectedSensors) {
            Sensor sensor = sensorRepository.findById(projection.getId()).get();

            //Undeploy sensor if running
            sshDeployer.undeployIfRunning(sensor);

            //TODO Delete value logs with idref sensor.getId()

            //Delete sensor
            sensorRepository.deleteById(projection.getId());
        }
    }
}
