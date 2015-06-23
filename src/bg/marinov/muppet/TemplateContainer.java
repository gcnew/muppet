package bg.marinov.muppet;

import re.agiledesign.mp2.InterpreterFactory;
import re.agiledesign.mp2.exception.ParsingException;
import bg.marinov.muppet.exception.TemplateException;

public class TemplateContainer {
	/**
	 * For documentation please check {@link #eval(TemplateCompiler, String)}
	 * 
	 * @see #eval(TemplateCompiler, String)
	 */
	public static String eval(final String aSource) throws Exception {
		return eval(TemplateCompiler.getInstance(), aSource);
	}

	/**
	 * @throws TemplateException
	 *             if compiling the source to script fails
	 * @throws ParsingException
	 *             if parsing/evaluating the script fails
	 * @throws Exception
	 *             if evaluating the script fails
	 */
	public static String eval(final TemplateCompiler aCompiler, final String aSource) throws Exception {
		final String script = aCompiler.compileToScript(aSource);

		final TemplateEnvironment env = new TemplateEnvironment();
		InterpreterFactory.createInterpreter(script, env.getGlobals()).eval();

		return env.getResult();
	}
}
