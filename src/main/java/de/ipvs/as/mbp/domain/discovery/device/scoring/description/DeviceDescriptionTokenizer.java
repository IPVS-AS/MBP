package de.ipvs.as.mbp.domain.discovery.device.scoring.description;

import smile.nlp.normalizer.Normalizer;
import smile.nlp.normalizer.SimpleNormalizer;
import smile.nlp.stemmer.PorterStemmer;
import smile.nlp.tokenizer.SimpleTokenizer;
import smile.nlp.tokenizer.Tokenizer;

import java.util.Arrays;

/**
 * Implementation of a {@link Tokenizer} that internally makes use of the {@link SimpleTokenizer} in order to tokenize
 * given text, but also applies the {@link PorterStemmer} to the resulting tokens.
 */
public class DeviceDescriptionTokenizer implements Tokenizer {

    //The tokenizer to use for splitting text into tokens
    private static final Tokenizer TOKENIZER = new SimpleTokenizer();

    //The normalizer to use
    private static final Normalizer NORMALIZER = SimpleNormalizer.getInstance();

    //The stemmer to use
    private static final PorterStemmer STEMMER = new PorterStemmer();

    /**
     * Splits the given string into a list of substrings.
     *
     * @param text The text to split
     * @return The resulting array of tokens
     */
    @Override
    public String[] split(String text) {
        //Tokenize the given text
        return Arrays.stream(TOKENIZER.split(text))
                .map(NORMALIZER::normalize) //Normalize
                .map(STEMMER::stripPluralParticiple) //Apply the stemmer
                .map(t -> t.replace("'", "")) //Replace remaining single quotes
                .toArray(String[]::new); //Collect results as array
    }
}
