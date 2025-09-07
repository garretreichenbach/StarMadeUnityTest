package org.schema.game.client.view.gui.navigation;

import org.schema.game.client.controller.manager.ingame.navigation.NavigationFilter;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputContentSizeInterface;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class NavigationFilterSettingPanel extends GUIInputPanel {

	public NavigationFilter filter;
	private GUIAnchor thisContent;

	private boolean inputActive;
	private GUIAnchor permissionTable;
	private GUITextOverlay headlineText;
	private GUITextOverlay rockText;
	private GUITextOverlay planetText;
	private GUITextOverlay playerText;
	private GUITextOverlay shipText;
	private GUITextOverlay shopText;
	private GUITextOverlay dockedText;
	private GUITextOverlay turretsText;

	private GUITextOverlay spaceStationText;
	private GUICheckBox rockCBox;
	private GUICheckBox planetCBox;
	private GUICheckBox planetCoreCBox;
	private GUICheckBox playerCBox;
	private GUICheckBox shipCBox;
	private GUICheckBox shopCBox;
	private GUICheckBox dockedCBox;
	private GUICheckBox turretsCBox;

	private GUICheckBox spaceStationCBox;

	private GUITextOverlay planetSegmentsText;

	public NavigationFilterSettingPanel(GameClientState state, NavigationFilter filter, int index, GUICallback callback, boolean showAdminControl) {
		super("NavigationFilterSettingPanelM", state, 436, 200, callback, Lng.str("Edit Navigation Filter"), "");
		this.filter = filter;

		thisContent = new GUIAnchor(getState(), 430, 121);
	}

	@Override
	public float getHeight() {
		return thisContent.getHeight();
	}

	@Override
	public float getWidth() {
		return thisContent.getWidth();
	}

	@Override
	public void cleanUp() {

	}

//	@Override
//	public void draw() {
//		adaptSizes();
//		drawAttached();
//	}

	@Override
	public void onInit() {
		setCancelButton(false);
		setOkButton(true);

		super.onInit();

		permissionTable = new GUIAnchor(getState(), 400, 100);

		planetText = new GUITextOverlay(getState());
		planetSegmentsText = new GUITextOverlay(getState());
		dockedText = new GUITextOverlay(getState()) {
			@Override
			public void draw() {
				if(shipCBox.isActivatedCheckBox()) {
					super.draw();
				}

			}
		};
		turretsText = new GUITextOverlay(getState()) {
			@Override
			public void draw() {
				if(shipCBox.isActivatedCheckBox()) {
					super.draw();
				}

			}
		};
		playerText = new GUITextOverlay(getState());
		shipText = new GUITextOverlay(getState());
		rockText = new GUITextOverlay(getState());
		shopText = new GUITextOverlay(getState());
		spaceStationText = new GUITextOverlay(getState());
		headlineText = new GUITextOverlay(getState());
		rockText.setTextSimple(Lng.str("Asteroids"));
		planetText.setTextSimple(Lng.str("PlanetParts"));
		planetSegmentsText.setTextSimple(Lng.str("Planet"));
		dockedText.setTextSimple(Lng.str("Docked"));
		turretsText.setTextSimple(Lng.str("Turrets"));
		playerText.setTextSimple(Lng.str("Astronauts"));
		shipText.setTextSimple(Lng.str("Ships"));
		shopText.setTextSimple(Lng.str("Shops"));
		spaceStationText.setTextSimple(Lng.str("Space Stations"));
		headlineText.setTextSimple(Lng.str("Filters"));

		rockCBox = new PCheckBox(getState(), NavigationFilter.POW_FLOATINGROCK);
		planetCBox = new PCheckBox(getState(), NavigationFilter.POW_PLANET);
		planetCoreCBox = new PCheckBox(getState(), NavigationFilter.POW_PLANET_CORE);
		dockedCBox = new PCheckBox(getState(), NavigationFilter.POW_DOCKED) {
			@Override
			public void draw() {
				if(shipCBox.isActivatedCheckBox()) {
					super.draw();
				}

			}
		};
		turretsCBox = new PCheckBox(getState(), NavigationFilter.POW_TURRET) {
			@Override
			public void draw() {
				if(shipCBox.isActivatedCheckBox()) {
					super.draw();
				}

			}
		};
		playerCBox = new PCheckBox(getState(), NavigationFilter.POW_PLAYER);
		shipCBox = new PCheckBox(getState(), NavigationFilter.POW_SHIP);
		shopCBox = new PCheckBox(getState(), NavigationFilter.POW_SHOP);
		spaceStationCBox = new PCheckBox(getState(), NavigationFilter.POW_SPACESTATION);

		//			permissionTable.attach(headlineText);
		int textHeights = 0;
		int boxHeights = 12;

		int distHeight = 60;

		float xD = 93;

		rockText.setPos(0, textHeights, 0);
		planetText.setPos(1 * xD, textHeights, 0);
		planetSegmentsText.setPos(2 * xD, textHeights, 0);
		playerText.setPos(3 * xD, textHeights, 0);
		shipText.setPos(4 * xD, textHeights, 0);
		shopText.setPos(0, textHeights + distHeight, 0);
		spaceStationText.setPos(1 * xD, textHeights + distHeight, 0);
		dockedText.setPos(2 * xD, textHeights + distHeight, 0);
		turretsText.setPos(3 * xD, textHeights + distHeight, 0);

		permissionTable.attach(rockText);
		permissionTable.attach(planetText);
		permissionTable.attach(planetSegmentsText);
		permissionTable.attach(playerText);
		permissionTable.attach(shipText);
		permissionTable.attach(shopText);
		permissionTable.attach(spaceStationText);
		permissionTable.attach(dockedText);
		permissionTable.attach(turretsText);

		rockCBox.setPos(0, boxHeights, 0);
		planetCBox.setPos(1 * xD, boxHeights, 0);
		planetCoreCBox.setPos(2 * xD, boxHeights, 0);
		playerCBox.setPos(3 * xD, boxHeights, 0);
		shipCBox.setPos(4 * xD, boxHeights, 0);
		shopCBox.setPos(0, boxHeights + distHeight, 0);
		spaceStationCBox.setPos(1 * xD, boxHeights + distHeight, 0);
		dockedCBox.setPos(2 * xD, boxHeights + distHeight, 0);
		turretsCBox.setPos(3 * xD, boxHeights + distHeight, 0);

		permissionTable.attach(rockCBox);
		permissionTable.attach(planetCBox);
		permissionTable.attach(planetCoreCBox);
		permissionTable.attach(playerCBox);
		permissionTable.attach(shopCBox);
		permissionTable.attach(spaceStationCBox);
		permissionTable.attach(shipCBox);
		permissionTable.attach(dockedCBox);
		permissionTable.attach(turretsCBox);

		permissionTable.getPos().y = 0;

		thisContent.attach(permissionTable);

		//			GUITextOverlay none = new GUITextOverlay(100, 20, getState());
		//			none.setTextSimple("Filter objects");
		//			thisContent.attach(none);
		thisContent.getPos().x = 2;
		thisContent.getPos().y = 2;
		getContent().attach(thisContent);

		contentInterface = new GUIInputContentSizeInterface() {

			@Override
			public int getWidth() {
				return (int) thisContent.getWidth();
			}

			@Override
			public int getHeight() {
				return (int) thisContent.getHeight();
			}
		};
	}

	/**
	 * @return the inputActive
	 */
	public boolean isInputActive() {
		return inputActive;
	}

	/**
	 * @param inputActive the inputActive to set
	 */
	public void setInputActive(boolean inputActive) {
		this.inputActive = inputActive;
	}

	private class PCheckBox extends GUICheckBox {
		private long f;

		public PCheckBox(InputState state, long filter) {
			super(state);
			this.f = filter;
		}

		@Override
		protected void activate() throws StateParameterNotFoundException {
			filter.setFilter(true, f);
		}

		@Override
		protected void deactivate() throws StateParameterNotFoundException {
			filter.setFilter(false, f);

		}

		@Override
		protected boolean isActivated() {
			return filter.isFiltered(f);
		}

	}

}
