package org.schema.game.client.view.gui.weapon;

import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.SegmentControllerUsable;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WeaponSegmentControllerUsableElement implements WeaponRowElementInterface {
	
	private static Vector3i absPosTmp = new Vector3i();
	public final SegmentController segmentController;
	public final GameClientState state;
	public final List<Object> descriptionList = new ObjectArrayList<Object>();
	public final SlotAssignment shipConfiguration;
	public GUIAnchor weaponColumn;
	public GUIAnchor mainSizeColumn;
	public GUIAnchor secondaryColumn;
	public GUIAnchor sizeColumn;
	public GUIAnchor keyColumn;
	public GUIAnchor tertiaryColumn;
	private int key = -1;
	private WeaponSlotOverlayElement weaponIcon;
	private final SegmentControllerUsable rc;

	public WeaponSegmentControllerUsableElement(SegmentControllerUsable m) {
		super();
		this.state = (GameClientState) m.getSegmentController().getState();
		this.segmentController = m.getSegmentController();
		this.rc = m; 
		shipConfiguration = m.getSegmentController().getSlotAssignment();
			
		initOverlays();
	}
	@Override
	public long getUsableId(){
		return rc.getUsableId();
	}
	public String getName(){
		return rc.getWeaponRowName();
	}
	public short getIconType(){
		return rc.getWeaponRowIcon();
	}
	public String getFirstColumn(){
		return "";
	}
	public String getSecondColumn(){
		return "";
	}
	private void initOverlays() {
		float scale = 0.75f;
		float scaleSlave = 0.65f;
		weaponIcon = new WeaponSlotOverlayElement(state);
		weaponIcon.setScale(scale, scale, scale);
		weaponIcon.setType(getIconType(), getUsableId());
		weaponIcon.setSpriteSubIndex(ElementKeyMap.getInfo(getIconType()).getBuildIconNum());

		GUITextOverlay nameOverlay = new GUITextOverlay(state);
		nameOverlay.setTextSimple(getName());

		GUITextOverlay keyTextOverlay = new GUITextOverlay(FontSize.MEDIUM_19, state);
		int key;
		if ((key = shipConfiguration.getByIndex(getUsableId())) >= 0) {
			this.key = key;
			keyTextOverlay.setTextSimple(((key + 1) % 10) + " [" + (((key) / 10) + 1) + "]");
		} else {
			this.key = -1;
			keyTextOverlay.setTextSimple("");
		}
		GUITextOverlay mainSizeText = new GUITextOverlay(FontSize.SMALL_14, state);
		mainSizeText.setTextSimple(new Object(){
			@Override
			public String toString() {
				return getFirstColumn();
			}
			
		});


		GUITextOverlay totalSizeText = new GUITextOverlay(FontSize.MEDIUM_18, state);
		totalSizeText.setTextSimple(new Object(){
			@Override
			public String toString() {
				return getSecondColumn();
			}
			
		});

		this.keyColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		keyTextOverlay.setPos(2, 8, 0);
		this.keyColumn.attach(keyTextOverlay);

		mainSizeColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		mainSizeText.setPos(4, 8, 0);
		mainSizeColumn.attach(mainSizeText);

		this.weaponColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		weaponColumn.attach(weaponIcon);
		nameOverlay.setPos(16, 25, 0);
		weaponColumn.attach(nameOverlay);

		this.sizeColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		sizeColumn.attach(totalSizeText);

		this.secondaryColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		

		this.tertiaryColumn = new GUIAnchor(state, 32, WeaponScrollableListNew.getRowHeight());
		

	}

	@Override
	public boolean isBlocked() {
		return weaponIcon.isInside();
	}

	@Override
	public WeaponDescriptionPanel getDescriptionPanel(InputState state, GUIElement dependend) {
		WeaponDescriptionPanel pa = new WeaponDescriptionPanel(state, FontSize.SMALL_14, dependend);

		return pa;
	}


	public void update(ElementCollectionManager<?, ?, ?> selectedManager, List<Object> text) {
		if (selectedManager.getContainer().getSegmentController() != ((GameClientState) selectedManager.getSegmentController().getState()).getCurrentPlayerObject()) {
			//do not update panel for other controllers
			return;
		}
		

	}

	public void update(String selectedManager, List<Object> text) {
			StringBuffer b = new StringBuffer();
			b.append(Lng.str("Type: 		")+getName()+"\n"
			);
			text.add(b.toString());
	}
	@Override
	public int getTotalSize(){
		return 1;
	}
	@Override
	public int compareTo(WeaponRowElementInterface s) {
		return (s.getTotalSize()) - getTotalSize();
	}

	@Override
	public GUIAnchor getWeaponColumn() {
		return weaponColumn;
	}


	@Override
	public GUIAnchor getMainSizeColumn() {
		return mainSizeColumn;
	}


	@Override
	public GUIAnchor getSecondaryColumn() {
		return secondaryColumn;
	}


	@Override
	public GUIAnchor getSizeColumn() {
		return sizeColumn;
	}


	@Override
	public GUIAnchor getKeyColumn() {
		return keyColumn;
	}


	@Override
	public GUIAnchor getTertiaryColumn() {
		return tertiaryColumn;
	}


	@Override
	public List<Object> getDescriptionList() {
		return descriptionList;
	}

	@Override
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	@Override
	public int getMaxCharges() {
		return rc.getMaxCharges();
	}
	@Override
	public int getCurrentCharges() {
		return rc.getCharges();
	}
}
