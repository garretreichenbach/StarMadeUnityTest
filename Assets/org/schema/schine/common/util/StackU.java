package org.schema.schine.common.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class StackU {
	public static void printStackTrace() {
		System.err.println(ExceptionUtils.getStackTrace(new Exception()));
	}
}
