package xyz.simek.jgeck.model;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class IniHighlighter extends Highlighter {

    public IniHighlighter(CodeArea codeArea) {
		super(codeArea);
	}

    private static final String SECTION_PATTERN = "\\[[^\\]]*\\]";
    private static final String KEY_PATTERN = "(\\w+(?=\\=))";
    private static final String VALUE_PATTERN = "(?<=\\=)(.*)";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    // one line comments: # or ; or // and multi-line comments: /**/
    private static final String COMMENT_PATTERN = "#[^\n]*" + "|" + ";[^\n]*" + "|" + "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<SECTION>" + SECTION_PATTERN + ")"
            + "|(?<KEY>" + KEY_PATTERN + ")"
            + "|(?<VALUE>" + VALUE_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    @Override
	protected StyleSpans<Collection<String>> computeHighlighting(final String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("SECTION") != null ? "ini_section" :
                    matcher.group("KEY") != null ? "ini_key" :
                    matcher.group("VALUE") != null ? "ini_value" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
