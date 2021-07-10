package de.ipvs.as.mbp.domain.discovery.device.scoring.description;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionScorer;
import smile.nlp.SimpleCorpus;
import smile.nlp.SimpleText;
import smile.nlp.Text;
import smile.nlp.dictionary.EnglishPunctuations;
import smile.nlp.dictionary.EnglishStopWords;
import smile.nlp.relevance.RelevanceRanker;
import smile.nlp.tokenizer.SimpleSentenceSplitter;
import smile.nlp.tokenizer.Tokenizer;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Objects of this class represent description scoring criteria for devices.
 */
@JsonIgnoreProperties
public class DescriptionScoringCriterion extends ScoringCriterion {
    //Type name of this scoring criterion
    private static final String TYPE_NAME = "description";

    //Tokenizer to use
    private static final Tokenizer TOKENIZER = new DeviceDescriptionTokenizer();

    //Scoring scheme to use
    private static final RelevanceRanker SCORING_SCHEME = new ImprovedBM25();

    //The textual query against the description to use
    private String query;

    //Score for exact matches
    private double exactMatchScore;

    /**
     * Creates a new description scoring criterion.
     */
    public DescriptionScoringCriterion() {

    }

    /**
     * Creates a new description scoring criterion from a given query string and a score increment/decrement that
     * is supposed to be assigned to a device with a description that exactly matches the query.
     *
     * @param query           The textual query against the description of the device
     * @param exactMatchScore The score that is supposed to be assigned to exactly matching descriptions
     */
    @JsonCreator
    public DescriptionScoringCriterion(@JsonProperty("query") String query,
                                       @JsonProperty("exactMatchScore") double exactMatchScore) {
        //Set fields
        setQuery(query);
        setExactMatchScore(exactMatchScore);
    }

    /**
     * Returns the textual query that is supposed to be evaluated against the description of the device.
     *
     * @return The query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the textual query that is supposed to be evaluated against the description of the device.
     *
     * @param query The query to set
     * @return THe description scoring criterion
     */
    public DescriptionScoringCriterion setQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     * Returns the score value that is supposed to be assigned to devices whose descriptions exactly match the query.
     *
     * @return The exact match score
     */
    public double getExactMatchScore() {
        return exactMatchScore;
    }

    /**
     * Sets the score value that is supposed to be assigned to devices whose descriptions exactly match the query.
     *
     * @param exactMatchScore The exact match score to set
     * @return THe description scoring criterion
     */
    public DescriptionScoringCriterion setExactMatchScore(double exactMatchScore) {
        this.exactMatchScore = exactMatchScore;
        return this;
    }

    /**
     * Returns the name of the requirement.
     *
     * @return The name
     */
    @JsonProperty("type")
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Validates the device requirement by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    @Override
    public void validate(EntityValidationException exception, String fieldPrefix) {
        //Check query string
        if ((query == null) || (query.isEmpty())) {
            exception.addInvalidField(fieldPrefix + ".query", "The query must not be empty.");
        }

        //Check exact match score
        if (exactMatchScore <= 0) {
            exception.addInvalidField(fieldPrefix + ".exactMatchScore", "The score for exact matches must be greater than zero.");
        }
    }

    /**
     * Applies the scoring criterion to a given {@link DeviceDescription} and returns the resulting scoring increment
     * (positive number) or scoring decrement (negative number) for this description as result. In addition, a
     * reference to the {@link DeviceDescriptionScorer} that currently performs the overall score calculations is
     * provided, which may contain additional information about the collection of {@link DeviceDescription}s that are
     * currently subject to the scoring process and thus enables the calculation of relative scores.
     *
     * @param deviceDescription The device description for which the score increment of this scoring criterion is
     *                          supposed to be calculated
     * @param scorer            The {@link DeviceDescriptionScorer} that currently performs the overall score
     *                          calculations for a collection of {@link DeviceDescription}s.
     * @return The score increment/decrement that results from the application of this scoring criterion to the given
     * device description
     */
    @Override
    public double getScoreIncrement(DeviceDescription deviceDescription, DeviceDescriptionScorer scorer) {
        //Retrieve corpus pf pre-processed device descriptions from the scorer
        SimpleCorpus corpus = scorer.getDescriptionCorpus();

        //Get MAC address of the device description that is supposed to be scored
        String macAddress = deviceDescription.getIdentifiers().getMacAddress();

        //Retrieve the required pre-processed description from the scorer
        SimpleText preprocessedDescription = (SimpleText) scorer.getDescriptionMap().get(macAddress);

        //Null check
        if (preprocessedDescription == null) {
            //Something went wrong, do not change the score
            return 0;
        }

        //Retrieve inverted index from the corpus
        HashMap<String, List<SimpleText>> invertedIndex = getInvertedIndex(corpus);

        //Null check
        if (invertedIndex == null) {
            //Something went wrong, do not change the score
            return 0;
        }

        //Create query document from query tokens
        SimpleText queryDocument = (SimpleText) new SimpleCorpus(SimpleSentenceSplitter.getInstance(),
                new DeviceDescriptionTokenizer(),
                EnglishStopWords.DEFAULT, EnglishPunctuations.getInstance()).add(new Text("query", "", query));

        //Calculate BM25 score for "optimal" document
        double goodBM25Score = StreamSupport.stream(queryDocument.unique().spliterator(), false)
                .distinct() //Get rid of duplicated query terms
                .mapToDouble(t -> SCORING_SCHEME.rank(corpus, queryDocument, t, queryDocument.tf(t) * 2, invertedIndex.getOrDefault(t, Collections.emptyList()).size()))
                .sum(); //Sum the scores for all terms

        //Calculate BM25 score for device description
        double descriptionBM25Score = StreamSupport.stream(queryDocument.unique().spliterator(), false)
                .distinct() //Get rid of duplicated query terms
                .mapToDouble(t -> SCORING_SCHEME.rank(corpus, preprocessedDescription, t, preprocessedDescription.tf(t), invertedIndex.getOrDefault(t, Collections.emptyList()).size()))
                .sum(); //Sum the scores for all terms

        //Calculate final score relative to the score for an exact match
        return (this.exactMatchScore / goodBM25Score) * descriptionBM25Score;
    }

    /**
     * Uses Java reflections in order to retrieve and return the inverted index of a given {@link SimpleCorpus}
     * as {@link HashMap}.
     *
     * @param corpus The corpus to retrieve the inverted index from
     * @return The hash map representing the inverted index of the corpus
     */
    @SuppressWarnings("unchecked")
    private HashMap<String, List<SimpleText>> getInvertedIndex(SimpleCorpus corpus) {
        //Null check
        if (corpus == null) {
            throw new IllegalArgumentException("The corpus must not be null.");
        }

        try {
            //Get access to the field of the corpus that holds the inverted index
            Field field = corpus.getClass().getDeclaredField("invertedFile");
            field.setAccessible(true);

            //Retrieve the map representing the inverted index
            return (HashMap<String, List<SimpleText>>) field.get(corpus);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }
}
