package eflomal;

import java.util.*;

public class SamplingIBM1 {

    private Random r = new Random(10);
    private final int NULL_WORD = 0;
    private final int NULL_LINK = 0xFFFF;
    private final double NULL_PRIOR = 0.2; // this seems to be what's used in eflomal
    private final double LEX_ALPHA = 0.001;
    private final double NULL_ALPHA = 0.001;
    private ArrayList<ArrayList<Integer>> links = new ArrayList<>();
    private int maxLines = 10000;
    private ArrayList<SentencePair> corpus = new ArrayList<>();
    private HashMap<Pair, Integer> counts = new HashMap<>();
    private ArrayList<TreeMap<Integer, Double>> dirichlet = new ArrayList<>();
    private ArrayList<TreeMap<Integer, Double>> priors = new ArrayList<>();
    private boolean argmax = false;

    /**
     * Reading from file the meaningful sentences,
     * and initializing the values of
     * data structures that matter, In our situation, the link
     * data structure and the counts data structure
     */

    public SamplingIBM1(String filename) {
        BuildCorpus(filename);
        initializeLinksAndCounts();
    }

    /**
     * 
     * @param filename the name of the zip file
     *                 make the set of sentence pairs from the
     *                 file into the data structure
     */
    public void BuildCorpus(String filename) {

        SentencePairReader reader = new SentencePairReader(filename, false);
        int line = 0;

        while (reader.hasNext() && line < maxLines) {
            SentencePair pair = reader.next();
            corpus.add(pair);
            line = line + 1;
        }
    }

    /**
     * Initializing counts
     * There is one entry for every non-zero mapping from a source word to its
     * consequential target word. All missing entries are assumed to be 0.
     * 
     * An entry of 57 at index Pair(66, 42) means that the word represented by
     * the word token 66 is attributed to the word represented by the word token
     * 42 a total of 57 times accross the corpus.
     * counts = new Map<Pair<target, source>, counts>()
     * counts = new Map<Pair<uint32, uint32>, uint32>()
     *
     * * There is one links array per src-tgt sentence pair
     * 
     * Each links array is of the same length as the target sentence
     * 
     * Let i be a sentence pair index in the translation corpus
     * Let j be the index of a word in the target sentence of that pair
     * links[i][j] is the index of the word in the source sentece of that pair
     * which is responsible for the presence of word j
     * 
     * Initializing links
     * for every pair
     * grab the target
     * create a list
     * for every word in the target
     * generate a number between 1
     * and the length of source sentence
     * 
     */
    public void initializeLinksAndCounts() {

        for (int i = 0; i < corpus.size(); i++) {
            links.add(i, new ArrayList<Integer>());
            int srcLength = corpus.get(i).getSource().size();
            int trgLength = corpus.get(i).getTarget().size();
            for (int j = 0; j < trgLength; j++) {
                // to initialize counts:
                // if exists, increment counts value
                // otherwise, add new counts pair with 1
                int linkIndex = r.nextInt(srcLength);
                links.get(i).add(linkIndex);
                Pair pairToUpdate = new Pair(corpus.get(i).getTarget().get(j),
                        corpus.get(i).getSource().get(linkIndex));
                if (counts.containsKey(pairToUpdate)) {
                    counts.put(pairToUpdate, counts.get(pairToUpdate) + 1);
                } else {
                    counts.put(pairToUpdate, 1);
                }
            }
        }
    }

    /**
     * 
     * @param Iterations: the number of time to run the stuff.
     * @return Link: Data-structure with the mapping.
     */

    public ArrayList<TreeMap<Integer, Double>> WordAlignment(int iterations) {

        for (int p = 1; p < iterations; p++) {
            for (int k = 0; k < corpus.size(); k++) {

                // Getting the source sentence and target sentence
                Sentence S = corpus.get(k).getSource();
                Sentence T = corpus.get(k).getTarget();
                // Assume that all the words are well translated except the word j
                // then figure out a best source for it
                for (int j = 0; j < T.size(); j++) {
                    int t = T.get(j);
                    // get the word index in the source sentence that previously mapped to this word
                    int old_i = links.get(k).get(j);
                    // get that word token from the source sentence
                    int old_s = -1;
                    if (old_i == NULL_LINK) {
                        old_s = NULL_WORD;
                    } else {
                        old_s = S.get(old_i);
                    }

                    // make a pair
                    Pair pairToUpdate = new Pair(t, old_s);

                    // update the count of that pair
                    if (counts.containsKey(pairToUpdate)) {
                        if (counts.get(pairToUpdate) <= 1) {
                            // remove any mapping of the token to its translation
                            counts.remove(pairToUpdate);
                            if (dirichlet.size() > t && dirichlet.get(t) != null) {
                                dirichlet.get(t).remove(old_s);
                            }
                        } else {
                            // decrease the count and Dirichlet prior of the word old_s
                            counts.put(pairToUpdate, counts.get(pairToUpdate) - 1);
                            double dirichletVal = LEX_ALPHA; // handle case where dirichlet is null
                            if (dirichlet.size() > t && dirichlet.get(t) != null
                                    && dirichlet.get(t).containsKey(old_s)) {
                                dirichletVal = dirichlet.get(t).get(old_s);
                            }
                            double newDirichletVal = 1 / (1 / dirichletVal - 1);
                            if (dirichlet.size() <= t) {
                                while (dirichlet.size() <= t) {
                                    dirichlet.add(dirichlet.size(), new TreeMap<Integer, Double>());
                                }
                            }
                            dirichlet.get(t).put(old_s, newDirichletVal);
                        }
                    }

                    // update probabilities assuming this pair is unmapped
                    double ps_sum = 0;
                    ArrayList<Double> ps = new ArrayList<>(); // one entry per word in source sentence + 1
                    for (int i = 0; i < S.size(); i++) {
                        int s = S.get(i); // ith word in src
                        // get the number of times that t is caused by s
                        int n = counts.getOrDefault(new Pair(t, s), 0);
                        // get the prior count of t caused by s
                        double alpha = 0;
                        if (priors.size() > t) {
                            alpha = priors.get(t).get(s) + LEX_ALPHA;
                        } else {
                            alpha = LEX_ALPHA;
                        }
                        // estimate * dirichlet
                        double dirichletVal = LEX_ALPHA;
                        if (dirichlet.size() > t && dirichlet.get(t) != null && dirichlet.get(t).containsKey(s)) {
                            dirichletVal = dirichlet.get(t).get(s);
                        }
                        ps_sum += dirichletVal * (alpha + n);
                        // add this number to the CPD
                        ps.add(i, ps_sum);
                        // include null word in the sum
                        double dirichletValNull = LEX_ALPHA;
                        if (dirichlet.size() > t && dirichlet.get(t) != null
                                && dirichlet.get(t).containsKey(NULL_WORD)) {
                            dirichletValNull = dirichlet.get(t).get(NULL_WORD);
                        }
                        ps_sum += NULL_PRIOR * dirichletValNull * // change from pseudocode: +=
                                (NULL_ALPHA + counts.getOrDefault(new Pair(t, NULL_WORD), 0));
                    }
                    ps.add(S.size(), ps_sum);

                    // determine based on ps_sum which source token caused the target token

                    // select a new_i to replace old_i
                    int new_i = -1;
                    int new_s = -1;
                    if (!argmax) {
                        // the probability of any i is proportional to its probability in ps
                        new_i = sample_random_from_CD(ps);
                    } else {
                        // whichever i is most probable based on ps will be chosen
                        new_i = sample_best_from_CD(ps);
                    }
                    // identify the word token that goes with this sentence index
                    if (new_i < S.size()) {
                        new_s = S.get(new_i);
                        links.get(k).set(j, new_i);
                    } else {
                        new_s = NULL_WORD;
                        links.get(k).set(j, NULL_LINK);
                    }

                    // increase the count and dirichlet variables to reflect the new i and s
                    pairToUpdate = new Pair(t, new_s);
                    if (counts.containsKey(pairToUpdate)) {
                        counts.put(pairToUpdate, counts.get(pairToUpdate) + 1);

                    } else {
                        counts.put(pairToUpdate, 1);

                    }
                    double dirichletVal = LEX_ALPHA;
                    if (dirichlet.size() > t && dirichlet.get(t) != null && dirichlet.get(t).containsKey(new_s)) {
                        dirichletVal = dirichlet.get(t).get(new_s);
                    }
                    double newDirichletVal = 1 / (1 / dirichletVal + 1);
                    if (dirichlet.size() <= t) {
                        while (dirichlet.size() <= t) {
                            dirichlet.add(dirichlet.size(), new TreeMap<Integer, Double>());
                        }
                    } else if (dirichlet.get(t) == null) {
                        dirichlet.set(t, new TreeMap<Integer, Double>());
                    }
                    dirichlet.get(t).put(new_s, newDirichletVal);
                }

            }
        }

        return dirichlet;

    }

    /**
     * 
     * @param ps probability distribution
     * @return the mode from the samples in this distribution
     */

    private int sample_best_from_CD(ArrayList<Double> ps) {
        double best_p = ps.get(0);
        int new_i = 0;
        for (int i = 1; i < ps.size(); i++) {
            double p = ps.get(i) - ps.get(i - 1);
            if (p > best_p) {
                new_i = i;
                best_p = p;
            }
        }
        return new_i;
    }

    /**
     * 
     * @param ps probabitlity distribution
     * @return a sample from that distribution selected at random
     */
    private int sample_random_from_CD(ArrayList<Double> ps) {
        double max = ps.get(ps.size() - 1);
        double randomValue = max * r.nextDouble();
        for (int i = 0; i < ps.size() - 1; i++) {
            if (ps.get(i) >= randomValue) {
                return i;
            }
        }
        return ps.size() - 1;
    }

}