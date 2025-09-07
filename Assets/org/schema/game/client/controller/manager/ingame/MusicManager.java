package org.schema.game.client.controller.manager.ingame;

import api.common.GameClient;
import api.utils.gui.GUIControlManager;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerMusicTagManager;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.manager.engine.AudioSource;

import java.io.File;
import java.util.ArrayList;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class MusicManager extends GUIControlManager {

	public final AudioController audioController;
	public boolean shuffle;
	public GUITextOverlay timeCounter;
	public MusicManager(GameClientState state) {
		super(state);
		audioController = AudioController.instance;
	}

	public static boolean isAdmin() {
		return GameClient.getClientPlayerState().isAdmin();
	}

	public static String getArtist(AudioAsset asset) {
		File file = asset.getFile();
		if(file.exists()) {
			try {
				AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile(file);
				String artist = audioFile.getTag().getFirst(FieldKey.ARTIST);
				if(artist == null || artist.isEmpty()) return "Unknown Artist";
				else return artist;
			} catch(Exception exception) {
				exception.printStackTrace();
				return "Unknown Artist";
			}
		} else return "Unknown Artist";
	}

	public PlayerMusicTagManager getTagManager() {
		return getState().getPlayer().musicManager;
	}

	@Override
	public GUIMusicPanel createMenuPanel() {
		return new GUIMusicPanel(getState(), "Music", 750, 500, this);
	}

	@Override
	public GUIMusicPanel getMenuPanel() {
		return (GUIMusicPanel) super.getMenuPanel();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		if(getState() == null) {
			AudioController.instance.stopMusic();
			return;
		}
		if(isActive() && timeCounter != null && audioController.isMusicPlaying()) {
			int seconds = (int) (audioController.getMusicPlaying().getPlaybackTime());
			int minutes = seconds / 60;
			seconds %= 60;
			timeCounter.setTextSimple(getLastPlayed().getFile().getName().replaceAll(".ogg", "") + "   " + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds));
		}
	}

	public AudioAsset getLastPlayed() {
		return getTagManager().getLastPlayed();
	}

	public void previous() {
		ArrayList<AudioAsset> music = getMusic();
		int index = music.indexOf(getLastPlayed());
		AudioAsset asset;
		if(index == 0) asset = music.get(music.size() - 1);
		else asset = music.get(index - 1);
		play(asset);
	}

	public void play(AudioAsset asset) {
//		stop();
//		audioController.stopMusic();
		audioController.playMusic(asset);
		getTagManager().setLastPlayed(asset);
		if(timeCounter != null) timeCounter.setTextSimple(asset.getFile().getName().replaceAll(".ogg", "") + "   00:00");
	}

	public boolean isPlaying() {
		return audioController.isMusicPlaying();
	}

	public boolean isPaused() {
		return isPlaying() && audioController.getMusicPlaying().getStatus() == AudioSource.Status.PAUSED;
	}

	public void next() {
		AudioAsset next = getNext();
		play(next);
	}

	public AudioAsset getNext() {
		if(shuffle) return getRandom();
		else {
			ArrayList<AudioAsset> music = getMusic();
			int index = music.indexOf(getLastPlayed());
			if(index == music.size() - 1) return music.get(0);
			else return music.get(index + 1);
		}
	}

	public AudioAsset getRandom() {
		ArrayList<AudioAsset> music = getMusic();
		AudioAsset asset = music.get((int) (Math.random() * music.size()));
		if(asset == getLastPlayed()) return getRandom();
		else return asset;
	}

	public boolean isLooping() {
		if(audioController.getMusicPlaying() != null) return audioController.getMusicPlaying().isLooping();
		else return false;
	}

	public void setLooping(boolean looping) {
		if(audioController.getMusicPlaying() != null) audioController.getMusicPlaying().setLooping(looping);
	}

	public void pause() {
		if(audioController.getMusicPlaying() != null) audioController.getMusicPlaying().pause();
	}

	public void resume() {
		if(audioController.getMusicPlaying() != null) {
			float playbackTime = audioController.getMusicPlaying().getPlaybackTime();
			audioController.getMusicPlaying().play();
			audioController.getMusicPlaying().setTimeOffset(playbackTime);
		}
	}

	public ArrayList<AudioAsset> getMusic() {
		return new ArrayList<>(audioController.getConfig().assetManager.musicAssets);
	}

	public void setCurrent(AudioAsset selected) {
		if(timeCounter != null) timeCounter.setTextSimple(selected.getFile().getName().replaceAll(".ogg", "") + " - " + getArtist(selected) + "   00:00");
	}

	public void stop() {
		if(timeCounter != null) timeCounter.setTextSimple("Stopped   00:00");
	}

}
