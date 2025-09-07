package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public abstract class BlockMetaDataDummy {
	public long pos;
	protected abstract void fromTagStructrePriv(Tag tag, int shiftPoses);

	public void fromTagStructure(Tag tag, int shiftPoses) {
		Tag[] s = (Tag[]) tag.getValue();
		if(s[0].getType() == Type.VECTOR3i){
			Vector3i p = (Vector3i) s[0].getValue();
			p.add(shiftPoses, shiftPoses, shiftPoses);
			pos = ElementCollection.getIndex(p);
		}else{
			pos = s[0].getLong();
			if(!PlayerUsableInterface.ICONS.containsKey(pos)){
				int x = ElementCollection.getPosX(pos)+shiftPoses;
				int y = ElementCollection.getPosY(pos)+shiftPoses;
				int z = ElementCollection.getPosZ(pos)+shiftPoses;
				pos = ElementCollection.getIndex(x, y, z);
			}
		}
		
		fromTagStructrePriv(s[1], shiftPoses);
	}

	public long getControllerPos() {
		return pos;
	}
	
	
	public abstract String getTagName();
	
	public Tag toTagStructure(){
		return new Tag(Type.STRUCT, getTagName(), new Tag[]{
			new Tag(Type.LONG, null, pos), 
			toTagStructurePriv(),
			FinishTag.INST,
			
		});
	}

	protected abstract Tag toTagStructurePriv();
}
