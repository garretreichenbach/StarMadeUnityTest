package org.schema.game.common.data.creature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.client.view.character.BoneLocationInterface;
import org.schema.schine.graphicsengine.animation.AnimationChannel;
import org.schema.schine.graphicsengine.animation.AnimationController;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.CreatureStructure.PartType;
import org.schema.schine.resource.ResourceLoadEntryMesh;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CreaturePartNode implements TagSerializable, BoneLocationInterface {

	public final PartType type;
	private final ObjectArrayList<CreaturePartNode> chields = new ObjectArrayList<CreaturePartNode>();
	public String meshName;
	public byte attachedTo = -1;
	public String texture;
	private CreaturePartNode parent;
	private byte attachedFromType = -1;
	private String mainBone;
	private String torsoBone;
	private String heldBone;
	public CreaturePartNode(PartType type) {
		this.type = type;
	}

	public CreaturePartNode(PartType type, StateInterface state, String mesh, final String textureName) {
		this.type = type;
		this.meshName = mesh;
		ResourceLoadEntryMesh r = state.getResourceMap().getMesh(mesh);
		assert (r != null) : mesh;
		if (r.creature != null) {
			this.attachedFromType = (byte) AttachmentType.MAIN.ordinal();
		} else {
			System.err.println("[CreaturePart] no creature info for: " + mesh);
		}
		this.texture = textureName;

		assert (r.creature.mainBone != null);

		this.mainBone = r.creature.mainBone;
		this.torsoBone = r.creature.upperBody;
		this.heldBone = r.creature.heldBone;
	}

	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeUTF(meshName);
		buffer.writeUTF(texture != null ? texture : "");
		buffer.writeByte(attachedTo);
		buffer.writeByte(chields.size());
		for (int i = 0; i < chields.size(); i++) {
			chields.get(i).serialize(buffer);
		}
	}

	public void deserialize(DataInput stream) throws IOException {
		meshName = stream.readUTF();
		texture = stream.readUTF();
		texture = texture != null ? texture : null;
		attachedTo = stream.readByte();

		int c = stream.readByte();
		for (int i = 0; i < c; i++) {
			CreaturePartNode cc = new CreaturePartNode(type == PartType.BOTTOM ? PartType.MIDDLE : PartType.TOP);
			cc.deserialize(stream);
			chields.add(cc);
		}
	}

	public boolean isRoot() {
		return parent == null;
	}

	public void attach(StateInterface state, CreaturePartNode n, AttachmentType m) {
		n.attachedTo = (byte) m.ordinal();
		n.parent = this;
		chields.add(n);
	}

	public void attach(StateInterface state, CreaturePartNode n) {
		if (attachedFromType >= 0) {
			attach(state, n, AttachmentType.values()[attachedFromType]);
		} else {
			throw new NullPointerException();
		}
	}

	public void detach(CreaturePartNode n) {
		n.parent = null;
		n.attachedTo = -1;
		chields.remove(n);
	}

	/**
	 * @return the chields
	 */
	public ObjectArrayList<CreaturePartNode> getChields() {
		return chields;
	}

	/**
	 * @return the parent
	 */
	public CreaturePartNode getParent() {
		return parent;
	}


	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] p = (Tag[]) tag.getValue();
		meshName = (String) p[0].getValue();
		texture = (String) p[1].getValue();
		attachedTo = (Byte) p[2].getValue();

		texture = texture != null ? texture : null;

		Tag[] t = (Tag[]) p[3].getValue();

		for (int i = 0; i < t.length - 1; i++) {
			CreaturePartNode c = new CreaturePartNode(type == PartType.BOTTOM ? PartType.MIDDLE : PartType.TOP);
			c.fromTagStructure(t[i]);
			chields.add(c);
		}
	}

	@Override
	public Tag toTagStructure() {

		Tag meshNameTag = new Tag(Type.STRING, null, meshName);
		Tag texTag = new Tag(Type.STRING, null, texture != null ? texture : "");
		Tag attTag = new Tag(Type.BYTE, null, attachedTo);

		Tag[] t = new Tag[chields.size() + 1];
		t[t.length - 1] = FinishTag.INST;
		for (int i = 0; i < chields.size(); i++) {
			t[i] = chields.get(i).toTagStructure();
		}

		return new Tag(Type.STRUCT, null, new Tag[]{meshNameTag, texTag, attTag, new Tag(Type.STRUCT, null, t), FinishTag.INST});
	}

	@Override
	public String getRootBoneName() {

		return mainBone;
	}

	@Override
	public String getRootTorsoBoneName() {
		return torsoBone;
	}

	@Override
	public void initializeListeners(AnimationController controller,
	                                AnimationChannel channel, AnimationChannel channelTorso) {

	}

	@Override
	public String getHeldBoneName() {
		return heldBone;
	}

	@Override
	public void loadClientBones(StateInterface state) {
		ResourceLoadEntryMesh r = state.getResourceMap().getMesh(meshName);
		assert (r != null) : meshName;
		if (r.creature != null) {
			this.attachedFromType = (byte) AttachmentType.MAIN.ordinal();
		} else {
			System.err.println("[CreaturePart] no creature info for: " + meshName);
		}
		this.mainBone = r.creature.mainBone;
		this.torsoBone = r.creature.upperBody;
		this.heldBone = r.creature.heldBone;
	}

	public enum AttachmentType {
		MAIN,
		WEAPON,
	}

}
