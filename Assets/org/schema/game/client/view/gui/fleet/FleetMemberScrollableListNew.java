package org.schema.game.client.view.gui.fleet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.FleetStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class FleetMemberScrollableListNew extends ScrollableTableList<FleetMember> {

	private FleetManager fsi;

	public FleetMemberScrollableListNew(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		this.fsi = ((GameClientState) getState()).getFleetManager();
		((GameClientState) getState()).getFleetManager().obs.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		((GameClientState) getState()).getFleetManager().obs.deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 2.7f, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()), true);
		addColumn(Lng.str("Ship %"), 1.3f, (o1, o2) -> o1.getShipPercent() == o2.getShipPercent() ? 0 : o1.getShipPercent() > o2.getShipPercent() ? 1 : -1);
		addColumn(Lng.str("Engage Range"), 1.2f, (o1, o2) -> o1.getEngagementRange() == o2.getEngagementRange() ? 0 : o1.getEngagementRange() > o2.getEngagementRange() ? 1 : -1);
		addColumn(Lng.str("Pickup Area"), 1.3f, (o1, o2) -> o1.command.compareToIgnoreCase(o2.command));
		addColumn(Lng.str("Sector"), 1.3f, (o1, o2) -> o1.getSector().compareTo(o2.getSector()));
		addFixedWidthColumnScaledUI(Lng.str("Rank"), 35, (o1, o2) -> fsi.getSelected().getMembers().indexOf(o1) - fsi.getSelected().getMembers().indexOf(o2), true);
		addTextFilter(new GUIListFilterText<FleetMember>() {

			@Override
			public boolean isOk(String input, FleetMember listElement) {
				return listElement.getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, FilterRowStyle.FULL);
	}

	@Override
	protected Collection<FleetMember> getElementList() {
		Fleet selected = fsi.getSelected();
		return selected == null ? new ObjectArrayList<FleetMember>(0) : selected.getMembers();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<FleetMember> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final FleetManager factionManager = ((GameClientState) getState()).getFleetManager();
		final PlayerState player = ((GameClientState) getState()).getPlayer();
		int i = 0;
		for (final FleetMember f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable shipPercentText = new GUITextOverlayTable(getState());
			GUITextOverlayTable engagementRangeText = new GUITextOverlayTable(getState());
			GUITextOverlayTable pickupText = new GUITextOverlayTable(getState());
			GUITextOverlayTable sectorText = new GUITextOverlayTable(getState());
			GUITextOverlayTable orderText = new GUITextOverlayTable(getState());
			GUIClippedRow nameAnchorP = new GUIClippedRow(getState());
			nameAnchorP.attach(nameText);
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getName() + (fsi.getSelected() != null && fsi.getSelected().isFlagShip(f) ? "(*)" : "");
				}
			});
			shipPercentText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.isLoaded() ? StringTools.formatPointZero(f.getShipPercent() * 100f) + "%" : Lng.str("N/A");
				}
			});
			engagementRangeText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.isLoaded() ? StringTools.formatDistance(f.getEngagementRange()) : Lng.str("N/A");
				}
			});
			pickupText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getPickupPoint();
				}
			});
			sectorText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.getSector().toStringPure();
				}
			});
			orderText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return fsi.getSelected() != null ? String.valueOf(fsi.getSelected().getMembers().indexOf(f) + 1) : Lng.str("N/A");
				}
			});
			nameText.getPos().y = 4;
			shipPercentText.getPos().y = 4;
			engagementRangeText.getPos().y = 4;
			pickupText.getPos().y = 4;
			sectorText.getPos().y = 4;
			orderText.getPos().y = 4;
			FleetMemberRow r = new FleetMemberRow(getState(), f, nameAnchorP, shipPercentText, engagementRangeText, pickupText, sectorText, orderText);
			r.expanded = new GUIElementList(getState());
			GUIAnchor c = new GUIAnchor(getState(), 100, 1);
			GUITextButton deleteMemberButton = new GUITextButton(getState(), 80, 24, ColorPalette.CANCEL, Lng.str("Delete"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						new PlayerGameOkCancelInput("CONFIRM", (GameClientState) getState(), Lng.str("Confirm"), Lng.str("Do you really want to delete this Fleet Member?")) {

							@Override
							public void pressedOK() {
								((FleetStateInterface) getState()).getFleetManager().requestFleetMemberRemove(fsi.getSelected(), f);
								deactivate();
							}

							@Override
							public void onDeactivate() {
							}
						}.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(489);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton upBotton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("Move Up"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(490);
						((FleetStateInterface) getState()).getFleetManager().requestFleetOrder(fsi.getSelected(), f, -1);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton downBotton = new GUITextButton(getState(), 80, 24, ColorPalette.OK, Lng.str("Move Down"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(491);
						((FleetStateInterface) getState()).getFleetManager().requestFleetOrder(fsi.getSelected(), f, 1);
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			});
			GUITextButton wipePickupPointBotton = new GUITextButton(getState(), 120, 24, ColorPalette.CANCEL, Lng.str("Wipe Pickup Point"), new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						SegmentController c;
						if ((c = f.getLoaded()) != null) {
							((Ship) c).lastPickupAreaUsed = Long.MIN_VALUE;
							((Ship) c).getNetworkObject().lastPickupAreaUsed.add(((Ship) c).lastPickupAreaUsed);
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(493);
						} else {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
							AudioController.fireAudioEventID(492);
						}
					}
				}

				@Override
				public boolean isOccluded() {
					return !isActive();
				}
			}) {

				@Override
				public void draw() {
					SegmentController c;
					if ((c = f.getLoaded()) != null && ((Ship) c).lastPickupAreaUsed != Long.MIN_VALUE) {
						super.draw();
					}
				}
			};
			c.attach(deleteMemberButton);
			c.attach(upBotton);
			c.attach(downBotton);
			c.attach(wipePickupPointBotton);
			// idText.setPos(4, c.getHeight() - 16, 0);
			deleteMemberButton.setPos(0, c.getHeight(), 0);
			upBotton.setPos(90, c.getHeight(), 0);
			downBotton.setPos(190, c.getHeight(), 0);
			wipePickupPointBotton.setPos(190 + 150, c.getHeight(), 0);
			// 
			// relationButton.setPos(mailButton.getWidth() + 10 + viewRelation.getWidth() + 10, c.getHeight(), 0);
			// 
			// joinButton.setPos(mailButton.getWidth() + 10 + viewRelation.getWidth() + 10 + relationButton.getWidth() + 10, c.getHeight(), 0);
			r.expanded.add(new GUIListElement(c, c, getState()));
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	private class FleetMemberRow extends Row {

		public FleetMemberRow(InputState state, FleetMember f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
