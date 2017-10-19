import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Jessa Bekker
 *
 * This abstract class is an online text classifier.
 *
 * (c) 2017
 */
abstract public class OnlineTextClassifier {

    protected int nbExamplesProcessed = 0;

    protected double threshold;

    /**
     This method updates the parameters of your model using the incoming mail.

     @param labeledText is an incoming e-mail with a spam/ham label
     */
    public void update(LabeledText labeledText){
        nbExamplesProcessed++;
    }

    /**
     Uses the current model to make a prediction about the incoming e-mail belonging to class "1" (spam)
     If the prediction is larger than the threshold, then the e-mail is classified as spam.

     @param text is an parsed incoming e-mail
     @return the prediction
     */
    abstract public double makePrediction(ParsedText text);

    /**
     * This method maps a prediction to a classification by comparing it to the threshold
     *
     * @param prediction The predicted value
     * @return The predicted class (1: spam, 0: ham)
     */
    public int classify(double prediction) {return prediction>threshold?1:0;}


    /**
     This method produces files with the scores of the model after seeing different numbers of examples.
     The files can be used to plot learning curves.

     If the files do not exist, a new file named "out + . + scoreName" is
     created. The lines have the form:
     "nbExamplesProcessed <tab> evaluate\n".

     The reporting period decides how often the evaluation evaluate is reported to the file.
     Because learning curves change more in the beginning, the actual used period
     will start at 10 and grow exponentially until it reaches the given reporting period.

     This method can optionally also write out all the predictions made by the learner.
     The predictions are then written to a file named "out + .pred".
     The file format has the form of "true label + tab + predicted label + tab + prediction value".
     This option should only be used for debugging purposes because it will create too much output in a real setting.

     DO NOT CHANGE THIS METHOD

     @param mailStream is the incoming mail stream
     @param out the stem of the output file(s). The scores are written to out.scoreName, the predictions to out.pred
     @param reportingPeriod How often the model is evaluated (once every period, where period is expressed in number of examples)
     @param writeOutAllPredictions when this is true, all the predictions are written to file.
     **/
    public void makeLearningCurve(MailStream mailStream, EvaluationMetric[] evals, String out, int reportingPeriod, boolean writeOutAllPredictions) throws FileNotFoundException {

        PrintWriter[] evalWriters = new PrintWriter[evals.length];
        for (int e=0; e< evals.length; e++)
            evalWriters[e] = new PrintWriter(out+"."+evals[e].name());
        PrintWriter predictionWriter = writeOutAllPredictions ? new PrintWriter(out+".pred") : null;
        DecimalFormat df = new DecimalFormat("0.000");

        int nbToTest = 10;

        System.out.println("Start training/testing");

        Iterator<LabeledText> iterator = mailStream.iterator();
        int i;

        boolean hasNext = iterator.hasNext();

        while(hasNext) {
            ArrayList<LabeledText> buffer = new ArrayList<>(nbToTest);

            int TP = 0;
            int TN = 0;
            int FP = 0;
            int FN = 0;

            i = 0;

            while(hasNext && i <nbToTest){
                i ++;
                LabeledText example = iterator.next();
                double prediction = makePrediction(example.text);
                int predictedClass = classify(prediction);
                if (predictedClass==1){
                    if (example.label==1){
                        TP++;
                    } else {
                        FP++;
                    }
                } else {
                    if (example.label==0){
                        TN++;
                    } else {
                        FN++;
                    }
                }

                // write prediction to file
                if (writeOutAllPredictions) {
                    predictionWriter.println(example.label+"\t"+predictedClass+"\t"+prediction);
                    predictionWriter.flush();
                }

                buffer.add(example);
                hasNext = iterator.hasNext();
            }
            String outline = "trained with: "+nbExamplesProcessed;
            for (int e=0; e< evals.length; e++) {
                double score = evals[e].evaluate(TP,FP,TN,FN);
                evalWriters[e].println(nbExamplesProcessed + "\t" + score);
                evalWriters[e].flush();
                outline+="\t"+evals[e].name()+": "+df.format(score);
            }
            System.out.println(outline);

            for (LabeledText example: buffer){
                update(example);
            }

            nbToTest = Math.min(reportingPeriod, nbToTest*2);
        }
        for (int e=0; e< evals.length; e++) {
            evalWriters[e].close();
        }

    }

    /**
     * Info to print when testing. This is mainly for debug purpose
     *
     * You can override this method in the subclasses to print the information you wish.
     */
    public String getInfo(){
        return "";
    }
}
