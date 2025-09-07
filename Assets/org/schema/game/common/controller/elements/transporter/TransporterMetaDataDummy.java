package org.schema.game.common.controller.elements.transporter;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class TransporterMetaDataDummy extends BlockMetaDataDummy {

	public String destinationUID;
	public Vector3i destinationBlock;
	public String name;
	public byte publicAccess;

	@Override
	protected void fromTagStructrePriv(Tag tag, int shiftPoses) {
		Tag[] v = ((Tag[]) tag.getValue());
		name = (String) v[0].getValue();
		destinationUID = (String) v[1].getValue();
		destinationBlock = (Vector3i) v[2].getValue();
		destinationBlock.add(shiftPoses, shiftPoses, shiftPoses);
		publicAccess = (Byte) v[3].getValue() ;
	}

	@Override
	public String getTagName() {
		return TransporterElementManager.TAG_ID;
	}

	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.STRING, null, name),
				new Tag(Type.STRING, null, destinationUID),
				new Tag(Type.VECTOR3i, null, destinationBlock),
				new Tag(Type.BYTE, null, publicAccess),
				FinishTag.INST,
				
			});
	}

}
