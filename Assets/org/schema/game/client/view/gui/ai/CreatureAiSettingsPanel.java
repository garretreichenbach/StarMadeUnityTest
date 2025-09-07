package org.schema.game.client.view.gui.ai;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUISettingSelector;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.IndexChoiceSetting;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class CreatureAiSettingsPanel extends GUIInputPanel {

	private final AIGameCreatureConfiguration<?, ?> config;
	int attackSettingIndex = 0;
	private String[] attackSetting = new String[]{"proximity or attacked", "attacked", "never"};
	public CreatureAiSettingsPanel(InputState state, AiInterface ai, GUICallback guiCallback,
	                               Object info, Object description) {
		super("CREATURE_AI_SETTINGS_PANEL", state, guiCallback, info, description);
		config = ((AIGameCreatureConfiguration<?, ?>) ai.getAiConfiguration());
		setOkButton(false);

	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onInit() {
		super.onInit();

		int aggressivenessBitmask = ((AIConfiguationElements<Integer>) config.get(Types.AGGRESIVENESS)).getCurrentState().intValue();

		if (config.isAttackOnAttacked() && config.isAttackOnProximity()) {
			attackSettingIndex = 0;
		} else if (config.isAttackOnAttacked()) {
			attackSettingIndex = 1;
		} else {
			attackSettingIndex = 2;
		}
		attackSettingIndex %= attackSetting.length;

		//auto attack structures

		//stop attack after time

		//attack on proximity and when attacked
		//attack when attack
		//never attack

		GUISettingSelector attackStyleSelector = new GUISettingSelector(getState(), null, new IndexChoiceSetting(attackSetting.length) {

			@Override
			public String getNameAt(int i) {
				return attackSetting[i];
			}

			@Override
			public void onSelectedValue(int v) {
				attackSettingIndex = v;
				switch(attackSettingIndex) {
					case 0 -> config.setAttack(true, AIGameCreatureConfiguration.AGGRO_ATTACKED | AIGameCreatureConfiguration.AGGRO_PROXIMITY, true);
					case 1 -> {
						config.setAttackOnAttacked(true, false);
						config.setAttackOnProximity(false, true);
					}
					case 2 -> {
						config.setAttackOnAttacked(false, false);
						config.setAttackOnProximity(false, true);
					}
				}
			}
			@Override
			public int getCurrentValue() {
				return attackSettingIndex;
			}

		});
				


		getContent().attach(attackStyleSelector);

		GUICheckBox cbAttStruct = new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				config.setAttackStructures(true, true);
			}			@Override
			protected boolean isActivated() {
				return config.isAttackStructures();
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				config.setAttackStructures(false, true);
			}


		};

		getContent().attach(cbAttStruct);

		GUICheckBox cbAttStop = new GUICheckBox(getState()) {

			@Override
			protected boolean isActivated() {
				return config.isStopAttacking();
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				config.setStopAttacking(false, true);
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				config.setStopAttacking(true, true);
			}
		};

		getContent().attach(cbAttStop);

		GUITextOverlay tcbAttStyle = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		tcbAttStyle.setTextSimple("Auto-Aggro");
		GUITextOverlay tcbAttStruct = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		tcbAttStruct.setTextSimple("Auto-Attack Structures");
		GUITextOverlay tcbAttStop = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		tcbAttStop.setTextSimple("Auto-Stop Attacking");

		tcbAttStyle.setPos(UIScale.getUIScale().scale(10), UIScale.getUIScale().scale(2), 0);
		attackStyleSelector.setPos(UIScale.getUIScale().scale(200), UIScale.getUIScale().scale(0), 0);
		tcbAttStruct.setPos(UIScale.getUIScale().scale(10), UIScale.getUIScale().scale(35), 0);
		cbAttStruct.setPos(UIScale.getUIScale().scale(250), UIScale.getUIScale().scale(33), 0);

		tcbAttStop.setPos(UIScale.getUIScale().scale(10), UIScale.getUIScale().scale(68), 0);
		cbAttStop.setPos(UIScale.getUIScale().scale(250), UIScale.getUIScale().scale(66), 0);

		getContent().attach(tcbAttStyle);
		getContent().attach(tcbAttStruct);
		getContent().attach(tcbAttStop);
	}

}
