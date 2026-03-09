package org.example.util;

import java.util.List;

public class FormatUtils {
    public static String padRight(String text, int length) {
        return String.format("%-" + length + "s", truncate(text, length));
    }

    public static String padLeft(String text, int length) {
        return String.format("%" + length + "s", truncate(text, length));
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return (text.length() <= maxLength) ? text : text.substring(0, maxLength - 3) + "...";
    }

    public static String formatHeader(String text) {
        return "\n>>> " + text.toUpperCase() + " <<<";
    }

    public static String formatBox(String text) {
        String border = "+" + "-".repeat(text.length() + 2) + "+";
        return border + "\n| " + text + " |\n" + border;
    }

    public static String formatTable(String[] headers, List<String[]> rows) {
        int columns = headers.length;
        int[] widths = new int[columns];

        for (int i = 0; i < columns; i++) {
            widths[i] = headers[i].length();
            for (String[] row : rows) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        StringBuilder sb = new StringBuilder();
        String line = buildDivider(widths);

        sb.append(line).append("\n");
        sb.append(buildRow(headers, widths)).append("\n");
        sb.append(line).append("\n");
        for (String[] row : rows) {
            sb.append(buildRow(row, widths)).append("\n");
        }
        sb.append(line);

        return sb.toString();
    }

    private static String buildDivider(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) sb.append("-".repeat(w + 2)).append("+");
        return sb.toString();
    }

    private static String buildRow(String[] data, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < data.length; i++) {
            sb.append(" ").append(padRight(data[i], widths[i])).append(" |");
        }
        return sb.toString();
    }
}
