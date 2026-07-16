package com.ufcstudy.eventmatching.automated;

import java.util.Objects;

public final class FighterNameSimilarity {

    private final FighterNameNormalizer normalizer;

    public FighterNameSimilarity(
            FighterNameNormalizer normalizer
    ) {
        this.normalizer = Objects.requireNonNull(normalizer);
    }

    public double similarity(
            String left,
            String right
    ) {
        String normalizedLeft = normalizer.normalize(left);
        String normalizedRight = normalizer.normalize(right);

        if (normalizedLeft.equals(normalizedRight)) {
            return 1.0;
        }

        int distance = levenshtein(
                normalizedLeft,
                normalizedRight
        );

        int maximumLength = Math.max(
                normalizedLeft.length(),
                normalizedRight.length()
        );

        if (maximumLength == 0) {
            return 1.0;
        }

        return Math.max(
                0.0,
                1.0 - ((double) distance / maximumLength)
        );
    }

    private int levenshtein(
            String left,
            String right
    ) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];

        for (int column = 0;
             column <= right.length();
             column++) {
            previous[column] = column;
        }

        for (int row = 1;
             row <= left.length();
             row++) {
            current[0] = row;

            for (int column = 1;
                 column <= right.length();
                 column++) {
                int substitutionCost =
                        left.charAt(row - 1)
                                == right.charAt(column - 1)
                                ? 0
                                : 1;

                current[column] = Math.min(
                        Math.min(
                                current[column - 1] + 1,
                                previous[column] + 1
                        ),
                        previous[column - 1]
                                + substitutionCost
                );
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[right.length()];
    }
}