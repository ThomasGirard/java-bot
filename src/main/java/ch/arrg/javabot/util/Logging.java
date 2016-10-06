package ch.arrg.javabot.util;

// TODO use a proper logging framework (especially so that we can have by-handler logging)

public class Logging {
	private static Exception lastException = null;

	public static void log(String s) {
		System.out.println(s);
	}

	public static void logException(Exception e) {
		lastException = e;
		e.printStackTrace();
	}

	public static Exception getLastException() {
		return lastException;
	}
}
