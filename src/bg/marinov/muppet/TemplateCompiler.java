package bg.marinov.muppet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import re.agiledesign.mp2.util.ArrayUtil;
import bg.marinov.muppet.exception.TemplateException;

public class TemplateCompiler {
	private static final int PERVIEW_SIZE = 80;

	private final String mExprStart;
	private final Pattern mStartPattern;

	private final String mExprEnd;
	private final Pattern mExprEndPattern;

	private final String mScriptEnd;
	private final Pattern mScriptEndPattern;

	// @formatter:off
	private static List<String> SKIP_PATTERNS = Arrays.asList( 
		"\\/\\*(?:\\r|\\n|.)*?\\*\\/",	// multiline comment
		"\\/\\/.*",						// single line comment
		"'(?:\\\\'|[^'])*'",			// single quoted string
		"\"(?:\\\\\"|[^\"])*\""			// double quoted string
	);
	// @formatter:on

	public static final TemplateCompiler DEFAULT_COMPILER = new TemplateCompiler("<?", "?>", "<?=", "?>");

	private static Pattern getEndPattern(final String aString) {
		final List<String> patterns = new ArrayList<String>(SKIP_PATTERNS);
		patterns.add(Pattern.quote(aString));

		return Pattern.compile(ArrayUtil.arrayJoin("|", patterns.toArray()));
	}

	private TemplateCompiler(final String aScriptTagStart,
			final String aScriptTagEnd,
			final String aExprTagStart,
			final String aExprTagEnd) {

		mExprStart = aExprTagStart;
		mStartPattern = Pattern.compile(Pattern.quote(aExprTagStart) + '|' + Pattern.quote(aScriptTagStart));

		mExprEnd = aExprTagEnd;
		mExprEndPattern = getEndPattern(aExprTagEnd);

		mScriptEnd = aScriptTagEnd;
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

	public String compileToScript(final String aSource) throws TemplateException {
		final StringBuilder buffer = new StringBuilder(aSource.length());

		final Matcher m = mStartPattern.matcher(aSource);
		final Matcher scriptEnd = mScriptEndPattern.matcher(aSource);
		final Matcher exprEnd = mExprEndPattern.matcher(aSource);

		int index = 0;
		while (m.find(index)) {
			final String text = escapeString(aSource.substring(index, m.start()));
			buffer.append("echo('").append(text).append("');\n");

			if (m.group().equals(mExprStart)) {
				if (!findEnd(m.end(), exprEnd, mExprEnd)) {
					final String preview = aSource.substring(
						m.end(),
						Math.min(m.end() + PERVIEW_SIZE, aSource.length()));

					throw new TemplateException("Expression not closed: " + preview);
				}

				index = exprEnd.end();

				final int end = exprEnd.start();
				final String expr = aSource.substring(m.end(), end);
				buffer.append("echo(").append(expr).append(");\n");
			} else {
				final int end;
				if (!findEnd(m.end(), scriptEnd, mScriptEnd)) {
					end = aSource.length();
					index = end;
				} else {
					end = scriptEnd.start();
					index = scriptEnd.end();
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

	private static boolean findEnd(final int aOffset, final Matcher aMatcher, final String aAnchor) {
		int offset = aOffset;

		while (aMatcher.find(offset)) {
			if (aAnchor.equals(aMatcher.group())) {
				return true;
			}

			offset = aMatcher.end();
		}

		return false;
	}

	private static String escapeString(final String aString) {
		return aString.replace("\\", "\\\\").replace("'", "\\'");
	}
}
