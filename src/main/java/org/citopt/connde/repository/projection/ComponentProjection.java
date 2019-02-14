package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.component.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "list", types = {Component.class})
public interface ComponentProjection {

    String getId();

    String getName();
    
    String getComponentType();

    @Value("#{target.getComponentTypeName()}")
    String getComponent();
}
