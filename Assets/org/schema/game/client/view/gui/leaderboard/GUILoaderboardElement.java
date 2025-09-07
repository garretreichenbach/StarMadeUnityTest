package org.schema.game.client.view.gui.leaderboard;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;

import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.gamemode.battle.KillerEntity;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.network.client.ClientState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUILoaderboardElement extends GUIAnchor {

	public static final int nameWidth = 390 - 32; //scroll bar
	public static final int homeWidth = 40;
	public static final int relShipWidth = 55;
	public static final int joinWidth = 80;
	private final Entry<String, ObjectArrayList<KillerEntity>> entry;
	private GUITextOverlay nameText;
	private GUITextOverlay kills;
	private GUITextOverlay deaths;

	public GUILoaderboardElement(final ClientState state, final Entry<String, ObjectArrayList<KillerEntity>> f, String prefix, int index, GUICallback onSelectCallBack) {
		super(state, 310, 25);

		this.entry = f;
		nameText = new GUITextOverlay(state);

		kills = new GUITextOverlay(state);
		deaths = new GUITextOverlay(state);

		Object name = new Object() {
			@Override
			public String toString() {
				return f.getKey();
			}
		};
		Object kills = new Object() {
			@Override
			public String toString() {
				return String.valueOf(f.getValue().size());
			}
		};

		Object deathsStr = new Object() {
			@Override
			public String toString() {
				return String.valueOf(((GameClientState) state).getGameState().getClientDeadCount().getInt(f.getKey()));
			}
		};

		System.err.println("[GUI] LEADERBOARD: " + f);

		nameText.setText(new ArrayList());

		nameText.getText().add(name);

		this.kills.setTextSimple(kills);
		this.deaths.setTextSimple(deathsStr);

		if (prefix.length() > 0) {
			nameText.getPos().x = 8;
		}

		int h = 2;
		nameText.getPos().y = h;
		this.kills.getPos().x = nameWidth;
		this.kills.getPos().y = h;
		this.deaths.getPos().x = nameWidth + 50;
		this.deaths.getPos().y = h;

		setIndex(index);
		this.setUserPointer(f);
	}

	public static Vector4f getRowColor(int index,
	                                   Entry<String, ObjectArrayList<KillerEntity>> f,
	                                   GameClientState state) {

		if (state.getPlayer().getName().toLowerCase(Locale.ENGLISH).equals(f.getKey().toLowerCase(Locale.ENGLISH))) {
			return index % 2 == 0 ? new Vector4f(0.0f, 0.5f, 0.0f, 0.5f) : new Vector4f(0.1f, 0.5f, 0.1f, 0.5f);
		}

		return index % 2 == 0 ? new Vector4f(0.0f, 0.0f, 0.0f, 0.0f) : new Vector4f(0.1f, 0.1f, 0.1f, 0.5f);

	}

	public void setIndex(int index) {
		detachAll();
		if (index < 0) {

			attach(nameText);
			attach(kills);
			attach(deaths);
		} else {
			GUIColoredRectangle rt = new GUIColoredRectangle(getState(), UIScale.getUIScale().scale(510), (int)getHeight(),
					getRowColor(index, entry, (GameClientState) getState()));

			rt.attach(nameText);
			rt.attach(kills);
			rt.attach(deaths);
			attach(rt);

		}
	}

	//		/* (non-Javadoc)
	//		 * @see org.schema.schine.graphicsengine.forms.gui.GUIListElement#drawSelectedContent()
	//		 */
	//		@Override
	//		public void drawSelectedContent()  {
	//			nameText.getColor().a = 1;
	//			nameText.getColor().r = 1;
	//			nameText.getColor().g = 0.6f;
	//			nameText.getColor().b = 0.6f;
	//			super.drawSelectedContent();
	//			nameText.getColor().a = 1;
	//			nameText.getColor().r = 1;
	//			nameText.getColor().g = 1;
	//			nameText.getColor().b = 1;
	//		}

}
