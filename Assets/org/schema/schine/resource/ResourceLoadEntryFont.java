package org.schema.schine.resource;

import java.io.IOException;

import org.schema.schine.graphicsengine.forms.font.FontLibrary;

public class ResourceLoadEntryFont extends ResourceLoadEntry{


	public ResourceLoadEntryFont() {
		super("FONTS");
	}

	@Override
	public LoadEntryType getType() {
		return LoadEntryType.FONT;
	}

	@Override
	protected void loadResource(ResourceLoader resourceLoader) throws IOException {
		FontLibrary.initialize();	
	}

	@Override
	public String getFilePath() {
		return "FONT_LOADING_NO_FILE_PATH/";
	}

}
