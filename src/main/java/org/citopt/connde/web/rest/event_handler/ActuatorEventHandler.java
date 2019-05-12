package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Event handler for operations that are performed on actuators.
 */
@Component
@RepositoryEventHandler
public class ActuatorEventHandler {
    @Autowired
    private CEPTriggerService triggerService;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called, when an actuator was created. This method then takes care of registering a corresponding
     * event type at the CEP engine.
     *
     * @param actuator The created actuator
     */
    @HandleAfterCreate
    public void afterActuatorCreate(Actuator actuator) {
        triggerService.registerComponentEventType(actuator);
    }

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
}
