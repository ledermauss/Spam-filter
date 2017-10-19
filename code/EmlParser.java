import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;



/**
 * @author Jessa Bekker
 *
 * This class parses e-mails
 *
 * Optionally, you can make a subclass of the EmlParser that extracts more/different features from the text.
 *
 * (c) 2017
 */
public class EmlParser {

    private Set<String> stopWords;
    private int n;

    /**
     * Initialize the parser
     *
     * @param stopWordsPath Path to the file with stop words, i.e. words to ignore in e-mails
     * @param n The maximum n for n-grams. E.g. if n=3 the parser extracts single words, pairs and triples.
     * @throws IOException
     */
    public EmlParser(String stopWordsPath, int n) throws IOException {
        this.n = n;
        stopWords = new HashSet<>();
        stopWords.add("");
        BufferedReader in = new BufferedReader(new FileReader(stopWordsPath));
        String line;
        PorterStemmer stemmer = new PorterStemmer();
        while ((line = in.readLine())!=null){
            stopWords.add(stemmer.stem(line));
        }
    }

    /**
     * Parse the e-mail in file emlPath
     *
     * @param emlPath Path to the e-mail
     * @return The parsed text
     */
    public ParsedText parse(String emlPath) {
        return new ParsedText(getNgrams(emlPath, n, stopWords));
    }

    /**
     * Get the subject and body from a *.eml file.
     *
     * @param emlPath Path to the e-mail file
     * @return The subject and body as a string
     * @throws IOException
     * @throws MessagingException
     */
    private static String getSubjectAndBody(String emlPath) throws IOException, MessagingException {

        Properties props = System.getProperties();
        props.put("mail.host", "smtp.dummydomain.com");
        props.put("mail.transport.protocol", "smtp");


        Session mailSession = Session.getDefaultInstance(props, null);
        InputStream source = new FileInputStream(emlPath);
        MimeMessage message = new MimeMessage(mailSession, source);

        String subject = message.getSubject();
        Object bodyObj = message.getContent();
        while (bodyObj instanceof Multipart) {
            bodyObj = ((Multipart) bodyObj).getBodyPart(0).getContent();
        }
        String body = (String) bodyObj;

        return subject+" "+body;
    }

    /**
     * Clean the given text. Cleaning means removing html tags and punctuation
     *
     * @param text The text to clean
     * @return Cleaned text
     */
    private static String cleanText(String text) {
        String htmlTag = "<.*?>";
        String spam = "[sS][pP][aA][mM]";
        String nonText = "[^A-Za-z\\s\\._,/\\\\]";

        Pattern pRemove = Pattern.compile("("+htmlTag+")|("+spam+")|("+nonText+")");
        Pattern pSpace = Pattern.compile("[^A-Za-z0-9\\s]");

        return pSpace.matcher(pRemove.matcher(text).replaceAll("")).replaceAll(" ");

    }


    /**
     * Extract clean n-grams from a raw e-mail file.
     *
     * @param emlPath Path of the e-mail
     * @param n The maximum n for n-grams. E.g. if n=3 the parser extracts single words, pairs and triples.
     * @param stopWords Set of stopwords to be removed from the text. The stopwords are expected to be stemmed.
     * @return Set of clean N-grams
     */
    public static Set<String> getNgrams(String emlPath, int n, Set<String> stopWords){
        try{
            //get the words from the body and subject
            String subjectAndBody = cleanText(getSubjectAndBody(emlPath));
            String[] words = subjectAndBody.split("\\s+");

            // Stem all the word. Stemming reduces inflected (or sometimes derived) words to their word stem.
            // The goal of stemming is to map related words (e.g eat, eats, eating) to the same stem.
            // Meanwhile, remove the stop words
            PorterStemmer stemmer = new PorterStemmer();
            Set<String> ngrams = new HashSet<>();
            for (int i =0; i<words.length; i++) {
                String stemmed = stemmer.stem(words[i]);
                if (stopWords.contains(stemmed)) {
                    words[i]=null;
                }
                else{
                    words[i]=stemmed;
                }
            }

            //Extract n-grams from the stemmed sequence of words
            for (int i =0; i<words.length; i++){
                int length=0;
                String ngram = "";
                int j=0;
                while (length<n && i+j<words.length){
                    if (words[i+j]!=null){
                        ngram +=" "+words[i+j];
                        length++;
                        ngrams.add(ngram.trim());
                    }
                    j++;
                }
            }

            return ngrams;
        } catch (Exception e) {
            return new HashSet<>();
        }
    }


}
