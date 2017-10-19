import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Jessa Bekker
 *
 * This class creates a stream of e-mails from files. Index files link to e-mails or other index files.
 *
 * (c) 2017
 */
public class MailStream implements Iterable<LabeledText> {

    private String indexPath;
    private EmlParser parser;

    /**
     * Initialize the mail stream with the path to the root index and an e-mail parser
     *
     * An index is a file where each line represents either an e-mail or a sub index.
     * An e-mail line for of a spam mail has the following format: "spam /path/to/mail.eml"
     * An e-mail line for of a ham mail has the following format: "ham /path/to/mail.eml"
     * A sub index line has the following format: "/path/to/other/index"
     * The paths are relative to the location of the file which has the path.
     *
     * @param indexPath path to the root index.
     * @param parser The parser to parse each e-mail
     */
    public MailStream(String indexPath, EmlParser parser)  {
        this.indexPath = indexPath;
        this.parser = parser;
    }


    /**
     * The actual stream. This iterator iterates over all the e-mails in the root index and its sub indexes.
     *
     * @return The e-mail stream iterator.
     */
    @Override
    public Iterator<LabeledText> iterator() {
        return new Iterator<LabeledText>() {

            private Queue<File> indexQueue = null;
            private BufferedReader reader = null;
            private LabeledText nextExample = null;
            private File currentFolder = null;

            @Override
            public boolean hasNext() {

                // initialize
                if (indexQueue == null) {
                    indexQueue = new LinkedList<>();
                    try {
                        currentFolder = new File(indexPath).getParentFile();
                        reader = new BufferedReader(new FileReader(indexPath));
                    }catch (Exception e) {
                        System.err.println(e.getMessage());
                        return false; // Couldn't open index path
                    }
                }


                while (true) {
                    // read the next line
                    String line;
                    try {
                        line = reader.readLine();
                    } catch (Exception e) {
                        System.err.println("couldn't read line");
                        return false; // couldn't read line
                    }

                    // If the end of the current index file is reached, move on to the next buffered index file
                    if (line == null)
                        try {
                            reader.close();
                            File nextIndex = indexQueue.remove();
                            reader = new BufferedReader(new FileReader(nextIndex));
                            currentFolder = nextIndex.getParentFile();
                        } catch (Exception e) {
                            return false; // nothing left in the indexQueue
                        }

                    // parse the line
                    else {
                        try {
                            String[] splitLine = line.split("\\s");

                            //if the line contains a sub index, add this to the buffered index files
                            if (splitLine.length==1) {
                                indexQueue.add(new File(currentFolder, splitLine[0]));
                            }

                            // If the line contains an e-mail, parse it and put it as the next example
                            else {
                                int cl = splitLine[0].equals("spam")?1:0;
                                String emlPath = new File(currentFolder, splitLine[1]).getPath();
                                nextExample = new LabeledText(parser.parse(emlPath), cl);
                                return true; // a new e-mail is ready
                            }
                        } catch (Exception e) {
                            // If a line was not able to be parsed, write out an error message, and move on to the next line
                            System.err.println("Couldn't parse line " + line);
                        }

                    }
                }
            }

            @Override
            public LabeledText next() {
                return nextExample;
            }

            @Override
            public void remove() {
                //do nothing
            }
        };
    }

}
