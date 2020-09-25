package org.citopt.connde.domain.entity_type;

import org.citopt.connde.domain.component.Sensor;

public class SensorType extends EntityType {

    public SensorType() {
        super();
    }

    public SensorType(String name) {
        super(name);
    }

    /**
     * Returns the class of the user entity for which this type is for.
     *
     * @return The class of the user entity
     */
    @Override
    public Class<Sensor> getEntityClass() {
        return Sensor.class;
    }
}
