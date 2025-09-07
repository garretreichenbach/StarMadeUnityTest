package org.schema.schine.common;

import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;

public interface TextCallback {

	public String[] getCommandPrefixes();

	public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException;

	public void onFailedTextCheck(String msg);

	public void onTextEnter(String entry, boolean send, boolean onAutoComplete);

	public void newLine();

}
