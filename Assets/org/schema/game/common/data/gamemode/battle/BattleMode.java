package org.schema.game.common.data.gamemode.battle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.FactionChange;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.gamemode.AbstractGameMode;
import org.schema.game.common.data.gamemode.GameModeException;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.RemoteLeaderboard;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

public class BattleMode extends AbstractGameMode {

	private static final byte LEADERBOARD_VERSION = 0;
	public ObjectArrayList<FactionPreset> battleFactionNames = new ObjectArrayList<FactionPreset>();
	public ObjectArrayList<FactionPreset> ffaFactionNames = new ObjectArrayList<FactionPreset>();
	public ObjectArrayList<FactionPreset> spectatorFactionNames = new ObjectArrayList<FactionPreset>();
	public ObjectArrayList<BattleSector> battleSectors = new ObjectArrayList<BattleSector>();
	public ObjectArrayList<Faction> battleFactions = new ObjectArrayList<Faction>();
	public ObjectArrayList<Faction> ffaFactions = new ObjectArrayList<Faction>();
	public ObjectArrayList<Faction> spectators = new ObjectArrayList<Faction>();
	public int countdownRound = -4242;
	public int countdownStart = -4242;
	public float maxMass = -4242;
	public int maxDim = -4242;
	public float maxMassPerFaction = -4242;
	public int idGen = -1000;
	public BattleRound round;
	public Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> leaderboard = new Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>>();
	private String battleConfig;
	private String factionConfig;
	private boolean init;

	public BattleMode(GameServerState state) {
		super(state);

	}

	public static void main(String[] asd) {
		try {
			File file = new FileExt("./.battlemode-leaderboards");
			if (!file.exists()) {
				file.createNewFile();
			}

			Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> leaderboard = new Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>>();

			for (int i = 0; i < 50; i++) {
				String name = "player_" + i;

				ObjectArrayList<KillerEntity> l = new ObjectArrayList<KillerEntity>();

				leaderboard.put(name, l);
				int m = (int) (2 + Math.random() * 40);
				for (int j = 0; j < m; j++) {
					KillerEntity e = new KillerEntity();
					e.deadPlayerName = "player_" + (int) (Math.random() * 50);
					e.shipName = Math.random() < 0.5 ? "shipA" : (Math.random() < 0.5 ? "shipB" : (Math.random() < 0.5 ? "shipC" : "shipD"));
					e.killerPlayerName = name;
					l.add(e);
				}

			}

			DataOutputStream s = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

			serializeLeaderboard(s, leaderboard);

			s.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void serializeLeaderboard(DataOutputStream s, Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> e) throws IOException {
		s.writeByte(LEADERBOARD_VERSION);
		s.writeInt(e.size());

		for (Entry<String, ObjectArrayList<KillerEntity>> a : e.entrySet()) {
			s.writeUTF(a.getKey());
			s.writeInt(a.getValue().size());
			for (int i = 0; i < a.getValue().size(); i++) {
				serializeEntry(s, a.getValue().get(i));
			}
		}
	}

	public static void deserializeLeaderboard(DataInputStream s, Object2ObjectOpenHashMap<String, ObjectArrayList<KillerEntity>> e) throws IOException {
		byte version = s.readByte();
		int entrySize = s.readInt();

		for (int j = 0; j < entrySize; j++) {
			String key = s.readUTF();
			int listsize = s.readInt();
			ObjectArrayList<KillerEntity> a = new ObjectArrayList<KillerEntity>(listsize);

			for (int i = 0; i < listsize; i++) {
				KillerEntity ed = new KillerEntity();
				ed.killerPlayerName = key;
				deserializeEntry(s, ed);
				a.add(ed);
			}

			e.put(key, a);
		}
	}

	public static void deserializeEntry(DataInputStream s, KillerEntity e) throws IOException {

		e.deadPlayerName = s.readUTF();
		e.shipName = s.readUTF();
		e.time = s.readLong();
	}

	public static void serializeEntry(DataOutputStream s, KillerEntity e) throws IOException {
		s.writeUTF(e.deadPlayerName);
		s.writeUTF(e.shipName);
		s.writeLong(e.time);
	}

	private void checkConfig() throws GameModeException {
		if (battleSectors.isEmpty()) {
			throw new GameModeException("No Battle Sectors Defined");
		}
		if (battleFactions.isEmpty() && ffaFactions.isEmpty()) {
			throw new GameModeException("Need battle faction and/or ffa factions");
		}

		if (countdownRound == -4242) {
			throw new GameModeException("No countdownRound time defined");
		}
		if (countdownStart == -4242) {
			throw new GameModeException("No countdownStart time defined");
		}
		if (maxMass == -4242) {
			throw new GameModeException("No maxMass defined");
		}
		if (maxDim == -4242) {
			throw new GameModeException("No maxDim defined");
		}
		if (maxMassPerFaction == -4242) {
			throw new GameModeException("No maxMassPerFaction defined");
		}
	}

	private void parseFactionConfig(String string) throws GameModeException {
		//[TeamA, fighters, 500,500,500];[TeamB, fighters, -500,-500,-500];[TeamFFA,ffa, 500,0,0];[Spectators,spectators, 0,500,0]

		if (!string.startsWith("[") || !string.endsWith("]")) {
			throw new GameModeException("Faction definitions have to be between brackets like [TeamB, fighters, -500,-500,-500]");
		}

		string = string.substring(1, string.length() - 1);

		String[] split = string.split(",");

		int argLen = 8;
		if (split.length != argLen) {
			throw new GameModeException("faction definition has invalid number of arguments " + split.length + " (must be " + argLen + ") (" + Arrays.toString(split) + ")");
		}
		String name = split[0].trim();
		String type = split[1].trim();
		Vector3f spawnPos = new Vector3f(Float.parseFloat(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4]));
		Vector3f color = new Vector3f(Float.parseFloat(split[5]), Float.parseFloat(split[6]), Float.parseFloat(split[7]));

		FactionPreset f = new FactionPreset();
		f.name = name.trim();
		f.spawnPos = spawnPos;
		f.color = color;
		System.err.println("[BATTLEMODE] parsed faction " + f.name + "; Desired battle spawn point is: " + f.spawnPos);

		if (type.toLowerCase(Locale.ENGLISH).equals("fighters")) {
			f.type = Faction.MODE_FIGHTERS_TEAM;
			battleFactionNames.add(f);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("ffa")) {
			f.type = Faction.MODE_FIGHTERS_FFA;
			ffaFactionNames.add(f);
		} else if (type.toLowerCase(Locale.ENGLISH).equals("spectators")) {
			f.type = Faction.MODE_SPECTATORS;
			spectatorFactionNames.add(f);
		} else {
			throw new GameModeException("Faction type unknown (must be fighters, ffa, or spectators, but was: '" + type + "'");
		}

	}

	private void parseBattleConfig(String string) {
		String[] split = string.split("=", 2);
		//battleSector=0,0,0,Test1.smsec;battleSector=15,15,15,Test2.smsec;countdownRound=300;countdownStart=30;maxMass=-1;maxDim=300;
		String varName = split[0].trim();
		String value = split[1].trim();
		if (varName.toLowerCase(Locale.ENGLISH).equals("battlesector")) {
			String[] bsparts = value.split(",", 4);
			BattleSector bs = new BattleSector();
			bs.pos.set(Integer.parseInt(bsparts[0].trim()), Integer.parseInt(bsparts[1].trim()), Integer.parseInt(bsparts[2].trim()));
			bs.sectorImport = bsparts[3].trim();
			battleSectors.add(bs);
		} else if (varName.toLowerCase(Locale.ENGLISH).equals("countdownround")) {
			countdownRound = Integer.parseInt(value);
		} else if (varName.toLowerCase(Locale.ENGLISH).equals("countdownstart")) {
			countdownStart = Integer.parseInt(value);
		} else if (varName.toLowerCase(Locale.ENGLISH).equals("maxmass")) {
			maxMass = Float.parseFloat(value);
		} else if (varName.toLowerCase(Locale.ENGLISH).equals("maxdim")) {
			maxDim = Integer.parseInt(value);
		} else if (varName.toLowerCase(Locale.ENGLISH).equals("maxmassperfaction")) {
			maxMassPerFaction = Float.parseFloat(value);
		}
	}

	public void updateFactions() {
		for (Faction f : battleFactions) {
			while (!updateFaction(f)) ;
		}
		for (Faction f : ffaFactions) {
			while (!updateFaction(f)) ;
		}
		for (Faction f : spectators) {
			while (!updateFaction(f)) ;
		}
	}

	private boolean updateFaction(Faction f) {
		for (String s : f.getMembersUID().keySet()) {
			try {
				state.getPlayerFromName(s);
			} catch (PlayerNotFountException e) {
				f.removeMember(s, state.getGameState());
				return false;
			}
		}
		return true;
	}

	public void createFactionIfNotPresent(FactionManager factionManager, ObjectArrayList<FactionPreset> c) {
		fak:
		for (int i = 0; i < c.size(); i++) {
			FactionPreset factionPreset = c.get(i);
			for (Faction f : factionManager.getFactionCollection()) {
				if (f.getName().equals(factionPreset.name)) {
					if (f.isFactionMode(Faction.MODE_FIGHTERS_FFA)) {
						ffaFactions.add(f);
					}
					if (f.isFactionMode(Faction.MODE_FIGHTERS_TEAM)) {
						battleFactions.add(f);
					}
					if (f.isFactionMode(Faction.MODE_SPECTATORS)) {
						spectators.add(f);
					}
					continue fak;
				}
			}
			factionManager.existsFaction(i);
			Faction fac = new Faction(state, idGen--, factionPreset.name, factionPreset.name);
			fac.getColor().set(factionPreset.color);
			fac.setFactionMode(factionPreset.type);

			if (fac.isFactionMode(Faction.MODE_FIGHTERS_FFA)) {
				ffaFactions.add(fac);
			}
			if (fac.isFactionMode(Faction.MODE_FIGHTERS_TEAM)) {
				battleFactions.add(fac);
			}
			if (fac.isFactionMode(Faction.MODE_SPECTATORS)) {
				spectators.add(fac);
			}
			factionManager.getFactionMap().put(fac.getIdFaction(), fac);
		}
	}

	private void addKillerEntity(KillerEntity e) {
		ObjectArrayList<KillerEntity> objectArrayList = leaderboard.get(e.killerPlayerName);

		if (objectArrayList == null) {
			objectArrayList = new ObjectArrayList();
			leaderboard.put(e.killerPlayerName, objectArrayList);
		}

		objectArrayList.add(e);

		saveLoaderboard();
	}

	private void loadLoaderboard() {
		File file = new FileExt("./.battlemode-leaderboards");

		if (file.exists()) {
			try {
				DataInputStream s = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

				deserializeLeaderboard(s, leaderboard);

				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void checkLeaderboard() {
		long back = ((long) (ServerConfig.LEADERBOARD_BACKLOG.getInt()) * 60L * 60L * 1000L);

		ObjectIterator<Entry<String, ObjectArrayList<KillerEntity>>> iterator = leaderboard.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, ObjectArrayList<KillerEntity>> next = iterator.next();
			ObjectListIterator<KillerEntity> it = next.getValue().iterator();
			while (it.hasNext()) {
				KillerEntity n = it.next();
				if (n.time < System.currentTimeMillis() - back) {
					it.remove();
				}
			}
		}
	}

	private void saveLoaderboard() {
		checkLeaderboard();
		try {
			File file = new FileExt("./.battlemode-leaderboards");
			if (!file.exists()) {
				file.createNewFile();
			}
			DataOutputStream s = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

			serializeLeaderboard(s, leaderboard);

			s.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void updateToFullNT(NetworkGameState n) {
		sendLeaderBoard(n);
		if (round != null) {
			state.getGameState().getNetworkObject().battlemodeInfo.set(round.getBattleInfo(), true);
		}
	}

	@Override
	protected void initBegin() throws GameModeException {
		try {

			loadLoaderboard();
			//battleSector=0,0,0,Test1.smsec;battleSector=15,15,15,Test2.smsec;countdownRound=300;countdownStart=30;maxMass=-1;maxDim=300;
			this.battleConfig = (String) ServerConfig.BATTLE_MODE_CONFIG.getString();

			String[] battleConfigParts = battleConfig.split(";");

			for (int i = 0; i < battleConfigParts.length; i++) {
				parseBattleConfig(battleConfigParts[i]);
			}

			//[TeamA, fighters, 500,500,500];[TeamB, fighters, -500,-500,-500];[TeamFFA,ffa, 0,0,0];[Spectators,spectators, 0,500,0]
			this.factionConfig = (String) ServerConfig.BATTLE_MODE_FACTIONS.getString();

			String[] factionConfigParts = factionConfig.split(";");

			for (int i = 0; i < factionConfigParts.length; i++) {
				parseFactionConfig(factionConfigParts[i]);
			}

		} catch (Exception e) {
			throw new GameModeException(e);
		}

	}

	@Override
	protected void initEnd() throws GameModeException {
		checkConfig();
	}

	@Override
	public void onFactionInitServer(FactionManager factionManager) {

		System.err.println("[BATTLEMODE] initializing factions");

		createFactionIfNotPresent(factionManager, battleFactionNames);
		createFactionIfNotPresent(factionManager, ffaFactionNames);
		createFactionIfNotPresent(factionManager, spectatorFactionNames);

		for (int i = 0; i < battleFactions.size(); i++) {
			Faction factionA = battleFactions.get(i);
			for (int j = 0; j < battleFactions.size(); j++) {
				Faction factionB = battleFactions.get(j);
				if (factionA.getIdFaction() != factionB.getIdFaction()) {
					factionManager.setRelationServer(factionA.getIdFaction(), factionB.getIdFaction(), RType.ENEMY.code);
				}
			}
		}

	}

	@Override
	public void update(Timer timer) throws GameModeException {
		if (!init) {

			initialize();
			init = true;
		}

		updateFactions();
		if (round == null) {
			round = new BattleRound(this);
			round.initialize();
		}

		round.update(timer);

		state.getGameState().getNetworkObject().battlemodeInfo.set(round.getBattleInfo());

		if (!round.isAlive()) {
			saveLoaderboard();
			sendLeaderBoard(state.getGameState().getNetworkObject());
			round = null;
		}

	}

	@Override
	public String getCurrentOutput() {
		if (round != null) {
			return round.getCurrentOutput();
		}
		return "no round started";
	}

	@Override
	public boolean allowedToSpawnBBShips(PlayerState playerState, Faction f) {
		return round == null || round.allowedToSpawnBBShips(playerState, f);
	}

	@Override
	public void announceKill(PlayerState playerState, int id) {

		if (round != null && round.isFighter(playerState)) {

			Sendable killerEntity = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(id);
			if (killerEntity != null) {
				if (killerEntity instanceof PlayerControllable) {
					for (PlayerState killer : ((PlayerControllable) killerEntity).getAttachedPlayers()) {
						KillerEntity e = new KillerEntity();

						e.deadPlayerName = playerState.getName();
						e.time = System.currentTimeMillis();
						if (killerEntity instanceof SegmentController) {
							e.shipName = ((SegmentController) killerEntity).getRealName();
						} else if (killerEntity instanceof PlayerCharacter) {
							e.shipName = "astronautWeapon";
						} else if (killerEntity instanceof PlayerState) {
							e.shipName = "unknown";
						}

						e.killerPlayerName = killer.getName();

						if (e.shipName != null) {
							addKillerEntity(e);
						}
					}
				}
			}
		}
	}

	@Override
	public void onFactionChanged(PlayerState playerState, FactionChange change) {
		if (round != null) {
			round.onFactionChanged(playerState, change);
		}
	}

	private void sendLeaderBoard(NetworkGameState n) {
		n.leaderBoardBuffer.add(new RemoteLeaderboard(leaderboard, state instanceof GameServerState));
	}

	public class FactionPreset {
		public String name;
		public int type;
		public Vector3f spawnPos;
		public Vector3f color;
	}

	public class BattleSector {
		public Vector3i pos = new Vector3i();
		public String sectorImport;
	}

}
