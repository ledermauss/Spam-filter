import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jessa Bekker
 *
 * This class is a stub for naive Bayes with count-min sketch
 *
 * (c) 2017
 */
public class NaiveBayesCountMinSketch extends OnlineTextClassifier{

    private int hashSize;
    private int nbOfHashes;
    private int[][][] counts; // counts[c][h][i]: The count of n-grams in e-mails of class c (spam: c=1)
                              // that hash to value i for the h'th hash function.
    private int[] classCounts; //classCounts[c] the count of e-mails of class c (spam: c=1)
    private Function<String, Integer>[] hashFunctions;

    /* FILL IN HERE */

    /**
     * Initialize the naive Bayes classifier
     *
     * THIS CONSTRUCTOR IS REQUIRED, DO NOT CHANGE THE HEADER
     * You can write additional constructors if you wish, but make sure this one works
     *
     * This classifier uses the count-min sketch to estimate the conditional counts of the n-grams
     *
     * @param nbOfHashes The number of hash functions in the count-min sketch
     * @param logNbOfBuckets The hash functions hash to the range [0,2^NbOfBuckets-1]
     * @param threshold The threshold for classifying something as positive (spam). Classify as spam if Pr(Spam|n-grams)>threshold)
     */
    @SuppressWarnings("unchecked")
    public NaiveBayesCountMinSketch(int nbOfHashes, int logNbOfBuckets, double threshold){
        this.threshold = threshold;
        this.nbOfHashes = nbOfHashes;
        this.hashSize = (int)Math.pow(2, logNbOfBuckets) - 1;
        this.counts = new int[2][nbOfHashes][this.hashSize];
        this.classCounts = new int[2];
        this.hashFunctions = new Function[nbOfHashes];
        Random rand = new Random();
        //hash functions initialization
        for(int i = 0; i < nbOfHashes; i++){
            this.hashFunctions[i] = initHash(rand.nextInt(Integer.MAX_VALUE));
        }

    }

    /**
     * Returns a MurmurHash function of 32 bits for the seed provided
     * @param seed Murmurhash initialization parameter
     * @return a hashing function for the given seed that expects a string
     */

    private Function<String, Integer> initHash(int seed){
        // lambda: str is the argument (String) the generated function expects
        return str ->
                (MurmurHash.hash32(str, seed) & 0x7FFFFFFF) % this.hashSize;

    }

    /**
     * Calculate the hash value of the h'th hash function for string str
     *
     * THIS METHOD IS REQUIRED
     *
     * The hash function hashes to the range [0,2^NbOfBuckets-1]
     * This method should work for h in the range [0, nbOfHashes-1]
     *
     * @param str The string to calculate the hash function for
     * @param h The number of the hash function to use.
     * @return the hash value of the h'th hash function for string str
     */
    private int hash(String str, int h){
        return hashFunctions[h].apply(str);
    }

    /**
     * This method will update the parameters of your model using the incoming mail.
     *
     * THIS METHOD IS REQUIRED
     *
     * @param labeledText is an incoming e-mail with a spam/ham label
     */
    @Override
    public void update(LabeledText labeledText){
        super.update(labeledText);
        Set<String> ngrams = labeledText.text.ngrams;
        int c = labeledText.label;
        //update class counts
        this.classCounts[c]++;
        //update feature counts. Set removes duplicates, only presence matters
        //count matrix updated for each hashing funciton and feature value
        for(int d = 0; d < this.nbOfHashes; d++){
            final int finalD = d; //maps work only with final variables
            //TODO: reimplement proceduraly (faster, almost as readable)
            Set<Integer> hashedNgrams = ngrams.stream()
                    .map(s -> hash(s, finalD)).collect(Collectors.toSet());

            for(int i: hashedNgrams) counts[c][d][i]++;
        }
    }

    /**
     * About lambda performance: without lambda, training is 5.3 s on the
     * small train set. With lambda, it is 6. Doing nothing is 4.3
     */

    /**
     * Uses the current model to make a prediction about the incoming e-mail belonging to class "1" (spam)
     * The prediction is the probability for the e-mail to be spam.
     * If the probability is larger than the threshold, then the e-mail is classified as spam.
     *
     * THIS METHOD IS REQUIRED
     *
     * @param text is an parsed incoming e-mail
     * @return the prediction
     */
    @Override
    public double makePrediction(ParsedText text) {
        double pr;
        // stores the count of each ngram
        HashMap<String, Integer> ngramCountHam = new HashMap<String, Integer>();
        HashMap<String, Integer> ngramCountSpam = new HashMap<String, Integer>();

        double[] labelFeatJoint = new double[2];

        // computes the joint probability of each class and all feature
        for (int c = 0; c < 2; c++) {
            // class probability
            labelFeatJoint[c] = Math.log((double) this.classCounts[c]/ this.nbExamplesProcessed);
            for (String ngram : text.ngrams) {
                int minCount = this.counts[c][0][this.hashFunctions[0].apply(ngram)];
                //  finds minimum count of an ngram out of all hashing functions
                for (int d = 1; d < this.nbOfHashes; d++) {
                    int hashedNgram = this.hashFunctions[d].apply(ngram);
                    int hashCount = this.counts[c][d][hashedNgram];
                    minCount = hashCount < minCount ? hashCount : minCount;
                }
                //  add log(conditional probability) of each feature by class,
                //  using the minCount and laplace smoothing
                labelFeatJoint[c] += Math.log((1.0 + minCount) /
                                (this.classCounts[0] + this.hashSize));
            }
        }

        //log-sum trick. Log(a) = spamSum, Log(b) = hamSum
        // pr = spamSum - (spamSum + Log(1 + e^(hamSum-spamSum))
        pr = -Math.log(1 + Math.exp(labelFeatJoint[0] - labelFeatJoint[1]));
        return Math.exp(pr);
    }

    @Override
    public String getInfo() {
        StringBuffer raul = new StringBuffer();
        int c,d,i;
        for(c = 0; c < 2; c++){
            raul.append("##################\n");
            raul.append("##################\n");
            raul.append("##################\n");
            for(d = 0; d < this.nbOfHashes; d++){
                for(i = 10; i < 20; i++){
                    raul.append(this.counts[c][d][i] + "\t");
                }
                raul.append("\n");
            }
        }
        // prints a sketch of the matrix
//        System.out.print(raul);
/*        System.out.println("network counts - network hash");
        for(d = 0; d < this.nbOfHashes; d++){
            System.out.println(
                    this.counts[1][d][this.hashFunctions[d].apply("network")] +
                    " " + this.hashFunctions[d].apply("network"));
        }*/
        return(super.getInfo());
    }

    /**
     * This runs your code.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 8) {
            System.err.println("Usage: java NaiveBayesCountMinSketch <indexPath> <stopWordsPath> <logNbOfBuckets> <nbOfHashes> <threshold> <outPath> <reportingPeriod> <maxN> [-writeOutAllPredictions]");
            throw new Error("Expected 8 or 9 arguments, got " + args.length + ".");
        }
        try {
            // parse input
            String indexPath = args[0];
            String stopWordsPath = args[1];
            int logNbOfBuckets = Integer.parseInt(args[2]);
            int nbOfHashes = Integer.parseInt(args[3]);
            double threshold = Double.parseDouble(args[4]);
            String out = args[5];
            int reportingPeriod = Integer.parseInt(args[6]);
            int n = Integer.parseInt(args[7]);
            boolean writeOutAllPredictions = args.length>8 && args[8].equals("-writeOutAllPredictions");

            // initialize e-mail stream
            MailStream stream = new MailStream(indexPath, new EmlParser(stopWordsPath,n));

            // initialize learner
            NaiveBayesCountMinSketch nb = new NaiveBayesCountMinSketch(nbOfHashes ,logNbOfBuckets, threshold);

            // generate output for the learning curve
            EvaluationMetric[] evaluationMetrics = new EvaluationMetric[]{new Accuracy(), new Precision(), new Recall()};
            nb.makeLearningCurve(stream, evaluationMetrics, out+".nbcms", reportingPeriod, writeOutAllPredictions);

            nb.getInfo();

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }
}
