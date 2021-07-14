package de.ipvs.as.mbp.service.discovery.processing;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResultContainer;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.processing.steps.CandidateDevicesCollectionValidityFilter;
import de.ipvs.as.mbp.service.discovery.processing.steps.CandidateDevicesTimestampComparator;
import de.ipvs.as.mbp.service.discovery.processing.steps.CandidateDevicesValidityFilter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This component is able to process a given {@link CandidateDevicesResultContainer}, consisting out of multiple
 * {@link CandidateDevicesCollection}s with the descriptions of candidate devices that were retrieved for several
 * discovery repositories. The processing happens by filtering out invalid device candidates and duplicates,
 * aggregating the remaining ones to a common data structure, scoring them with respect to a
 * certain {@link DeviceTemplate} and calculating a corresponding ranking. All these steps are performed as part
 * of a pre-defined, pipes-and-filters-based processing chain.
 */
@Component
public class CandidateDevicesProcessor {

    /**
     * Creates and initializes the device description processor.
     */
    public CandidateDevicesProcessor() {

    }

    /**
     * Processes a given {@link CandidateDevicesResultContainer} by filtering out invalid candidate devices
     * and duplicates, aggregating the remaining ones, scoring them with respect to a given {@link DeviceTemplate}
     * and calculating a corresponding ranking, which is returned as result of this method.
     *
     * @param candidateDevices The {@link CandidateDevicesResultContainer} containing the candidate devices to process
     * @param deviceTemplate   The device template to use for scoring the device descriptions
     * @return The resulting ranking of the device descriptions
     */
    public CandidateDevicesRanking process(CandidateDevicesResultContainer candidateDevices, DeviceTemplate deviceTemplate) {
        //Sanity checks
        if ((candidateDevices == null) || (candidateDevices.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The candidate devices must not be null.");
        } else if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Define and run the processing chain
        List<DeviceDescription> processedCandidateDevices = candidateDevices.stream()
                .filter(new CandidateDevicesCollectionValidityFilter()) //Filter for valid collections
                .distinct() //Remove duplicated collections (just for robustness)
                .flatMap(CandidateDevicesCollection::stream) //Flat the sets
                .filter(new CandidateDevicesValidityFilter()) //Filter for valid candidate devices
                .sorted(new CandidateDevicesTimestampComparator()) //Sort them by their last update timestamp
                .distinct() //Remove duplicates, preserving the devices with newer last update timestamp
                .collect(Collectors.toList());

        //Create a ranking from the candidate devices
        return new CandidateDevicesRanking(deviceTemplate, processedCandidateDevices);
    }

}
