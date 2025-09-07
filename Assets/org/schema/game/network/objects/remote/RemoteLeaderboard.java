package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.gamemode.battle.BattleMode;
import org.schema.game.common.data.gamemode.battle.KillerEntity;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RemoteLeaderboard extends RemoteField<Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>>> {
	public RemoteLeaderboard(Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteLeaderboard(Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		set(new Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>>());
		BattleMode.deserializeLeaderboard(stream, get());
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		BattleMode.serializeLeaderboard(buffer, get());

		return 1;
	}

}
