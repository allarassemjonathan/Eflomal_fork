package eflomal;

import java.util.ArrayList;
import java.util.TreeMap;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {
        String filename = "piglatin_v0.zip";
        // String filename = "sl-en.zip";
        // String filename = "fr-en.zip";
        SamplingIBM1 model = new SamplingIBM1(filename);
        ArrayList<TreeMap<Integer, Double>> dirch = model.WordAlignment(1000);

        try {
            // Create a PrintWriter object with FileWriter as argument
            PrintWriter writer = new PrintWriter(new FileWriter("output.txt"));

            // Print content to the file

            for (int i = 0; i < dirch.size(); i++) {
                writer.println(i + " " + dirch.get(i));
                writer.println();
            }

            // Close the PrintWriter to flush and close the file
            writer.close();

            System.out.println("Content has been written to the file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}