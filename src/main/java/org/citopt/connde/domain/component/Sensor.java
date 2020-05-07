package org.citopt.connde.domain.component;

public class Sensor extends Component {

    private static final String COMPONENT_TYPE_NAME = "sensor";

    @Override
    public String getComponentTypeName() {
        return COMPONENT_TYPE_NAME;
    }
}
