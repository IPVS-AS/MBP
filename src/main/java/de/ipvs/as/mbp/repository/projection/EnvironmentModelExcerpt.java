package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.env_model.EnvironmentModel;
import org.springframework.data.rest.core.config.Projection;

import java.util.Date;

@Projection(name = "list", types = EnvironmentModel.class)
public interface EnvironmentModelExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();

    String getDescription();

    String getModelJSON();

    Date getCreated();

    Date getLastModified();
}
