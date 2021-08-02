package de.ipvs.as.mbp.domain.discovery.messages.query;

import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Template for request messages that are supposed to request collections of suitable candidate devices, matching
 * certain {@link DeviceTemplate}s, from the discovery repositories. For this, each request message consist out of
 * a set of {@link CandidateDevicesQuery}s, each specifying {@link DeviceRequirement}s and optional
 * {@link ScoringCriterion}s that were derived from such a {@link DeviceTemplate}. Based on these, the discovery
 * repositories that receive the request message are expected to determine matching candidate devices for each
 * {@link CandidateDevicesQuery} and send their descriptions back as {@link CandidateDevicesReply}.
 * Furthermore, each {@link CandidateDevicesQuery} can specify whether a subscription is supposed to be created
 * for this query at the discovery repositories, such that the MBP becomes asynchronously notified when
 * the collection of suitable candidate devices changes over time for the pertaining {@link DeviceTemplate}.
 */
@DomainMessageTemplate(value = "query", topicSuffix = "query")
public class CandidateDevicesRequest extends DomainMessageBody {

    //The set of candidate devices queries, one per device template
    private Set<CandidateDevicesQuery> queries;

    /**
     * Creates a new, empty {@link CandidateDevicesRequest} message.
     */
    public CandidateDevicesRequest() {
        //Initialize data structures
        this.queries = new HashSet<>();
    }


    /**
     * Creates a new {@link CandidateDevicesRequest} message from a given collection of {@link CandidateDevicesQuery}s.
     *
     * @param queries The collection of {@link CandidateDevicesQuery}s to set
     */
    public CandidateDevicesRequest(Collection<CandidateDevicesQuery> queries) {
        setQueries(queries);
    }


    /**
     * Returns the {@link CandidateDevicesQuery}s that are contained in the request message.
     *
     * @return The {@link CandidateDevicesQuery}s
     */
    public Set<CandidateDevicesQuery> getQueries() {
        return queries;
    }

    /**
     * Sets the the {@link CandidateDevicesQuery}s that are contained in the request message.
     *
     * @param queries The collection of {@link CandidateDevicesQuery}s to set
     * @return The request message
     */
    public CandidateDevicesRequest setQueries(Collection<CandidateDevicesQuery> queries) {
        //Null check
        if ((queries == null) || queries.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The queries must not be null.");
        }

        //Set queries
        this.queries = new HashSet<>(queries);
        return this;
    }

    /**
     * Adds a given {@link CandidateDevicesQuery} to the request message.
     *
     * @param query The {@link CandidateDevicesQuery} to add
     * @return The request message
     */
    public CandidateDevicesRequest addQuery(CandidateDevicesQuery query) {
        //Null check
        if (query == null) throw new IllegalArgumentException("The query must not be null.");

        //Add the query
        this.queries.add(query);
        return this;
    }
}
