import java.util.Set;

/**
 * @author Jessa Bekker
 *
 * This class represents a parsed text
 *
 * Optionally, you can make a subclass of Parsed Text that holds more features that were extracted from the text,
 * which can be used to learn better models.
 * If you do this, make sure that the classifiers does not require the subclass. It should still be able to operate
 * with n-grams alone.
 *
 * (c) 2017
 */
public class ParsedText {



    public final Set<String> ngrams;

    public ParsedText(Set<String> ngrams) {
        this.ngrams = ngrams;
    }

    @Override
    public String toString(){
        return ngrams.toString();
    }
}
