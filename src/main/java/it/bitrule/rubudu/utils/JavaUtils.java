package it.bitrule.rubudu.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;

@UtilityClass
public final class JavaUtils {

    public final static @NonNull String DATE_FORMAT = "dd-MM-yy HH:mm:ss";
    private final static @NonNull SimpleDateFormat FORMAT = new SimpleDateFormat(DATE_FORMAT);

    /**
     * Parses a string into a date, returning -1 if the string is null or not a valid date.
     *
     * @param format The string to parse.
     * @return The date value, or -1 if the string is null or not a valid date.
     */
    public static long parseDate(@Nullable String format) {
        if (format == null || format.isEmpty()) return -1L;

        try {
            return FORMAT.parse(format).getTime();
        } catch (Exception e) {
            return -1L;
        }
    }

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