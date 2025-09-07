package org.schema.game.client.controller.manager;

import api.listener.events.gui.ControlManagerActivateEvent;
import api.mod.StarLoader;
import api.utils.gui.GUIControlManager;
import api.utils.gui.ModGUIHandler;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.PlayerGameDropDownInput;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.WorldDrawer;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.InputHandler;
import org.schema.schine.common.JoystickInputHandler;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.input.JoystickEvent;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public abstract class AbstractControlManager extends GUIObservable implements InputHandler, Suspendable {

	public static final int CONTROLLER_PLAYER_EXTERN = 0;

	public static final int CONTROLLER_SHIP_EXTERN = 1;

	public static final int CONTROLLER_SHIP_BUILD = 2;

	private final GameClientState state;

	private boolean suspended = false;

	private boolean active;

	private HashSet<AbstractControlManager> controlManagers;

	private int delayedActive = -1;

	private boolean treeActive;

	private int hinderInteraction;

	private long hinderInteractionStart;

	public AbstractControlManager(GameClientState state) {
		super();
		this.state = state;
		controlManagers = new HashSet<>();
	}

	public boolean isOccluded() {
		return false;
	}

	public boolean isAllowedToBuildAndSpawnShips() {
		Faction faction = getState().getFactionManager().getFaction(getState().getPlayer().getFactionId());
		return faction == null || !faction.isFactionMode(Faction.MODE_SPECTATORS);
	}

	/**
	 * Activates a sub controller while deactivating the others
	 * on the same hierarchy level
	 *
	 * @param f the controller that should be activated single
	 * @
	 */
	public void activate(AbstractControlManager f) {
		for (AbstractControlManager af : controlManagers) {
			if (af != f) {
				af.setActive(false);
			}
		}
		if (!f.active) {
			f.setActive(true);
		}
	}

	public void openExitTutorialPanel(final PlayerInput p) {
		PlayerGameOkCancelInput diag = new PlayerGameOkCancelInput("AbstractControlManager_TUTORIALS_END", getState(), "Confirm", "Do you really want to end the tutorial\nYou will be teleported back into the game \nand get your inventory back!") {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public void pressedOK() {
				if (getState().getPlayer().getFirstControlledTransformableWOExc() == null || !(getState().getPlayer().getFirstControlledTransformableWOExc() instanceof PlayerCharacter)) {
					getState().getController().popupAlertTextMessage(Lng.str("You can't bring that ship!\nPlease get out of the ship\nfirst!"), 0);
				} else {
					getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.END_TUTORIAL);
					Vector3f[] defaultSpawingPoints = new Vector3f[] {
							new Vector3f(ServerConfig.DEFAULT_SPAWN_POINT_X_1.getFloat(),
									ServerConfig.DEFAULT_SPAWN_POINT_Y_1.getFloat(), ServerConfig.DEFAULT_SPAWN_POINT_Z_1.getFloat()), new Vector3f(ServerConfig.DEFAULT_SPAWN_POINT_X_2.getFloat(), ServerConfig.DEFAULT_SPAWN_POINT_Y_2.getFloat(), ServerConfig.DEFAULT_SPAWN_POINT_Z_2.getFloat()), new Vector3f(ServerConfig.DEFAULT_SPAWN_POINT_X_3.getFloat(), ServerConfig.DEFAULT_SPAWN_POINT_Y_3.getFloat(), ServerConfig.DEFAULT_SPAWN_POINT_Z_3.getFloat()), new Vector3f(ServerConfig.DEFAULT_SPAWN_POINT_X_4.getFloat(), ServerConfig.DEFAULT_SPAWN_POINT_Y_4.getFloat(), ServerConfig.DEFAULT_SPAWN_POINT_Z_4.getFloat()) };
					Transform t = new Transform();
					t.setIdentity();
					Random r = new Random();
					t.origin.set(defaultSpawingPoints[r.nextInt(defaultSpawingPoints.length)]);
					getState().getCharacter().getGhostObject().setWorldTransform(t);
					deactivate();
					if (p != null) {
						p.deactivate();
					}
				}
			}
		};
		diag.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(91);
	}

	public void openExitTestSectorPanel(final PlayerInput p) {
		PlayerGameOkCancelInput diag = new PlayerGameOkCancelInput("AbstractControlManager_TUTORIALS_END", getState(), "Confirm", "Do you really want to exit the test sector?\nYou will be teleported back into the game \nand get your original inventory back!") {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public void pressedOK() {
				if (getState().getPlayer().getFirstControlledTransformableWOExc() == null || !(getState().getPlayer().getFirstControlledTransformableWOExc() instanceof PlayerCharacter)) {
					getState().getController().popupAlertTextMessage(Lng.str("You can't bring that ship!\nPlease get out of the ship\nfirst!"), 0);
				} else {
					getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.END_SHIPYARD_TEST);
					deactivate();
					if (p != null) {
						p.deactivate();
					}
				}
			}
		};
		diag.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(92);
	}

	public void openTutorialPanel() {
		ObjectArrayList<GUIElement> l = new ObjectArrayList<GUIElement>();
		ObjectArrayList<String> sorted = new ObjectArrayList<String>(getState().getController().getTutorialMode().getMachineNames());
		for (int i = 0; i < sorted.size(); i++) {
			if (sorted.get(i).startsWith("TutorialBasics")) {
				sorted.remove(i);
				i--;
			}
		}
		Collections.sort(sorted);
		for (String s : sorted) {
			if (!s.equals(TutorialMode.BASIC_TUTORIAL)) {
				GUIAnchor a = new GUIAnchor(getState(), 300, 24);
				GUITextOverlay t = new GUITextOverlay(getState());
				t.setTextSimple(s);
				t.setPos(5, 4, 0);
				a.attach(t);
				l.add(a);
				a.setUserPointer(s);
			}
		}
		PlayerGameDropDownInput p = new PlayerGameDropDownInput("AbstractControlManager_TUTORIALS", getState(), "Tutorials", 24, "Choose a tutorial", l) {

			@Override
			public void onDeactivate() {
			}

			@Override
			public void pressedOK(GUIListElement current) {
				getState().getController().getTutorialMode().setCurrentMachine(current.getContent().getUserPointer().toString());
				getState().getController().getTutorialMode().getMachine().reset();
				deactivate();
			}
		};
		p.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(93);
	}

	public void activateAll(boolean activate) {
		for (AbstractControlManager a : controlManagers) {
			a.setActive(activate);
		}
		//INSERTED CODE
		if(!activate) ModGUIHandler.deactivateAll();
		//
	}

	public void activateDelayed() {
		if (delayedActive >= 0) {
			setActive(delayedActive == 1);
		}
		delayedActive = -1;
		for (AbstractControlManager a : controlManagers) {
			a.activateDelayed();
		}
	}

	/**
	 * @return the controlManagers
	 */
	public HashSet<AbstractControlManager> getControlManagers() {
		return controlManagers;
	}

	/**
	 * @param controlManagers the controlManagers to set
	 */
	public void setControlManagers(HashSet<AbstractControlManager> controlManagers) {
		this.controlManagers = controlManagers;
	}

	/**
	 * @return the state
	 */
	public GameClientState getState() {
		return state;
	}

	public boolean handleJoystickInputPanels(JoystickEvent e) {
		synchronized (getState().getController().getPlayerInputs()) {
			int size = getState().getController().getPlayerInputs().size();
			if (size > 0) {
				// only the last in list is active
				DialogInterface playerInput = getState().getController().getPlayerInputs().get(size - 1);
				if (playerInput instanceof JoystickInputHandler) {
					((JoystickInputHandler) playerInput).handleJoystickEvent(e);
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {

		if (!suspended && !isHinderedInteraction()) {
			for (AbstractControlManager a : controlManagers) {
				if (!a.suspended && a.active) {
					a.handleKeyEvent(e);
				}
			}
		}
	}

	// @Override
	// public void handleMouseEvent(MouseEvent e) {
	// if (!isSuspended() && !isHinderedInteraction()) {
	// for (AbstractControlManager a : getControlManagers()) {
	// if (!a.isSuspended() && a.isActive()) {
	// a.handleMouseEvent(e);
	// }
	// }
	// }
	// }
	public void hinderInteraction(int ms) {
		this.hinderInteraction = ms;
		this.hinderInteractionStart = System.currentTimeMillis();
		state.setHinderedInput(ms);
	}

	/**
	 * @return the active
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isHinderedInteraction() {
		return (System.currentTimeMillis() - hinderInteractionStart) < hinderInteraction;
	}

	/**
	 * @return the ignoreUpdate
	 */
	@Override
	public boolean isSuspended() {
		return suspended;
	}

	/**
	 * @return the treeActive
	 */
	@Override
	public boolean isTreeActive() {
		return treeActive;
	}

	@Override
	public void suspend(boolean suspend) {
		if (suspend != this.suspended) {
			this.onSuspend(suspend);
			this.suspended = suspend;
		}
	}

	/**
	 * @param active the active to set
	 * @
	 */
	public void setActive(boolean active) {
		boolean activeBefore = this.active;
		boolean changedActive = active != this.active;
		this.active = active;
		//INSERTED CODE @378
		ControlManagerActivateEvent controlManagerActivateEvent = new ControlManagerActivateEvent(this, active);
		StarLoader.fireEvent(ControlManagerActivateEvent.class, controlManagerActivateEvent, false);
		//Todo: Fix merge conflict
		if(controlManagerActivateEvent.isCanceled()){
			this.active = !this.active;
		}
		///
		if (changedActive) {
			// System.err.println("Active of "+this.getClass().getSimpleName()+" on "+getState()+" changed from "+activeBefore+" to "+active);
			onSwitch(active);
			if (!state.isPassive()) {
				if(state.getWorldDrawer() == null) state.setWorldDrawer(new WorldDrawer(state));
				getState().getWorldDrawer().getGuiDrawer().notifySwitch(this);
			}
			notifyObservers();
		}
	}

	/**
	 * @return the delayedActive
	 */
	public boolean isDelayedActive() {
		return delayedActive >= 0;
	}

	/**
	 * @param delayedActive the delayedActive to set
	 */
	public void setDelayedActive(int delayedActive) {
		this.delayedActive = delayedActive;
	}

	protected void onSuspend(boolean suspend) {
		getState().getPlayer().getControllerState().setSuspended(suspend);
	}

	public void onSwitch(boolean active) {
		this.treeActive = active;
		for (AbstractControlManager c : controlManagers) {
			c.onSwitch(active && c.active);
		}
		//INSERTED CODE
		for(GUIControlManager controlManager : ModGUIHandler.getAllModControlManagers()) {
			controlManager.onSwitch(active && controlManager.isActive());
		}
		//
	}

	public void printActive(int level) {
		if (active) {
			String s = "";
			for (int i = 0; i < level; i++) {
				s += "->";
			}
			s += getClass().getSimpleName();
			AbstractScene.infoList.add("|-- " + s);
			for (AbstractControlManager a : controlManagers) {
				a.printActive(level + 1);
			}
		}
	}

	public boolean isTreeActiveAndNotSuspended() {
		return treeActive && !suspended;
	}

	public void setDelayedActive(boolean active) {
		this.delayedActive = active ? 1 : 0;
	// System.err.println("DELAYEDActive of "+this.getClass().getSimpleName()+" on "+getState()+" changed from "+isActive()+" to "+active);
	}

	public void update(Timer timer) {
		if (!suspended) {
			for (AbstractControlManager a : controlManagers) {
				if (!a.suspended && a.active) {
					a.update(timer);
				}
			}
			//INSERTED CODE
			for(GUIControlManager controlManager : ModGUIHandler.getAllModControlManagers()) {
				if(!controlManager.isSuspended() && controlManager.isActive()) controlManager.update(timer);
			}
			//
		}
	}
}
