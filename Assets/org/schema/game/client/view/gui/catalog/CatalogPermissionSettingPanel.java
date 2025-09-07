package org.schema.game.client.view.gui.catalog;

import org.schema.game.client.controller.manager.ingame.catalog.CatalogPermissionEditDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;

public class CatalogPermissionSettingPanel extends GUIInputPanel {

	private final CatalogPermission catalogPermission;
	private final CatalogPermission editingPermission;
	private GUIAnchor thisContent;
	private boolean inputActive;
	private GUIAnchor permissionTable;
	private GUITextOverlay permissionEditHeadlineText;
	private GUITextOverlay factionPermissionText;
	private GUITextOverlay othersPermissionText;
	private GUITextOverlay homePermissionText;
	private GUITextOverlay spawnablePermissionText;
	private GUICheckBox factionCanSpawnCBox;
	private GUICheckBox othersPermissionCBox;
	private GUICheckBox homeOnlyPermissionCBox;
	private GUICheckBox enemySpawnablePermissionCBox;

	public CatalogPermissionSettingPanel(GameClientState state, CatalogPermission catalogEntry,
	                                     int index, CatalogPermissionEditDialog callback) {
		super("CatalogPermissionSettingPanel", state, 400, 170, callback, Lng.str("Edit Entry Permission"), "");

		setCancelButton(false);
		setOkButtonText(Lng.str("DONE"));

		this.catalogPermission = catalogEntry;

		this.editingPermission = new CatalogPermission(catalogEntry);

		thisContent = new GUIAnchor(getState(), 300, 80);
	}

	/**
	 * @return the catalogPermission
	 */
	public CatalogPermission getCatalogPermission() {
		return catalogPermission;
	}

	/**
	 * @return the editingPermission
	 */
	public CatalogPermission getEditingPermission() {
		return editingPermission;
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

	@Override
	public void onInit() {
		super.onInit();

		permissionTable = new GUIAnchor(getState(), 320, 100);

		othersPermissionText = new GUITextOverlay(getState());
		homePermissionText = new GUITextOverlay(getState());
		spawnablePermissionText = new GUITextOverlay(getState());
		factionPermissionText = new GUITextOverlay(getState());
		permissionEditHeadlineText = new GUITextOverlay(getState());
		factionPermissionText.setTextSimple(Lng.str("Faction"));
		othersPermissionText.setTextSimple(Lng.str("Others"));
		homePermissionText.setTextSimple(Lng.str("HomeBase"));
		spawnablePermissionText.setTextSimple(Lng.str("EnemyUsable"));
		permissionEditHeadlineText.setTextSimple(Lng.str("Permissions"));

		factionCanSpawnCBox = new GUICheckBox(getState()) {

			@Override
			protected void activate()
					throws StateParameterNotFoundException {
				getEditingPermission().setPermission(true, CatalogPermission.P_BUY_FACTION);
			}

			@Override
			protected void deactivate()
					throws StateParameterNotFoundException {
				getEditingPermission().setPermission(false, CatalogPermission.P_BUY_FACTION);

			}

			@Override
			protected boolean isActivated() {
				return getEditingPermission().faction();
			}
		};

		othersPermissionCBox = new GUICheckBox(getState()) {

			@Override
			protected void activate()
					throws StateParameterNotFoundException {
				getEditingPermission().setPermission(true, CatalogPermission.P_BUY_OTHERS);
			}

			@Override
			protected void deactivate()
					throws StateParameterNotFoundException {

				getEditingPermission().setPermission(false, CatalogPermission.P_BUY_OTHERS);

			}

			@Override
			protected boolean isActivated() {
				return getEditingPermission().others();
			}
		};

		homeOnlyPermissionCBox = new GUICheckBox(getState()) {

			@Override
			protected void activate()
					throws StateParameterNotFoundException {
				getEditingPermission().setPermission(true, CatalogPermission.P_BUY_HOME_ONLY);
			}

			@Override
			protected void deactivate()
					throws StateParameterNotFoundException {

				getEditingPermission().setPermission(false, CatalogPermission.P_BUY_HOME_ONLY);

			}

			@Override
			protected boolean isActivated() {
				return getEditingPermission().homeOnly();
			}
		};

		enemySpawnablePermissionCBox = new GUICheckBox(getState()) {

			@Override
			protected void activate()
					throws StateParameterNotFoundException {
				getEditingPermission().setPermission(true, CatalogPermission.P_ENEMY_USABLE);
			}

			@Override
			protected void deactivate()
					throws StateParameterNotFoundException {
				getEditingPermission().setPermission(false, CatalogPermission.P_ENEMY_USABLE);
			}

			@Override
			protected boolean isActivated() {
				return getEditingPermission().enemyUsable();
			}
		};
		permissionTable.attach(permissionEditHeadlineText);
		int textHeights = 20;
		int boxHeights = 35;

		factionPermissionText.setPos(0, textHeights, 0);
		othersPermissionText.setPos(70, textHeights, 0);
		homePermissionText.setPos(140, textHeights, 0);
		spawnablePermissionText.setPos(210, textHeights, 0);

		permissionTable.attach(factionPermissionText);
		permissionTable.attach(othersPermissionText);
		permissionTable.attach(homePermissionText);

		factionCanSpawnCBox.setPos(0, boxHeights, 0);
		othersPermissionCBox.setPos(70, boxHeights, 0);
		homeOnlyPermissionCBox.setPos(140, boxHeights, 0);
		enemySpawnablePermissionCBox.setPos(210, boxHeights, 0);

		permissionTable.attach(factionCanSpawnCBox);
		permissionTable.attach(othersPermissionCBox);
		permissionTable.attach(homeOnlyPermissionCBox);
		if (((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get()) {
			permissionTable.attach(spawnablePermissionText);
			permissionTable.attach(enemySpawnablePermissionCBox);
		}

		permissionTable.getPos().y = 20;

		thisContent.attach(permissionTable);

		GUITextOverlay none = new GUITextOverlay(getState());
		none.setTextSimple("Specify the permissions for " + catalogPermission.getUid());
		thisContent.attach(none);

		GUIScrollablePanel scrollPanel = new GUIScrollablePanel(100, 100, getContent(), getState());
		scrollPanel.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
		scrollPanel.setContent(thisContent);

		getContent().attach(scrollPanel);

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

}
