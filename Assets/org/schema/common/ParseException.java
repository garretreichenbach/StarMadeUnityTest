package org.schema.common;

import java.io.IOException;

public class ParseException extends IOException {

	
	private static final long serialVersionUID = 1L;
	public ParseException(String i) {
		super(i);
	}

	public ParseException(Exception e) {
		super(e);
	}
}
