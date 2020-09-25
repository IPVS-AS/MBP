package org.citopt.connde.domain.entity_type;

import org.citopt.connde.domain.component.Actuator;

public class ActuatorType extends EntityType {

    public ActuatorType(){
        super();
    }

    public ActuatorType(String name) {
        super(name);
    }

    /**
     * Returns the class of the user entity for which this type is for.
     *
     * @return The class of the user entity
     */
    @Override
    public Class<Actuator> getEntityClass() {
        return Actuator.class;
    }
}
