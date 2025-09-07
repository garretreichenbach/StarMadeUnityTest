package org.schema.game.common.data.world;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FactionBuildRight implements TagSerializable, SerializationInterface {

	private byte rank;
	private ObjectOpenHashSet<PlayerState> deniedCache = new ObjectOpenHashSet<PlayerState>();

	;

	public FactionBuildRight() {
	}

	public FactionBuildRight(int fid, byte rank) {
		super();
		this.rank = rank;
	}

	public boolean allowedToEditServer(PlayerState player, Vector3i pos) {
		if (deniedCache.contains(player)) {
			return false;
		}

		GameServerState state = (GameServerState) player.getState();
		Faction f;
		FactionPermission fp;
		if (player.getFactionId() != 0 && (f = state.getFactionManager().getFaction(player.getFactionId())) != null && (fp = f.getMembersUID().get(player.getName())) != null) {
			boolean allowed = fp.role >= rank;
			if (!allowed) {
				deniedCache.add(player);
			}
			return allowed;
		}

		return true;
	}


	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		rank = (Byte) t[0].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, rank),
		});
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeByte(rank);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {
		this.rank = b.readByte();
	}
}
