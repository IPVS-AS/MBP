package de.ipvs.as.mbp.domain.component;

import de.ipvs.as.mbp.service.cep.trigger.CEPTriggerService;
import de.ipvs.as.mbp.service.event_handler.ICreateEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creation event handler for component entities.
 */
@Service
public class ComponentCreateEventHandler implements ICreateEventHandler<Component> {

    @Autowired
    private CEPTriggerService triggerService;

    /**
     * Called in case an entity has been created and saved successfully.
     *
     * @param entity The created entity
     */
    @Override
    public void onCreate(Component entity) {
        //Register component at trigger service
        triggerService.registerComponentEventType(entity);
    }
}
