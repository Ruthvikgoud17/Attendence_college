package com.attendance.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for formatting tabular data with clean ASCII boxes in console.
 */
public class TableFormatter {

    // ANSI Colors for console presentation
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String CYAN = "\u001B[36m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";

    private final String[] headers;
    private final List<String[]> rows;
    private String title = null;

    public TableFormatter(String... headers) {
        this.headers = headers;
        this.rows = new ArrayList<>();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addRow(String... cells) {
        rows.add(cells);
    }

    public void print() {
        int columns = headers.length;
        int[] colWidths = new int[columns];

        for (int i = 0; i < columns; i++) {
            colWidths[i] = stripAnsi(headers[i]).length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, columns); i++) {
                int len = stripAnsi(row[i]).length();
                if (len > colWidths[i]) {
                    colWidths[i] = len;
                }
            }
        }

        // Add padding
        for (int i = 0; i < columns; i++) {
            colWidths[i] += 2;
        }

        int totalWidth = 1;
        for (int w : colWidths) {
            totalWidth += w + 1;
        }

        // Horizontal separator
        StringBuilder sep = new StringBuilder("+");
        for (int w : colWidths) {
            sep.append("-".repeat(w)).append("+");
        }
        String separator = sep.toString();

        // Print Title if exists
        if (title != null && !title.isEmpty()) {
            System.out.println("+" + "=".repeat(totalWidth - 2) + "+");
            int innerLen = totalWidth - 2;
            int padTotal = innerLen - stripAnsi(title).length();
            int padLeft = Math.max(0, padTotal / 2);
            int padRight = Math.max(0, padTotal - padLeft);
            System.out.println("|" + " ".repeat(padLeft) + BOLD + CYAN + title + RESET + " ".repeat(padRight) + "|");
        }

        System.out.println(separator);

        // Print Headers
        StringBuilder headerRow = new StringBuilder("|");
        for (int i = 0; i < columns; i++) {
            String text = headers[i];
            int pad = colWidths[i] - stripAnsi(text).length();
            headerRow.append(" ").append(BOLD).append(text).append(RESET).append(" ".repeat(Math.max(0, pad - 1))).append("|");
        }
        System.out.println(headerRow);
        System.out.println(separator);

        // Print Rows
        if (rows.isEmpty()) {
            int innerLen = totalWidth - 2;
            String emptyMsg = "No records found";
            int padLeft = (innerLen - emptyMsg.length()) / 2;
            int padRight = innerLen - emptyMsg.length() - padLeft;
            System.out.println("|" + " ".repeat(padLeft) + YELLOW + emptyMsg + RESET + " ".repeat(padRight) + "|");
        } else {
            for (String[] row : rows) {
                StringBuilder rowStr = new StringBuilder("|");
                for (int i = 0; i < columns; i++) {
                    String val = (i < row.length && row[i] != null) ? row[i] : "";
                    int rawLen = stripAnsi(val).length();
                    int pad = colWidths[i] - rawLen;
                    rowStr.append(" ").append(val).append(" ".repeat(Math.max(0, pad - 1))).append("|");
                }
                System.out.println(rowStr);
            }
        }

        System.out.println(separator);
    }

    private static String stripAnsi(String s) {
        if (s == null) return "";
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
