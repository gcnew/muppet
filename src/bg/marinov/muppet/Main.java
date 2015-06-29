package bg.marinov.muppet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
	public static void main(final String[] args) {
		final boolean showHelp = Arrays.asList(args).contains("--help");

		if ((args.length != 1) || showHelp) {
			System.err.println("Syntax: muppet <file>");
			System.exit(-1);
		}

		final String fileName = args[0];
		final File file = new File(fileName);

		if (!file.exists()) {
			System.err.println("File not found: " + fileName);
			System.exit(-1);
		}

		InputStream fs = null;
		try {
			fs = new FileInputStream(file);

			final String contents = readStream(fs);
			final String result = TemplateContainer.eval(contents);

			System.out.print(result);
		} catch (Exception e) {
			System.err.println("Building template failed!");
			e.printStackTrace(System.err);

			System.exit(-1);
		} finally {
			closeQuietly(fs);
		}
	}

	private static String readStream(final InputStream aStream) {
		final Scanner s = new Scanner(aStream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private static void closeQuietly(final InputStream aStream) {
		try {
			if (aStream != null) {
				aStream.close();
			}
		} catch (final IOException ignored) {
		}
	}
}
