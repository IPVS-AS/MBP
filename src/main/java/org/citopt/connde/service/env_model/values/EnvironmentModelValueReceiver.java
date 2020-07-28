package org.citopt.connde.service.env_model.values;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.env_model.events.EnvironmentModelEventService;
import org.citopt.connde.service.env_model.events.types.ComponentValueEvent;
import org.citopt.connde.service.receiver.ValueLogReceiver;
import org.citopt.connde.service.receiver.ValueLogReceiverObserver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@org.springframework.stereotype.Component
public class EnvironmentModelValueReceiver implements ValueLogReceiverObserver {

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private EnvironmentModelEventService eventService;

    private ValueLogReceiver valueLogReceiver;

    @Autowired
    public EnvironmentModelValueReceiver(ValueLogReceiver valueLogReceiver) {
        this.valueLogReceiver = valueLogReceiver;

        //Observe incoming value logs
        this.valueLogReceiver.registerObserver(this);

    }

    /**
     * Called in case a new value message arrives at the ValueLogReceiver. The transformed message is passed
     * as value log.
     *
     * @param valueLog The corresponding value log that arrived
     */
    @Override
    public void onValueReceived(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            return;
        }

        //Get component type
        String componentType = valueLog.getComponent().toLowerCase();

        //Get component from repository
        Component component = null;
        if (componentType.equals("actuator")) {
            component = actuatorRepository.get(valueLog.getIdref()).get();
        } else if (componentType.equals("sensor")) {
            component = sensorRepository.get(valueLog.getIdref()).get();
        }

        //Check if component could be found
        if (component == null) {
            return;
        }

        //Get environment model of the component
        EnvironmentModel model = component.getEnvironmentModel();

        //Check if component is part of an environment model
        if (model == null) {
            return;
        }

        //Get all entities of the model
        Map<String, UserEntity> entityMap = model.getEntityMap();

        //Find node id of the component
        String nodeId = null;
        for (String currentNodeId : entityMap.keySet()) {
            if (entityMap.get(currentNodeId).equals(component)) {
                nodeId = currentNodeId;
                break;
            }
        }

        //Check if id could be found
        if ((nodeId == null) || nodeId.isEmpty()) {
            return;
        }

        //Get adapter unit
        String adapterUnit = component.getAdapter().getUnit();

        //Create corresponding event
        ComponentValueEvent event = new ComponentValueEvent(nodeId, adapterUnit, valueLog.getValue());

        //Publish event to all subscribers of the model
        eventService.publishEvent(model.getId(), event);
    }
}
