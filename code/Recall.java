/**
 * @author Mauro Paradela
 *
 * This class calculates the Recall
 *
 * (c) 2017
 */
public class Recall implements EvaluationMetric{
    /**
     * Calculates the precision given the values of the contingency table
     *
     * @param TP Number of true positives
     * @param FP Number of false positives, not used
     * @param TN Number of true negatives, not used
     * @param FN Number of false negatives
     * @return evaluation evaluate
     */
    @Override public double evaluate(int TP, int FP, int TN, int FN) {
        return (TP + FN > 0) ? ((double) TP) / (TP + FN) : 0;
    }

    @Override
    public String name() { return "rec"; }
}
