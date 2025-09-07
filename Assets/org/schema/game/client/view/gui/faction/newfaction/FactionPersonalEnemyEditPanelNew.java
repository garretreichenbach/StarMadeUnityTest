package org.schema.game.client.view.gui.faction.newfaction;

import org.schema.game.client.controller.manager.ingame.faction.FactionPersonalEnemyDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FactionPersonalEnemyEditPanelNew extends GUIInputPanel {
	private final Faction from;
	private final FactionPersonalEnemyDialog dialog;
	ObjectOpenHashSet<String> set = new ObjectOpenHashSet<String>();
	private GUIScrollablePanel p;
	private GUIElementList l;
	private boolean init;
	private GUITextButton add;

	public FactionPersonalEnemyEditPanelNew(InputState state, Faction from, FactionPersonalEnemyDialog dialog) {
		super("FactionPersonalEnemyEditPanelNew", state, 420, 180, dialog, Lng.str("Personal Enemies of the faction"), "");
		this.from = from;
		this.dialog = dialog;

		setCancelButton(false);

	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#draw()
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
			init = true;
		}
		if (set.size() != from.getPersonalEnemies().size() || !set.equals(from.getPersonalEnemies())) {
			System.err.println("[CLIENT] updated panel for personal enemies of faction");
			updateList();
		}
		add.getPos().set(getButtonCancel().getPos().x + 30, getButtonCancel().getPos().y, 0);
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {

		super.onInit();

		p = new GUIScrollablePanel(100, 100, getContent(), getState());
		l = new GUIElementList(getState());
		p.setContent(l);
		updateList();
		GUITextOverlayTable current = new GUITextOverlayTable(getState());

		add = new GUITextButton(getState(), (int) getButtonOK().getWidth(), (int) getButtonOK().getHeight(),
				ColorPalette.OK,
				FontSize.SMALL_14, Lng.str("Add"), dialog) {
			@Override
			public void draw() {
				FactionPermission factionPermission = from.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
				if (factionPermission == null || !factionPermission.hasRelationshipPermission(from)) {
					return;
				}
				super.draw();
			}

		};

		getBackground().attach(add);
		getContent().attach(p);

		getContent().attach(current);
		add.setUserPointer("ADD");

	}

	private void updateList() {
		l.clear();
		this.set.clear();
		this.set.addAll(from.getPersonalEnemies());
		for (String s : set) {
			int rowHeight = 25;
			GUIAnchor r = new GUIAnchor(getState(), 400, rowHeight);
			GUITextOverlayTable t = new GUITextOverlayTable(getState());
			t.getPos().y = 3;
			t.setTextSimple(s);
			r.attach(t);
			GUITextButton b = new GUITextButton(getState(), 80, rowHeight - 6,
					ColorPalette.CANCEL,
					FontSize.SMALL_14, Lng.str("Remove"), dialog) {
				@Override
				public void draw() {
					FactionPermission factionPermission = from.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
					if (factionPermission == null || !factionPermission.hasRelationshipPermission(from)) {
						return;
					}
					super.draw();
				}

			};
			b.setUserPointer("remove_" + s);
			b.getPos().x = 250;
			b.getPos().y = 3;
			r.attach(b);
			l.add(new GUIListElement(r, r, getState()));
		}

	}

}
