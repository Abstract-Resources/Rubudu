package it.bitrule.rubudu.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public final class JavaUtils {

    /**
     * Parses a string into an integer, returning null if the string is null or not a valid integer.
     * @param s The string to parse.
     * @return The integer value, or null if the string is null or not a valid integer.
     */
    public @Nullable Integer parseInt(@Nullable String s) {
        if (s == null) return null;

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}