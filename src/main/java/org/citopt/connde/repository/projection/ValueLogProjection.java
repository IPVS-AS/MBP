package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.valueLog.ValueLog;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection for value logs that only consists out of fields that are relevant for the user.
 *
 * @author Jan
 */
@Projection(name = "list", types = ValueLog.class)
public interface ValueLogProjection {

    String getDate();

    String getValue();

    String getTopic();

    String getMessage();
}
