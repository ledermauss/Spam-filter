/**
 */
public class FPRate implements EvaluationMetric{
    @Override
    public double evaluate(int TP, int FP, int TN, int FN) {
        return ((double) FP / (FP + TN));
    }

    @Override
    public String name() {
        return "fpr";
    }
}
