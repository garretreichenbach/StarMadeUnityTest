package org.schema.game.client.view.gui.mapgui;

import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.map.MapFilterEditDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gamemap.GameMapPosition;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class MapToolsPanel extends GUIAnchor {

	private Vector3i relPosTmp = new Vector3i();

	public MapToolsPanel(InputState state) {
		super(state, 800, 128);
		init();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#getState()
	 */
	@Override
	public GameClientState getState() {
		return (GameClientState) super.getState();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		super.update(timer);
	}

	public GameMapPosition getMapPosition() {
		return getState().getWorldDrawer().getGameMapDrawer().getGameMapPosition();
	}

	public GameMapDrawer getMapDrawer() {
		return getState().getWorldDrawer().getGameMapDrawer();
	}

	public String getSystemInfo(VoidSystem sys) {
		String sysTxt;
		if (sys != null) {
			String facTxt;
			if (sys.getOwnerFaction() != 0 && sys.getOwnerUID() != null && getState().getFactionManager().existsFaction(sys.getOwnerFaction())) {
				Faction f = getState().getFactionManager().getFaction(sys.getOwnerFaction());
				facTxt = " " + Lng.str("Owner: %s", f.getName());
			} else {
				facTxt = "";
			}
			Vector3i relPos = Galaxy.getLocalCoordinatesFromSystem(sys.getPos(), relPosTmp);
			sysTxt = getState().getCurrentGalaxy().getName(relPos) + " " + sys.getPos() + facTxt;
		} else {
			sysTxt = Lng.str("Calculating...");
		}
		return sysTxt;
	}

	private void init() {
		GUIColoredRectangle bg = new GUIColoredRectangle(getState(), (int) getWidth(), (int) getHeight(), new Vector4f(0.1f, 0.1f, 0.3f, 0.5f));
		bg.rounded = 6;
		attach(bg);
		GUITextOverlay help = new GUITextOverlay(getState());
		help.setTextSimple(Lng.str("Hold right mouse button to rotate, and use mouse wheel to zoom. Use your move keys to move. Hold shift to move a system instead of a sector."));
		GUITextOverlay ownPos = new GUITextOverlay(getState());
		ownPos.setTextSimple(new Object() {

			@Override
			public String toString() {
				VoidSystem sys = getState().getCurrentClientSystem();
				if (MapToolsPanel.this.getState().getPlayer().isInTutorial()) {
					return "Own Position: [System " + "Tutorial" + "] [Sector: " + "Tutorial" + "]";
				} else if (MapToolsPanel.this.getState().getPlayer().isInPersonalSector()) {
					return Lng.str("Own Position: [System Personal] [Sector: Personal]");
				} else if (MapToolsPanel.this.getState().getPlayer().isInTestSector()) {
					return Lng.str("Own Position: [System Test System] [Sector: Test Sector]");
				} else {
					return Lng.str("Own Position: [System %s] [Sector: %s]", getSystemInfo(sys), getState().getPlayer().getCurrentSector().toString());
				}
			}
		});
		GUITextButton goHome = new GUITextButton(getState(), 140, 18, Lng.str("Center on own pos"), new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					getMapDrawer().resetToCurrentSector();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(552);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		goHome.setTextPos(3, 0);
		GUITextOverlay curSelPos = new GUITextOverlay(getState());
		curSelPos.setTextSimple(new Object() {

			final Vector3i tmpPos = new Vector3i();

			@Override
			public String toString() {
				String sysTxt;
				if (getState().getController() != null && getState().getController().getClientChannel() != null) {
					VoidSystem sys = getState().getController().getClientChannel().getGalaxyManagerClient().getSystemOnClient(getMapPosition().get(tmpPos));
					if (sys != null) {
						return Lng.str("Selected: [System %s] [Sector: %s]", getSystemInfo(sys), tmpPos.toString());
					} else {
						return Lng.str("n/a");
					}
				} else {
					return Lng.str("n/a");
				}
			}
		});
		GUITextButton plotPath = new GUITextButton(getState(), 140, 18, Lng.str("Plot path to current"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					Vector3i c = getState().getPlayer().getCurrentSector();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(553);
					getState().getController().getClientGameData().setWaypoint(getMapPosition().get(new Vector3i()));
				}
			}
		});
		plotPath.setTextPos(3, 0);
		GUITextButton filter = new GUITextButton(getState(), 140, 18, Lng.str("Filter"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(554);
					MapFilterEditDialog a = new MapFilterEditDialog(getState(), GameMapDrawer.filter, false);
					a.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(555);
				}
			}
		});
		plotPath.setTextPos(3, 0);
		GUITextButton moveToNavDest = new GUITextButton(getState(), 139, 18, new Object() {

			@Override
			public String toString() {
				return Lng.str("Select %s", getState().getController().getClientGameData().getWaypoint().toString());
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(556);
					Vector3i waypoint = getState().getController().getClientGameData().getWaypoint();
					if (waypoint != null) {
						getMapPosition().set(waypoint.x, waypoint.y, waypoint.z, false);
					}
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if (MapToolsPanel.this.getState().getController().getClientGameData().getWaypoint() != null) {
					super.draw();
				}
			}
		};
		GUITextButton adminWarp = new GUITextButton(getState(), 300, 18, new Object() {

			@Override
			public String toString() {
				return Lng.str("Admin Warp %s", getMapPosition().get(new Vector3i()).toString());
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(557);
					Vector3i to = getMapPosition().get(new Vector3i());
					MapToolsPanel.this.getState().getController().sendAdminCommand(AdminCommands.CHANGE_SECTOR, to.x, to.y, to.z);
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if (MapToolsPanel.this.getState().getPlayer().getNetworkObject().isAdminClient.get() && !MapToolsPanel.this.getState().getPlayer().isInTutorial()) {
					super.draw();
				}
			}
		};
		GUITextButton adminScan = new GUITextButton(getState(), 300, 18, new Object() {

			@Override
			public String toString() {
				return Lng.str("Admin Scan %s", getMapPosition().get(new Vector3i()).toString());
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(558);
					Vector3i to = getMapPosition().getCurrentSysPos();
					MapToolsPanel.this.getState().getController().sendAdminCommand(AdminCommands.SCAN, to.x, to.y, to.z);
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if (MapToolsPanel.this.getState().getPlayer().getNetworkObject().isAdminClient.get() && !MapToolsPanel.this.getState().getPlayer().isInTutorial()) {
					super.draw();
				}
			}
		};
		GUICheckBox drawPlanetOrbits = new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GameMapDrawer.drawPlanetOrbits = true;
			}

			@Override
			protected boolean isActivated() {
				return GameMapDrawer.drawPlanetOrbits;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GameMapDrawer.drawPlanetOrbits = false;
			}
		};
		GUITextOverlay drawPlanetOrbitsText = new GUITextOverlay(getState());
		drawPlanetOrbitsText.setTextSimple(Lng.str("Planet Orbits"));
		GUICheckBox drawAsteroidBeltOrbits = new GUICheckBox(getState()) {

			@Override
			protected boolean isActivated() {
				return GameMapDrawer.drawAsteroidBeltOrbits;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GameMapDrawer.drawAsteroidBeltOrbits = false;
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GameMapDrawer.drawAsteroidBeltOrbits = true;
			}
		};
		GUITextOverlay drawAsteroidBeltOrbitsText = new GUITextOverlay(getState());
		drawAsteroidBeltOrbitsText.setTextSimple(Lng.str("Asteroid Belt Orbits"));
		GUICheckBox highlightOrbitSectors = new GUICheckBox(getState()) {

			@Override
			protected boolean isActivated() {
				return GameMapDrawer.highlightOrbitSectors;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GameMapDrawer.highlightOrbitSectors = false;
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GameMapDrawer.highlightOrbitSectors = true;
			}
		};
		GUITextOverlay highlightOrbitSectorsText = new GUITextOverlay(getState());
		highlightOrbitSectorsText.setTextSimple(Lng.str("Orbital Sectors"));
		GUICheckBox drawFactionByRelation = new GUICheckBox(getState()) {

			@Override
			protected boolean isActivated() {
				return GameMapDrawer.drawFactionByRelation;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GameMapDrawer.drawFactionByRelation = false;
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GameMapDrawer.drawFactionByRelation = true;
			}
		};
		GUITextOverlay drawFactionByRelationText = new GUITextOverlay(getState());
		drawFactionByRelationText.setTextSimple(Lng.str("Faction Territory by Relation"));
		GUICheckBox drawFactionTerritory = new GUICheckBox(getState()) {

			@Override
			protected boolean isActivated() {
				return GameMapDrawer.drawFactionTerritory;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GameMapDrawer.drawFactionTerritory = false;
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GameMapDrawer.drawFactionTerritory = true;
			}
		};
		GUITextOverlay drawFactionTerritoryText = new GUITextOverlay(getState());
		drawFactionTerritoryText.setTextSimple(Lng.str("Faction Territory"));
		GUICheckBox drawWormHoles = new GUICheckBox(getState()) {

			@Override
			protected boolean isActivated() {
				return GameMapDrawer.drawWormHoles;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GameMapDrawer.drawWormHoles = false;
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GameMapDrawer.drawWormHoles = true;
			}
		};
		GUITextOverlay drawWormHolesText = new GUITextOverlay(getState());
		drawWormHolesText.setTextSimple(Lng.str("Worm Holes"));
		GUICheckBox drawWarpGates = new GUICheckBox(getState()) {

			@Override
			protected boolean isActivated() {
				return GameMapDrawer.drawWarpGates;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				GameMapDrawer.drawWarpGates = false;
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				GameMapDrawer.drawWarpGates = true;
			}
		};
		GUITextOverlay drawWarpGatesText = new GUITextOverlay(getState());
		drawWarpGatesText.setTextSimple(Lng.str("Warp Gates"));
		drawPlanetOrbits.setScale(0.5f, 0.5f, 0.5f);
		drawAsteroidBeltOrbits.setScale(0.5f, 0.5f, 0.5f);
		highlightOrbitSectors.setScale(0.5f, 0.5f, 0.5f);
		drawFactionByRelation.setScale(0.5f, 0.5f, 0.5f);
		drawFactionTerritory.setScale(0.5f, 0.5f, 0.5f);
		drawWarpGates.setScale(0.5f, 0.5f, 0.5f);
		drawWormHoles.setScale(0.5f, 0.5f, 0.5f);
		moveToNavDest.setTextPos(7, 0);
		int xStart = 1;
		int bPos = 520;
		int ySpacing = 20;
		adminWarp.setPos(xStart + 0, -1 * ySpacing, 0);
		bg.attach(adminWarp);
		adminScan.setPos(xStart + 0 + adminWarp.getWidth() + 10, -1 * ySpacing, 0);
		bg.attach(adminScan);
		help.setPos(xStart + 0, 0 * ySpacing, 0);
		bg.attach(help);
		ownPos.setPos(xStart + 0, 1 * ySpacing, 0);
		bg.attach(ownPos);
		goHome.setPos(xStart + bPos, 1 * ySpacing, 0);
		bg.attach(goHome);
		curSelPos.setPos(xStart + 0, 2 * ySpacing, 0);
		bg.attach(curSelPos);
		plotPath.setPos(xStart + bPos, 2 * ySpacing, 0);
		bg.attach(plotPath);
		moveToNavDest.setPos(xStart + bPos + plotPath.getWidth(), 2 * ySpacing, 0);
		bg.attach(moveToNavDest);
		filter.setPos(xStart + bPos, 3 * ySpacing, 0);
		bg.attach(filter);
		drawPlanetOrbits.setPos(xStart + 0, 3 * ySpacing, 0);
		bg.attach(drawPlanetOrbits);
		drawPlanetOrbitsText.setPos(xStart + 16, 3 * ySpacing, 0);
		bg.attach(drawPlanetOrbitsText);
		drawAsteroidBeltOrbits.setPos(xStart + 0, 4 * ySpacing, 0);
		bg.attach(drawAsteroidBeltOrbits);
		drawAsteroidBeltOrbitsText.setPos(xStart + 16, 4 * ySpacing, 0);
		bg.attach(drawAsteroidBeltOrbitsText);
		highlightOrbitSectors.setPos(xStart + 0, 5 * ySpacing, 0);
		bg.attach(highlightOrbitSectors);
		highlightOrbitSectorsText.setPos(xStart + 16, 5 * ySpacing, 0);
		bg.attach(highlightOrbitSectorsText);
		drawFactionTerritory.setPos(xStart + 0 + 300, 3 * ySpacing, 0);
		bg.attach(drawFactionTerritory);
		drawFactionTerritoryText.setPos(xStart + 16 + 300, 3 * ySpacing, 0);
		bg.attach(drawFactionTerritoryText);
		drawFactionByRelation.setPos(xStart + 0 + 300, 4 * ySpacing, 0);
		bg.attach(drawFactionByRelation);
		drawFactionByRelationText.setPos(xStart + 16 + 300, 4 * ySpacing, 0);
		bg.attach(drawFactionByRelationText);
		drawWormHoles.setPos(xStart + 0 + 300, 5 * ySpacing, 0);
		bg.attach(drawWormHoles);
		drawWormHolesText.setPos(xStart + 16 + 300, 5 * ySpacing, 0);
		bg.attach(drawWormHolesText);
		drawWarpGates.setPos(xStart + 0 + 600, 5 * ySpacing, 0);
		bg.attach(drawWarpGates);
		drawWarpGatesText.setPos(xStart + 16 + 600, 5 * ySpacing, 0);
		bg.attach(drawWarpGatesText);
	}
}
