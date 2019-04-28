package org.citopt.connde.service.cep.core.queries;

import org.citopt.connde.service.cep.core.queries.output.CEPOutput;

public interface CEPQuerySubscriber {
    void onEventTriggered(CEPOutput outputColumns);
}
