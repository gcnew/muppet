package bg.marinov.muppet;

import java.util.HashMap;
import java.util.Map;

import re.agiledesign.mp2.util.InteropUtil;

public class TemplateEnvironment {
	private final Map<String, Object> mGlobals;
	private final StringBuilder mResult = new StringBuilder();

	public TemplateEnvironment() {
		mGlobals = new HashMap<String, Object>();

		mGlobals.put("echo", InteropUtil.proxy(this, "echo"));
	}

	public Map<String, Object> getGlobals() {
		return mGlobals;
	}

	public String getResult() {
		return mResult.toString();
	}

	public void echo(final Object aObject) {
		if (aObject != null) {
			mResult.append(aObject);
		}
	}
}
