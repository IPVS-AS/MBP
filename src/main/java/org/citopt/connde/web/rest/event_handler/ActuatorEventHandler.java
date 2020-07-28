package org.citopt.connde.web.rest.event_handler;

import java.io.IOException;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

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
     * Called in case an actuator was created. This method then takes care of registering a corresponding
     * event type at the CEP engine.
     *
     * @param actuator The created actuator
     */
    @HandleAfterCreate
    public void afterActuatorCreate(Actuator actuator) {
        triggerService.registerComponentEventType(actuator);
    }

    /**
     * Called in case an actuator is supposed to be deleted. This method then takes care of undeploying it before.
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
        //TODO Delete value logs with idref actuator.getId()
    }
}
