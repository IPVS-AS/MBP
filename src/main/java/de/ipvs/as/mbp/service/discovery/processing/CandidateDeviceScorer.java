package de.ipvs.as.mbp.service.discovery.processing;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.device.scoring.description.DeviceDescriptionTokenizer;
import smile.nlp.SimpleCorpus;
import smile.nlp.Text;
import smile.nlp.dictionary.EnglishPunctuations;
import smile.nlp.dictionary.EnglishStopWords;
import smile.nlp.tokenizer.SimpleSentenceSplitter;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Objects of this class can be used to calculate scores for candidate devices, given by their
 * {@link DeviceDescription}s, based on the scoring criteria that are part of a given {@link DeviceTemplate} and
 * relative to a collection of related candidate devices.
 * The resulting scores are guaranteed to be greater than or equal to zero.
 */
public class CandidateDeviceScorer  {
    //Device template to use for calculating the scores
    private DeviceTemplate deviceTemplate;

    //Descriptions of related candidate devices for calculating relative scores
    private Collection<DeviceDescription> relatedCandidateDevices;

    //Corpus of pre-processed description fields
    private SimpleCorpus descriptionCorpus;

    //Map (device MAC --> description) of pre-processed description fields
    private Map<String, Text> descriptionMap;

    /**
     * Creates a new candidate device scorer from a given {@link DeviceTemplate} and a collection of
     * {@link DeviceDescription}s, representing the candidate devices to which the calculated scores are relative.
     *
     * @param deviceTemplate          The device template to use for calculating the scores
     * @param relatedCandidateDevices The descriptions of related candidate devices to use
     */
    public CandidateDeviceScorer(DeviceTemplate deviceTemplate, Collection<DeviceDescription> relatedCandidateDevices) {
        //Set fields
        setDeviceTemplate(deviceTemplate);
        setRelatedCandidateDevices(relatedCandidateDevices);
    }

    /**
     * Returns the device template that is used for calculating the scores of the candidate devices.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return deviceTemplate;
    }

    /**
     * Sets the device template that is used for calculating the scores of the candidate devices.
     *
     * @param deviceTemplate The device template to set
     */
    private void setDeviceTemplate(DeviceTemplate deviceTemplate) {
        //Null check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        this.deviceTemplate = deviceTemplate;
    }

    /**
     * Returns the descriptions of the related candidate devices to which the calculated scores are relative.
     *
     * @return The descriptions of the related candidate devices
     */
    public Collection<DeviceDescription> getRelatedCandidateDevices() {
        return this.relatedCandidateDevices;
    }

    /**
     * Sets the descriptions of the related candidate devices to which the calculated scores are relative. Furthermore,
     * the description fields are extracted and pre-processed, thus allowing their usage within the score calculations.
     *
     * @param relatedCandidateDevices The descriptions of the related candidate devices to set
     */
    private void setRelatedCandidateDevices(Collection<DeviceDescription> relatedCandidateDevices) {
        //Null checks
        if ((relatedCandidateDevices == null) || (relatedCandidateDevices.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The device descriptions must not be null.");
        }

        this.relatedCandidateDevices = relatedCandidateDevices;

        //Pre-process the descriptions
        preprocessDescriptions();
    }

    /**
     * Returns the corpus of pre-processed device description fields as originating from the collection of related
     * candidate devices.
     *
     * @return The corpus of pre-processed description fields
     */
    public SimpleCorpus getDescriptionCorpus() {
        return this.descriptionCorpus;
    }

    /**
     * Returns a map (device MAC --> pre-processed description) of the individual pre-processed device description
     * fields, associated with the MAC address of the corresponding device.
     *
     * @return The map of pre-processed device description fields
     */
    public Map<String, Text> getDescriptionMap() {
        return this.descriptionMap;
    }

    /**
     * Calculates and returns the score for a certain candidate device, given by its {@link DeviceDescription},
     * with respect to the current {@link DeviceTemplate} that contains the scoring criteria.
     *
     * @param deviceDescription The description of the candidate device for which the score is supposed to be calculated
     * @return The resulting score of the candidate device
     */
    public double score(DeviceDescription deviceDescription) {
        //Sanity check
        if (deviceDescription == null) {
            throw new IllegalArgumentException("The device description must not be null.");
        }

        //Stream trough the scoring criteria of the device template and sum the scores
        double scoreSum = this.deviceTemplate.getScoringCriteria().stream()
                .filter(Objects::nonNull) //Eliminate null criteria
                .mapToDouble(c -> c.getScoreIncrement(deviceDescription, this)) //Apply criteria and retrieve their score
                .sum(); //Sum all score increments/decrements

        //Check resulting score for NaN and negative values and return the final score
        return (Double.isNaN(scoreSum) || scoreSum < 0) ? 0 : scoreSum;
    }

    /**
     * Pre-processes the device description fields that can be retrieved from the collection of related candidate
     * devices. The results of the pre-processing are stored in a corpus.
     */
    private void preprocessDescriptions() {
        //Create new simple document corpus
        this.descriptionCorpus = new SimpleCorpus(SimpleSentenceSplitter.getInstance(),
                new DeviceDescriptionTokenizer(),
                EnglishStopWords.DEFAULT, EnglishPunctuations.getInstance());

        //Stream through the related candidate devices and add their descriptions to the corpus
        this.descriptionMap = this.relatedCandidateDevices.stream()
                .map(d -> this.descriptionCorpus.add(new Text(d.getIdentifiers().getMacAddress(), "", d.getDescription())))
                .collect(Collectors.toMap(t -> t.id, t -> t)); //Collect the pre-processed descriptions to the map
    }
}
