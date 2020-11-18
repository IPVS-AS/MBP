package de.ipvs.as.mbp.domain.component;

import de.ipvs.as.mbp.service.ComponentDeleteValidator;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;

@MBPEntity(deleteValidator = ComponentDeleteValidator.class)
public class Sensor extends Component {

    private static final String COMPONENT_TYPE_NAME = "sensor";

    @Override
    public String getComponentTypeName() {
        return COMPONENT_TYPE_NAME;
    }
}
