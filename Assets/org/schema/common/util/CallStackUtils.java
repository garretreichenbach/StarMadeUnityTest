package org.schema.common.util;

import java.util.ArrayList;

public class CallStackUtils {

	public synchronized static String getCallStackHeadAsString() {
		StackTraceElement[] stackTraceElements = Thread.currentThread()
				.getStackTrace();

		if (stackTraceElements.length > 2) {
			return getStackTraceElementAtIndexAsString(stackTraceElements, 2);
		}
		return "";
	}

	public synchronized static String getCallStackAsString() {
		StringBuilder sb = new StringBuilder();

		StackTraceElement[] stackTraceElements = Thread.currentThread()
				.getStackTrace();

		String[] array = getCallStackAsStringArray(stackTraceElements);

		for (int i = 0; i < array.length; i++) {
			sb.append(array[i] + "\n");
		}
		return sb.toString();
	}

	public synchronized static String[] getCallStackAsStringArray() {
		StackTraceElement[] stackTraceElements = Thread.currentThread()
				.getStackTrace();

		String[] array = getCallStackAsStringArray(stackTraceElements);

		return array;
	}

	private synchronized static String[] getCallStackAsStringArray(
			StackTraceElement[] stackTraceElements) {
		ArrayList<String> list = new ArrayList<String>();
		String[] array = new String[1];

		for (int i = 0; i < stackTraceElements.length; i++) {
			list.add(getStackTraceElementAtIndexAsString(stackTraceElements, i));
		}
		return list.toArray(array);
	}

	private static String getStackTraceElementAtIndexAsString(StackTraceElement[] stackTraceElements, int i) {
		StackTraceElement element = stackTraceElements[i];
		String classname = element.getClassName();
		String methodName = element.getMethodName();
		int lineNumber = element.getLineNumber();
		return classname + "." + methodName + ":" + lineNumber;
	}

}
