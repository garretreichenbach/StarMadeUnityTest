package org.schema.game.common.facedit;

public class MindMapParsingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MindMapParsingException(String string) {
		super(string);
	}

	public MindMapParsingException(String string, Exception e) {
		super(string, e);
	}

}
