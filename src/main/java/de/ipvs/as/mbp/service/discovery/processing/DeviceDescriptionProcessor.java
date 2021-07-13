package de.ipvs.as.mbp.service.discovery.processing;

import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionRanking;
import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionCollection;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.processing.steps.DeviceDescriptionSetValidityFilter;
import de.ipvs.as.mbp.service.discovery.processing.steps.DeviceDescriptionTimestampComparator;
import de.ipvs.as.mbp.service.discovery.processing.steps.DeviceDescriptionValidityFilter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This component is able to process a given collection of {@link DeviceDescriptionCollection}s by filtering out invalid
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
     * Processes a given collection of {@link DeviceDescriptionCollection}s by filtering out invalid device descriptions
     * and duplicates, aggregating the remaining ones, scoring them with respect to a given {@link DeviceTemplate}
     * and calculating a corresponding ranking, which is returned as result of this method.
     *
     * @param deviceDescriptionCollections The collection of {@link DeviceDescriptionCollection}s to process
     * @param deviceTemplate        The device template to use for scoring the device descriptions
     * @return The resulting ranking of the device descriptions
     */
    public DeviceDescriptionRanking process(Collection<DeviceDescriptionCollection> deviceDescriptionCollections,
                                            DeviceTemplate deviceTemplate) {
        //Sanity checks
        if ((deviceDescriptionCollections == null) || (deviceDescriptionCollections.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The device descriptions must not be null.");
        } else if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Define and run the processing chain
         List<DeviceDescription> deviceDescriptionList = deviceDescriptionCollections.stream()
                .filter(new DeviceDescriptionSetValidityFilter(deviceTemplate)) //Filter for valid sets
                .distinct() //Remove duplicated sets (just for robustness)
                .flatMap(DeviceDescriptionCollection::stream) //Flat the sets
                .filter(new DeviceDescriptionValidityFilter()) //Filter for valid device descriptions
                .sorted(new DeviceDescriptionTimestampComparator()) //Sort them by their last update timestamp
                .distinct() //Remove duplicates, preserving the descriptions with newer last update timestamp
                .collect(Collectors.toList());

        //Create a ranking from the device descriptions
        return new DeviceDescriptionRanking(deviceTemplate, deviceDescriptionList);
    }

}
