import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.logging.*;
import java.util.stream.Collectors;


/**
 * @author Jessa Bekker
 *
 * This class is a stub for naive bayes with feature hashing
 *
 * (c) 2017
 */
public class NaiveBayesFeatureHashing extends OnlineTextClassifier{

    private int logNbOfBuckets;
    private int hashSize;
    private int[][] counts; // counts[c][i]: The count of n-grams in e-mails of class c (spam: c=1) that hash to value i
    private int[] classCounts; //classCounts[c] the count of e-mails of class c (spam: c=1)
    private static final Logger LOGGER = Logger.getLogger(NaiveBayesFeatureHashing.class.getName());

    /* FILL IN HERE */

    /**
     * Initialize the naive Bayes classifier
     *
     * THIS CONSTRUCTOR IS REQUIRED, DO NOT CHANGE THE HEADER
     * You can write additional constructors if you wish, but make sure this one works
     *
     * This classifier uses simple feature hashing: The features of this classifier are the hash values that n-grams
     * hash to.
     *
     * @param logNbOfBuckets The hash function hashes to the range [0,2^NbOfBuckets-1]
     * @param threshold The threshold for classifying something as positive (spam). Classify as spam if Pr(Spam|n-grams)>threshold)
     */
    public NaiveBayesFeatureHashing(int logNbOfBuckets, double threshold){
        this.logNbOfBuckets=logNbOfBuckets;
        this.threshold = threshold;
        this.hashSize = (int )Math.pow(2, this.logNbOfBuckets) - 1;
        this.counts = new int[2][this.hashSize];
        this.classCounts = new int[2];
        LOGGER.setUseParentHandlers(false);
        Handler fileHandler  = null;
        try{
            //Creating consoleHandler and fileHandler
            fileHandler  = new FileHandler("./log/nbfh.log");
            //Assigning handlers to LOGGER object
            LOGGER.addHandler(fileHandler);
            //Setting levels to handlers and LOGGER
            fileHandler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.ALL);
            LOGGER.config("Configuration done.");
            //Console handler removed
            LOGGER.log(Level.FINE, "Finer logged");
        }catch(IOException exception){
            LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
        }
        LOGGER.finer("Finest example on LOGGER handler completed.");
        LOGGER.info("Buckets number is " + this.logNbOfBuckets + " , and hashsize: " + this.hashSize);
    }

    /**
     * Calculate the hash value for string str
     *
     * THIS METHOD IS REQUIRED
     *
     * The hash function hashes to the range [0,2^NbOfBuckets-1]
     *
     * @param str The string to calculate the hash function for
     * @return the hash value of the h'th hash function for string str
     */
    private int hash(String str){
        int strHash = MurmurHash.hash32(str, 0xe17a1465);
        int positiveHash = (strHash & 0x7FFFFFFF) % this.hashSize;
//        System.out.println(this.hashSize);
        return positiveHash;
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
        //update feature counts. Since only presence matters, duplicates must be removed.
        // Set does it automatically. Also, Set is (way) faster than List.
        Set<Integer> hashedNgrams = ngrams.stream().map(this::hash).collect(Collectors.toSet());
        for(int i: hashedNgrams){
            counts[c][i]++;
        }
    }


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
        Set<Integer> features = text.ngrams.stream().map(this::hash).collect(Collectors.toSet());
        double sumProb = 0;
        for(int f: features){
            // NOTE: some features might not be in my matrix, an thus return 0. In that case,
            // the log will return a NaN (and all subsequently results.
            double conditional = (double) counts[1][f]/ classCounts[1];
            if(conditional == 0) continue;
            else sumProb += Math.log(conditional);
        }
        sumProb += Math.log((double) classCounts[1] / this.nbExamplesProcessed);
        pr = Math.exp(sumProb);
        LOGGER.log(Level.INFO, "The predicted value is: " + pr);
        return pr;
    }

    @Override
    public String getInfo() {
        int c, i;
        for(c = 0; c < 2; c++){
            for(i = 0; i < counts[c].length; i++){
                if(counts[c][i] > 0)
                    System.out.println("Class " + c + ", feature " + i + " has " +
                            counts[c][i] + " counts");
            }
            System.out.println("Class " + c + " has " + classCounts[c] + " counts");
        }
        // boolean raul = this.nbExamplesProcessed == (classCounts[0] + classCounts[1]); returns true
        return(super.getInfo());
    }

    /**
     * This runs your code.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 7) {
            System.err.println("Usage: java NaiveBayesFeatureHashing <indexPath> <stopWordsPath> <logNbOfBuckets> <threshold> <outPath> <reportingPeriod> <maxN> [-writeOutAllPredictions]");
            throw new Error("Expected 7 or 8 arguments, got " + args.length + ".");
        }
        try {
            // parse input
            String indexPath = args[0];
            String stopWordsPath = args[1];
            int logNbOfBuckets = Integer.parseInt(args[2]);
            double threshold = Double.parseDouble(args[3]);
            String out = args[4];
            int reportingPeriod = Integer.parseInt(args[5]);
            int n = Integer.parseInt(args[6]);
            boolean writeOutAllPredictions = args.length>7 && args[7].equals("-writeOutAllPredictions");

            // initialize e-mail stream
            // n is the maximum size of n-grams.
            MailStream stream = new MailStream(indexPath, new EmlParser(stopWordsPath,n));

            // initialize learner
            NaiveBayesFeatureHashing nb = new NaiveBayesFeatureHashing(logNbOfBuckets, threshold);

            // generate output for the learning curve
            EvaluationMetric[] evaluationMetrics = new EvaluationMetric[]{new Accuracy()}; //ADD AT LEAST TWO MORE EVALUATION METRICS
            // nbfh stands for feature hashing
            nb.makeLearningCurve(stream, evaluationMetrics, out+".nbfh", reportingPeriod, writeOutAllPredictions);

            nb.getInfo();

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }


}
