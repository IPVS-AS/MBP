package de.ipvs.as.mbp.service.env_model.values;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.env_model.EnvironmentModel;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import de.ipvs.as.mbp.service.receiver.ValueLogObserver;
import de.ipvs.as.mbp.service.env_model.events.EnvironmentModelEventService;
import de.ipvs.as.mbp.service.env_model.events.types.ComponentValueEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@org.springframework.stereotype.Component
public class EnvironmentModelValueReceiver implements ValueLogObserver {

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
            component = actuatorRepository.findById(valueLog.getIdref()).get();
        } else if (componentType.equals("sensor")) {
            component = sensorRepository.findById(valueLog.getIdref()).get();
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
        String adapterUnit = component.getOperator().getUnit();

        //Create corresponding event
        ComponentValueEvent event = new ComponentValueEvent(nodeId, adapterUnit, valueLog.getValue());

        //Publish event to all subscribers of the model
        eventService.publishEvent(model.getId(), event);
    }
}
