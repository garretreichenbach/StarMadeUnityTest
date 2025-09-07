package org.schema.game.client.data.gamemap.entry;

import api.common.GameClient;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.schine.graphicsengine.forms.Sprite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GasPlanetEntityMapEntry extends TransformableEntityMapEntry {

    public SectorInformation.GasPlanetType planetType;

    /* (non-Javadoc)
     * @see org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry#decodeEntryImpl(java.io.DataInputStream)
     */
    @Override
    protected void decodeEntryImpl(DataInputStream stream) throws IOException {
        super.decodeEntryImpl(stream);
        planetType = SectorInformation.GasPlanetType.values()[stream.readByte()];
    }

    @Override
    public boolean canDraw() {
        if(GameClient.getClientState().getController().getClientGameData().hasWaypointAt(sector)) return false; //Prioritize Custom Waypoints
        return super.canDraw();
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
     * @see org.schema.game.client.data.gamemap.entry.TransformableEntityMapEntry#getSubSprite()
     */
    @Override
    public int getSubSprite(Sprite sprite) {
        return switch(planetType) {
            case FROZEN -> 27;
            case WINDY -> 26;
            case HOT -> 24;
            case MISTY -> 25;
            case ENERGETIC -> 28;
        };
    }
}
