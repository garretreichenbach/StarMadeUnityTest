package org.schema.game.client.view.gui.ai.newai;

import org.schema.game.client.controller.manager.AiConfigurationManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.ai.GUIAICheckBox;
import org.schema.game.client.view.gui.ai.GUIAISettingSelector;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;

public class AIEntityScrollableList extends GUIAnchor {

	private GUIScrollablePanel scrollPanel;

	private GUITextOverlay deniedText;

	private boolean firstDraw = true;

	private GUIElementList generalList;

	private GUIAnchor dependend;

	public AIEntityScrollableList(GameClientState state, GUIAnchor dependend) {
		super(state);
		this.dependend = dependend;

	}

	public AiConfigurationManager getAiManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if (!(((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().
				getPlayerGameControlManager().getAiConfigurationManager().canEdit())) {
			GlUtil.glPushMatrix();
			transform();
			deniedText.draw();
			GlUtil.glPopMatrix();
		} else {
			super.draw();
		}
	}

	@Override
	public void onInit() {

		deniedText = new GUITextOverlay(FontSize.BIG_24, getState());
		deniedText.setTextSimple("Entity AI can not be edited\n(please use the AI module, or enter a ship)");

		scrollPanel = new GUIScrollablePanel(getWidth(), getHeight(), dependend, getState());
		scrollPanel.getPos().set(0, 0, 0);

		generalList = new GUIElementList(getState());

		generalList.setCallback(getAiManager());

		scrollPanel.setContent(generalList);

		attach(scrollPanel);

		firstDraw = false;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public void reconstructList(AIGameConfiguration<?, ?> ai) {
		generalList.clear();

		int i = 0;
		if (ai != null) {
			for (AIConfiguationElements s : ai.getElements().values()) {
				if (s.getCurrentState() instanceof Boolean) {
					GUIAICheckBox guiCheckBox = new GUIAICheckBox(getState(), s);
					generalList.add(new GUISettingsListElement(getState(), s.getDescription(), guiCheckBox, i % 2 == 0, false));
				} else {
					GUIAISettingSelector guiaiSettingSelector = new GUIAISettingSelector(getState(), s);
					generalList.add(new GUISettingsListElement(getState(), s.getDescription(), guiaiSettingSelector, i % 2 == 0, false));
				}
			}
		}
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);

		if (getAiManager().isNeedsUpdate()) {
			if (getAiManager().getAi() != null) {
				reconstructList((AIGameConfiguration<?, ?>) getAiManager().getAi().getAiConfiguration());
			} else {
				reconstructList(null);
			}
			getAiManager().setNeedsUpdate(false);

		}
	}
}
