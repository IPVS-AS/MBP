package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@Projection(name = "list", types = {Component.class})
public interface ComponentExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();
    
    String getComponentType();

    @Value("#{target.getComponentTypeName()}")
    String getComponent();
}
