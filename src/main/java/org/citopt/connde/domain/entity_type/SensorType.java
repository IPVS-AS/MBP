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

    /**
     * Creates and returns a new sensor type by a given name and an icon context.
     *
     * @param name        The name of the new sensor type
     * @param iconContent An URL to an existing icon or a base64 string of an icon to use for this sensor entity
     * @return The created sensor type
     */
    public static SensorType createSensorType(String name, String iconContent) {
        //Create sensor type
        SensorType sensorType = new SensorType(name);

        //Set icon
        sensorType.setIcon(new EntityTypeIcon(iconContent));
        return sensorType;
    }
}
