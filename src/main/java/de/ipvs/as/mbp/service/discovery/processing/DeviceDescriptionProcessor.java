package de.ipvs.as.mbp.service.discovery.processing;

import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionRanking;
import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionSet;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.processing.steps.DeviceDescriptionRankingCollector;
import de.ipvs.as.mbp.service.discovery.processing.steps.DeviceDescriptionSetValidityFilter;
import de.ipvs.as.mbp.service.discovery.processing.steps.DeviceDescriptionTimestampComparator;
import de.ipvs.as.mbp.service.discovery.processing.steps.DeviceDescriptionValidityFilter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This component is able to process a given collection of {@link DeviceDescriptionSet}s by filtering out invalid
 * device descriptions and duplicates, aggregating the remaining ones to a common data structure, scoring them
 * with respect to a certain {@link DeviceTemplate} and calcuating a corresponding ranking. All these steps
 * are performed as part of pre-defined, pipes-and-filters-based processing chain.
 */
@Component
public class DeviceDescriptionProcessor {

    /**
     * Creates and initializes the device description processor.
     */
    public DeviceDescriptionProcessor() {

    }

    /**
     * Processes a given collection of {@link DeviceDescriptionSet}s by filtering out invalid device descriptions
     * and duplicates, aggregating the remaining ones, scoring them with respect to a given {@link DeviceTemplate}
     * and calculating a corresponding ranking, which is returned as result of this method.
     *
     * @param deviceDescriptionSets The collection of {@link DeviceDescriptionSet}s to process
     * @param deviceTemplate        The device template to use for scoring the device descriptions
     * @return The resulting ranking of the device descriptions
     */
    public DeviceDescriptionRanking process(Collection<DeviceDescriptionSet> deviceDescriptionSets,
                                            DeviceTemplate deviceTemplate) {
        //Sanity checks
        if ((deviceDescriptionSets == null) || (deviceDescriptionSets.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The device descriptions must not be null.");
        } else if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Define and run the processing chain
        return deviceDescriptionSets.stream()
                .filter(new DeviceDescriptionSetValidityFilter(deviceTemplate)) //Filter for valid sets
                .distinct() //Remove duplicated sets (just for robustness)
                .flatMap(DeviceDescriptionSet::stream) //Flat the sets
                .filter(new DeviceDescriptionValidityFilter()) //Filter for valid device descriptions
                .sorted(new DeviceDescriptionTimestampComparator()) //Sort them by their last update timestamp
                .distinct() //Remove duplicates, preserving the descriptions with newer last update timestamp
                .collect(Collectors.toCollection( //Collect, score and rank the descriptions
                        new DeviceDescriptionRankingCollector(deviceTemplate)));
    }

}
