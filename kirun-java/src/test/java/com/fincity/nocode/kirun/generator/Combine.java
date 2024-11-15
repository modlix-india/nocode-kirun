package com.fincity.nocode.kirun.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Combine {

    public static final String PATH_PREFIX = "./../kirun-";

    public static void main(String[] args) throws IOException {

        Path jscsvPath = Paths.get(PATH_PREFIX + "js/generator/validation-js.csv");
        Path javacsvPath = Paths.get(PATH_PREFIX + "java/generator/validation-java.csv");

        if (!Files.exists(jscsvPath) || !Files.exists(javacsvPath)) {
            System.out.println("CSV files not found");
            return;
        }

        // In case you have trouble reading the file just remove the charset param or
        // change the charset.

        List<String> jsLines = Files.readAllLines(jscsvPath, StandardCharsets.US_ASCII);
        List<String> javaLines = Files.readAllLines(javacsvPath, StandardCharsets.US_ASCII);

        Map<String, List<String>> jsPerFunction = getPerFunction(jsLines);
        Map<String, List<String>> javaPerFunction = getPerFunction(javaLines);

        List<String> combinedLines = new ArrayList<>();

        List<String> keys = Stream.concat(jsPerFunction.keySet().stream(), javaPerFunction.keySet().stream())
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        for (String key : keys) {
            List<String> left = jsPerFunction.get(key);
            List<String> right = javaPerFunction.get(key);

            if ((left == null || left.isEmpty()) && (right == null || right.isEmpty())) {
                continue;
            }

            if (right == null || right.isEmpty()) {
                combinedLines.addAll(left);
            } else if (left == null || left.isEmpty()) {
                combinedLines.addAll(right.stream().map(e -> ",,,," + e).collect(Collectors.toList()));
            } else {
                int i = 0;
                int j = 0;

                while (i < left.size() && j < right.size()) {
                    String leftString = i < left.size() ? left.get(i) : "";
                    String rightString = j < right.size() ? right.get(j) : "";
                    String commas;
                    if (leftString.isBlank()) {
                        commas = ",,,,";
                    } else {
                        int count = 0;
                        for (int k = 0; k < leftString.length(); k++) {
                            if (leftString.charAt(k) == ',') {
                                count++;
                            }
                        }
                        commas = switch (count) {
                            case 0 -> ",,,,";
                            case 1 -> ",,,";
                            default -> ",,";
                        };
                    }
                    i++;
                    j++;

                    combinedLines.add(leftString + commas + rightString);
                }
            }
            combinedLines.add("");
        }

        Files.write(Paths.get(PATH_PREFIX + "java/generator/validation-combined.csv"), combinedLines);
    }

    private static Map<String, List<String>> getPerFunction(List<String> lines) {

        Map<String, List<String>> perFunction = new HashMap<>();

        List<String> bind = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {

            String line = lines.get(i);

            if (line.isBlank()) {

                if (!bind.isEmpty()) {
                    perFunction.put(bind.get(0), bind);
                    bind = new ArrayList<>();
                }
                i++;
                continue;
            }

            bind.add(line);
            i++;
        }

        return perFunction;
    }
}
