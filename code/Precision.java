/**
 * @author Mauro Paradela
 *
 * This class calculates the precision
 *
 * (c) 2017
 */
public class Precision implements EvaluationMetric{

    /**
     * Calculates the precision given the values of the contingency table
     *
     * @param TP Number of true positives
     * @param FP Number of false positives
     * @param TN Number of true negatives, not used
     * @param FN Number of false negatives, not used
     * @return evaluation evaluate
     */
    @Override
    public double evaluate(int TP, int FP, int TN, int FN) {
        return (TP + FP > 0) ? ((double) TP) / (TP + FP) : 0;
    }

    @Override
    public String name() { return "prec"; }
}
