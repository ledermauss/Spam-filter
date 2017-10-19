/**
 * @author Jessa Bekker
 *
 * This class represents a parsed text and its label
 *
 * (c) 2017
 */
public class LabeledText {

    public final ParsedText text;
    public final int label;

    /**
     * Initializes labeled text
     *
     * @param text The parsed text
     * @param label The label (0 or 1 for binary classes)
     */
    public LabeledText(ParsedText text, int label) {
        this.text = text;
        this.label = label;
    }

    @Override
    public String toString(){
        return label+"\t"+text;
    }
}
