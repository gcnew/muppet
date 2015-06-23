package bg.marinov.muppet;

public class Main {
	public static void main(String[] args) {
		try {
			final String s = TemplateContainer.eval("<? echo('Hello')");

			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
