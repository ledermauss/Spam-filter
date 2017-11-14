import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private double squaredError;
    private double[] weights; //weights[i]: The weight for n-grams that hash to value i
    private static final Logger LOGGER = Logger.getLogger(PerceptronFeatureHashing.class.getName());

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
        this.learningRate = learningRate;
        this.threshold = 0.0;
        this.hashSize = (int )Math.pow(2, logNbOfBuckets) - 1;
        this.weights = new double[this.hashSize];
        this.bias = 1;
        this.squaredError = 0;
        LOGGER.setUseParentHandlers(false);
        Handler fileHandler;
        try{
            //Creating consoleHandler and fileHandler
            fileHandler  = new FileHandler("./log/pfh.log");
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
        for(int f: hashedNgrams){
            // the 1 stands for the feature value (1 = present in the text, 0 = non present)
            weights[f] += this.learningRate * (y - pr) * 1;
        }
        this.squaredError += 0.5 * Math.pow(y - pr, 2);
        LOGGER.log(Level.INFO, "Squared error is " + this.squaredError);
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
        int f;
        for(f = 0; f < this.hashSize; f++){
            if(weights[f] != 0) {
                System.out.println("Weight of feature " + f + " is " + weights[f]);
            }
        }
        return(super.getInfo());
    }


    /**
     * This runs your code.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 7) {
            System.err.println("Usage: java PerceptronFeatureHashing <indexPath> <stopWordsPath> <logNbOfBuckets> <learningRate> <outPath> <reportingPeriod> <maxN> [-writeOutAllPredictions]");
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
            EvaluationMetric[] evaluationMetrics = new EvaluationMetric[]{new Accuracy()}; //ADD AT LEAST TWO MORE EVALUATION METRICS
            perceptron.makeLearningCurve(stream, evaluationMetrics, out+".pfh", reportingPeriod, writeOutAllPredictions);

            perceptron.getInfo();

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
    }


}
