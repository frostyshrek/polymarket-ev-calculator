package com.ufcstudy.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ConsoleInput {

    private final BufferedReader reader =
            new BufferedReader(
                    new InputStreamReader(System.in)
            );

    public String readLine(String prompt) {
        System.out.print(prompt);

        try {
            String value = reader.readLine();

            if (value == null) {
                return "";
            }

            return value.trim();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read console input",
                    exception
            );
        }
    }

    public int readInteger(
            String prompt,
            int defaultValue
    ) {
        String value = readLine(prompt);

        if (value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            System.out.println(
                    "Invalid number. Using " + defaultValue + "."
            );

            return defaultValue;
        }
    }

    public double readDouble(
            String prompt,
            double defaultValue
    ) {
        String value = readLine(prompt);

        if (value.isBlank()) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            System.out.println(
                    "Invalid number. Using " + defaultValue + "."
            );

            return defaultValue;
        }
    }

    public void pause() {
        readLine("\nPress Enter to continue...");
    }
}