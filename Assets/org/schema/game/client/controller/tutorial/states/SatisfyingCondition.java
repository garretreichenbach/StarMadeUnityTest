package org.schema.game.client.controller.tutorial.states;

import java.util.ArrayList;
import java.util.List;

import org.schema.game.client.controller.tutorial.TutorialDialog;
import org.schema.game.client.controller.tutorial.states.conditions.TutorialCondition;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIPopupInterface;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.forms.Sprite;

public abstract class SatisfyingCondition extends State {

	/**
	 *
	 */
	
	private final GameClientState gameState;
	private final List<TutorialCondition> tutorialConditions;
	protected boolean next;
	protected boolean skipIfSatisfiedAtEnter;
	protected boolean endIfSatisfied;
	private String message;
	private boolean forcedSatisfied;
	private Sprite image;
	private GUIPopupInterface popupTipTextMessage;
	private TutorialDialog tutorialDialog;
	private List<TutorialMarker> markers;
	private boolean skipWindowMessage;

	public SatisfyingCondition(AiEntityStateInterface gObj, String message, GameClientState state) {
		super(gObj);
		this.gameState = state;
		this.tutorialConditions = new ArrayList<TutorialCondition>();
		this.setMessage(message);
	}

	private TutorialCondition checkStateKeepingConsditions() throws FSMException {

		for (TutorialCondition c : tutorialConditions) {
			if (!c.checkAndStateTransitionIfMissed(gameState)) {
				return c;
			}
		}
		return null;
	}

	private boolean checkBasicCondition() {
		if (gameState.getController().getTutorialMode().isBackgroundMode()) {
			return true;
		} else {
			return next;
		}
	}

	protected abstract boolean checkSatisfyingCondition() throws FSMException;

	public void forceSatisfied() {
		this.forcedSatisfied = true;
	}

	/**
	 * @return the gameState
	 */
	public GameClientState getGameState() {
		return gameState;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message.trim();
	}

	public void satisfy() {
		next = true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.State#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + ": " + getMessage();
	}

	@Override
	public boolean onEnter() {
		next = false;
		popupTipTextMessage = null;
		forcedSatisfied = false;

		if (markers != null) {
			gameState.getController().getTutorialMode().markers = markers;
		}

		try {
			if (skipIfSatisfiedAtEnter && checkSatisfyingCondition()) {
				System.err.println("AUTO SKIP STATE " + this);
				forcedSatisfied = true;
			}
		} catch (FSMException e) {
			e.printStackTrace();
		}
		if (!forcedSatisfied && !skipWindowMessage) {
			if (!gameState.getController().getTutorialMode().isBackgroundMode()) {
				if (!(this instanceof TimedState)) {
					//dialog is not blocking
					tutorialDialog = new TutorialDialog(gameState, getMessage() + "\n(click NEXT to take control)", image, this);
				} else {
					tutorialDialog = new TutorialDialog(gameState, getMessage(), image, this);
				}
				gameState.getPlayerInputs().add(tutorialDialog);
			}
		}
		return true;
	}

	@Override
	public boolean onExit() {
		if (markers != null) {
			gameState.getController().getTutorialMode().markers = null;
		}
		gameState.getController().getTutorialMode().highlightType = 0;
		if (tutorialDialog != null) {
			tutorialDialog.deactivate();
		}

		return true;
	}

	@Override
	public boolean onUpdate() throws FSMException {

		TutorialCondition failingCondition;
		if ((failingCondition = checkStateKeepingConsditions()) != null) {
			gameState.getController().popupTipTextMessage(failingCondition.getNotSactifiedText(), 0);
			return false;
		}

		if ((checkBasicCondition() && checkSatisfyingCondition()) || forcedSatisfied) {
			forcedSatisfied = false;
			if (popupTipTextMessage != null) {
				popupTipTextMessage.timeOut();
			}
			if (endIfSatisfied) {
				getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_STOP);
			} else {
				getEntityState().getMachine().getFsm().stateTransition(Transition.CONDITION_SATISFIED);
			}
			System.err.println("CONDITION SATISFIED: NEW STATE: " + this + " -> " + getEntityState().getMachine().getFsm().getCurrentState());
			return false;
		} else {
			if (gameState.getController().getTutorialMode().isBackgroundMode()) {
				if (popupTipTextMessage == null) {
					popupTipTextMessage = gameState.getController().popupFlashingTextMessage(getMessage(), 0);
				} else {

					if (!(this instanceof TimedState)) {
						popupTipTextMessage.setMessage(getMessage() + "\n(press 'u' to skip)");
					} else {
						popupTipTextMessage.setMessage(getMessage());
					}
					popupTipTextMessage.restartPopupMessage();
				}
			}
		}
		if ((next || skipWindowMessage) && !gameState.getController().getTutorialMode().isBackgroundMode()) {
			//there is another satifying condition to be met
			if (popupTipTextMessage == null) {
				popupTipTextMessage = gameState.getController().popupFlashingTextMessage(getMessage(), 0);
			} else {
				if (!(this instanceof TimedState)) {
					popupTipTextMessage.setMessage(getMessage() + "\n(press 'u' to skip)");
				} else {
					popupTipTextMessage.setMessage(getMessage());
				}
				popupTipTextMessage.restartPopupMessage();
			}
		}
		//		System.err.println("CONDITION: "+this+":  next "+next+"; cond "+checkSatisfyingCondition());
		return true;
	}

	/**
	 * @return the image
	 */
	public Sprite getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(Sprite image) {
		this.image = image;
	}

	/**
	 * @return the tutorialConditions
	 */
	public List<TutorialCondition> getTutorialConditions() {
		return tutorialConditions;
	}

	/**
	 * @return the markers
	 */
	public List<TutorialMarker> getMarkers() {
		return markers;
	}

	/**
	 * @param markers the markers to set
	 */
	public void setMarkers(List<TutorialMarker> markers) {
		this.markers = markers;
	}

	/**
	 * @return the skipWindowMessage
	 */
	public boolean isSkipWindowMessage() {
		return skipWindowMessage;
	}

	/**
	 * @param skipWindowMessage the skipWindowMessage to set
	 */
	public void setSkipWindowMessage(boolean skipWindowMessage) {
		this.skipWindowMessage = skipWindowMessage;
	}

}
