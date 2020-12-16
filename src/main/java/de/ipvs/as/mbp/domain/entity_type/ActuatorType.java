package de.ipvs.as.mbp.domain.entity_type;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;

@MBPEntity(createValidator = EntityTypeCreateValidator.class)
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

    /**
     * Creates and returns a new actuator type by a given name and an icon context.
     *
     * @param name        The name of the new actuator type
     * @param iconContent An URL to an existing icon or a base64 string of an icon to use for this actuator entity
     * @return The created actuator type
     */
    public static ActuatorType createActuatorType(String name, String iconContent) {
        //Create actuator type
        ActuatorType actuatorType = new ActuatorType(name);

        //Set icon
        actuatorType.setIcon(new EntityTypeIcon(iconContent));
        return actuatorType;
    }
}
