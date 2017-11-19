/**
 * Created by Mauro on 19/11/17.
 */
public class Parameter implements EvaluationMetric {
    private String name;
    private double value;

    public Parameter(String name, double value){
        this.name = name;
        this.value = value;

    }
    @Override
    public double evaluate(int TP, int FP, int TN, int FN) {
        return value;
    }

    @Override
    public String name() {
        return name;
    }
}
