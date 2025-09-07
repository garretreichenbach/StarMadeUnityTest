package org.schema.game.client.data.gamemap.entry;

import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.effects.Indication;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;

public interface MapEntryInterface extends PositionableSubColorSprite, SelectableMapEntry {

	void drawPoint(boolean colored, int filter, Vector3i positionVec);

	void encodeEntryImpl(DataOutputStream buffer) throws IOException;

	Indication getIndication(Vector3i system);

	int getType();

	void setType(byte type);

	boolean include(int filter, Vector3i tmpIntVec);

}
