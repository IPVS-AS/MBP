package org.citopt.connde.domain.component;

/**
 *
 * @author rafaelkperes
 */
public class Actuator extends Component {

    private static final String COMPONENT_TYPE_NAME = "actuator";

    @Override
    public String getComponentTypeName() {
        return COMPONENT_TYPE_NAME;
    }
}
