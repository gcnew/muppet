package bg.marinov.muppet.test;

import junit.framework.TestCase;
import re.agiledesign.mp2.util.Util;
import bg.marinov.muppet.TemplateCompiler;
import bg.marinov.muppet.TemplateContainer;

public class TemplateContainerTest extends TestCase {
	private void assertEval(final String aSource, final String aExpected) {
		assertEval(TemplateCompiler.getInstance(), aSource, aExpected);
	}

	private void assertEval(final TemplateCompiler aCompiler, final String aSource, final String aExpected) {
		try {
			final String result = TemplateContainer.eval(aCompiler, aSource);

			assertEquals(aExpected, result);
		} catch (Exception e) {
			Util.rethrowUnchecked(e);
		}
	}

	private void assertException(final String aSource, final Class<? extends Throwable> aExpected) {
		try {
			TemplateContainer.eval(aSource);
			fail();
		} catch (Exception e) {
			assertTrue(aExpected.isAssignableFrom(e.getClass()));
		}
	}

	public void testExpr() {
		assertEval("Hello <?= 'New' ?> World", "Hello New World");
		assertEval("Hello <?= 40 + 2 ?> balloons", "Hello 42 balloons");
	}

	public void testUnclosedExpr() {
		assertException("Hello <?= // Not closed", Exception.class);
	}

	public void testScript() {
		assertEval("Hello <? echo('\\'New World\\'') ?> 2", "Hello 'New World' 2");
		assertEval("Hello <? local c = 40 + 2; echo(c); ?> balloons", "Hello 42 balloons");
	}

	public void testUnclosedScript() {
		assertEval("And... <? echo('I work');  ", "And... I work");
	}

	public void testClosingTagInsideString() {
		assertEval("Catch me <? echo('if you can ?>') ?>!", "Catch me if you can ?>!");
		assertEval("Catch me <? echo('if you can \r\n?>') \r\n?>!", "Catch me if you can \r\n?>!");
		assertEval("Catch me <?= 'if ' + 'you ' + 'can ' + '?>' ?>!", "Catch me if you can ?>!");
		assertEval("Catch me <?= 'if you can \r\n?>' \r\n?>!", "Catch me if you can \r\n?>!");
	}

	public void testCustomTags() {
		final TemplateCompiler cc = TemplateCompiler.getInstance("{%", "%}", "{=", "=}");
		assertEval(cc, "Custom {% echo(\"Tag\") %}", "Custom Tag");
		assertEval(cc, "Custom {= 'Tag' =}", "Custom Tag");
	}
}
