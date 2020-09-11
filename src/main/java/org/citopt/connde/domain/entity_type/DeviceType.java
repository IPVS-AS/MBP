package org.citopt.connde.domain.entity_type;

import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user_entity.UserEntity;

public class DeviceType extends EntityType {



    /**
     * Returns the class of the user entity for which this type is for.
     *
     * @return The class of the user entity
     */
    @Override
    public Class<? extends UserEntity> getEntityClass() {
        return Device.class;
    }
}
