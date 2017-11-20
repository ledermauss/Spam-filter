import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;


/**
 * @author Jessa Bekker
 *
 * This class is a stub for a perceptron with count-min sketch
 *
 * (c) 2017
 */
public class PerceptronCountMinSketch extends OnlineTextClassifier{

    private int hashSize;
    private int nbOfHashes;
    private double learningRate;
    private double[] bias;
    private double[][] weights; // weights[h][i]: The h'th weight estimate for n-grams that hash to value i for the h'th hash function
    private Function<String, Integer>[] hashFunctions;


    /* FILL IN HERE */

    /**
     * Initialize the perceptron classifier
     *
     * THIS CONSTRUCTOR IS REQUIRED, DO NOT CHANGE THE HEADER
     * You can write additional constructors if you wish, but make sure this one works
     *
     * This classifier uses the count-min sketch to estimate the weights of the n-grams
     *
     * @param nbOfHashes The number of hash functions in the count-min sketch
     * @param logNbOfBuckets The hash functions hash to the range [0,2^NbOfBuckets-1]
     * @param learningRate The size of the updates of the weights
     */
    public PerceptronCountMinSketch(int nbOfHashes, int logNbOfBuckets, double learningRate){
        this.threshold = 0;
        this.nbOfHashes = nbOfHashes;
        this.hashSize = (int)Math.pow(2, logNbOfBuckets) - 1;
        this.learningRate = learningRate;
        this.hashFunctions = new Function[nbOfHashes];
        //hash functions initialization
        Random rand = new Random();
        for(int i = 0; i < nbOfHashes; i++){
            this.hashFunctions[i] = initHash(rand.nextInt(Integer.MAX_VALUE));
        }

        //weights initialization
        double rangeMin =  -1;
        double rangeMax =  1;
        this.bias = new double[this.nbOfHashes];
        this.weights = new double[this.nbOfHashes][this.hashSize];
        for (int h = 0; h < this.nbOfHashes; h++) {
            this.bias[h] = rangeMin + (rangeMax - rangeMin) * rand.nextDouble();
            for (int i = 0; i < this.hashSize; i++)
                this.weights[h][i] = rangeMin + (rangeMax - rangeMin) * rand.nextDouble();
        }
    }

    /**
     *
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
    private int hash(String str, int h){ return hashFunctions[h].apply(str); }

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
        // update pr
        int pr = this.classify(this.makePrediction(labeledText.text));
        int y = labeledText.label;
        for(int h = 0; h < this.nbOfHashes; h++){
            //bias is also updated
            this.bias[h] += this.learningRate * (y - pr);
            for (String ngram: labeledText.text.ngrams){
                int f = this.hash(ngram, h);
                weights[h][f] += this.learningRate * (y - pr) * 1;
            }
        }
    }

    /**
     * Uses the current model to make a prediction about the incoming e-mail belonging to class "1" (spam)
     * If the prediction is positive, then the e-mail is classified as spam.
     *
     * This method gives the output of the perceptron, before it is passed through the threshold function.
     *
     * THIS METHOD IS REQUIRED
     *
     * @param text is an parsed incoming e-mail
     * @return the prediction
     */
    @Override
    public double makePrediction(ParsedText text) {
        double pr = this.median(bias);
        for (String feat: text.ngrams){
            double[] featCounts = new double[this.nbOfHashes];
            // extracts the weights for the feature for each hashing function
            for (int h = 0; h < this.nbOfHashes; h++){
                featCounts[h] = weights[h][this.hash(feat, h)];
            }
        // median value is added to the prediction
        pr += this.median(featCounts);
        }
        return pr;
    }


    public double median(double[] nums){
        // Could be more efficient if it used Quicksort for half of the array
        Arrays.sort(nums);
        if (nums.length % 2 == 0)
            return (nums[(nums.length / 2) - 1] + nums[(nums.length / 2)]) / 2;
        else
            return nums[(nums.length / 2)];
    }



    /**
     * This runs your code.
     */
    public static void main(String[] args) throws IOException {

        if (args.length < 8) {
            System.err.println("Usage: java PerceptronCountMinSketch <indexPath> <stopWordsPath>" +
                    " <logNbOfBuckets> <nbOfHashes> <learningRate> <outPath> <reportingPeriod> <maxN> [-writeOutAllPredictions]");
            throw new Error("Expected 8 or 9 arguments, got " + args.length + ".");
        }
        try {
            // parse input
            String indexPath = args[0];
            String stopWordsPath = args[1];
            int logNbOfBuckets = Integer.parseInt(args[2]);
            int nbOfHashes = Integer.parseInt(args[3]);
            double learningRate = Double.parseDouble(args[4]);
            String out = args[5];
            int reportingPeriod = Integer.parseInt(args[6]);
            int n = Integer.parseInt(args[7]);
            boolean writeOutAllPredictions = args.length>8 && args[8].equals("-writeOutAllPredictions");

            // initialize e-mail stream
            MailStream stream = new MailStream(indexPath, new EmlParser(stopWordsPath,n));

            // initialize learner
            PerceptronCountMinSketch perceptron = new PerceptronCountMinSketch(nbOfHashes ,logNbOfBuckets, learningRate);

            // generate output for the learning curve
            EvaluationMetric[] evaluationMetrics;
            // if indicated, an extra column will be created indicating the parameter to test
            // otherwise, just the normal metrics are used
            if (args.length > 10){
                String paramToTest = args[9];
                double paramValue = Double.parseDouble(args[10]);
                evaluationMetrics = new EvaluationMetric[]{new Accuracy(), new Precision(),
                        new Recall(), new FPRate(),new Parameter(paramToTest, paramValue)};
            } else{
                evaluationMetrics = new EvaluationMetric[]{new Accuracy(), new Precision(), new Recall(), new FPRate()};
            }

            perceptron.makeLearningCurve(stream, evaluationMetrics, out+".pcms", reportingPeriod, writeOutAllPredictions);

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }


}
