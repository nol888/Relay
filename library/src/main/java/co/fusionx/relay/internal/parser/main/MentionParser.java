package co.fusionx.relay.internal.parser.main;

import co.fusionx.relay.util.ParseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class MentionParser {
    private static final String MENTION_PATTERN_FORMAT = "(^|[^\\d\\w])%s([^\\d\\w]|$)";

    private static final Map<String, Pattern> MENTION_PATTERNS = new HashMap<>();

    public static boolean onMentionableCommand(final String message, final String userNick,
                                               final String[] additionalHighlightWords) {
        final List<String> list = ParseUtils.splitRawLine(message, false);

        for (final String s : list) {
            final Pattern nickPattern = getPatternFor(userNick);
            if (nickPattern.matcher(s).matches()) {
                return true;
            }

            for (String w : additionalHighlightWords) {
                final Pattern wordPattern = getPatternFor(w);
                if (wordPattern.matcher(s).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Pattern getPatternFor(final String highlightWord) {
        if (!MENTION_PATTERNS.containsKey(highlightWord)) {
            MENTION_PATTERNS.put(highlightWord, Pattern.compile(String.format(
                    Locale.US, MENTION_PATTERN_FORMAT, Pattern.quote(highlightWord))));
        }

        return MENTION_PATTERNS.get(highlightWord);
    }
}