import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public final static String sentencePattern = "[\\.!\\?]\\s*";
    public final static String wordPattern = ",*\\s+";

    public static String readFileToString(String filePath) throws IOException {
        String string = new String(Files.readAllBytes(Paths.get(filePath)));
        return string;
    }

    public static void main(String[] args) {
        File file = new File(args[0]);
        StringBuilder sb = new StringBuilder();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                sb.append(scanner.nextLine());
            }
            String text = sb.toString();
            System.out.println("java Main " + file.getName());
            printResult(text, scanner);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }

    }


    private static void printResult(String text, Scanner scanner) {
        int words = getWordCount(text);
        int sentences = getSentenceCount(text);
        int characters = getCharacterCount(text);
        int syllables = getSyllableCount(text)[0];
        int polysyllables = getSyllableCount(text)[1];

        System.out.println("The text is: ");
        System.out.println(text);
        System.out.println("Words: " + words);
        System.out.println("Sentences: " + sentences);
        System.out.println("Characters: " + characters);
        System.out.println("Syllables: " + syllables);
        System.out.println("Polysyllables: " + polysyllables);

        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
        String input = scanner.next();
        System.out.println();
        switch (input) {
            case "ARI":
                printARI(text);
                break;
            case "FK":
                printFK(text);
                break;
            case "SMOG":
                printSMOG(text);
                break;
            case "CL":
                printCL(text);
                break;
            case "all":
                printARI(text);
                printFK(text);
                printSMOG(text);
                printCL(text);
                break;
        }

        double cl = indexScore(calculateColeman(text));
        double smog = indexScore(calculateSmog(text));
        double fk = indexScore(calculateFlesch(text));
        double ari = indexScore(calculateAutomatedReadability(text));
        double average = (cl + smog + fk + ari) / 4;

        System.out.println("\nThis text should be understood on average by " + Math.round(average * 100.0) / 100.0 + " year olds.");
    }

    private static void printCL(String text) {
        String ageRange = "(about " + Integer.toString(indexScore(calculateColeman(text))) + " year olds).";
        System.out.println("Coleman" + (char) 8211 + "Liau: " + calculateColeman(text) + " " + ageRange);
    }

    private static void printSMOG(String text) {
        String ageRange = "(about " + indexScore(calculateSmog(text)) + " year olds).";
        System.out.println("Simple Measure of Gobbledygook: " + calculateSmog(text) + " " + ageRange);
    }

    private static void printFK(String text) {
        String ageRange = "(about " + indexScore(calculateFlesch(text)) + " year olds).";
        System.out.println("Flesch" + (char) 8211 + "Kincaid: " + calculateFlesch(text) + " " + ageRange);

    }

    private static void printARI(String text) {
        String ageRange = "(about " + indexScore(calculateAutomatedReadability(text)) + " year olds).";
        System.out.println("Automated Readability Index: " + calculateAutomatedReadability(text) + " " + ageRange);
    }

    private static int indexScore(double score) {
        int index = (int) Math.round(score - 1);
        int[] ageBracket = {6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 24, 24};
        return ageBracket[index];
    }

    private static double calculateAutomatedReadability(String text) {
        int characters = getCharacterCount(text);
        int words = getWordCount(text);
        int sentences = getSentenceCount(text);

        double score = (4.71 * characters / words) + (0.5 * words / sentences) - 21.43;
        return Math.round(score * 100.0) / 100.0;
    }

    private static double calculateFlesch(String text) {
        int words = getWordCount(text);
        int sentences = getSentenceCount(text);
        int syllables = getSyllableCount(text)[0];

        double score = (0.39 * words / sentences) + (11.8 * syllables / words) - 15.59;
        return Math.round(score * 100.0) / 100.0;
    }

    private static double calculateSmog(String text) {
        int polysyllables = getSyllableCount(text)[1];
        int sentences = getSentenceCount(text);

        double score = 1.043 * Math.sqrt((double) polysyllables * 30 / sentences) + 3.1291;
        return Math.round(score * 100.0) / 100.0;
    }

    private static double calculateColeman(String text) {
        double L = (double) getCharacterCount(text) / getWordCount(text) * 100;
        double S = (double) getSentenceCount(text) / getWordCount(text) * 100;

        double score = 0.0588 * L - 0.296 * S - 15.8;
        return Math.round(score * 100.0) / 100.0;
    }


    private static int getSentenceCount(String text) {
        return text.split(sentencePattern).length;
    }

    private static int getWordCount(String text) {
        return text.split(wordPattern).length;
    }

    private static int getCharacterCount(String text) {
        return text.replaceAll("\\s", "").length();
    }

    private static int[] getSyllableCount(String text) {
        int totalCount = 0;
        int polySyllableCount = 0;

        for (String sentence : text.split(sentencePattern)) {
            for (String word : sentence.split(wordPattern)) {
                if (getSyllableCountForWord(word) > 2) {
                    polySyllableCount++;
                }
                totalCount += getSyllableCountForWord(word);
            }
        }
        return new int[]{totalCount, polySyllableCount};
    }

    private static int getSyllableCountForWord(String word) {
        int count = 0;
        char[] characters = word.toLowerCase().toCharArray();
        for (int i = 0; i < characters.length; i++) {
            char c = characters[i];
            if (isVowel(c)) {
                count++;
                if ((i > 0) && (isVowel(characters[i - 1]))) {
                    count--;
                }
            }
        }
        if (word.endsWith("e")) {
            count--;
        }
        if (count == 0) {
            count = 1;
        }
        return count;
    }

    private static boolean isVowel(char c) {
        return (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'y');
    }

}
