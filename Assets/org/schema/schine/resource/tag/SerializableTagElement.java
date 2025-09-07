package org.schema.schine.resource.tag;

import java.io.DataOutput;
import java.io.IOException;

public interface SerializableTagElement {

	public static final byte CONTROL_ELEMENT_MAPPER = 0;
	public static final byte ELEMENT_COUNT_MAP = 1;
	public static final byte NPC_FACTION_NEWS_EVENT = 2;
	public static final byte LONG_SET = 3;
	public static final byte BLOCK_BUFFER = 4;
	public static final byte LONG_2_VECTOR3f_MAP = 5;
	public static final byte LONG_2_TRANSFORM_MAP = 6;
	
	public byte getFactoryId();

	public void writeToTag(DataOutput dos) throws IOException;

}
