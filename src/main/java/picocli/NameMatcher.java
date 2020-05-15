package picocli;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class NameMatcher {
    static List<String> splitIntoChunks(String command) {
        List<String> result = new ArrayList<String>();
        int start = 0;
        for (int i = 0, codepoint; i < command.length(); i += Character.charCount(codepoint)) {
            codepoint = command.codePointAt(i);
            if (Character.isUpperCase(codepoint) || '-' == codepoint) {
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
            return ((char) Character.toUpperCase(codepoint)) + str.substring(1 + Character.charCount(codepoint));
        }
        return str;
    }

    static String match(Set<String> set, String abbreviation) {
        if (set.contains(abbreviation)) { // return exact match
            return abbreviation;
        }
        List<String> abbreviatedKeyChunks = splitIntoChunks(abbreviation);
        List<String> candidates = new ArrayList<String>();
        next_key:
        for (String key : set) {
            List<String> keyChunks = splitIntoChunks(key);
            if (abbreviatedKeyChunks.size() <= keyChunks.size() && keyChunks.get(0)
                    .startsWith(abbreviatedKeyChunks.get(0))) { // first chunk must match
                int matchCount = 1;
                int keyChunk = 1;
                for (int i = 1; i < abbreviatedKeyChunks.size(); i++) {
                    boolean found = false;
                    for (int j = keyChunk; j < keyChunks.size(); j++) {
                        if (keyChunks.get(j).startsWith(abbreviatedKeyChunks.get(i))) { // first chunk must match
                            keyChunk = j + 1;
                            found = true;
                            break;
                        }
                    }
                    if (!found) { // not a candidate
                        continue next_key;
                    }
                    matchCount++;
                }
                if (matchCount == abbreviatedKeyChunks.size()) {
                    candidates.add(key);
                }
            }
        }
        if (candidates.size() > 1) {
            String str = candidates.toString();
            throw new IllegalArgumentException(abbreviation + " is not unique: it matches '" +
                    str.substring(1, str.length() - 1).replace(", ", "', '") + "'");
        }
        return candidates.isEmpty() ? abbreviation : candidates.get(0); // return original if fail to match
    }
}
