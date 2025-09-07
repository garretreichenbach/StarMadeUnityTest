package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector3f;

import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class SaveSizeAndPosition implements TagSerializable {
	private final Vector3f posS = new Vector3f();
	public String id;
	public boolean newPanel;
	private int heightS;
	private int widthS;
	public boolean expanded = false;
	public boolean hidden;

	public SaveSizeAndPosition() {
	}

	public SaveSizeAndPosition(String id) {
		this.id = id;
		this.newPanel = true;
	}

	public void applyTo(GUIResizableGrabbableWindow w) {
		w.setWidth(widthS);
		w.setHeight(heightS);
		w.getPos().set(posS);
		if(w instanceof GUIExpandableWindow){
			((GUIExpandableWindow)w).expanded = expanded;
		}
	}

	public void setFrom(float width, float height, Vector3f pos, boolean expanded) {
		this.widthS = (int) width;
		this.heightS = (int) height;
		this.posS.set(pos);
		this.expanded = expanded;
	}


	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();

		this.widthS = t[0].getInt();
		this.heightS = t[1].getInt();
		this.posS.set(t[2].getVector3f());
		this.id = t[3].getString();
		this.expanded = t.length > 4 && t[4].getType() == Type.BYTE && t[4].getByte() != 0;
		this.hidden = t.length > 5 && t[5].getType() == Type.BYTE && t[5].getByte() != 0;
	}

	@Override
	public Tag toTagStructure() {

		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.INT, null, widthS),
				new Tag(Type.INT, null, heightS),
				new Tag(Type.VECTOR3f, null, posS),
				new Tag(Type.STRING, null, id),
				new Tag(Type.BYTE, null, expanded ? (byte) 1 : (byte)0),
				new Tag(Type.BYTE, null, hidden ? (byte) 1 : (byte)0),
				FinishTag.INST});
	}
}
