package org.schema.game.client.data.gamemap.entry;

import api.common.GameClient;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.schine.graphicsengine.forms.Sprite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlanetEntityMapEntry extends TransformableEntityMapEntry {

	public PlanetType planetType;

	@Override
	public boolean canDraw() {
		if(GameClient.getClientState().getController().getClientGameData().hasWaypointAt(sector)) return false; //Prioritize Custom Waypoints
		return super.canDraw();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry#decodeEntryImpl(java.io.DataInputStream)
	 */
	@Override
	protected void decodeEntryImpl(DataInputStream stream) throws IOException {
		super.decodeEntryImpl(stream);
		planetType = PlanetType.values()[stream.readByte()];
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry#encodeEntryImpl(java.io.DataOutputStream)
	 */
	@Override
	public void encodeEntryImpl(DataOutputStream stream) throws IOException {
		super.encodeEntryImpl(stream);
		stream.writeByte(planetType.ordinal());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.data.gamemapn.entry.TransformableEntityMapEntry#getSubSprite()
	 */
	@Override
	public int getSubSprite(Sprite sprite) {
		return switch(planetType) {
			case DESERT -> 1;
			case EARTH -> 2;
			case FROZEN -> 0;
			case MESA -> 4;
			case CORRUPTED -> 3;
			case VOLCANIC -> 5;
			case BARREN -> 6;
		};
	}
}
