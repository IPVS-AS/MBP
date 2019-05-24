package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Event handler for operations that are performed on actuators.
 *
 * @author Jan
 */
@Component
@RepositoryEventHandler
public class ActuatorEventHandler {

    @Autowired
    private ValueLogRepository valueLogRepository;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called, when an actuator is supposed to be deleted. This method then takes care of undeploying it before.
     *
     * @param actuator The actuator that is supposed to be deleted
     * @throws IOException In case of an I/O issue
     */
    @HandleBeforeDelete
    public void beforeActuatorDelete(Actuator actuator) throws IOException {
        sshDeployer.undeployIfRunning(actuator);
    }

    /**
     * Called in case an actuator is supposed to be deleted. This method then takes care of deleting all
     * value logs that are associated with this actuator.
     *
     * @param actuator The actuator that is supposed to be deleted
     */
    @HandleAfterDelete
    public void afterActuatorDelete(Actuator actuator) {
        //Get affected value logs
        List<ValueLog> valueLogs = valueLogRepository.findListByIdref(actuator.getId());

        //Delete the logs from repository
        valueLogRepository.delete(valueLogs);
    }
}
