package com.arrownock.internal.live;

import java.io.PrintWriter;
import java.io.StringWriter;

public class UnhandledExceptionHandler implements Thread.UncaughtExceptionHandler {
	public UnhandledExceptionHandler() {
		super();
	}

	public void uncaughtException(Thread unusedThread, final Throwable e) {
		String title = "Fatal error: " + getTopLevelCauseMessage(e);
		String msg = getRecursiveStackTrace(e);
	}

	// Returns the Message attached to the original Cause of |t|.
	private static String getTopLevelCauseMessage(Throwable t) {
		Throwable topLevelCause = t;
		while (topLevelCause.getCause() != null) {
			topLevelCause = topLevelCause.getCause();
		}
		return topLevelCause.getMessage();
	}

	// Returns a human-readable String of the stacktrace in |t|, recursively
	// through all Causes that led to |t|.
	private static String getRecursiveStackTrace(Throwable t) {
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
}
