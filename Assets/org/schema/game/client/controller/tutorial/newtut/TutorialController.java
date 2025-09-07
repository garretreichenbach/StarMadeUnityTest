package org.schema.game.client.controller.tutorial.newtut;

import api.utils.textures.StarLoaderTexture;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.GUICloneableElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TutorialController {

//	public static GUIMoviePlayer running;

	private final GameClientState state;

	private boolean shownFirstTutorial;

	private Set<String> watched;

//	private GUIMoviePlayer backgroundPlayer;

	private float zoom = 1.0f;

	//Map for elements and graphics pinned to the screen for reference
	private final HashMap<Long, GUIElement> pinnedElements = new HashMap<>();

	public TutorialController(GameClientState state) {
		this.state = state;
	}

	public void togglePin(GUICloneableElement cloneableElement) {
		if(cloneableElement instanceof GUIElement element) {
			if(pinnedElements.containsKey(cloneableElement.getSharedID())) {
				element.cleanUp();
				pinnedElements.remove(cloneableElement.getSharedID());
			} else pinnedElements.put(cloneableElement.getSharedID(), cloneableElement.clone());
		}
	}

	public boolean isPinned(GUICloneableElement element) {
		return pinnedElements.containsKey(element.getSharedID());
	}

	public void update(Timer timer) {
		StarLoaderTexture.runOnGraphicsThread(() -> {
			for(GUIElement element : pinnedElements.values()) {
				element.update(timer);
				element.draw();
			}
		});

		if(!isEnabled()) {
			return;
		}
		if(!shownFirstTutorial) {
//			TutorialVideoPlayer p = new TutorialVideoPlayer(state);
//			p.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			(new TutorialMenu(state)).activate();
			AudioController.fireAudioEventID(289);
//			if(EngineSettings.TUTORIAL_PLAY_INTRO.isOn()) {
//				p.playIntroVideo();
//			}
			shownFirstTutorial = true;
		}
	}

	public boolean isEnabled() {
		return EngineSettings.TUTORIAL_NEW.isOn() && state.getCharacter() != null && !state.isWaitingForPlayerActivate();
	}

	public void onActivateFromTopTaskBar() {
//		TutorialVideoPlayer p = new TutorialVideoPlayer(state);
//		p.activate();
		(new TutorialMenu(state)).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(290);
	}

	public boolean isTutorialSelectorActive() {
		List<DialogInterface> p = state.getController().getInputController().getPlayerInputs();
//		return !p.isEmpty() && p.get(p.size() - 1) instanceof TutorialVideoPlayer;
		return !p.isEmpty() && p.get(p.size() - 1) instanceof TutorialMenu;
	}

	public void onDeactivateFromTopTaskBar() {
		List<DialogInterface> p = state.getController().getInputController().getPlayerInputs();
		for(DialogInterface c : p) {
//			if (c instanceof TutorialVideoPlayer) {
			if(c instanceof TutorialMenu) {
				c.deactivate();
				break;
			}
		}
	}

	private void writeWatched() {
		StringBuffer b = new StringBuffer();
		Set<String> wt = getWatched(false);
		for(String s : wt) {
			b.append(s);
			b.append(";");
		}
		EngineSettings.TUTORIAL_WATCHED.setString(b.toString());
	}

	private Set<String> getWatched(boolean read) {
		if(read || watched == null) {
			String wRaw = EngineSettings.TUTORIAL_WATCHED.getString();
			String[] w = wRaw.split(";");
			watched = new ObjectOpenHashSet<>();
			for(String wt : w) {
				if(!wt.trim().isEmpty()) {
					watched.add(wt);
				}
			}
		}
		return watched;
	}

	public boolean isWatched(String title) {
		return getWatched(false).contains(title.toLowerCase(Locale.ENGLISH));
	}

	public void addWatched(String title) {
		getWatched(true).add(title.toLowerCase(Locale.ENGLISH));
		writeWatched();
	}

//	public void drawBackgroundPlayer() {
//		if(backgroundPlayer != null) {
//			running = backgroundPlayer;
//			if(backgroundPlayer.isEndedOrClosed()) {
//				backgroundPlayer = null;
//				running = null;
//				return;
//			}
//			float ratio = 1280.0f / 738.0f;
//			backgroundPlayer.setPos(0, UIScale.getUIScale().scale(28), 0);
//			backgroundPlayer.setHeight((int) ((GLFrame.getHeight() / 1.8f) * zoom));
//			backgroundPlayer.setWidth((int) (ratio * backgroundPlayer.getHeight()));
//			backgroundPlayer.draw();
//		}
//	}

//	public void setBackgroundVideo(MovieDialog m) {
//		GUIMoviePlayer player = m.getPlayer();
//		player.dependent = null;
//		if(backgroundPlayer != null) {
//			backgroundPlayer.cleanUp();
//		}
//		backgroundPlayer = player;
//		player.mode = GUIMoviePlayer.MovieControlMode.BORDERLESS;
//	}

	public void handleKeyEvent(KeyEventInterface e) {
		for(KeyboardMappings m : e.getTriggeredMappings())
			switch(m) {
				case TUTORIAL_KEY_ZOOM_IN:
					if(zoom < 4.0f) {
						zoom += 0.1f;
					}
					break;
				case TUTORIAL_KEY_ZOOM_OUT:
					if(zoom > 0.21f) {
						zoom -= 0.1f;
					}
					break;
//				case TUTORIAL_KEY_PAUSE:
//					if(backgroundPlayer != null) {
//						backgroundPlayer.switchPause();
//					}
//					break;
//				case TUTORIAL_KEY_CLOSE:
//					if(backgroundPlayer != null) {
//						backgroundPlayer.cleanUp();
//					}
//					break;
				default:
					break;
			}
	}

//	public boolean isBackgroundVideoActive() {
//		return backgroundPlayer != null;
//	}

	public void resetTutorials() {
		EngineSettings.TUTORIAL_BUTTON_BLINKING.setOn(true);
		EngineSettings.TUTORIAL_PLAY_INTRO.setOn(true);
		EngineSettings.TUTORIAL_WATCHED.setString("");
		EngineSettings.TUTORIAL_NEW.setOn(true);
		try {
			EngineSettings.write();
		} catch(IOException e) {
			e.printStackTrace();
		}
		watched = null;
	}
}
