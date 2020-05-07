package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.domain.rules.RuleActionType;
import org.springframework.data.rest.core.config.Projection;

import java.util.Map;

/**
 * Excerpt projection for rule action entities.
 */
@Projection(name = "list", types = RuleAction.class)
public interface RuleActionExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();

    RuleActionType getType();
}
