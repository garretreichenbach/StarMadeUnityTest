package org.schema.schine.input;

import java.util.List;

import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.movie.subtitles.Subtitle;

public interface InputState {

	public InputController getController();
	
	public short getNumberOfUpdate();

	public List<Object> getGeneralChatLog();

	public List<Object> getVisibleChatLog();

	public void onSwitchedSetting(EngineSettings engineSettings);

	public String onAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException;

	public String getGUIPath();

	public GraphicsContext getGraphicsContext();
	
	public void setActiveSubtitles(List<Subtitle> activeSubtitles);

	public void setInTextBox(boolean b);

	public boolean isInTextBox();
	
	public long getUpdateTime();
}
