package bg.marinov.muppet;

import re.agiledesign.mp2.InterpreterFactory;

public class TemplateContainer {
	public static String eval(final String aSource) throws Exception {
		return eval(TemplateCompiler.getInstance(), aSource);
	}

	public static String eval(final TemplateCompiler aCompiler, final String aSource) throws Exception {
		final String script = aCompiler.compileToScript(aSource);

		final TemplateEnvironment env = new TemplateEnvironment();
		InterpreterFactory.createInterpreter(script, env.getGlobals()).eval();

		return env.getResult();
	}
}
