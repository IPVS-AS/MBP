package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.component.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "list", types = {Component.class})
public interface ComponentExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();
    
    String getComponentType();

    @Value("#{target.getComponentTypeName()}")
    String getComponent();
}
