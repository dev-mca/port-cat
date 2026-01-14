package com.melihcan.portcat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WindowsCommand {
    static final String cmd = "echo Hello World!";

    public static void main(String[] args){
        try {
            // Run command
            Process process = Runtime.getRuntime().exec(cmd);

            // Get input
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read command standard output
            String s;
            System.out.println("Standard output: ");
            while ((s = stdInput.readLine()) != null){
                System.out.println(s);
            }

            // Read command errors
            System.out.println("Standard error: ");
            while ((s = stdError.readLine()) != null){
                System.out.println(s);
            }

        }catch (Exception e){
            e.printStackTrace(System.err);
        }
    }
}
