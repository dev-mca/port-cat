package com.melihcan.portcat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WindowsCommand {
    static final String cmd = "cmd /c netstat -an";
    static boolean toggle = true;
    static int count = 0;

    static List<String> netstatHistory = new ArrayList<String>();

    public static void main(String[] args) {

        while (toggle && count < 4) {
            count++;
            StringBuilder output = new StringBuilder();

            try {
                // Run command
                Process process = Runtime.getRuntime().exec(cmd);

                // Get input
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                // Read command standard output
                String s;
                System.out.println("Standard output: ");
                while ((s = stdInput.readLine()) != null) {
                    output.append(s).append("\n");
                }

                // Read command errors
                System.out.println("Standard error: ");
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }

                process.waitFor();

                netstatHistory.add(output.toString());

                // delay for while loop
                Thread.sleep(2000);

            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        System.out.println("History: " + netstatHistory);
    }
}
