package org.schema.game.client.view.gui.faction;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.manager.ingame.faction.FactionPersonalEnemyDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FactionPersonalEnemyEditPanel extends GUIInputPanel {
	private final Faction from;
	private final FactionPersonalEnemyDialog dialog;
	ObjectOpenHashSet<String> set = new ObjectOpenHashSet<String>();
	private GUIScrollablePanel p;
	private GUIElementList l;
	private boolean init;

	public FactionPersonalEnemyEditPanel(InputState state, Faction from, FactionPersonalEnemyDialog dialog) {
		super("FACTION_PERSONAL_ENEMY_EDIT", state, dialog, "Personal Enemies of the faction", "");
		this.from = from;
		this.dialog = dialog;

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
		super.draw();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {

		super.onInit();

		p = new GUIScrollablePanel(400, 110, getState());
		l = new GUIElementList(getState());
		p.setContent(l);
		updateList();
		GUITextOverlay current = new GUITextOverlay(getState());

		GUITextButton add = new GUITextButton(getState(), UIScale.getUIScale().scale(40), UIScale.getUIScale().scale(20),
				new Vector4f(0.7f, 0.0f, 0.7f, 1), new Vector4f(1.0f, 1, 1, 1),
				FontSize.TINY_12, "Add", dialog) {
			@Override
			public void draw() {
				FactionPermission factionPermission = from.getMembersUID().get(((GameClientState) getState()).getPlayer().getName());
				if (factionPermission == null || !factionPermission.hasRelationshipPermission(from)) {
					return;
				}
				super.draw();
			}

		};
		add.setTextPos(5, 1);

		add.getPos().y = UIScale.getUIScale().scale(5);
		p.getPos().y = UIScale.getUIScale().scale(30);

		getContent().attach(add);
		getContent().attach(p);

		getContent().attach(current);
		add.setUserPointer("ADD");

	}

	private void updateList() {
		l.clear();
		this.set.clear();
		this.set.addAll(from.getPersonalEnemies());
		for (String s : set) {
			int rowHeight = UIScale.getUIScale().scale(25);
			GUIAnchor r = new GUIAnchor(getState(), 400, rowHeight);
			GUITextOverlay t = new GUITextOverlay(getState());
			t.getPos().y = 3;
			t.setTextSimple(s);
			r.attach(t);
			GUITextButton b = new GUITextButton(getState(), 80, rowHeight - 6,
					new Vector4f(0.7f, 0.0f, 0.0f, 1), new Vector4f(1.0f, 1, 1, 1),
					FontSize.TINY_12, "Remove", dialog) {
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
