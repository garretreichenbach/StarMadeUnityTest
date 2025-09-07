package org.schema.game.common.data.player.inventory;

import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.forms.PositionableSubSprite;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class FreeItem implements PositionableSubSprite {

	public static final short CREDITS_TYPE = -2;
	private static int LIFE_TIME = ServerConfig.FLOATING_ITEM_LIFETIME_SECS.getInt() * 1000;
	private static int PHYSICS_CHECKS = 10;
	private static int PHYSICS_CHECKS_PER_LIFETIME = (LIFE_TIME / PHYSICS_CHECKS);
	private static Vector3i tmpAbsPos = new Vector3i();
	private static Vector3f tmpInvPos = new Vector3f();
	private static Vector3i tmpAbsPosTest = new Vector3i();
	private short type;
	private int count;
	private Vector3f position;
	private long timeSpawned = -1;
	private int id;
	private int physicsFlag = -1;
	private int physicsNextFlag = 0;
	private int metaId = -1;
	private Short metaSubId;

	public FreeItem() {
		super();
		timeSpawned = System.currentTimeMillis();
	}

	public FreeItem(int id, short type, int count, int metaId, Vector3f position) {
		this();
		set(id, type, count, metaId, position);
	}

	public boolean checkFlagPhysics() {
		boolean flagged = physicsFlag != physicsNextFlag;
		this.physicsFlag = physicsNextFlag;
		return flagged;
	}

	public void checkMeta(GameClientState state) {
		state.getMetaObjectManager().checkAvailable(metaId, state);
	}

	public boolean doPhysicsTest(Sector serverSector) throws IOException {
		synchronized (serverSector.getRemoteSector().getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : serverSector.getRemoteSector().getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof SegmentController) {

					SegmentController c = ((SegmentController) s);

					if (c.getSectorId() != serverSector.getId()) {
						continue;
					}

					tmpInvPos.set(position);
					c.getWorldTransformInverse().transform(tmpInvPos);

					if (c.getSegmentBuffer().getBoundingBox().isInside(tmpInvPos)) {

						SegmentPiece pointUnsave = null;
						int x = FastMath.round(tmpInvPos.x) + SegmentData.SEG_HALF;
						int y = FastMath.round(tmpInvPos.y) + SegmentData.SEG_HALF;
						int z = FastMath.round(tmpInvPos.z) + SegmentData.SEG_HALF;
						tmpAbsPos.set(x, y, z);

						pointUnsave = c.getSegmentBuffer().getPointUnsave(tmpAbsPos);

						System.err.println(c + " POINT INSIDE " + this + ":    " + position + " -trans> " + tmpAbsPos + ": " + pointUnsave);

						if (pointUnsave != null && pointUnsave.getType() != Element.TYPE_NONE) {

							//collision

							tmpAbsPosTest.set(tmpAbsPos);

							int i;
							boolean found = false;

							/*
							 * check in each direction, if there is a free place to go
							 */
							for (i = 1; i < 8; i++) {

								tmpAbsPos.set(tmpAbsPosTest);
								tmpAbsPos.y -= i;
								pointUnsave = c.getSegmentBuffer().getPointUnsave(tmpAbsPos);
								if (pointUnsave == null || pointUnsave.getType() == Element.TYPE_NONE) {
									break;
								}

								tmpAbsPos.set(tmpAbsPosTest);
								tmpAbsPos.x += i;
								pointUnsave = c.getSegmentBuffer().getPointUnsave(tmpAbsPos);
								if (pointUnsave == null || pointUnsave.getType() == Element.TYPE_NONE) {
									break;
								}

								tmpAbsPos.set(tmpAbsPosTest);
								tmpAbsPos.x -= i;
								pointUnsave = c.getSegmentBuffer().getPointUnsave(tmpAbsPos);
								if (pointUnsave == null || pointUnsave.getType() == Element.TYPE_NONE) {
									break;
								}

								tmpAbsPos.set(tmpAbsPosTest);
								tmpAbsPos.z += i;
								pointUnsave = c.getSegmentBuffer().getPointUnsave(tmpAbsPos);
								if (pointUnsave == null || pointUnsave.getType() == Element.TYPE_NONE) {
									break;
								}

								tmpAbsPos.set(tmpAbsPosTest);
								tmpAbsPos.z -= i;
								pointUnsave = c.getSegmentBuffer().getPointUnsave(tmpAbsPos);
								if (pointUnsave == null || pointUnsave.getType() == Element.TYPE_NONE) {
									break;
								}

								//do up last, since it will be the default direction
								//the items traveles if its deep inside a bigger structure
								tmpAbsPos.set(tmpAbsPosTest);
								tmpAbsPos.y += i;
								pointUnsave = c.getSegmentBuffer().getPointUnsave(tmpAbsPos);
								if (pointUnsave == null || pointUnsave.getType() == Element.TYPE_NONE) {
									break;
								}
							}

							tmpAbsPos.sub(tmpAbsPosTest); //get the difference we traveled

							position.x += tmpAbsPos.x; //add that distance
							position.y += tmpAbsPos.y; //add that distance
							position.z += tmpAbsPos.z; //add that distance

							position.x = FastMath.fastFloor(position.x) + 0.5f;
							position.y = FastMath.fastFloor(position.y) + 0.5f;
							position.z = FastMath.fastFloor(position.z) + 0.5f;

							System.err.println("[ITEM][COLLISION] warping item out of collision " + position);
							return true;
						}

					}

				}
			}
		}
		return false;
	}

	public void fromTagStructure(Tag tag, GameServerState state) {
		Tag[] t = (Tag[]) tag.getValue();
		if (t[0].getType() == Type.BYTE) {
			try {
				//metaObject
				position = (Vector3f) t[1].getValue();
				type = (Short) t[2].getValue();
				count = (Integer) t[3].getValue();
				metaSubId = (Short) t[5].getValue();

				MetaObject o = MetaObjectManager.instantiate(type, metaSubId, true); //asigns new id
				o.fromTag(t[4]);

				this.metaId = o.getId();
				System.err.println("[FREEITEM] LOADING META OBJECT " + o + "; " + this.metaId);
				if (state != null) {
					state.getMetaObjectManager().putServer(o);
				}
			} catch (InvalidMetaItemException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("[FREEITEM] LOADING NORMAL BLOCK ITEM");
			position = (Vector3f) t[0].getValue();
			type = (Short) t[1].getValue();
			count = (Integer) t[2].getValue();
		}
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getLifeTime(long time) {
		return time - timeSpawned;
	}

	/**
	 * @return the metaId
	 */
	public int getMetaId() {
		return metaId;
	}

	/**
	 * @param metaId the metaId to set
	 */
	public void setMetaId(int metaId) {
		this.metaId = metaId;
	}

	/**
	 * @return the position
	 */
	@Override
	public Vector3f getPos() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPos(Vector3f position) {
		this.position = position;
	}

	@Override
	public float getScale(long time) {
		long t = (time - timeSpawned);
		return 0.001f + 0.009f * (1.0f - ((float) t / (float) (LIFE_TIME)));
	}

	@Override
	public int getSubSprite(Sprite sprite) {

		if (type == CREDITS_TYPE) {
			if (sprite.getName().startsWith(UIScale.getUIScale().getGuiPath()+"build-icons-extra")) {
				return 0;
			} else {
				return -1;
			}
		} else if (type < 0) {
			if (sprite.getName().startsWith("meta-icons")) {
				return Math.abs(type);
			} else {
				return -1;
			}
		} else if (!ElementKeyMap.exists(type)) {
			System.err.println("[CLIENT] WARNING: TRIED TO DRAW FREE ITEM, BUT type == " + type);
			return -1;
		} else {
			int n = ElementKeyMap.getInfo(type).getBuildIconNum() / 256;
			if (sprite.getName().startsWith(UIScale.getUIScale().getGuiPath()+"build-icons-" + StringTools.formatTwoZero(n))) {
				return ElementKeyMap.getInfo(type).getBuildIconNum() % 256;
			} else {
				return -1; //do not draw
			}
		}

	}

	@Override
	public boolean canDraw() {
		return true;
	}

	/**
	 * @return the timeSpawned
	 */
	public long getTimeSpawned() {
		return timeSpawned;
	}

	/**
	 * @param timeSpawned the timeSpawned to set
	 */
	public void setTimeSpawned(long timeSpawned) {
		this.timeSpawned = timeSpawned;
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(short type) {
		this.type = type;
	}

	public boolean isAlive(long time) {
		long lt = getLifeTime(time);
		physicsNextFlag = (int) (lt / PHYSICS_CHECKS_PER_LIFETIME);
		return lt < LIFE_TIME;
	}

	public void set(int id, short type, int count, int metaId, Vector3f position) {
		this.id = id;
		this.type = type;
		this.count = count;
		this.position = position;
		this.metaId = metaId;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(ITEM[" + id + "]: type " + type + "; #" + count + "; " + position + ")";
	}

	public Tag toTagStructure(GameServerState state) {

		if (type >= 0) {
			System.err.println("[FREEITEM] WRITING NORMAL BLOCK ITEM");
			Tag p = new Tag(Type.VECTOR3f, null, position);
			Tag t = new Tag(Type.SHORT, null, type);
			Tag c = new Tag(Type.INT, null, count);
			return new Tag(Type.STRUCT, null, new Tag[]{p, t, c, FinishTag.INST});
		} else {
			MetaObject object = state.getMetaObjectManager().getObject(metaId);
			System.err.println("[FREEITEM] WRITING META OBJECT " + object);
			if (object == null) {
				System.err.println("Exception: Could not write metaItem " + metaId + " -> not found");
				//type 0 wont be added when laoded again
				Tag p = new Tag(Type.VECTOR3f, null, position);
				Tag t = new Tag(Type.SHORT, null, 0);
				Tag c = new Tag(Type.INT, null, 0);
				return new Tag(Type.STRUCT, null, new Tag[]{p, t, c, FinishTag.INST});
			} else {
				Tag[] str = new Tag[7];
				str[0] = new Tag(Type.BYTE, null, 1);
				str[1] = new Tag(Type.VECTOR3f, null, position);
				str[2] = new Tag(Type.SHORT, null, object.getObjectBlockID());
				str[3] = new Tag(Type.INT, null, count);
				str[4] = object.getBytesTag();
				str[5] = new Tag(Type.SHORT, null, object.getSubObjectId());
				str[6] = FinishTag.INST;

				return new Tag(Type.STRUCT, null, str);
			}

		}
	}

}
