package de.ipvs.as.mbp.domain.discovery.collections;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDeviceScorer;
import de.ipvs.as.mbp.service.discovery.ranking.CandidateDevicesRanker;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Objects of this class represent ordered lists of candidate devices, given by their {@link DeviceDescription}s,
 * which are automatically sorted by the scores that can be calculated for each device with respect to a given
 * {@link DeviceTemplate}. Despite being a list, duplicated candidate devices are automatically eliminated.
 */
public class CandidateDevicesRanking implements Iterable<ScoredCandidateDevice> {
    //The comparator to use for sorting the ranking
    private static final Comparator<ScoredCandidateDevice> COMPARATOR = new CandidateDevicesRanker();

    //The device template to use as reference for calculating the device description scores
    private DeviceTemplate deviceTemplate;

    //Internally managed set of all candidate devices
    private final Set<DeviceDescription> candidateDevices = new HashSet<>();

    //Sorted list of device descriptions as resulting from the set
    private List<ScoredCandidateDevice> candidateDevicesRanking = Collections.emptyList();


    /**
     * Creates and initializes a new candidate devices ranking by passing a reference to the
     * {@link DeviceTemplate} that is supposed to be used for calculating the scores of the candidate devices.
     *
     * @param deviceTemplate The device template to use for calculating the scores
     */
    public CandidateDevicesRanking(DeviceTemplate deviceTemplate) {
        //Set device template
        setDeviceTemplate(deviceTemplate);
    }

    /**
     * Creates and initializes a new candidates devices ranking by passing a reference to the
     * {@link DeviceTemplate} that is supposed to be used for calculating the scores of the {@link DeviceDescription}s.
     * Furthermore, an initial collection of candidate devices, given by their {@link DeviceDescription}s, is provided
     * from which the first ranking is calculated.
     *
     * @param deviceTemplate   The device template to use for calculating the scores
     * @param candidateDevices The descriptions of the initial candidate devices
     */
    public CandidateDevicesRanking(DeviceTemplate deviceTemplate, Collection<DeviceDescription> candidateDevices) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Store device template
        this.deviceTemplate = deviceTemplate;

        //Add candidate devices
        this.addAll(candidateDevices);
    }

    /**
     * Returns the {@link DeviceTemplate} that is supposed to be used for calculating the scores of the
     * candidate devices.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return this.deviceTemplate;
    }

    /**
     * Sets the {@link DeviceTemplate} that is supposed to be used for calculating the scores of the candidate devices.
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
     * Returns the score of the first candidate device within the ranking that matches a given MAC address. If
     * no device is found that matches the MAC address, <code>-1</code> is returned.
     *
     * @param macAddress The MAC address to match
     * @return The score of the candidate device with the MAC address or -1 if not found
     */
    public double getScoreByMacAddress(String macAddress) {
        //Sanity check
        if ((macAddress == null) || (macAddress.isEmpty())) return -1;

        //Stream through the ranking in order
        return this.stream()
                //Filter for matching MAC addresses
                .filter(d -> (d.getIdentifiers() != null) && macAddress.equals(d.getIdentifiers().getMacAddress()))
                .map(ScoredCandidateDevice::getScore) //Map device to score
                .findFirst().orElse(-1.0); //Return the score or -1 if no matching device found
    }

    /**
     * Re-calculates the scores of the candidate devices and adjusts the ranking accordingly.
     */
    private void updateRanking() {
        //Create candidate device scorer
        CandidateDeviceScorer scorer = new CandidateDeviceScorer(this.deviceTemplate, this.candidateDevices);

        //Score all candidate devices by using their descriptions
        this.candidateDevicesRanking = this.candidateDevices.stream()
                .map(d -> new ScoredCandidateDevice(d, scorer.score(d))) //Calculate scores
                .sorted(COMPARATOR) //Sort using the comparator
                .collect(Collectors.toList()); //Collect ranking as sorted list
    }


    /**
     * Returns the size of the ranking, i.e. the number of candidate devices that are contained.
     *
     * @return The number of candidate devices
     */
    public int size() {
        return this.candidateDevicesRanking.size();
    }

    /**
     * Returns whether the ranking is empty, i.e. contains no candidate devices.
     *
     * @return True, if the ranking is empty; false otherwise
     */
    public boolean isEmpty() {
        return this.candidateDevicesRanking.isEmpty();
    }

    /**
     * Returns whether the ranking contains a given object.
     *
     * @param o The object to check
     * @return True, if the ranking contains the object; false otherwise
     */
    public boolean contains(Object o) {
        return this.candidateDevicesRanking.contains(o);
    }

    /**
     * Adds a single candidate device, given by its {@link DeviceDescription}, to the ranking.
     *
     * @param candidateDevice The description of the candidate device to add
     * @return True, if the ranking did not already contain the device description; false otherwise
     */
    public boolean add(DeviceDescription candidateDevice) {
        //Null check
        if (candidateDevice == null) {
            throw new IllegalArgumentException("The device description must not be null.");
        }

        //Add candidate device
        boolean changed = this.candidateDevices.add(candidateDevice);

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
        boolean contained = this.candidateDevices.remove(o);

        //Update ranking
        updateRanking();

        //Return if object was contained
        return contained;
    }

    /**
     * Adds a collection of candidate devices, given by their {@link DeviceDescription}s, to the ranking.
     *
     * @param candidateDevices The descriptions of the candidate devices to add
     */
    public void addAll(Collection<DeviceDescription> candidateDevices) {
        //Null checks
        if ((candidateDevices == null) || candidateDevices.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The candidate devices must not be null.");
        }

        //Add all candidate devices
        this.candidateDevices.addAll(candidateDevices);

        //Update the ranking
        updateRanking();
    }

    /**
     * Removes all candidate devices from the ranking.
     */
    public void clear() {
        this.candidateDevices.clear();
        updateRanking();
    }

    /**
     * Returns the first candidate device from the ranking or null, if the ranking is empty.
     *
     * @return The first candidate device
     */
    public ScoredCandidateDevice first() {
        return this.isEmpty() ? null : this.candidateDevicesRanking.get(0);
    }

    /**
     * Returns the candidate device with a certain, zero-based rank from the ranking or null if it does not exist.
     *
     * @param rank The rank of the candidate device
     * @return The candidate device at the given rank
     */
    public ScoredCandidateDevice get(int rank) {
        //Check bounds
        if (rank > this.candidateDevicesRanking.size() - 1) {
            //Not available
            return null;
        }

        //Retrieve description
        return this.candidateDevicesRanking.get(rank);
    }

    /**
     * Returns an iterator that allows to iterate over the ranking in ascending rank order.
     *
     * @return The resulting iterator
     */
    public Iterator<ScoredCandidateDevice> iterator() {
        return this.candidateDevicesRanking.iterator();
    }

    /**
     * Returns an ordered stream of the ranking.
     *
     * @return The resulting stream
     */
    public Stream<ScoredCandidateDevice> stream() {
        //Stream through the list while preserving the order
        return candidateDevicesRanking.stream().sorted();
    }

    /**
     * Returns an ordered list representation of the ranking.
     *
     * @return The resulting list representing the ranking
     */
    public List<ScoredCandidateDevice> toList() {
        return this.candidateDevicesRanking;
    }
}
