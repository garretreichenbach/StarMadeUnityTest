package org.schema.game.client.data.gamemap.entry;

import api.common.GameClient;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.map.MapControllerManager;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationFilter;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.SelectableSprite;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TransformableEntityMapEntry extends AbstractMapEntry implements SelectableSprite {
	public static boolean selectDrawMode;
	public Vector3f pos = new Vector3f();
	public byte type;
	public String name;
	private boolean drawIndication = true;
	protected Indication indication;
	private Vector4f color = new Vector4f(1, 1, 1, 1);
	private float selectDepth;
	public Vector3i sector = new Vector3i();
	private SimpleTransformableSendableObject.EntityType entityType;

	public TransformableEntityMapEntry() {
	}

	@Override
	protected void decodeEntryImpl(DataInputStream stream) throws IOException {
		pos = new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());
		name = stream.readUTF();
		sector = new Vector3i(stream.readInt(), stream.readInt(), stream.readInt());
	}

	@Override
	public void drawPoint(boolean colored, int filter, Vector3i selectedSector) {
		if(colored) {
			float alpha = 1f;
			if(!include(filter, selectedSector)) {
				alpha = 0.1f;
			}
			if(entityType == SimpleTransformableSendableObject.EntityType.PLANET_SEGMENT || entityType == SimpleTransformableSendableObject.EntityType.PLANET_CORE || entityType == SimpleTransformableSendableObject.EntityType.PLANET_ICO) {
				GlUtil.glColor4f(1, 1, 0, alpha);
			}
			if(entityType == SimpleTransformableSendableObject.EntityType.SHOP) {
				GlUtil.glColor4f(1, 0, 1, alpha);
			}
			if(entityType == SimpleTransformableSendableObject.EntityType.SPACE_STATION) {
				GlUtil.glColor4f(0.3f, 0.6f, 1, alpha);
			}
			if(entityType == SimpleTransformableSendableObject.EntityType.SHIP) {
				GlUtil.glColor4f(0.9f, 0.1f, 0.1f, alpha);
			}
		}
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex3f(pos.x, pos.y, pos.z);
		GL11.glEnd();
	}

	@Override
	public void encodeEntryImpl(DataOutputStream stream) throws IOException {
		stream.writeFloat(pos.x);
		stream.writeFloat(pos.y);
		stream.writeFloat(pos.z);
		stream.writeUTF(name);
		stream.writeInt(sector.x);
		stream.writeInt(sector.y);
		stream.writeInt(sector.z);
	}

	@Override
	public Indication getIndication(Vector3i system) {
		if(indication == null) {
			Transform t = new Transform();
			t.setIdentity();
			t.origin.set(system.x * 100 + pos.x - GameMapDrawer.halfsize, system.y * 100 + pos.y - GameMapDrawer.halfsize, system.z * 100 + pos.z - GameMapDrawer.halfsize);
			indication = new ConstantIndication(t, name + "\n(Double Click for options)");
			return indication;
		} else {
			indication.getCurrentTransform().origin.set(system.x * 100 + pos.x - GameMapDrawer.halfsize, system.y * 100 + pos.y - GameMapDrawer.halfsize, system.z * 100 + pos.z - GameMapDrawer.halfsize);
			return indication;
		}
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void setType(byte type) {
		this.type = type;
		this.entityType = SimpleTransformableSendableObject.EntityType.values()[type];
	}

	@Override
	public boolean include(int filter, Vector3i selectedSector) {
		int x = (int) ((pos.x / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
		int y = (int) ((pos.y / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
		int z = (int) ((pos.z / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
		if((filter & GameMapDrawer.FILTER_X) == GameMapDrawer.FILTER_X) {
			if(x < selectedSector.x || x > selectedSector.x + 1) {
				return false;
			}
		}
		if((filter & GameMapDrawer.FILTER_Y) == GameMapDrawer.FILTER_Y) {
			if(y < selectedSector.y || y > selectedSector.y + 1) {
				return false;
			}
		}
		if((filter & GameMapDrawer.FILTER_Z) == GameMapDrawer.FILTER_Z) {
			if(z < selectedSector.z || z > selectedSector.z + 1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Vector4f getColor() {
		int x = (int) ((pos.x / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
		int y = (int) ((pos.y / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
		int z = (int) ((pos.z / GameMapDrawer.size) * VoidSystem.SYSTEM_SIZEf);
		if(GameMapDrawer.highlightIcon != null) {
			//			System.err.println("HIGHLIGHT ::: "+GameMapDrawer.highlightIcon+" ::: "+x+", "+y+", "+z+" (("+pos+"))");
			if(ByteUtil.modU16(GameMapDrawer.highlightIcon.x) == x && ByteUtil.modU16(GameMapDrawer.highlightIcon.y) == y && ByteUtil.modU16(GameMapDrawer.highlightIcon.z) == z) {
				color.set(1, 0, 0, 1);
				return color;
			}
		}
		color.w = 1f;
		if((GameMapDrawer.filterAxis & GameMapDrawer.FILTER_X) == GameMapDrawer.FILTER_X) {
			if(x < GameMapDrawer.positionVec.x || x > GameMapDrawer.positionVec.x + 1) {
				color.w = 0.1f;
			}
		}
		if((GameMapDrawer.filterAxis & GameMapDrawer.FILTER_Y) == GameMapDrawer.FILTER_Y) {
			if(y < GameMapDrawer.positionVec.y || y > GameMapDrawer.positionVec.y + 1) {
				color.w = 0.1f;
			}
		}
		if((GameMapDrawer.filterAxis & GameMapDrawer.FILTER_Z) == GameMapDrawer.FILTER_Z) {
			if(z < GameMapDrawer.positionVec.z || z > GameMapDrawer.positionVec.z + 1) {
				color.w = 0.1f;
			}
		}
		return color;
	}

	@Override
	public Vector3f getPos() {
		return pos;
	}

	@Override
	public float getScale(long time) {
		return 0.1f;
	}

	@Override
	public int getSubSprite(Sprite sprite) {
		return SimpleTransformableSendableObject.EntityType.values()[type].mapSprite;
	}

	@Override
	public boolean canDraw() {
		if(GameClient.getClientState().getController().getClientGameData().hasWaypointAt(sector)) return false; //Prioritize Custom Waypoints

		if(entityType == SimpleTransformableSendableObject.EntityType.GAS_PLANET || entityType == SimpleTransformableSendableObject.EntityType.PLANET_SEGMENT || entityType == SimpleTransformableSendableObject.EntityType.PLANET_CORE || entityType == SimpleTransformableSendableObject.EntityType.PLANET_ICO) {
			return GameMapDrawer.filter.isFiltered(NavigationFilter.POW_PLANET) && GameMapDrawer.filter.isFiltered(NavigationFilter.POW_PLANET_CORE);
		}
		if(entityType == SimpleTransformableSendableObject.EntityType.SHOP) {
			return GameMapDrawer.filter.isFiltered(NavigationFilter.POW_SHOP);
		}
		if(entityType == SimpleTransformableSendableObject.EntityType.SPACE_STATION) {
			return GameMapDrawer.filter.isFiltered(NavigationFilter.POW_SPACESTATION);
		}
		if(entityType == SimpleTransformableSendableObject.EntityType.SHIP) {
			return GameMapDrawer.filter.isFiltered(NavigationFilter.POW_SHIP);
		}
		return false;
	}

	@Override
	public float getSelectionDepth() {
		return selectDepth;
	}

	@Override
	public boolean isSelectable() {
		if((GameMapDrawer.filterAxis & GameMapDrawer.FILTER_X) == GameMapDrawer.FILTER_X) {
			if((pos.x / 100f) * VoidSystem.SYSTEM_SIZEf < GameMapDrawer.positionVec.x || (pos.x / 100f) * VoidSystem.SYSTEM_SIZEf > GameMapDrawer.positionVec.x + 1) {
				return false;
			}
		}
		if((GameMapDrawer.filterAxis & GameMapDrawer.FILTER_Y) == GameMapDrawer.FILTER_Y) {
			if((pos.y / 100f) * VoidSystem.SYSTEM_SIZEf < GameMapDrawer.positionVec.y || (pos.y / 100f) * VoidSystem.SYSTEM_SIZEf > GameMapDrawer.positionVec.y + 1) {
				return false;
			}
		}
		if((GameMapDrawer.filterAxis & GameMapDrawer.FILTER_Z) == GameMapDrawer.FILTER_Z) {
			if((pos.z / 100f) * VoidSystem.SYSTEM_SIZEf < GameMapDrawer.positionVec.z || (pos.z / 100f) * VoidSystem.SYSTEM_SIZEf > GameMapDrawer.positionVec.z + 1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onSelect(float depth) {
		drawIndication = true;
		this.selectDepth = depth;
		MapControllerManager.selected.add(this);
	}

	@Override
	public void onUnSelect() {
		drawIndication = false;
		MapControllerManager.selected.remove(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return pos.hashCode() + type + name.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TransformableEntityMapEntry) {
			TransformableEntityMapEntry o = (TransformableEntityMapEntry) obj;
			return o.pos.equals(pos) && o.type == type && o.name.equals(name);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TEME[pos=" + pos + ", type=" + type + ", name=" + name + "]";
	}

	/**
	 * @return the drawIndication
	 */
	@Override
	public boolean isDrawIndication() {
		return drawIndication;
	}

	/**
	 * @param drawIndication the drawIndication to set
	 */
	@Override
	public void setDrawIndication(boolean drawIndication) {
		this.drawIndication = drawIndication;
	}
}
