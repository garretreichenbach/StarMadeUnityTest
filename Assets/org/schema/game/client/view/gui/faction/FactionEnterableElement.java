package org.schema.game.client.view.gui.faction;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class FactionEnterableElement extends GUIAnchor {

	public static final int nameWidth = 305 - 32; //scroll bar
	public static final int sizeWidth = 34;
	public static final int homeWidth = 110;
	public static final int relShipWidth = 64;
	public static final int joinWidth = 30;
	private final Faction faction;
	private GUITextOverlay nameText;
	private GUITextOverlay sizeText;
	private GUITextButton joinOption;
	private GUITextButton relationText;
	private GUITextOverlay homeText;
	private GUITextOverlay prefixText;

	public FactionEnterableElement(InputState state, final Faction f, String prefix, int index, GUICallback onSelectCallBack) {
		super(state, 310, 25);

		this.faction = f;
		nameText = new GUITextOverlay(state);
		sizeText = new GUITextOverlay(state);
		homeText = new GUITextOverlay(state);

		prefixText = new GUITextOverlay(state);

		Object name = new Object() {
			@Override
			public String toString() {
				return f.getName() + (((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get() ? "[" + f.getIdFaction() + "]" : "");
			}
		};
		Object size = new Object() {
			@Override
			public String toString() {
				if (f.getIdFaction() < 0 && f.getFactionMode() == 0) {
					return "[NPC]";
				}
				return "[" + f.getMembersUID().size() + "]";
			}
		};
		Object location = new Object() {
			@Override
			public String toString() {
				if (f.getHomebaseUID().length() > 0) {
					return f.getHomeSector().toString();
				}
				return "";
			}
		};
		Object optionsText = new Object() {
			@Override
			public String toString() {
				if ((f.getIdFaction() < 0 && f.getFactionMode() == 0) || f.getIdFaction() == ((GameClientState) getState()).getPlayer().getFactionController().getFactionId()) {
					return "  -";
				}
				if (f.isOpenToJoin()) {
					return "join";
				} else {
					return "  -";
				}
			}
		};

		Object relationShip = new Object() {
			@Override
			public String toString() {

				if (f.getIdFaction() == ((GameClientState) getState()).getPlayer().getFactionController().getFactionId()) {
					return "*own*";
				}
				FactionManager factionManager = ((GameClientState) getState()).getFactionManager();

				boolean enemy = factionManager.isEnemy(f.getIdFaction(), ((GameClientState) getState()).getPlayer().getFactionController().getFactionId());
				boolean friend = factionManager.isFriend(f.getIdFaction(), ((GameClientState) getState()).getPlayer().getFactionController().getFactionId());

				if (enemy) {
					return "Enemy";
				} else if (friend) {
					return "Ally";
				} else {
					return "Neutral";
				}
			}
		};

		nameText.setText(new ArrayList());
		sizeText.setText(new ArrayList());
		homeText.setText(new ArrayList());

		nameText.getText().add(name);
		sizeText.getText().add(size);
		homeText.getText().add(location);

		prefixText.setTextSimple(prefix);

		if (prefix.length() > 0) {
			nameText.getPos().x = 8;
		}

		relationText = new GUITextButton(state, relShipWidth, 20, relationShip, onSelectCallBack);
		relationText.setUserPointer("REL_" + f.getIdFaction());

		relationText.setTextPos(10, 2);

		joinOption = new GUITextButton(getState(), joinWidth, 20, optionsText, onSelectCallBack);

		joinOption.setUserPointer("JOIN_" + f.getIdFaction());

		sizeText.getPos().x = nameWidth;
		homeText.getPos().x = nameWidth + sizeWidth;
		relationText.getPos().x = nameWidth + sizeWidth + homeWidth;
		joinOption.getPos().x = nameWidth + sizeWidth + homeWidth + relShipWidth;

		int h = 2;
		nameText.getPos().y = h;
		sizeText.getPos().y = h;
		homeText.getPos().y = h;
		relationText.getPos().y = h;
		joinOption.getPos().y = h;
		prefixText.getPos().y = h;

		setIndex(index);
		this.setUserPointer(f);
	}

	public static Vector4f getRowColor(int index, Faction f, GameClientState state) {
		//		if(state.getPlayer().getFactionId() == 0 || !state.getFactionManager().existsFaction(state.getPlayer().getFactionId())){
		//			return index % 2 == 0 ? new Vector4f(0.0f,0.0f,0.0f,0.0f) : new Vector4f(0.1f,0.1f,0.1f,0.5f);
		//		}else{
		if (state.getPlayer().getFactionId() == f.getIdFaction()) {
			GUIElementList.getRowColor(index);
		}
		RType relation = state.getFactionManager().getRelation(state.getPlayer().getName(), state.getPlayer().getFactionId(), f.getIdFaction());
		return switch(relation) {
			case ENEMY -> index % 2 == 0 ? new Vector4f(0.8f, 0.0f, 0.0f, 0.1f) : new Vector4f(0.8f, 0.1f, 0.1f, 0.5f);
			case FRIEND -> index % 2 == 0 ? new Vector4f(0.0f, 0.8f, 0.0f, 0.1f) : new Vector4f(0.1f, 0.8f, 0.1f, 0.5f);
			case NEUTRAL -> index % 2 == 0 ? new Vector4f(0.0f, 0.0f, 0.0f, 0.0f) : new Vector4f(0.1f, 0.1f, 0.1f, 0.5f);
			default -> index % 2 == 0 ? new Vector4f(0.0f, 0.0f, 0.0f, 0.0f) : new Vector4f(0.1f, 0.1f, 0.1f, 0.5f);
		};
		//		}
	}

	public void setIndex(int index) {
		detachAll();
		if (index < 0) {

			attach(nameText);
			attach(sizeText);
			attach(homeText);
			attach(relationText);
			attach(joinOption);
			attach(prefixText);
		} else {
			GUIColoredRectangle rt = new GUIColoredRectangle(getState(), UIScale.getUIScale().scale(510), (int)getHeight(),
					getRowColor(index, faction, (GameClientState) getState()));

			rt.attach(nameText);
			rt.attach(sizeText);
			rt.attach(homeText);
			rt.attach(relationText);
			rt.attach(joinOption);
			rt.attach(prefixText);
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
