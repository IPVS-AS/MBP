package org.citopt.connde.domain.component;

import org.citopt.connde.domain.user_entity.MBPEntity;
import org.citopt.connde.service.ComponentDeleteValidator;

@MBPEntity(deleteValidator = ComponentDeleteValidator.class)
public class Sensor extends Component {

    private static final String COMPONENT_TYPE_NAME = "sensor";

    @Override
    public String getComponentTypeName() {
        return COMPONENT_TYPE_NAME;
    }
}
