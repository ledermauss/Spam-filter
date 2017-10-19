/**
 * @author Jessa Bekker
 *
 * This interface represents an evaluation metric
 *
 * (c) 2017
 */
public interface EvaluationMetric {

    /**
     * Evaluate given the values of the contingency table
     *
     * @param TP Number of true positives
     * @param FP Number of false positives
     * @param TN Number of true negatives
     * @param FN Number of false negatives
     * @return evaluation evaluate
     */
    double evaluate(int TP, int FP, int TN, int FN);

    /**
     *
     * @return name of the evaluator
     */
    String name();
}
