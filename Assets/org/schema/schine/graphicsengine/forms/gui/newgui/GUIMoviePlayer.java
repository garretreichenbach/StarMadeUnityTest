package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIResizableElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.movie.MoviePlayer;
import org.schema.schine.graphicsengine.movie.subtitles.Subtitle;
import org.schema.schine.input.InputState;
import org.schema.schine.input.Keyboard;
import org.schema.schine.sound.controller.AudioController;

public class GUIMoviePlayer extends GUIResizableElement {

	public MovieControlMode mode = MovieControlMode.STANDARD_CONTROLS;
	public enum MovieControlMode {

		STANDARD_CONTROLS, BORDERLESS
	}

	private MoviePlayer player;

	private float width;

	private float height;

	public GUIElement dependent;

	private GUIActiveInterface activeInterface;

	private GUIAnchor controls;

	private long started = System.nanoTime();

	private long startedLastSecond = System.nanoTime();

	private int videoFramesLastSecond = 0;

	private GUIAnchor controlsBorderless;

	private File movieFile;

	private boolean init;

	private List<Subtitle> activeSubtitles;

	private boolean failed;

	public GUIMoviePlayer(InputState state, File movieFile, GUIElement dependent, final GUIActiveInterface activeInterface, boolean doControls) throws IOException {
		super(state);
		this.movieFile = movieFile;
		this.dependent = dependent;
		this.activeInterface = activeInterface;
		if (dependent != null) {
			width = dependent.getWidth();
			height = dependent.getHeight();
		} else {
			setWidth(UIScale.getUIScale().scale(300));
			setHeight(UIScale.getUIScale().scale(300));
		}
		if(doControls) {
			createControls();
			createBorderlessControls();
		}
		//if (getState() == null) {
		//	throw new NullPointerException("state null");
		//}
		//if (getState().getGraphicsContext() == null) {
		//	throw new NullPointerException("graphics context null");
		//}
	}

	public void setVolume(float v) {
		player.audio().setVolume(v);
	}

	public void setVideoOnly(boolean b) {
		player.videoOnly = b;

	}

	private void createBorderlessControls() {
		controlsBorderless = new GUIAnchor(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().scale(300));
		GUITooltipBackground controlsBg = new GUITooltipBackground(getState(), 30, 30) {

			@Override
			public void draw() {
				setWidth(controlsBorderless.getWidth());
				setHeight(controlsBorderless.getHeight());
				super.draw();
			}
		};
		controlsBg.onInit();
		controlsBorderless.attach(controlsBg);
		final GUIHorizontalProgressBar progress = new GUIHorizontalProgressBar(getState(), controlsBorderless) {

			@Override
			public float getValue() {
				// System.err.println("dfgdf "+player.getPercentDone()+"; "+player.passed()+"/"+player.duration());
				return Math.min(1, player.getPercentDone());
			}
		};
		progress.setCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !activeInterface.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					float perc = progress.getRelMousePos().x / progress.getWidth();
					try {
						player.absoluteSeek(Math.round(perc * player.duration()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		progress.widthAdd = UIScale.getUIScale().scale(450);
		progress.setPos(progress.widthAdd, 0, 0);
		GUITextOverlay t = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		t.setTextSimple(Lng.str("PageUp bigger, PageDown smaller, F3 pause/unpause, F4 close"));
		t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		controlsBorderless.attach(t);
		controlsBorderless.attach(progress);
	}

	private void createControls() {
		controls = new GUIAnchor(getState(), UIScale.getUIScale().scale(300), UIScale.getUIScale().scale(300));
		GUITooltipBackground controlsBg = new GUITooltipBackground(getState(), 30, 30) {

			@Override
			public void draw() {
				setWidth(controls.getWidth());
				setHeight(controls.getHeight());
				super.draw();
			}
		};
		controlsBg.onInit();
		controls.attach(controlsBg);
		GUIHorizontalButton b = new GUIHorizontalButton(getState(), HButtonType.BUTTON_BLUE_MEDIUM, new Object() {

			@Override
			public String toString() {
				return (player != null && (player.isPaused() || player.isEnded() || player.isClosed())) ? Lng.str("PLAY") : Lng.str("PAUSE");
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !activeInterface.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (player.isPaused() || player.isEnded() || player.isClosed()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(26);
						player.resume();
					} else {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(25);
						player.pause();
					}
				}
			}
		}, activeInterface, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		b.setWidth(70);
		controls.attach(b);
		final GUIHorizontalProgressBar progress = new GUIHorizontalProgressBar(getState(), controls) {

			@Override
			public float getValue() {
				// System.err.println("dfgdf "+player.getPercentDone()+"; "+player.passed()+"/"+player.duration());
				return Math.min(1, player != null ? player.getPercentDone() : 0);
			}
		};
		progress.setCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !activeInterface.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					float perc = progress.getRelMousePos().x / progress.getWidth();
					try {
						player.absoluteSeek(Math.round(perc * player.duration()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		progress.widthAdd = b.getWidth();
		progress.setPos(b.getWidth(), 0, 0);
		controls.attach(progress);
	}

	@Override
	public void cleanUp() {
		if (failed) {
			return;
		}
		try {
			player.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void draw() {
		if (failed) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot play movie file (currently not yet supported on this machine.\nAll tutorials are alternatively on https://www.youtube.com/user/schemastarmade"));
			return;
		}
		if (!init) {
			onInit();
		}
		if (GUIElement.renderModeSet == GUIElement.RENDER_MODE_SHADOW) {
			return;
		}
		if (dependent != null) {
			width = dependent.getWidth();
			height = dependent.getHeight();
		}
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical("BEFORE MOVIE");
		}
		drawMovie();
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical("AFTER MOVIE");
		}
		if (mode == MovieControlMode.STANDARD_CONTROLS) {
			drawControls();
		} else if (mode == MovieControlMode.BORDERLESS) {
			drawControlsBorderless();
		}
	}

	private void drawControls() {
		GlUtil.glPushMatrix();
		transform();
		controls.setHeight(UIScale.getUIScale().h);
		controls.setWidth(getMovieCanvasWidth());
		controls.setPos(0, getMovieCanvasHeight(), 0);
		controls.draw();
		GlUtil.glPopMatrix();
	}

	private void drawControlsBorderless() {
		GlUtil.glPushMatrix();
		transform();
		controlsBorderless.setHeight(UIScale.getUIScale().h);
		controlsBorderless.setWidth(getMovieCanvasWidth());
		controlsBorderless.setPos(0, getMovieCanvasHeight(), 0);
		controlsBorderless.draw();
		GlUtil.glPopMatrix();
	}

	public int getMovieCanvasHeight() {
		return (int) (height - UIScale.getUIScale().h);
	}

	public int getMovieCanvasWidth() {
		return (int) (width);
	}

	private void drawBlackBackground() {
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(0, 0, 0, 1);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(0, 0);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(0, getMovieCanvasHeight());
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(getMovieCanvasWidth(), getMovieCanvasHeight());
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(getMovieCanvasWidth(), 0);
		GL11.glEnd();
	}

	protected void drawMovie() {
		if (failed) {
			return;
		}
		GlUtil.glDisable(GL11.GL_LIGHTING);
		if (player.isEnded() || player.isClosed()) {
			drawSplash();
			return;
		}
		player.tick();
		long milliPassed = (long) (player.passed() * 1000d);
		checkSubtitles(milliPassed);
		GlUtil.glPushMatrix();
		transform();
		drawBlackBackground();
		drawMovieFrame();
		if (System.nanoTime() > startedLastSecond + 1000000000L) {
			startedLastSecond += 1000000000L;
			videoFramesLastSecond = player.textureUpdateTook.addCount();
		}
		GlUtil.glPopMatrix();
	}

	private void checkSubtitles(long milliPassed) {
		if (player.subtitles != null) {
			player.subtitles.updateSubtitles(milliPassed);
			activeSubtitles = player.subtitles.getActiveSubtitles();
		}
	}

	public void drawMovieFrame() {
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		if (!player.syncTexture(5, getState().getGraphicsContext().timer)) {
			return;
		// break;
		}
		// render scene
		long textureRenderTook = System.nanoTime();
		{
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			float h = (float) player.movie.height() / player.movie.width() * 2;
			GlUtil.glPushMatrix();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(1, 1, 1, 1);
			// render flat screen
			float wRatio = (float) getMovieCanvasWidth() / player.movie.width();
			float hRatio = (float) getMovieCanvasHeight() / player.movie.height();
			float minRatio = Math.min(wRatio, hRatio);
			float wMovie = player.movie.width() * minRatio;
			float hMovie = player.movie.height() * minRatio;
			float xMovie = (getMovieCanvasWidth() - wMovie) * 0.5f;
			float yMovie = (getMovieCanvasHeight() - hMovie) * 0.5f;
			// System.err.println("DRAW :: h"+getHeight()+"  ::: "+getMovieCanvasWidth()+"x"+getMovieCanvasHeight()+"  xy "+xMovie+"; "+yMovie+"; ... "+wMovie+"; "+hMovie);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(xMovie + 0 * wMovie, yMovie + 0 * hMovie);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(xMovie + 0 * wMovie, yMovie + 1 * hMovie);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(xMovie + 1 * wMovie, yMovie + 1 * hMovie);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(xMovie + 1 * wMovie, yMovie + 0 * hMovie);
			GL11.glEnd();
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glPopMatrix();
		}
		textureRenderTook = System.nanoTime() - textureRenderTook;
	}

	private void drawSplash() {
	}

	@Override
	public void onInit() {
		try {
			this.player = new MoviePlayer(movieFile);
//			if (!EngineSettings.S_SOUND_SYS_ENABLED.isOn() || !EngineSettings.USE_OPEN_AL_SOUND.isOn()) {
//				player.movie.onMissingAudio();
//			}
			init = true;
		} catch (IOException e) {
			e.printStackTrace();
			failed = true;
			getState().getController().popupAlertTextMessage(Lng.str("Cannot play movie file (currently not yet supported on this machine.\nAll tutorials are alternatively on https://www.youtube.com/user/schemastarmade"));
		}
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public void setWidth(float width) {
		this.width = width;
	}

	@Override
	public void setHeight(float height) {
		this.height = height;
	}

	@Override
	public boolean isActive() {
		return activeInterface == null || activeInterface.isActive();
	}

	public void setLooping(boolean b) {
		player.setLooping(b);
	}

	public boolean isEndedOrClosed() {
		if (failed) {
			return true;
		}
		return player.isEnded() || player.isClosed();
	}

	public void switchPause() {
		if (failed) {
			return;
		}
		if (player.isPaused()) {
			player.resume();
		} else {
			player.pause();
		}
	}

	public List<Subtitle> getActiveSubtitles() {
		return activeSubtitles;
	}

	public void setActiveSubtitles(List<Subtitle> activeSubtitles) {
		this.activeSubtitles = activeSubtitles;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public int getVideoFramesLastSecond() {
		return videoFramesLastSecond;
	}

	public void setVideoFramesLastSecond(int videoFramesLastSecond) {
		this.videoFramesLastSecond = videoFramesLastSecond;
	}

	public long getStarted() {
		return started;
	}

	public void setStarted(long started) {
		this.started = started;
	}
}
