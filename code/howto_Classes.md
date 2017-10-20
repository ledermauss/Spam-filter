OnlineTextClassifier
============
* Attributes:
    * threshold
    * nbExamplesProcessed

- update(labeledText): updates model parameters
    + labeledText: incoming email
    + void
- _abstract_ makePrediction(text): returns the probability (prediction) of a mail
 belonging to class 1 (__spam__).
    + text: parsed incoming mail (just the text?)
    + return: _double_ prediction
- classify(prediction): maps prediction to classification comparing to threshold
    + return: _int_ prediction>threshold? 1:0

- makeLearningCurve(mailStream, []evaluationMetrics, out, reportingPeriod,
writeOutAllPredictions)
    + _String_ out: output file. Scores are written to "out.scoreName", predictions
    to "out.pred"
    + _bool_ writeOut...: if _True_, writes all prediction to file
    + _int_ reportingPeriod: every how many examples the model is evaluated.
- getInfo(): debugging method for printing

--------------------------

NaiveBayesFeatureHashing
================

Usage: "java -cp .:lib/javax.mail.jar NaiveBayesFeatureHashing \\
 [0] indexPath stopWordsPath logNbOfBuckets threshold outPath
  reportingPeriod maxN [-writeOutAllPredictions]"

-------------------
Parsing
==================
By now, I can ignore how parsing is done, just that EmlParser returns a stream
 of emails, that the classifier later parses as ParsedText.

When constructing the EmlParser, n stands for the maximum n-gramm size

Printing parsedText returns weird words: they have been stemmed,
that's why

ParsedText
~~~~~~~~~~~~~~~~
- Set<string> ngrams: set of ngramms with spaces. Example:
[raul, raul vaz, vaz]

EmlParser
~~~~~~~~~~~~~~
Contains regular expresions that eliminate spaces, tags and apostrophes. They are:

```java
    String htmlTag = "<.*?>";
        String spam = "[sS][pP][aA][mM]";
        String nonText = "[^A-Za-z\\s\\._,/\\\\]";

        Pattern pRemove = Pattern.compile("("+htmlTag+")|("+spam+")|("+nonText+")");
        Pattern pSpace = Pattern.compile("[^A-Za-z0-9\\s]");

        return pSpace.matcher(pRemove.matcher(text).replaceAll("")).replaceAll(" ");
```

TODO: start applying NB on the ParsedTexts.


