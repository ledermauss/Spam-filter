import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author Jessa Bekker
 *
 * This class is a stub for naive bayes with feature hashing
 *
 * (c) 2017
 */
public class NaiveBayesFeatureHashing extends OnlineTextClassifier{

    private int hashSize;
    private int[][] counts; // counts[c][i]: The count of n-grams in e-mails of class c (spam: c=1) that hash to value i
    private int[] classCounts; //classCounts[c] the count of e-mails of class c (spam: c=1)


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
        this.threshold = threshold;
        this.hashSize = (int )Math.pow(2, logNbOfBuckets) - 1;
        this.counts = new int[2][this.hashSize];
        this.classCounts = new int[2];
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
        return (strHash & 0x7FFFFFFF) % this.hashSize;
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
        double hamSum = Math.log((double) classCounts[0]/this.nbExamplesProcessed);
        double spamSum = Math.log((double) classCounts[1]/this.nbExamplesProcessed);
//        System.out.println("Class 0 is " + classCounts[0] + ", Class 1 is " + classCounts[1]);
        for(int f: features){
            // adding 1 and this.hashSize (vocabulary size) for Laplace smothing
            hamSum += Math.log((1.0 + counts[0][f]) / (classCounts[0] + this.hashSize));
            spamSum += Math.log((1.0 + counts[1][f]) / (classCounts[1] + this.hashSize));
        }
        //log-sum trick. Log(a) = spamSum, Log(b) = hamSum
        // pr = spamSum - (spamSum + Log(1 + e^(hamSum-spamSum))
        pr = -Math.log(1 + Math.exp(hamSum - spamSum));
        return Math.exp(pr);
    }

    @Override
    public String getInfo() {
        return(super.getInfo());
    }

    /**
     * This runs your code.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 7) {
            System.err.println("Usage: java NaiveBayesFeatureHashing <indexPath> <stopWordsPath> <logNbOfBuckets> " +
                    "<threshold> <outPath> <reportingPeriod> <ngramsSize> [-writeOutAllPredictions] [ParamToTestName] [ParamToTestValue]");
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
            EvaluationMetric[] evaluationMetrics;
            // if indicated, an extra column will be created indicating the parameter to test
            // otherswise, just the normal metrics are used
            if (args.length > 9){
                String paramToTest = args[8];
                double paramValue = Double.parseDouble(args[9]);
                evaluationMetrics = new EvaluationMetric[]{new Accuracy(), new Precision(),
                        new Recall(), new FPRate(),new Parameter(paramToTest, paramValue)};
            } else{
                evaluationMetrics = new EvaluationMetric[]{new Accuracy(), new Precision(), new Recall(), new FPRate()};
            }

            // nbfh stands for feature hashing
            nb.makeLearningCurve(stream, evaluationMetrics, out+".nbfh", reportingPeriod, writeOutAllPredictions);

            nb.getInfo();

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }


}
