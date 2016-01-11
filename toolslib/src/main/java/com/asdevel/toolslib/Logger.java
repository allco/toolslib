package com.asdevel.toolslib;

import android.util.Log;

public class Logger {

	public static void log(Throwable e) {

		log(e, true);
	}

	public static void log(Throwable e, boolean verbose) {

		if (e == null) {log("e == null"); return;}

		if (!Tools.DEBUG) {
			log(e);
			return;
		}

		StackTraceElement[] stackTraceElements = e.getStackTrace();

		log("----------- Exception! -----------");
		log("message: " + e.getMessage());
		log("class: " + e.getClass().getCanonicalName());
		log("file: " + stackTraceElements == null || stackTraceElements.length < 1 ? "<null>" : stackTraceElements[0].toString());

		/*if (!(
				e instanceof SocketException ||
				e instanceof SocketTimeoutException ||
				e instanceof org.apache.http.conn.ConnectTimeoutException
		))*/
		if (verbose) {
			log("---------- Stack trace: ----------");
			StringBuilder sb = new StringBuilder().append('\n');
			for (StackTraceElement s : stackTraceElements) {
				sb.append(s.toString()).append('\n');
			}

			log(sb.toString());
		}
		log("----------------------------------");
	}

	public static void log(String s) {

		log(null, s == null ? "<null>" : s);
	}

	public static void logTh(String s) {

		log(null, s == null ? "<null>" : s + " th:" + Thread.currentThread());
	}

	public static <T extends Object> void log(T s) {

		log(null, s == null ? "<null>" : s.getClass().getSimpleName() + " " + s.toString());
	}

	public static void log(String tag, String message) {

		if (!Tools.DEBUG) { return; }

		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement se = null;

		for (int i = 4; i < stackTraceElements.length; i++) {
			StackTraceElement s = stackTraceElements[i];
			String methodName = s.getMethodName();
			if (methodName.startsWith("access$") || methodName.equals("log") || methodName.equals("logth")) {
				continue;
			}
			se = s;
			break;
		}

		if (se == null) {
			se = stackTraceElements[4];
		}

		if (tag == null) {
			//tag = se.getClassName();
			//tag = tag.substring(tag.lastIndexOf('.') + 1);
			tag = se.getFileName() + ":" + se.getLineNumber();
		}

		Log.e(tag,
			  //se.getFileName() + ":" + se.getLineNumber() + " " +
			  se.getMethodName() + "() --> " + message + " <-- ");
	}
}
