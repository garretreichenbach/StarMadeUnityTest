package org.schema.game.client.view.mainmenu;

import api.SMModLoader;
import api.utils.GameRestartHelper;
import api.utils.textures.TextureSwapper;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.common.ParseException;
import org.schema.common.util.data.DataUtil;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.view.mainmenu.gui.screenshotviewer.ScreenshotManager;
import org.schema.game.common.LanguageManager;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.movie.MoviePlayer;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;
import org.schema.schine.input.Keyboard;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.ResourceLoadEntryOther;
import org.schema.schine.resource.ResourceLoader;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.controller.asset.AudioAsset.AudioGeneralTag;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.vecmath.Vector4f;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class MainMenuFrame implements GraphicsFrame {

	private final GameMainMenuController state;

	public MainMenuGUI gui;

	private SinusTimerUtil sinus0 = new SinusTimerUtil(1.2f);

	private SinusTimerUtil sinus1 = new SinusTimerUtil(1.0f);

	private Shader bgShader;

	private int displayListQuad = -1;

	private int bgWidth;

	private int bgHeight;

	private boolean init;

	public MoviePlayer player;

	long startedLastSecond = System.nanoTime();

	int videoFramesLastSecond = 0;

	int renderFramesLastSecond = 0;
	public static boolean isMusicPlaying;
	private boolean doVideo = true;
	private boolean introPlaying = true;
	public static MainMenuFrame instance;
	private Sprite bgSprite;
	private Sprite smLogoSprite;
//	private BufferedImage smLogoImage;
	private Sprite schineLogoSprite;
//	private BufferedImage schineLogoImage;
	private static final float UPDATE_TIME = 10000;
	private float timer = UPDATE_TIME;
	private String loadingScreenName;
	private GUITextOverlay screenshotNote;
	private GUITextOverlay versionOverlay;
	private static File lastScreen;

	public MainMenuFrame(GameMainMenuController state) {
		instance = this;
		this.state = state;
		this.gui = new MainMenuGUI(state);
		//Set the window to maximized
	}

	public void switchLanguage(String language) {
		Controller.getResLoader().enqueueWithResetForced(new ResourceLoadEntryOther("Purging Fonts") {

			@Override
			public void loadResource(ResourceLoader resourceLoader) throws ResourceException, IOException {
				FontLibrary.purge();
			}
		});
		Controller.getResLoader().enqueueWithResetForced(new ResourceLoadEntryOther("Load Language") {

			@Override
			public void loadResource(ResourceLoader resourceLoader) throws ResourceException, IOException {
				LanguageManager.loadLanguage(language.toLowerCase(Locale.ENGLISH), false);
			}
		});
		Controller.getResLoader().enqueueWithResetForced(new ResourceLoadEntryOther("Reinitialize Fonts") {

			@Override
			public void loadResource(ResourceLoader resourceLoader) throws ResourceException, IOException {
				FontLibrary.initialize();
			}
		});
		Controller.getResLoader().enqueueWithResetForced(new ResourceLoadEntryOther("Reload Main Menu") {

			@Override
			public void loadResource(ResourceLoader resourceLoader) throws ResourceException, IOException {
				MainMenuFrame.this.gui.cleanUp();
				MainMenuFrame.this.gui = new MainMenuGUI(state);
			}
		});
	}

	@Override
	public void doFrameAndUpdate(GraphicsContext context) throws RuntimeException {
		if(!init) {
			onInit();
		}
//		if (TutorialController.running != null) {
//			try {
//				TutorialController.running.cleanUp();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			TutorialController.running = null;
//		}
		if(player != null && !player.isEnded() && introPlaying) {
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_ESCAPE) || Keyboard.isKeyDown(GLFW.GLFW_KEY_ENTER) || Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
				player.setEnded(true);
			}
			//INSERTED CODE
			if(SMModLoader.shouldUplink) {
				System.err.println("Title screen drawn; uplinking game");
				this.player.setEnded(true);
				if(SMModLoader.uplinkServerHost.equals("localhost")) {
					GameRestartHelper.startLocalWorld();
				} else {
					GameRestartHelper.startOnlineWorld(SMModLoader.uplinkServerHost, SMModLoader.uplinkServerPort);
				}
			}
			///
			movieTest();
		} else {
			if(!isMusicPlaying) startMusic();
			else if(doVideo) {
				movieTest();
				GL11.glClearColor(0, 0, 0, 1);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				if(screenshotNote == null) {
					screenshotNote = new GUITextOverlay(FontLibrary.FontSize.SMALL_15, null);
					screenshotNote.onInit();
					screenshotNote.setPos(30, GLFrame.getHeight() - 50, 0);
					screenshotNote.setTextSimple(loadingScreenName);
				}
				if(versionOverlay == null) {
					versionOverlay = new GUITextOverlay(FontLibrary.FontSize.SMALL_15, null);
					versionOverlay.onInit();
					versionOverlay.setPos( 30, GLFrame.getHeight() - 70, 0);
					versionOverlay.setTextSimple("StarMade " + VersionContainer.getVersionTitle());
				}
				drawBG();
				gui.draw();
				if(AudioController.instance.getMusicPlaying() != null && !AudioController.instance.getMusicPlaying().isLooping()) AudioController.instance.getMusicPlaying().setLooping(true);
			} else {
				drawBg();
				gui.draw();
			}
		}
		state.update();
	}

	private void drawBG() {
		if(bgSprite == null || timer <= 0) {
			loadBG();
			timer = UPDATE_TIME;
		}
		if(timer > 0) {
			timer -= 10;
			if(bgSprite == null) return;
			if(timer <= 500) {
				//Fade out
				Vector4f tint = new Vector4f(1, 1, 1, timer / 500f);
				bgSprite.setTint(tint);
				if(screenshotNote != null) screenshotNote.setColor(tint);
//				if(versionOverlay != null) versionOverlay.setColor(tint);
			} else if(timer >= UPDATE_TIME - 500) {
				//Fade in
				Vector4f tint = new Vector4f(1, 1, 1, (UPDATE_TIME - timer) / 500f);
				bgSprite.setTint(tint);
				if(screenshotNote != null) screenshotNote.setColor(tint);
//				if(versionOverlay != null) versionOverlay.setColor(tint);
			}
		}
		bgSprite.draw();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		smLogoSprite.draw();
		schineLogoSprite.draw();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		screenshotNote.draw();
		versionOverlay.draw();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

	private void startMusic() {
		introPlaying = false;
		try {
			FileExt file = new FileExt("data/audio-resource/Music/Main Theme.ogg");
			AudioAsset asset = new AudioAsset(file);
			asset.loadAudio(AudioController.instance);
			AudioController.instance.playMusic(asset);
			loadBG();
		} catch(IOException exception) {
			exception.printStackTrace();
			doVideo = false;
		}
		isMusicPlaying = true;
	}

	public void resizeBG() {
		if(bgSprite == null) loadBG();
		else {
			//Reloads the background image to fit the new window size but keeps the current image instead of setting a new one
			//Not particularly efficient, but whatever...
			File randomScreen = lastScreen;
			if(randomScreen == null) {
				randomScreen = ScreenshotManager.getRandomLoadingScreen();
				lastScreen = randomScreen;
			}
			if(GLFrame.getWidth() <= 0 || GLFrame.getHeight() <= 0) return;
			BufferedImage image = null;
			try {
				image = ImageIO.read(randomScreen);
			} catch(IOException e) {
				e.printStackTrace();
			}
			BufferedImage resized = new BufferedImage(GLFrame.getWidth(), GLFrame.getHeight(), image.getType());
			Graphics2D g = resized.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, 0, 0, GLFrame.getWidth(), GLFrame.getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			g.dispose();

			smLogoSprite = Controller.getResLoader().getSprite("version-logo");
			if(smLogoSprite == null) smLogoSprite = Controller.getResLoader().getSprite("sm_logo");
			smLogoSprite.onInit();
			smLogoSprite.setPos(50, 30, 0);

			schineLogoSprite = Controller.getResLoader().getSprite("schine");
			schineLogoSprite.onInit();
			schineLogoSprite.setPos(GLFrame.getWidth() - schineLogoSprite.getWidth() - 30, GLFrame.getHeight() - schineLogoSprite.getHeight(), 0);

			bgSprite = new Sprite(TextureSwapper.getTextureFromImage(resized, "loading-screen-bg", true, true));
			bgSprite.onInit();
			bgSprite.setPos(0, 0, 0);
			smLogoSprite.setBlend(true);
			schineLogoSprite.setBlend(true);
			bgSprite.setBlend(true);
			bgSprite.setTint(new Vector4f(1, 1, 1, 1.0f));
		}
	}

	public void loadBG() {
		try {
			File randomScreen = ScreenshotManager.getRandomLoadingScreen();
			if(GLFrame.getWidth() <= 0 || GLFrame.getHeight() <= 0) return;
			assert randomScreen != null;
			BufferedImage image = ImageIO.read(randomScreen);
			BufferedImage resized = new BufferedImage(GLFrame.getWidth(), GLFrame.getHeight(), image.getType());
			Graphics2D g = resized.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, 0, 0, GLFrame.getWidth(), GLFrame.getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			g.dispose();

			if(smLogoSprite == null) {
				smLogoSprite = Controller.getResLoader().getSprite("version-logo");
				if(smLogoSprite == null) smLogoSprite = Controller.getResLoader().getSprite("sm_logo");
				smLogoSprite.onInit();
				smLogoSprite.setPos(50, UIScale.getUIScale().scale(30), 0);
				smLogoSprite.setBlend(true);
			}

			if(schineLogoSprite == null) {
				schineLogoSprite = Controller.getResLoader().getSprite("schine");
				schineLogoSprite.onInit();
				schineLogoSprite.setPos(GLFrame.getWidth() - schineLogoSprite.getWidth() - 30, GLFrame.getHeight() - schineLogoSprite.getHeight() + UIScale.getUIScale().scale(8), 0);
				schineLogoSprite.setBlend(true);
			}

			if(bgSprite != null) bgSprite.cleanUp();
			bgSprite = new Sprite(TextureSwapper.getTextureFromImage(resized, "loading-screen-bg", true, true));
			bgSprite.onInit();
			bgSprite.setPos(0, 0, 0);
			bgSprite.setBlend(true);
			bgSprite.setTint(new Vector4f(1, 1, 1, 1.0f));

			loadingScreenName = randomScreen.getName().substring(0, randomScreen.getName().lastIndexOf('.'));
			if(randomScreen.getPath().contains("loading-screens")) loadingScreenName = "Screenshot provided by community";
			if(screenshotNote != null) screenshotNote.setTextSimple(loadingScreenName);
			if(versionOverlay != null) versionOverlay.setTextSimple("StarMade v" + VersionContainer.VERSION.toString());
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	private void onInit() {
		if(EngineSettings.PLAY_INTRO.isOn()) {
			movieInit();
		}
		GLFW.glfwMaximizeWindow(state.getGraphicsContext().id);
		init = true;
	}

	public void movieInit() {
		File movieFile = new FileExt("./data/video/SchineSplashScreen.mp4");
		try {
			player = new MoviePlayer(movieFile);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void movieTest() {
		if(player == null) return;
		player.tick();
		// System.err.println("MMM:: "+player.movie.getPlayingTime()+"; "+player.movie.framerate());
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		// Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT);
		boolean is3D = false;
		// position camera, make it sway
		// if (is3D) {
		// glMatrixMode(GL_PROJECTION);
		// glLoadIdentity();
		// GLU.gluPerspective(60.0f, displayWidth / (float) displayHeight, 0.01f, 100.0f);
		//
		// glMatrixMode(GL_MODELVIEW);
		// glLoadIdentity();
		//
		// long elapsed = (System.nanoTime() - started) / 1000000;
		// float angle = 90 + (float) Math.sin(elapsed * 0.001) * 15;
		//
		// // inverse camera transformations
		// glRotatef(-angle, 0, 1, 0);
		// glRotatef(-15f, 0, 0, 1); // look down
		// glTranslatef(-3, -1.7f, -0);
		// } else {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, GLFrame.getWidth(), GLFrame.getHeight(), 0, -1.0f, +1.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		// }
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		if(!player.syncTexture(5, state.graphicsContext.timer)) {
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
			float wRatio = (float) GLFrame.getWidth() / player.movie.width();
			float hRatio = (float) GLFrame.getHeight() / player.movie.height();
			float minRatio = Math.min(wRatio, hRatio);
			float wMovie = player.movie.width() * minRatio;
			float hMovie = player.movie.height() * minRatio;
			float xMovie = (GLFrame.getWidth() - wMovie) * 0.5f;
			float yMovie = (GLFrame.getHeight() - hMovie) * 0.5f;
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(xMovie + 0 * wMovie, yMovie + 0 * hMovie, 0);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex3f(xMovie + 1 * wMovie, yMovie + 0 * hMovie, 0);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex3f(xMovie + 1 * wMovie, yMovie + 1 * hMovie, 0);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex3f(xMovie + 0 * wMovie, yMovie + 1 * hMovie, 0);
			GL11.glEnd();
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glPopMatrix();
		}
		// GL11.glFlush();
		textureRenderTook = System.nanoTime() - textureRenderTook;
		renderFramesLastSecond++;
		if(System.nanoTime() > startedLastSecond + 1000000000L) {
			startedLastSecond += 1000000000L;
			// String b1 = TextValues.formatNumber(player.textureUpdateTook.min() / 1000000.0, 1);
			// String b2 = TextValues.formatNumber(player.textureUpdateTook.avg() / 1000000.0, 1);
			// String b3 = TextValues.formatNumber(player.textureUpdateTook.max() / 1000000.0, 1);
			// String c = TextValues.formatNumber(textureRenderTook / 1000000.0, 1);
			//
			// b1 = Text.replace(b1, ',', '.');
			// b2 = Text.replace(b2, ',', '.');
			// b3 = Text.replace(b3, ',', '.');
			// c = Text.replace(c, ',', '.');
			//
			// Display.setTitle(//
			// "rendering " + renderFramesLastSecond + "fps, " + //
			// "video " + (player.textureUpdateTook.addCount() - videoFramesLastSecond) + "fps, " + //
			// "texture update: [min: " + b1 + ", avg: " + b2 + ", max: " + b3 + "ms] " + //
			// "rendering: " + c + "ms");
			renderFramesLastSecond = 0;
			videoFramesLastSecond = player.textureUpdateTook.addCount();
		}
	}

	private void drawBg() {
		if(bgShader == null) {
			return;
		}
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPushMatrix();
		GlUtil.glLoadIdentity();
		GLU.gluOrtho2D(-1, 1, 1, -1);
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPushMatrix();
		GlUtil.glLoadIdentity();
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		bgShader.load();
		GlUtil.glPushMatrix();
		renderQuad();
		GlUtil.glPopMatrix();
		bgShader.unload();
		GlUtil.glPopMatrix();
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
	}

	private class BackgroundShader implements Shaderable {

		@Override
		public void onExit() {
			Texture logo = Controller.getResLoader().getSprite("loadingscreen-background").getMaterial().getTexture();
			logo.detach();
		}

		@Override
		public void updateShader(DrawableScene scene) {
		}

		@Override
		public void updateShaderParameters(Shader shader) {
			Texture logo = Controller.getResLoader().getSprite("loadingscreen-background").getMaterial().getTexture();
			logo.attach(0);
			GlUtil.updateShaderInt(shader, "tex", 0);
			GlUtil.updateShaderVector2f(shader, "res", GLFrame.getWidth(), GLFrame.getHeight());
			GlUtil.updateShaderVector2f(shader, "dir", (sinus0.getTime()) * 5.0f, (sinus1.getTime()) * 5.0f);
			GlUtil.updateShaderVector2f(shader, "dir", (sinus0.getTime()) * 5.0f, (sinus1.getTime()) * 5.0f);
		}
	}

	@Override
	public void handleException(Exception e) {
		System.err.println("[GLFRAME] THROWN: " + e.getClass() + " Now Printing StackTrace");
		e.printStackTrace();
		handleError(e);
		// GLFrame.processErrorDialogException(e, null);
	}

	@Override
	public void onEndLoop(GraphicsContext context) {
		try {
			Controller.cleanUpAllBuffers();
			context.destroy();
			try {
				throw new Exception("System.exit() called");
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void afterFrame() {
	}

	private void renderQuad() {
		if(bgWidth != GLFrame.getWidth() || bgHeight != GLFrame.getHeight()) {
			if(displayListQuad != -1) {
				GL11.glDeleteLists(displayListQuad, 1);
			}
			bgWidth = GLFrame.getWidth();
			bgHeight = GLFrame.getHeight();
			displayListQuad = GL11.glGenLists(1);
			GL11.glNewList(displayListQuad, GL11.GL_COMPILE);
			// bgWidth/2f;
			float left = 1;
			// bgWidth/2f;
			float right = -1;
			// bgHeight/2f;
			float top = 1;
			// bgHeight/2f;
			float bottom = -1;
			GL11.glBegin(GL11.GL_QUADS);
			GlUtil.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(right, top, 0);
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(right, bottom, 0);
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(left, bottom, 0);
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(left, top, 0);
			// GL11.glTexCoord2f(1.0f, 1.0f);
			// GL11.glVertex3f(left, top, 0);
			//
			// GL11.glTexCoord2f(0.0f, 1.0f);
			// GL11.glVertex3f(left, bottom, 0);
			//
			// GL11.glTexCoord2f(1.0f, 1.0f);
			// GL11.glVertex3f(right, bottom, 0);
			//
			// GL11.glTexCoord2f(1.0f, 0.0f);
			// GL11.glVertex3f(right, top, 0);
			GL11.glEnd();
			GL11.glEndList();
		}
		GL11.glCallList(displayListQuad);
	}

	@Override
	public void enqueueFrameResources() throws FileNotFoundException, ResourceException, ParseException, SAXException, IOException, ParserConfigurationException {
		Controller.getResLoader().enqueueWithReset(new ResourceLoadEntryOther("ServerSettings") {

			@Override
			public void loadResource(ResourceLoader resourceLoader) throws ResourceException, IOException {
				ServerConfig.read();
			}
		});
		Controller.getResLoader().enqueueWithReset(new ResourceLoadEntryOther("MainMenuBackgroundShader") {

			@Override
			public void loadResource(ResourceLoader resourceLoader) throws ResourceException, IOException {
				try {
					bgShader = new Shader(DataUtil.dataPath + "/shader/loadingscreen/bg.vert.glsl", DataUtil.dataPath + "/shader/loadingscreen/bg.frag.glsl");
					bgShader.setShaderInterface(new BackgroundShader());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		Controller.getResLoader().enqueueImageResources();
		Controller.getResLoader().enqueueAudio(AudioGeneralTag.BASIC);
		Controller.getResLoader().enqueueConfigResources("GuiConfigMainMenu.xml", false);
	}

	public void update(Timer timer) {
		gui.update(timer);
		sinus0.update(timer);
		sinus1.update(timer);
		this.timer -= timer.getDelta();
	}

	@Override
	public void setFinishedFrame(boolean b) {
		GraphicsContext.setFinished(true);
	}

	public void handleError(Exception e) {
		String msg = (e.getMessage() != null && e.getMessage().length() > 0) ? e.getMessage() : Lng.str("No Message Attached.");
		msg += "\n\n";
		if(!e.getClass().getSimpleName().contains("Disconnect") && !msg.contains("you are banned from this server")) {
			msg += Lng.str("We are sorry that this has happened. Please contact our support at help.star-made.org");
		}
		try {
			PlayerOkCancelInput error = new PlayerOkCancelInput("ERROR", state, 500, 300, e.getClass().getSimpleName(), msg) {

				@Override
				public void pressedOK() {
					System.err.println("[GUI] PRESSED OK ON ERROR");
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void cancel() {
					PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", state, 200, 100, Lng.str("Confirm"), Lng.str("Exit to desktop?")) {

						@Override
						public void pressedOK() {
							GLFrame.setFinished(true);
							deactivate();
						}

						@Override
						public void onDeactivate() {
						}
					};
					deactivate();
					c.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(847);
				}
			};
			error.getInputPanel().onInit();
			error.getInputPanel().setOkButtonText("CONTINUE");
			error.getInputPanel().setCancelButtonText("TO DESKTOP");
			error.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(848);
		} catch(Exception ex) {
			ex.printStackTrace();
			System.err.println("ORIGINAL EXCEPTION: ");
			e.printStackTrace();
		}
	}

	@Override
	public boolean synchByFrame() {
		GraphicsContext.current.sync.sync(60);
		return true;
	}

	@Override
	public void queueException(RuntimeException e) {
		state.handleError(e);
	}
}
