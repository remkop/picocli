package picocli;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class AbbreviationMatcher {
    public static List<String> splitIntoChunks(String command, boolean caseInsensitive) {
        List<String> result = new ArrayList<String>();
        int start = 0;
        StringBuilder nonAlphabeticPrefix = new StringBuilder();
        while (start < command.length() && !Character.isLetterOrDigit(command.codePointAt(start))) {
            nonAlphabeticPrefix.appendCodePoint(command.codePointAt(start));
            start++;
        }
        if (nonAlphabeticPrefix.length() > 0) {
            result.add(nonAlphabeticPrefix.toString());
        }
        for (int i = start, codepoint; i < command.length(); i += Character.charCount(codepoint)) {
            codepoint = command.codePointAt(i);
            if ((!caseInsensitive && Character.isUpperCase(codepoint)) || '-' == codepoint) {
                String chunk = makeCanonical(command.substring(start, i));
                if (chunk.length() > 0) {
                    result.add(chunk);
                }
                start = i;
            }
        }
        if (start < command.length()) {
            String chunk = makeCanonical(command.substring(start));
            if (chunk.length() > 0) {
                result.add(chunk);
            }
        }
        return result;
    }

    private static String makeCanonical(String str) {
        if ("-".equals(str)) {
            return "";
        }
        if (str.startsWith("-") && str.length() > 1) {
            int codepoint = str.codePointAt(1);
            char[] uppercase = Character.toChars(Character.toUpperCase(codepoint));
            return new String(uppercase) + str.substring(1 + Character.charCount(codepoint));
        }
        return str;
    }

    public static String match(Set<String> set, String abbreviation, boolean caseInsensitive) {
        if (set.contains(abbreviation)) { // return exact match
            return abbreviation;
        }
        List<String> abbreviatedKeyChunks = splitIntoChunks(abbreviation, caseInsensitive);
        List<String> candidates = new ArrayList<String>();
        for (String key : set) {
            List<String> keyChunks = splitIntoChunks(key, caseInsensitive);
            if (matchKeyChunks(abbreviatedKeyChunks, keyChunks, caseInsensitive)) {
                candidates.add(key);
            }
        }
        if (candidates.size() > 1) {
            String str = candidates.toString();
            throw new IllegalArgumentException(abbreviation + " is not unique: it matches '" +
                    str.substring(1, str.length() - 1).replace(", ", "', '") + "'");
        }
        return candidates.isEmpty() ? abbreviation : candidates.get(0); // return the original if no match found
    }

    private static boolean matchKeyChunks(List<String> abbreviatedKeyChunks, List<String> keyChunks, boolean caseInsensitive) {
        if (abbreviatedKeyChunks.size() > keyChunks.size()) {
            return false;
        } else if (!startsWith(keyChunks.get(0), abbreviatedKeyChunks.get(0), caseInsensitive)) { // first chunk must match
            return false;
        }
        int matchCount = 1, lastMatchChunk = 1;
        for (int i = 1; i < abbreviatedKeyChunks.size(); i++, matchCount++) {
            boolean found = false;
            for (int j = lastMatchChunk; j < keyChunks.size(); j++) {
                if (found = startsWith(keyChunks.get(j), abbreviatedKeyChunks.get(i), caseInsensitive)) {
                    lastMatchChunk = j + 1;
                    break;
                }
            }
            if (!found) { // not a candidate
                break;
            }
        }
        return matchCount == abbreviatedKeyChunks.size();
    }

    private static boolean startsWith(String str, String prefix, boolean caseInsensitive) {
        if (prefix.length() > str.length()) {
            return false;
        } else if (isNonAlphabetic(str)) {
            return str.equals(prefix);
        }
        String strPrefix = str.substring(0, prefix.length());
        return caseInsensitive ? strPrefix.equalsIgnoreCase(prefix) : strPrefix.equals(prefix);
    }

    private static boolean isNonAlphabetic(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetterOrDigit(str.codePointAt(i))) { return false; }
        }
        return true;
    }
}
