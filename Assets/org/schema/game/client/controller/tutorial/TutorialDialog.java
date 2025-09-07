package org.schema.game.client.controller.tutorial;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.tutorial.states.SatisfyingCondition;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.tutorial.tutorialnew.GUITutorialStepPanelNew;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class TutorialDialog extends PlayerInput implements GUIActiveInterface {

	private GUITutorialStepPanelNew panelStat;

	private SatisfyingCondition condition;

	public TutorialDialog(GameClientState state, String string, Sprite image, SatisfyingCondition condition) {
		super(state);
		this.condition = condition;
		panelStat = new GUITutorialStepPanelNew("TUTORIAL", state, string, this, image);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse() && !isDelayedFromMainMenuDeactivation()) {
			if (callingGuiElement.getUserPointer().equals("NEXT")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(295);
				condition.satisfy();
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("BACK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(294);
				try {
					getState().getController().getTutorialMode().back();
				// deactivate();
				} catch (FSMException e) {
					System.err.println("FSMException: " + e.getMessage());
				}
			} else if (callingGuiElement.getUserPointer().equals("SKIP")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(293);
				getState().getController().getTutorialMode().skip();
			// deactivate();
			} else if (callingGuiElement.getUserPointer().equals("END")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(292);
				getState().getController().getTutorialMode().end();
			// deactivate();
			} else {
				assert (false) : "not known command: " + callingGuiElement.getUserPointer();
			}
		}
	}

	/**
	 * @return the condition
	 */
	public SatisfyingCondition getCondition() {
		return condition;
	}

	/**
	 * @param condition the condition to set
	 */
	public void setCondition(SatisfyingCondition condition) {
		this.condition = condition;
	}

	@Override
	public GUIElement getInputPanel() {
		return panelStat;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().indexOf(this) == getState().getController().getPlayerInputs().size() - 1;
	}

	public void pressedNext() {
		synchronized (getState()) {
			boolean needsSynch = !getState().isSynched();
			if (needsSynch) {
				getState().setSynched();
			}
			condition.satisfy();
			try {
				// do two updates to fast forward to the next
				getState().getController().getTutorialMode().getMachine().update();
				getState().getController().getTutorialMode().getMachine().update();
			} catch (FSMException e) {
				e.printStackTrace();
			}
			deactivate();
			if (needsSynch) {
				getState().setUnsynched();
			}
		}
	}

	@Override
	public void onDeactivate() {
	// getState().getGlobalGameControlManager().getIngameControlManager().suspend(false);
	}

	public void pressedBack() {
		synchronized (getState()) {
			boolean needsSynch = !getState().isSynched();
			if (needsSynch) {
				getState().setSynched();
			}
			try {
				getState().getController().getTutorialMode().back();
				try {
					// do two updates to fast forward to the next
					getState().getController().getTutorialMode().getMachine().update();
					getState().getController().getTutorialMode().getMachine().update();
				} catch (FSMException e) {
					e.printStackTrace();
				}
			} catch (FSMException e) {
				System.err.println("FSMException: " + e.getMessage());
			}
			if (needsSynch) {
				getState().setUnsynched();
			}
		}
	}

	public void pressedSkip() {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.CANCEL)*/
		AudioController.fireAudioEventID(296);
		getState().getController().getTutorialMode().skip();
	}

	public void pressedEnd() {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.CANCEL)*/
		AudioController.fireAudioEventID(297);
		if (getState().getController().getTutorialMode().isEndState()) {
			pressedNext();
		} else {
			if (getState().getPlayer().isInTutorial()) {
				getState().getGlobalGameControlManager().openExitTutorialPanel(this);
			} else {
				PlayerGameOkCancelInput p = (new PlayerGameOkCancelInput("CONFIRM", getState(), "TUTORIAL EXIT", "Show tutorial on next start?") {

					@Override
					public void onDeactivate() {
					}

					@Override
					public void pressedOK() {
						deactivate();
					}

					/* (non-Javadoc)
					 * @see org.schema.game.client.controller.PlayerInput#cancel()
					 */
					@Override
					public void cancel() {
						EngineSettings.TUTORIAL_NEW.setOn(false);
						super.cancel();
					}
				});
				p.getInputPanel().setOkButtonText("YES");
				p.getInputPanel().setCancelButtonText("NO");
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(298);
				getState().getController().getTutorialMode().endNow();
				pressedNext();
			}
		}
	}
}
