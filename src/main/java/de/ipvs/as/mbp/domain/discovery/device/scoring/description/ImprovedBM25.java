package de.ipvs.as.mbp.domain.discovery.device.scoring.description;

import smile.nlp.Corpus;
import smile.nlp.TextTerms;
import smile.nlp.relevance.RelevanceRanker;

/**
 * This class is a copy of {@link smile.nlp.relevance.BM25}, but fixes the calculation
 * of the inverse document frequency (IDF) by adding a one to the formula.
 * <p>
 * Previous approach:
 * <code>idf = Math.log((N - n + 0.5) / (n + 0.5))</code>
 * <p>
 * New approach:
 * <code>idf = Math.log((N - n + 0.5) / (n + 0.5) + 1)</code>
 * <p>
 * This way, it can be guaranteed that the IDF values and thus the BM25 do
 * not become negative.
 * <p>
 * The BM25 weighting scheme, often called Okapi weighting, after the system in
 * which it was first implemented, was developed as a way of building a
 * probabilistic model sensitive to term frequency and document length while
 * not introducing too many additional parameters into the model. It is not
 * a single function, but actually a whole family of scoring functions, with
 * slightly different components and parameters.
 * <p>
 * At the extreme values of the coefficient b, BM25 turns into ranking functions
 * known as BM11 (for b = 1) and BM15 (for b = 0). BM25F is a modification of
 * BM25 in which the document is considered to be composed from several fields
 * (such as headlines, main text, anchor text) with possibly different degrees
 * of importance.
 * BM25 and its newer variants represent state-of-the-art TF-IDF-like retrieval
 * functions used in document retrieval, such as web search.
 *
 * @author Haifeng Li, Jan Schneider
 */
public class ImprovedBM25 implements RelevanceRanker {

    /**
     * Free parameter, usually chosen as k1 = 2.0.
     */
    private final double k1;
    /**
     * Free parameter, usually chosen as b = 0.75.
     */
    private final double b;
    /**
     * The control parameter in BM25+. The standard BM25 in which the
     * component of term frequency normalization by document length
     * is not properly lower-bounded; as a result of this deficiency,
     * long documents which do match the query term can often be scored
     * unfairly by BM25 as having a similar relevance to shorter
     * documents that do not contain the query term at all.
     */
    private final double delta;

    /**
     * Default constructor with k1 = 1.2, b = 0.75, delta = 1.0.
     */
    public ImprovedBM25() {
        this(1.2, 0.75, 1.0);
    }

    /**
     * Constructor.
     *
     * @param k1    is a positive tuning parameter that calibrates
     *              the document term frequency scaling. A k1 value of 0
     *              corresponds to a binary model (no term frequency),
     *              and a large value corresponds to using raw term frequency.
     * @param b     b is another tuning parameter ({@code 0 <= b <= 1}) which
     *              determines the scaling by document length: b = 1 corresponds
     *              to fully scaling the term weight by the document length,
     *              while b = 0 corresponds to no length normalization.
     * @param delta the control parameter in BM25+. The standard BM25 in
     *              which the component of term frequency normalization
     *              by document length is not properly lower-bounded;
     *              as a result of this deficiency, long documents which
     *              do match the query term can often be scored unfairly
     *              by BM25 as having a similar relevance to shorter
     *              documents that do not contain the query term at all.
     */
    public ImprovedBM25(double k1, double b, double delta) {
        if (k1 < 0) {
            throw new IllegalArgumentException("Negative k1 = " + k1);
        }

        if (b < 0 || b > 1) {
            throw new IllegalArgumentException("Invalid b = " + b);
        }

        if (delta < 0) {
            throw new IllegalArgumentException("Invalid delta = " + delta);
        }

        this.k1 = k1;
        this.b = b;
        this.delta = delta;
    }

    /**
     * Returns the relevance score between a term and a document based on a corpus.
     *
     * @param termFreq       the term frequency in the text body.
     * @param docSize        the text length.
     * @param avgDocSize     the average text length in the corpus.
     * @param titleTermFreq  the term frequency in the title.
     * @param titleSize      the title length.
     * @param avgTitleSize   the average title length in the corpus.
     * @param anchorTermFreq the term frequency in the anchor.
     * @param anchorSize     the anchor length.
     * @param avgAnchorSize  the average anchor length in the corpus.
     * @param N              the number of documents in the corpus.
     * @param n              the number of documents containing the given term in the corpus;
     * @return the relevance score.
     */
    public double score(int termFreq, int docSize, double avgDocSize, int titleTermFreq,
                        int titleSize, double avgTitleSize, int anchorTermFreq, int anchorSize,
                        double avgAnchorSize, long N, long n) {
        if (termFreq <= 0) return 0.0;

        // BM25F parameters
        final double kf = 4.9; // k1 in BM25F
        final double bTitle = 0.6;
        final double bBody = 0.5;
        final double bAnchor = 0.6;
        final double wTitle = 13.5;
        final double wBody = 1.0;
        final double wAnchor = 11.5;

        double tf = wBody * termFreq / (1.0 + bBody * (docSize / avgDocSize - 1.0));

        if (titleTermFreq > 0) {
            tf += wTitle * titleTermFreq / (1.0 + bTitle * (titleSize / avgTitleSize - 1.0));
        }

        if (anchorTermFreq > 0) {
            tf += wAnchor * anchorTermFreq / (1.0 + bAnchor * (anchorSize / avgAnchorSize - 1.0));
        }

        tf = tf / (kf + tf);
        double idf = Math.log((N - n + 0.5) / (n + 0.5) + 1);

        return (tf + delta) * idf;
    }

    /**
     * Returns the relevance score between a term and a document based on a corpus.
     *
     * @param freq the normalized term frequency of searching term in the document to rank.
     * @param N    the number of documents in the corpus.
     * @param n    the number of documents containing the given term in the corpus;
     * @return the relevance score.
     */
    public double score(double freq, long N, long n) {
        if (freq <= 0) return 0.0;

        double tf = (k1 + 1) * freq / (freq + k1);
        double idf = Math.log((N - n + 0.5) / (n + 0.5) + 1);

        return (tf + delta) * idf;
    }

    /**
     * Returns the relevance score between a term and a document based on a corpus.
     *
     * @param freq       the frequency of searching term in the document to rank.
     * @param docSize    the size of document to rank.
     * @param avgDocSize the average size of documents in the corpus.
     * @param N          the number of documents in the corpus.
     * @param n          the number of documents containing the given term in the corpus;
     * @return the relevance score.
     */
    public double score(double freq, int docSize, double avgDocSize, long N, long n) {
        if (freq <= 0) return 0.0;

        double tf = freq * (k1 + 1) / (freq + k1 * (1 - b + b * docSize / avgDocSize));
        double idf = Math.log((N - n + 0.5) / (n + 0.5) + 1);

        return (tf + delta) * idf;
    }

    /**
     * Returns a relevance score between a term and a document based on a corpus.
     *
     * @param corpus the corpus.
     * @param doc    the document to rank.
     * @param term   the searching term.
     * @param tf     the term frequency in the document.
     * @param n      the number of documents containing the given term in the corpus;
     */
    @Override
    public double rank(Corpus corpus, TextTerms doc, String term, int tf, int n) {
        if (tf <= 0) return 0.0;

        int N = corpus.getNumDocuments();
        int docSize = doc.size();
        int avgDocSize = corpus.getAverageDocumentSize();

        return score(tf, docSize, avgDocSize, N, n);
    }

    /**
     * Returns a relevance score between a set of terms and a document based on a corpus.
     *
     * @param corpus the corpus.
     * @param doc    the document to rank.
     * @param terms  the searching terms.
     * @param tf     the term frequencies in the document.
     * @param n      the number of documents containing the given term in the corpus;
     */
    @Override
    public double rank(Corpus corpus, TextTerms doc, String[] terms, int[] tf, int n) {
        int N = corpus.getNumDocuments();
        int docSize = doc.size();
        int avgDocSize = corpus.getAverageDocumentSize();

        double r = 0.0;
        for (int i = 0; i < terms.length; i++) {
            r += score(tf[i], docSize, avgDocSize, N, n);
        }

        return r;
    }
}
