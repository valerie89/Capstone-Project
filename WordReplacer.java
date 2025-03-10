import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Program that preforms a global search and replacement in a text file.
 * Supports three data structures(BSTreemap, RBTreeMap, MyHashMap)
 * author: valerie Pena vsp2116
 *
 */

public class WordReplacer {
    /**
     * Main method that will run my program which will check the input arguments and the word
     * replacments rules that it has to follow into the selected data structure and goes through the
     * input text file to replace words based on each Map or Tree Rule(BST, RBT, HASH)
     *
     * @param args This will take the input text file and the word replacement file and use the BST, RBT, or HASH
     *             To find the fastest run time
     */

    public static void main(String[] args) {
        // we need to validate the input arguments which are the inputTextFile, wordReplacementsFile, dataStructure
        if (args.length != 3) {
            System.err.println("Usage: java WordReplacer <input text file> <word replacements file> <bst|rbt|hash>");
            System.exit(1);
        }

        // this will ensure that the input file, the replacement file and which ever data structure is used will work
        //together when executing
        String inputTextFile = args[0];
        String wordReplacementsFile = args[1];
        String dataStructure = args[2];

        // Will initialize the BST, RBT, or HashMap
        MyMap<String, String> map = initializeMap(dataStructure);
        if (map == null) {
            System.err.println("Error: Invalid data structure '" + dataStructure + "' received.");
            System.exit(1);
        }
        //Will load our replacement constraints into the map from the replacement files
        try (BufferedReader replacementsInput = new BufferedReader(new FileReader(wordReplacementsFile))) {
            String fileLine;
            while ((fileLine = replacementsInput.readLine()) != null) {
                //
                String[] parts = fileLine.split("->");
                if (parts.length == 2) {
                    //Our word that we will have to replace
                    String key = parts[0].trim();
                    // Our word that we will have to replace it with
                    String value = parts[1].trim();

                    //Ensures no cycles (loops) are present when we replace because it can crash making it ineffective
                    if (checkForCycle(map, key, value)) {
                        System.err.println("Error: Cycle detected when trying to add replacement rule: " + key + "->" + value);
                        System.exit(1);
                    }
                    // replacement rules added to our map
                    map.put(key, value);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Cannot open file '" + wordReplacementsFile + "' for input.");
            System.exit(1);

        } catch (IOException e) {
            System.err.println("Error: An I/O error occurred reading '" + wordReplacementsFile + "'.");
            System.exit(1);
        }
        // will process the input texts files line by line replacing the words we need to replace.
        try (BufferedReader inputReader = new BufferedReader(new FileReader(inputTextFile))) {
            String currentLine;
            while ((currentLine = inputReader.readLine()) != null) {
                // will store the entire line after the process of replacements is done
                StringBuilder output = new StringBuilder();
                //temporarily holds the characters to form complete words for replacement
                StringBuilder wordHolder = new StringBuilder();

                // ensures to process every character in the line
                for (char ch : currentLine.toCharArray()) {
                    if (Character.isLetterOrDigit(ch)) {
                        //builds our current word
                        wordHolder.append(ch);
                    } else {
                        if (wordHolder.length() > 0) {
                            output.append(replaceWord(wordHolder.toString(), map));
                            wordHolder.setLength(0);
                        }
                        output.append(ch);
                    }
                }
                //This if handles the last word in the line if we have one that exist
                if (wordHolder.length() > 0) {
                    output.append(replaceWord(wordHolder.toString(), map));
                }
                System.out.printf("%s\n", output.toString().trim());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Cannot open file '" + inputTextFile + "' for input.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error: An I/O error occurred reading '" + inputTextFile + "'.");
            System.exit(1);
        }
    }
    /** *
     *
     * @param  word  The chosen word that will be replaced
     * @param  map The map that contains our replacement word pair
     * @return      This return statement will return the replacement word if found within our map or t will print the original word
     */

    private static String replaceWord(String word, MyMap<String, String> map) {
        // holds our final result with replacements applied
        StringBuilder result = new StringBuilder();
        // temp stores a part of the word being processed
        StringBuilder currentWord = new StringBuilder();

        for (char ch : word.toCharArray()) {
            if (Character.isLetter(ch)) {
                // will build on the current word if it's just a letter
                currentWord.append(ch);
            } else {
                //this takes into account if we were to encounter a nonletter which will make the one made already go first
                if (currentWord.length() > 0) {
                    String replacement = getTransitiveReplacement(currentWord.toString(), map);
                    if (replacement != null) {
                        // will replace with our matches case
                        result.append(matchCaseFormat(currentWord.toString(), replacement));
                    } else {
                        // else if no replacement will be found keeping the og word
                        result.append(currentWord);
                    }
                    // will clear the current word for the next batch
                    currentWord.setLength(0);
                }
                result.append(ch);
            }
        }
        if (currentWord.length() > 0) {
            String replacement = getTransitiveReplacement(currentWord.toString(), map);
            if (replacement != null) {
                result.append(matchCaseFormat(currentWord.toString(), replacement));
            } else {
                result.append(currentWord);
            }
        }
        return result.toString();

    }

    /** *
     *Find the final replacement for a word by following its chain of replacements.
     * <p>
     *     This method starts with the given word and keeps checking the map for its replacement.
     *     This will contiune the into the last word that needs to be replaced is replaced.
     * </p>
     * @param  word  The chosen word that will be replaced
     * @param  map The map that contains our replacement word pair
     * @return      This return statement will return the replacement word if found within our map or t will print the original word
     */

    private static String getTransitiveReplacement(String word, MyMap<String, String> map) {
        //directly replaces the words
        String replacement = map.get(word);
        while (replacement != null) {
            // checks if the replacement has a replacement (accounts for 2 cases of replacements)
            String next = map.get(replacement);
            //will stop if no replacements exit
            if (next == null) break;
            replacement = next;
        }
        // returns the final replacement or null of there are none that exists.
        return replacement;
    }

    /** *
     * This section will adjust the replacement word to match the case of the orginal word.
     * <p>
     *     If the original word is in all CAPS, the replacement will also be in all CAPS
     *     If we start with a capital letter the replacement will follow that
     *     Or the replacement is than changed to lowercase.
     * </p>
     * @param  original  The chosen word that will be replacing
     * @param  replacement The word that will take in the new word
     * @return      This return statement word will print out the replacement word
     */

    private static String matchCaseFormat(String original, String replacement) {
        if (original.equals(original.toUpperCase())) {
            // if the original is all caps it will make the replacement all caps
            return replacement.toUpperCase();

        }
        if (Character.isUpperCase(replacement.charAt(0))) {
            //capiyalizes the first letter of the replacement if the original starts with it
            return Character.toUpperCase(replacement.charAt(0)) + replacement.substring(1);
        }
        // or else it will od vice versa with lowercase
        return replacement.toLowerCase();
    }

    /**
     * @param  dataStructure  the name of the map type we will be using ("bst", "rbt:, or "hash")
     * @return  A new map instance of the either BST RBT OR HASH. Test for null as well
     */


    private static MyMap<String, String> initializeMap(String dataStructure) {
        if (dataStructure.equalsIgnoreCase("bst")) {
            return new BSTreeMap<>();
        } else if (dataStructure.equalsIgnoreCase("rbt")) {
            return new RBTreeMap<>();
        } else if (dataStructure.equalsIgnoreCase("hash")) {
            return new MyHashMap<>();
        } else {
            return null;
        }
    }
    /** *
     *
     * @param  map  The map will check ou pair replacement
     * @param  originalWord Our original word that is chosen to be replaced
     * @param  replacementValue The word to check for cycles in the replacement chain
     * @return      will return true if cycle is found otherwise false
     */

    private static boolean checkForCycle(MyMap<String, String> map, String originalWord, String replacementValue) {
        for (String current = replacementValue; current != null; current = map.get(current)) {
            if (originalWord.equals(current)) {
                // returns true if we see that a cycle is detected when the replacement loops back to the original word
                return true;
            }
        }
        // or else no cycles detected
        return false;
    }
}

