/*
 * File: PlagiarismDetector2
 * Created By: Fwaad Ahmad
 * Created On: 09-03-2024
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class PlagiarismDetector {

    // Threshold for similarity scores
    private static final double SIMILARITY_THRESHOLD = 0.5;
    // Minimum sequence length to report as plagiarism
    private static final int MIN_SEQUENCE_LENGTH = 5;
    // Set of common stop words
    private static final Set<String> STOP_WORDS = new HashSet<>(Set.of("the", "a", "an", "in", "on", "of", "for"));

    public static void main(String[] args) {
        System.out.println();
        if (args.length < 2) {
            System.out.println("Usage: PlagiarismDetector <file1> <file2> ...");
            return;
        }

        // Update common stop words set from common_stop_words text file
        try {
            String stopWordsText = readFile("./src/common_stop_words.txt");
            STOP_WORDS.addAll(Arrays.stream(stopWordsText.split("\\s+")).toList());
        } catch (Exception ignored) {
        }

        // Store preprocessed file contents for comparison
        List<String> preprocessedContents = new ArrayList<>();
        for (String filePath : args) {
            try {
                // reading, preprocessing and storing files
                preprocessedContents.add(preprocessText(readFile(filePath)));
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + filePath);
            }
        }

        // Compare text documents and detect potential plagiarism
        for (int i = 0; i < preprocessedContents.size() - 1; i++) {
            for (int j = i + 1; j < preprocessedContents.size(); j++) {
                // get the similarity score between two texts
                double similarity = calculateSimilarity(preprocessedContents.get(i), preprocessedContents.get(j));
                // get the longest similar sequence
                List<String> longestSimilarSequence = findLongestCommonSubsequence(preprocessedContents.get(i),
                                                                                   preprocessedContents.get(j));

                // if similarity is above threshold and similar sequence's length is greater than threshold -> Plagiarism Detected
                // else No Plagiarism
                if (similarity >= SIMILARITY_THRESHOLD && longestSimilarSequence.size() >= MIN_SEQUENCE_LENGTH) {
                    System.out.printf("Potential plagiarism detected between %s and %s \nSimilarity: %.2f%% %n",
                                      args[i],
                                      args[j],
                                      similarity * 100);
                    System.out.println("Similar sequence(s):");
                    System.out.println("- " + String.join(" ", longestSimilarSequence.reversed()));
                } else {
                    System.out.println("No Plagiarism Detected.");
                }
            }
        }
    }

    /**
     * Calculate the Edit Distance between two texts
     *
     * @param text1 {@link String} - text to compare to
     * @param text2 {@link String} - text to compare from
     * @return {@link Integer} edit distance between two texts
     */
    private static int calculateEditDistance(String text1, String text2) {
        int len1 = text1.length();
        int len2 = text2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        return dp[len1][len2];
    }

    /**
     * Calculate similarity based on Edit Distance
     *
     * @param text1 {@link String} - text to compare to
     * @param text2 {@link String} - text to compare from
     * @return {@link Double} edit distance percent value between two texts (0 to 1)
     */
    private static double calculateSimilarity(String text1, String text2) {
        int maxLen = Math.max(text1.length(), text2.length());
        int editDistance = calculateEditDistance(text1, text2);
        return 1 - ((double) editDistance / maxLen);
    }

    /**
     * this method is used to read text content from a file
     *
     * @param filePath {@link String} path of the file to read
     * @return {@link String} text read from the file
     * @throws FileNotFoundException if file is not found
     */
    private static String readFile(String filePath) throws FileNotFoundException {
        StringBuilder content;
        try (Scanner scanner = new Scanner(new File(filePath))) {
            content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * this method preprocesses text by removing stop words and converting to lowercase
     *
     * @param text {@link String} text to preprocess
     * @return {@link String} processed text
     */
    private static String preprocessText(String text) {
        String lowercaseText = text.toLowerCase();
        return Arrays.stream(lowercaseText.split("\\s+"))
                     .filter(word -> !STOP_WORDS.contains(word))
                     .collect(Collectors.joining(" "));
    }

    /**
     * Find the longest common subsequences between two string
     *
     * @param str1 {@link String}
     * @param str2 {@link String}
     * @return {@link List<String>} list of longest common subsequence(s).
     */
    public static List<String> findLongestCommonSubsequence(String str1, String str2) {
        String[] words1 = str1.split("\\s+");
        String[] words2 = str2.split("\\s+");

        // Create a DP table to store lengths of longest common subsequences
        int[][] dp = new int[words1.length + 1][words2.length + 1];

        // Find the length of the longest common subsequence
        for (int i = 1; i <= words1.length; i++) {
            for (int j = 1; j <= words2.length; j++) {
                if (words1[i - 1].equals(words2[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // Backtrack to find the actual subsequences
        List<String> subsequences = new ArrayList<>();
        backtrack(words1, words2, dp, subsequences, words1.length, words2.length);

        return subsequences;
    }

    /**
     * Helper function to extract subsequences recursively
     *
     * @param words1       {@link String[]}
     * @param words2       {@link String[]}
     * @param dp           {@link Integer[][]}
     * @param subsequences {@link List<String>}
     * @param i            {@link Integer}
     * @param j            {@link Integer}
     */
    private static void backtrack(String[] words1,
                                  String[] words2,
                                  int[][] dp,
                                  List<String> subsequences,
                                  int i,
                                  int j) {
        if (i == 0 || j == 0) {
            return;
        }

        if (words1[i - 1].equals(words2[j - 1]) && dp[i][j] == dp[i - 1][j - 1] + 1) {
            subsequences.add(words1[i - 1]);
            backtrack(words1, words2, dp, subsequences, i - 1, j - 1);
        } else {
            if (dp[i][j] == dp[i - 1][j]) {
                backtrack(words1, words2, dp, subsequences, i - 1, j);
            } else {
                backtrack(words1, words2, dp, subsequences, i, j - 1);
            }
        }
    }

}


