package bg.marinov.muppet;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateCompiler {
	private static final int PERVIEW_SIZE = 80;

	private final String mExprStart;
	private final Pattern mStartPattern;
	private final Pattern mExprEndPattern;
	private final Pattern mScriptEndPattern;

	// @formatter:off
	// TODO: these regexps are not quite right - e.g. single line comment DOTALL
	private static List<String> SKIP_PATTERNS = Arrays.asList( 
		"\\/\\*.*?\\*\\/",		// multiline comment
		"\\/\\/.*",				// single line comment
		"'(?:\\\\'|[^'])*'",	// single quoted string
		"\"(?:\\\\\"|[^\"])*\"", // double quoted string
		".*?"					// anything else
	);
	// @formatter:on

	public static final TemplateCompiler DEFAULT_COMPILER = new TemplateCompiler("<?", "?>", "<?=", "?>");

	private static Pattern getEndPattern(final String aRegExp) {
		final String patterns = String.join("|", SKIP_PATTERNS);

		return Pattern.compile("(?:" + patterns + ")*(" + Pattern.quote(aRegExp) + ")", Pattern.DOTALL);
	}

	private TemplateCompiler(final String aScriptTagStart,
			final String aScriptTagEnd,
			final String aExprTagStart,
			final String aExprTagEnd) {

		mExprStart = aExprTagStart;
		mStartPattern = Pattern.compile(Pattern.quote(aExprTagStart) + '|' + Pattern.quote(aScriptTagStart));
		mExprEndPattern = getEndPattern(aExprTagEnd);
		mScriptEndPattern = getEndPattern(aScriptTagEnd);
	}

	public static TemplateCompiler getInstance() {
		return DEFAULT_COMPILER;
	}

	public static TemplateCompiler getInstance(final String aScriptTagStart,
			final String aScriptTagEnd,
			final String aExprTagStart,
			final String aExprTagEnd) {
		return new TemplateCompiler(aScriptTagStart, aScriptTagEnd, aExprTagStart, aExprTagEnd);
	}

	public String compileToScript(final String aSource) throws Exception {
		final StringBuilder buffer = new StringBuilder(aSource.length());

		final Matcher m = mStartPattern.matcher(aSource);
		final Matcher mScriptEnd = mScriptEndPattern.matcher(aSource);
		final Matcher mExprEnd = mExprEndPattern.matcher(aSource);

		int index = 0;
		while (m.find(index)) {
			final String text = escapeString(aSource.substring(index, m.start()));
			buffer.append("echo('").append(text).append("');\n");

			if (m.group().equals(mExprStart)) {
				if (!mExprEnd.find(m.end())) {
					final String preview = aSource.substring(
						m.end(),
						Math.min(m.end() + PERVIEW_SIZE, aSource.length()));

					throw new Exception("Expression not closed: " + preview);
				}

				index = mExprEnd.end();

				final int end = mExprEnd.start(1);
				final String expr = aSource.substring(m.end(), end);
				buffer.append("echo(").append(expr).append(");\n");
			} else {
				final int end;
				if (!mScriptEnd.find(m.end())) {
					end = aSource.length();
					index = end;
				} else {
					end = mScriptEnd.start(1);
					index = mScriptEnd.end();
				}

				final String code = aSource.substring(m.end(), end);
				buffer.append(code);
			}
		}

		// append remaining if there is any
		if (index != aSource.length()) {
			final String remaining = escapeString(aSource.substring(index));
			buffer.append("echo('").append(remaining).append("');\n");
		}

		return buffer.toString();
	}

	private static String escapeString(final String aString) {
		return aString.replace("\\", "\\\\").replace("'", "\\'");
	}
}
