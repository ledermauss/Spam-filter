import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jessa Bekker
 *
 * This class is a stub for a perceptron with count-min sketch
 *
 * (c) 2017
 */
public class PerceptronFeatureHashing extends OnlineTextClassifier{

    private int hashSize;
    private double learningRate;
    private double bias;
    private double[] weights; //weights[i]: The weight for n-grams that hash to value i

    /* FILL IN HERE */

    /**
     * Initialize the perceptron classifier
     *
     * THIS CONSTRUCTOR IS REQUIRED, DO NOT CHANGE THE HEADER
     * You can write additional constructors if you wish, but make sure this one works
     *
     * This classifier uses simple feature hashing: The features of this classifier are the hash values that n-grams
     * hash to.
     *
     * @param logNbOfBuckets The hash functions hash to the range [0,2^NbOfBuckets-1]
     * @param learningRate The size of the updates of the weights
     */
    public PerceptronFeatureHashing(int logNbOfBuckets, double learningRate){
        //bias is another wight, must be initialized. Its feature is always one
        this.learningRate = learningRate;
        this.threshold = 0.0;
        this.hashSize = (int )Math.pow(2, logNbOfBuckets) - 1;
        Random rand = new Random();
        double rangeMin =  -1;
        double rangeMax =  1;
        this.bias = rangeMin + (rangeMax - rangeMin) * rand.nextDouble();
        this.weights = new double[this.hashSize];
        //random = rangeMin + (rangeMax - rangeMin) * randomDouble
        for (int i = 0; i < this.hashSize; i++) {
            this.weights[i] = rangeMin + (rangeMax - rangeMin) * rand.nextDouble();
        }
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
        int pr = this.classify(this.makePrediction(labeledText.text));
        int y = labeledText.label;
        Set<Integer> hashedNgrams = labeledText.text.ngrams.stream()
                .map(this::hash).collect(Collectors.toSet());
        //bias is also a weight with helper feature x0 = 1, must be updated
        this.bias += this.learningRate * (y - pr);
        for(int f: hashedNgrams){
            // the 1 stands for the feature value (1 = present in the text, 0 = non present)
            // y - pr is the error
            // learningRate x error x feature value = delta
            weights[f] += this.learningRate * (y - pr) * 1;
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
        double pr = bias;
        Set<Integer> features = text.ngrams.stream().map(this::hash).collect(Collectors.toSet());
        for(int f: features){
            pr += f * weights[f];
        }
        return pr;
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
            System.err.println("Usage: java PerceptronFeatureHashing <indexPath> <stopWordsPath> <logNbOfBuckets> " +
                    "<learningRate> <outPath> <reportingPeriod> <maxN> [-writeOutAllPredictions][ParamToTestName] [ParamToTestValue]");
            throw new Error("Expected 7 or 8 arguments, got " + args.length + ".");
        }
        try {
            // parse input
            String indexPath = args[0];
            String stopWordsPath = args[1];
            int logNbOfBuckets = Integer.parseInt(args[2]);
            double learningRate = Double.parseDouble(args[3]);
            String out = args[4];
            int reportingPeriod = Integer.parseInt(args[5]);
            int n = Integer.parseInt(args[6]);
            boolean writeOutAllPredictions = args.length>7 && args[7].equals("-writeOutAllPredictions");

            // initialize e-mail stream
            MailStream stream = new MailStream(indexPath, new EmlParser(stopWordsPath,n));

            // initialize learner
            PerceptronFeatureHashing perceptron = new PerceptronFeatureHashing(logNbOfBuckets, learningRate);

            // generate output for the learning curve
            EvaluationMetric[] evaluationMetrics;
            // if indicated, an extra column will be created indicating the parameter to test
            // otherwise, just the normal metrics are used
            if (args.length > 9){
                String paramToTest = args[8];
                double paramValue = Double.parseDouble(args[9]);
                evaluationMetrics = new EvaluationMetric[]{new Accuracy(), new Precision(),
                        new Recall(), new FPRate(),new Parameter(paramToTest, paramValue)};
            } else{
                evaluationMetrics = new EvaluationMetric[]{new Accuracy(), new Precision(), new Recall(), new FPRate()};
            }
            perceptron.makeLearningCurve(stream, evaluationMetrics, out+".pfh", reportingPeriod, writeOutAllPredictions);

            //perceptron.getInfo();

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }


}
