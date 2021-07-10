package de.ipvs.as.mbp.domain.discovery.collections;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionScorer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Objects of this class represent ordered lists of {@link DeviceDescription}s which are sorted by the scores that
 * can be calculated for each device description with respect to a given {@link DeviceTemplate}. Despite being a list,
 * duplicated device descriptions are automatically eliminated.
 */
public class DeviceDescriptionRanking implements Iterable<ScoredDeviceDescription> {
    //The device template to use as reference for calculating the device description scores
    private DeviceTemplate deviceTemplate;

    //Internally managed set of all device descriptions
    private final Set<DeviceDescription> deviceDescriptions = new HashSet<>();

    //Sorted list of device descriptions as resulting from the set
    private List<ScoredDeviceDescription> deviceDescriptionsRanking = Collections.emptyList();


    /**
     * Creates and initializes a new device description ranking by passing a reference to the
     * {@link DeviceTemplate} that is supposed to be used for calculating the scores of the {@link DeviceDescription}s.
     *
     * @param deviceTemplate The device template to use for calculating the scores
     */
    public DeviceDescriptionRanking(DeviceTemplate deviceTemplate) {
        //Set device template
        setDeviceTemplate(deviceTemplate);
    }

    /**
     * Creates and initializes a new device description ranking by passing a reference to the
     * {@link DeviceTemplate} that is supposed to be used for calculating the scores of the {@link DeviceDescription}s.
     * Furthermore, an initial collection of {@link DeviceDescription}s is provided from which the first ranking
     * is calculated.
     *
     * @param deviceTemplate The device template to use for calculating the scores
     */
    public DeviceDescriptionRanking(DeviceTemplate deviceTemplate, Collection<DeviceDescription> deviceDescriptions) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Store device template
        this.deviceTemplate = deviceTemplate;

        //Add device descriptions
        this.addAll(deviceDescriptions);
    }

    /**
     * Returns the {@link DeviceTemplate} that is used for calculating the scores of the {@link DeviceDescription}s.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return this.deviceTemplate;
    }

    /**
     * Sets the {@link DeviceTemplate} that is supposed to be used for calculating the scores of the
     * {@link DeviceDescription}s.
     *
     * @param deviceTemplate The device template to set
     */
    public void setDeviceTemplate(DeviceTemplate deviceTemplate) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Store device template
        this.deviceTemplate = deviceTemplate;

        //Re-calculate the ranking
        updateRanking();
    }

    /**
     * Re-calculates the scores of the device descriptions and adjusts the ranking accordingly.
     */
    private void updateRanking() {
        //Create device description scorer
        DeviceDescriptionScorer scorer = new DeviceDescriptionScorer(this.deviceTemplate, this.deviceDescriptions);

        //Score all device descriptions
        this.deviceDescriptionsRanking = this.deviceDescriptions.stream()
                .map(d -> new ScoredDeviceDescription(d, scorer.score(d))) //Calculate scores
                .sorted() //Sort as comparables
                .collect(Collectors.toList());
    }


    /**
     * Returns the size of the ranking, i.e. the number of {@link DeviceDescription}s that are part of it.
     *
     * @return The number of device descriptions
     */
    public int size() {
        return this.deviceDescriptionsRanking.size();
    }

    /**
     * Returns whether the ranking is empty, i.e. contains no device descriptions.
     *
     * @return True, if the ranking is empty; false othwise
     */
    public boolean isEmpty() {
        return this.deviceDescriptionsRanking.isEmpty();
    }

    /**
     * Returns whether the ranking contains a given object.
     *
     * @param o The object to check
     * @return True, if the ranking contains the object; false otherwise
     */
    public boolean contains(Object o) {
        return this.deviceDescriptionsRanking.contains(o);
    }

    /**
     * Adds a single device description to the ranking.
     *
     * @param deviceDescription The device description to add
     * @return True, if the ranking did not already contain the device description; false otherwise
     */
    public boolean add(DeviceDescription deviceDescription) {
        //Null check
        if (deviceDescription == null) {
            throw new IllegalArgumentException("The device description must not be null.");
        }

        //Add device description
        boolean changed = this.deviceDescriptions.add(deviceDescription);

        //Update the ranking
        updateRanking();

        //Return if added
        return changed;
    }

    /**
     * Removes a given object from the ranking.
     *
     * @param o The object to remove
     * @return True, if the object was contained in the ranking; false otherwise
     */
    public boolean remove(Object o) {
        //Null check
        if (o == null) {
            return false;
        }

        //Remove device description
        boolean contained = this.deviceDescriptions.remove(o);

        //Update ranking
        updateRanking();

        //Return if object was contained
        return contained;
    }

    /**
     * Adds a collection of {@link DeviceDescription}s to the ranking.
     *
     * @param deviceDescriptions The collections of device descriptions to add
     */
    public void addAll(Collection<DeviceDescription> deviceDescriptions) {
        //Null checks
        if ((deviceDescriptions == null) || deviceDescriptions.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The device descriptions must not be null.");
        }

        //Add all device descriptions
        this.deviceDescriptions.addAll(deviceDescriptions);

        //Update the ranking
        updateRanking();
    }

    /**
     * Removes all device descriptions from the ranking.
     */
    public void clear() {
        this.deviceDescriptions.clear();
        updateRanking();
    }

    /**
     * Returns the first device description of the ranking or null, if the ranking is empty.
     *
     * @return The first device description
     */
    public ScoredDeviceDescription first() {
        return this.isEmpty() ? null : this.deviceDescriptionsRanking.get(0);
    }

    /**
     * Returns a device description with a certain, zero-based rank or null if it does not exist.
     *
     * @param rank The rank of the device description to return
     * @return The device description at the given rank
     */
    public ScoredDeviceDescription get(int rank) {
        if (rank > this.deviceDescriptionsRanking.size() - 1) {
            //Not available
            return null;
        }
        return this.deviceDescriptionsRanking.get(rank);
    }

    /**
     * Returns an iterator that allows to iterate over the ranking in ascending rank order.
     *
     * @return The resulting iterator
     */
    public Iterator<ScoredDeviceDescription> iterator() {
        return this.deviceDescriptionsRanking.iterator();
    }

    /**
     * Returns an ordered stream of the ranking.
     *
     * @return The resulting stream
     */
    public Stream<ScoredDeviceDescription> stream() {
        //Stream through the queue while preserving the order
        return deviceDescriptionsRanking.stream().sorted();
    }

    /**
     * Returns an ordered list representation of the ranking.
     *
     * @return The resulting list representing the ranking
     */
    public List<ScoredDeviceDescription> toList() {
        return this.deviceDescriptionsRanking;
    }
}
