package org.schema.game.client.view.gui.navigation.navigationnew;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.WeaponAssignControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.PlayerMultipleSectorInput;
import org.schema.game.client.view.gui.PlayerSectorInput;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SavedCoordinate;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class SavedCoordinatesScrollableListNew extends ScrollableTableList<SavedCoordinate> {

	private static final Vector3f dir = new Vector3f();

	private static final Vector3f dir1 = new Vector3f();

	private static final Vector3f dir2 = new Vector3f();

	private final PlayerSectorInput pI;

	public SavedCoordinatesScrollableListNew(InputState state, GUIElement p, PlayerSectorInput pI) {
		super(state, 100, 100, p);
		this.pI = pI;
		setColumnsHeight(UIScale.getUIScale().scale(32));
		((GameClientState) state).getPlayer().savedCoordinatesList = this;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		getAssignWeaponControllerManager().deleteObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 8, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
		addColumn(Lng.str("Coordinate"), 1, (o1, o2) -> {
			Vector3i ownPos = new Vector3i(getState().getPlayer().getCurrentSector());
			double dist1 = Vector3i.getDisatance(ownPos, o1.getSector());
			double dist2 = Vector3i.getDisatance(ownPos, o2.getSector());
			return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
		});
		addColumn(Lng.str("Distance"), 1, (o1, o2) -> {
			Vector3i ownPos = new Vector3i(getState().getPlayer().getCurrentSector());
			double dist1 = Vector3i.getDisatance(ownPos, o1.getSector());
			double dist2 = Vector3i.getDisatance(ownPos, o2.getSector());
			return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
		});
		addFixedWidthColumnScaledUI(Lng.str("Options"), 134, (o1, o2) -> 0);
	}

	@Override
	protected Collection<SavedCoordinate> getElementList() {
		PlayerState player = getState().getPlayer();
		return player.getSavedCoordinates();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<SavedCoordinate> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		FactionManager factionManager = getState().getGameState().getFactionManager();
		CatalogManager catalogManager = getState().getGameState().getCatalogManager();
		PlayerState player = getState().getPlayer();
		int i = 0;
		for(SavedCoordinate f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable coordText = new GUITextOverlayTable(getState());
			GUITextOverlayTable distText = new GUITextOverlayTable(getState());
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			coordText.getPos().y = heightInset;
			distText.getPos().y = heightInset;
			nameText.setTextSimple(f.name);
			coordText.setTextSimple(f.getSector().toStringPure());
			distText.setTextSimple(new Object() {

				@Override
				public String toString() {
					Vector3i ownPos = new Vector3i(getState().getPlayer().getCurrentSector());
					double dist1 = Vector3i.getDisatance(ownPos, f.getSector());
					dist1 *= getState().getSectorSize();
					return StringTools.formatDistance(dist1);
				}
			});
			assert (!nameText.getText().isEmpty());
			assert (!coordText.getText().isEmpty());
			assert (!distText.getText().isEmpty());
			GUIAnchor optionPane = new GUIAnchor(getState(), 120, getDefaultColumnsHeight());
			GUITextButton plotButton = new GUITextButton(getState(), 50, 22, GUITextButton.ColorPalette.OK, pI.getSelectCoordinateButtonText(), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						if(pI instanceof PlayerMultipleSectorInput) {
							if(((PlayerMultipleSectorInput) pI).getCurrentInput().length <= 10) ((PlayerMultipleSectorInput) pI).addCoordinate(f);
							else pI.deactivate();
						} else {
							pI.handleEntered(f.getSector());
							pI.deactivate();
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton deleteButton = new GUITextButton(getState(), 60, 22, GUITextButton.ColorPalette.CANCEL, Lng.str("DELETE"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(566);
						getState().getController().getClientChannel().removeSavedCoordinateToServer(f);
					}
				}
			});
			plotButton.setPos(0, UIScale.getUIScale().smallinset, 0);
			optionPane.attach(plotButton);
			deleteButton.setPos(56, 2, 0);
			optionPane.attach(deleteButton);
			WeaponRow r = new WeaponRow(getState(), f, nameAnchorP, coordText, distText, optionPane);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	public boolean isPlayerAdmin() {
		return getState().getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(getState().getPlayer().getName().toLowerCase(Locale.ENGLISH)) || isPlayerAdmin();
	}

	public WeaponAssignControllerManager getAssignWeaponControllerManager() {
		return getPlayerGameControlManager().getWeaponControlManager();
	}

	public InShipControlManager getInShipControlManager() {
		return getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
	}

	public PlayerGameControlManager getPlayerGameControlManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
	}

	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	private class WeaponRow extends ScrollableTableList.Row {

		public WeaponRow(InputState state, SavedCoordinate f, GUIElement... elements) {
			super(state, f, elements);
			highlightSelect = true;
		}
	}
}
