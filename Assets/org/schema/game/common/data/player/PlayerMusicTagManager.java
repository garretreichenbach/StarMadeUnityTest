package org.schema.game.common.data.player;

import com.bulletphysics.util.ObjectArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.game.client.controller.GameClientController.EntitiesChangedListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.*;
import org.schema.schine.sound.controller.asset.AudioAsset;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerMusicTagManager implements EntitiesChangedListener, MusicControlListener {

	private static final int UPDATE_TIME = 500000;
	public final Short2ObjectOpenHashMap<ActiveTag> tagMap = new Short2ObjectOpenHashMap<>();
	private final Set<MusicTag> activeSetTmp = new ObjectOpenHashSet<>();
	private final Object2IntOpenHashMap<AudioAsset> playedSongs = new Object2IntOpenHashMap<>();
	private final List<AudioAsset> selectedSongsTmp = new ObjectArrayList<>();
	private AudioAsset lastPlayed;
	private final SongPlayedComp comp = new SongPlayedComp();

	public class ActiveTag {
		public final MusicTag tag;
		public long started;
		public long lastSet;

		public ActiveTag(MusicTag t) {
			tag = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((tag == null) ? 0 : tag.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			ActiveTag other = (ActiveTag) obj;
			return tag == other.tag;
		}

		private PlayerMusicTagManager getEnclosingInstance() {
			return PlayerMusicTagManager.this;
		}

		public boolean set(long time) {
			if(started <= 0) {
				started = time;
			}
			lastSet = time;
			return isActive(time);
		}

		public boolean isActive(long time) {
			return (time < lastSet + tag.getMilliActive()) && (time < started + tag.getMilliMaxActive());
		}
	}

	/**
	 * @param t
	 * @param time
	 *
	 * @return if tag is currently active
	 */
	private boolean setOrAdd(MusicTag t, long time) {
		ActiveTag at = tagMap.get(t.getTagId());
		if(at == null) {
			at = new ActiveTag(t);
			tagMap.put(t.getTagId(), at);
		}
		return at.set(time);
	}

	private final PlayerState player;
	private final AudioController con;

	public boolean tagHighPrioFlagged;

	private long lastCheck;

	public AudioAsset getLastPlayed() {
		return lastPlayed;
	}

	public void setLastPlayed(AudioAsset asset) {
		lastPlayed = asset;
	}

	public boolean isActive(MusicTag t) {
		ActiveTag at = tagMap.get(t.getTagId());
		return at != null && at.isActive(player.getState().getUpdateTime());
	}

	public PlayerMusicTagManager(PlayerState player) {
		this.player = player;
		this.con = AudioController.instance;
	}

	public void updateMusicTag(AudioTag ts, long time) {
		MusicTag t = (MusicTag) ts;
		boolean wasActive = isActive(t);
		boolean active = setOrAdd(t, time);

		if(t == MusicTags.BATTLE_ANY) {
			if(!wasActive && active) {
				checkBattleSize(time);
			}
		}
	}

	private void checkBattleSize(long time) {
		GameClientState c = (GameClientState) player.getState();
		float totalMass = 0;
		int totalEnemies = 0;
		for(SimpleTransformableSendableObject<?> s : c.getCurrentSectorEntities().values()) {
			if(c.getFactionManager().getRelation(s.getFactionId(), player.getFactionId()) == FactionRelation.RType.ENEMY) {
				totalMass += s.getMass();
				totalEnemies++;
			}
		}
		if(totalEnemies == 0) return;
		if(totalEnemies < 10 && totalMass < 30000) updateMusicTag(MusicTags.BATTLE_SMALL, time);
		else if(totalEnemies < 50 && totalMass < 200000) updateMusicTag(MusicTags.BATTLE_MEDIUM, time);
		else updateMusicTag(MusicTags.BATTLE_BIG, time);
		tagHighPrioFlagged = true;
	}

	public void update(Timer timer) {
		assert (!player.isOnServer());
		if((!AudioController.instance.isMusicPlaying() && timer.currentTime - lastCheck > UPDATE_TIME) || tagHighPrioFlagged) {
			if(!AudioController.instance.isMusicPlaying()) doMusicCheck(timer.currentTime);
			tagHighPrioFlagged = false;
			lastCheck = timer.currentTime;
		}
	}

	private void doMusicCheck(long time) {
		if(AudioController.instance.getConfig().assetManager.musicAssets.isEmpty()) {
			File musicFolder = new FileExt("data/audio-resource/Music");
			if(musicFolder.exists()) {
				for(File f : Objects.requireNonNull(musicFolder.listFiles())) {
					if(f.getName().endsWith(".ogg")) {
						try {
							AudioAsset ma = new AudioAsset(f);
							ma.loadAudio(AudioController.instance);
							AudioController.instance.getConfig().assetManager.musicAssets.add(ma);
						} catch(IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
		try {
			for(ActiveTag a : tagMap.values()) {
				if(a == null || a.tag == null) continue;
				if(a.isActive(time)) activeSetTmp.add(a.tag);
				for(AudioAsset ma : AudioController.instance.getConfig().assetManager.musicAssets) {
					if(activeSetTmp.containsAll(ma.musicTags)) selectedSongsTmp.add(ma);
				}

				if(selectedSongsTmp.isEmpty()) {
					List<AudioAsset> musicAssetsShuff = new ObjectArrayList<>(AudioController.instance.getConfig().assetManager.musicAssets.size());
					musicAssetsShuff.addAll(AudioController.instance.getConfig().assetManager.musicAssets);
					Collections.shuffle(musicAssetsShuff);
					for(AudioAsset ma : AudioController.instance.getConfig().assetManager.musicAssets) {
						if(ma.musicTags.contains(MusicTags.BUILDING) || ma.musicTags.contains(MusicTags.EXPLORATION) || ma.musicTags.contains(MusicTags.HOME)) selectedSongsTmp.add(ma);
					}
				}

				if(!selectedSongsTmp.isEmpty()) {
					for(AudioAsset ignored : selectedSongsTmp) selectedSongsTmp.sort(comp);
					Collections.shuffle(selectedSongsTmp);
					AudioAsset selected = selectedSongsTmp.get(0);
					if(selected != lastPlayed && !AudioController.instance.isMusicPlaying()) {
						playedSongs.put(selected, playedSongs.getInt(selected) + 1);
						AudioController.instance.stopMusic();
						AudioController.instance.playMusic(selected);
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().musicManager.setCurrent(selected);
						lastPlayed = selected;
					}
				} else System.err.println("[AUDIO] [MUSIC] No music found for tags: " + activeSetTmp);
			}
		} finally {
			selectedSongsTmp.clear();
			activeSetTmp.clear();
		}
	}

	private class SongPlayedComp implements Comparator<AudioAsset> {

		@Override
		public int compare(AudioAsset o1, AudioAsset o2) {
			//last played always at the end
			if(o1 == lastPlayed) return -1;
			else if(o2 == lastPlayed) return 1;
			int p1 = playedSongs.getInt(o1);
			int p2 = playedSongs.getInt(o2);
			if(p1 == p2) {
				float prio1 = o1.getMusicPrioAvg();
				float prio2 = o2.getMusicPrioAvg();
				return Float.compare(prio1, prio2);
			}
			return p1 - p2;
		}
	}

	public void initialize() {
		((GameClientState) player.getState()).getController().entitiesChangedListeners.add(this);
		con.musicControlListener.add(this);
		con.stopMusic();
	}

	@Override
	public void onEntitiesChanged(Int2ObjectOpenHashMap<SimpleTransformableSendableObject<?>> sectorEntities) {
		for(SimpleTransformableSendableObject<?> s : sectorEntities.values()) {
			if(s.getType() == SimpleTransformableSendableObject.EntityType.PLANET_ICO) updateMusicTag(MusicTags.PLANET, getState().getUpdateTime());
			else if(s.getType() == SimpleTransformableSendableObject.EntityType.SPACE_STATION) updateMusicTag(MusicTags.STATION, getState().getUpdateTime());
			else if(s.getType() == SimpleTransformableSendableObject.EntityType.SHOP) updateMusicTag(MusicTags.SHOP, getState().getUpdateTime());
			if(s.isHomeBaseFor(player.getFactionId())) updateMusicTag(MusicTags.HOME, getState().getUpdateTime());
			if(s.getFactionId() == FactionManager.PIRATES_ID) updateMusicTag(MusicTags.PIRATES, getState().getUpdateTime());
			if(s.getFactionId() == FactionManager.TRAIDING_GUILD_ID) updateMusicTag(MusicTags.TRADING_GUILD, getState().getUpdateTime());
			if(FactionManager.isNPCFaction(s.getFactionId())) updateMusicTag(MusicTags.NPC_FACTION, getState().getUpdateTime());
		}
	}

	public GameClientState getState() {
		return (GameClientState) player.getState();
	}

	@Override
	public void onMusicStopped(AudioAsset a) {

	}
}
