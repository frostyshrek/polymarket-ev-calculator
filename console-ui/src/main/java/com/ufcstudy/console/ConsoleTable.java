package com.ufcstudy.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConsoleTable {

    private final List<String[]> rows = new ArrayList<>();
    private String[] headers;

    public ConsoleTable headers(String... values) {
        headers = Arrays.copyOf(values, values.length);
        return this;
    }

    public ConsoleTable row(Object... values) {
        String[] row = Arrays.stream(values)
                .map(value -> value == null ? "" : value.toString())
                .toArray(String[]::new);

        rows.add(row);
        return this;
    }

    public void print() {
        if (headers == null) {
            throw new IllegalStateException(
                    "Table headers must be configured"
            );
        }

        int[] widths = calculateWidths();

        printSeparator(widths);
        printRow(headers, widths);
        printSeparator(widths);

        for (String[] row : rows) {
            printRow(row, widths);
        }

        printSeparator(widths);

        if (rows.isEmpty()) {
            System.out.println("(no records)");
        }
    }

    private int[] calculateWidths() {
        int[] widths = new int[headers.length];

        for (int index = 0;
             index < headers.length;
             index++) {
            widths[index] = headers[index].length();
        }

        for (String[] row : rows) {
            for (int index = 0;
                 index < Math.min(row.length, widths.length);
                 index++) {
                widths[index] = Math.max(
                        widths[index],
                        row[index].length()
                );
            }
        }

        return widths;
    }

    private void printSeparator(int[] widths) {
        StringBuilder result = new StringBuilder("+");

        for (int width : widths) {
            result.append("-".repeat(width + 2))
                    .append("+");
        }

        System.out.println(result);
    }

    private void printRow(
            String[] values,
            int[] widths
    ) {
        StringBuilder result = new StringBuilder("|");

        for (int index = 0;
             index < widths.length;
             index++) {
            String value =
                    index < values.length
                            ? values[index]
                            : "";

            result.append(" ")
                    .append(
                            String.format(
                                    "%-" + widths[index] + "s",
                                    value
                            )
                    )
                    .append(" |");
        }

        System.out.println(result);
    }
}