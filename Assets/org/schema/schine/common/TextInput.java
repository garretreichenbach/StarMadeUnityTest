package org.schema.schine.common;

public class TextInput extends TextAreaInput {

	public TextInput(int limit, TextCallback callback) {
		super(limit, 1, callback);
	}

	public TextInput(int limit, int lineLimit, TextCallback callback) {
		super(limit, lineLimit, callback);
	}
}