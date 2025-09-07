package org.schema.game.client.view.gui;

import api.utils.textures.TextureSwapper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.schema.common.util.data.DataUtil;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.mainmenu.gui.screenshotviewer.ScreenshotManager;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.forms.Light;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.FileExt;

import javax.imageio.ImageIO;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LoadingScreenDetailed extends LoadingScreen {
	private GUITextOverlay loadInfo;
	private GUITextOverlay loadMessage;
	private boolean init;
	private Light light;
	private float adviceStartTime;
	private float time;
	private GameMainMenuController mainMenu;
	private List<String> tips;
	//INSERTED CODE
	private final GUITextOverlay modMainInfo;
	private final GUITextOverlay modSecondaryInfo;
	private final GUITextOverlay starloaderVersion;
	public static String modMainStatus = "[No Mod Initialization]";
	public static String modSecondaryStatus = "...";

	private static Vector4f toSlickColor(java.awt.Color awt) {
		return new Vector4f(awt.getRed() / 255F, awt.getGreen() / 255F, awt.getBlue() / 255F, 1F);
	}

	private Sprite bgSprite;
	private Sprite logo;
	private Sprite logoSM;
	private final GUITextOverlay smTitle;
	private final GUITextOverlay loadingScreenNote;
	private final GUITextOverlay versionOverlay;
	private float timer;
	private final static float UPDATE_TIME = 5000;
	private String loadingScreenName;
	private static File lastScreen;

	//COLOR PALETTE
	enum Palette {
		BACKGROUND(new Color(0, 0, 0)),
		MILD(new Color(255, 0, 111)),
		IMPORTANT(new Color(255, 0, 0)),
		SL_INFO(new Color(255, 0, 10)),
		DEFAULT(new Color(232, 232, 232)),
		;

		Vector4f color;

		Palette(Color awtColor) {
			color = LoadingScreenDetailed.toSlickColor(awtColor);
		}
	}

	///

	private void initLoadingTips() {
		tips = new ArrayList<>(Arrays.asList(
				Lng.str("You can view options for a map position by double-clicking it with [%s].\nFrom there, you can set a custom waypoint, order fleets around, and more!", KeyboardMappings.MAP_SELECT_ITEM.getKeyChar()),
				Lng.str("Exiting a flying ship isn't the best of ideas."),
				Lng.str("Destroying containers will jettison their contents into space."),
				Lng.str("The Navigation menu allows you to select which markers to display."),
				Lng.str("Using transporters will temporarily disable your shields."),
				Lng.str("The Trading Guild is friendly and ferries items between shops. (Player shops, too!)"),
				Lng.str("Hostility towards the Trading Guild isn't wise."),
				Lng.str("It's often worth trying to befriend your galactic neighbors."),
				Lng.str("Be careful when settling near Pirate stations, as they are hostile and will continually hunt you down."),
				Lng.str("Each faction can have one home base.\nEverything docked there is invulnerable to enemy attacks."),
				Lng.str("You can see cloaked ships if you have enough scanning power!"),
				Lng.str("Jump Inhibitors are wonderful... until you're the prey."),
				Lng.str("What else is out there?"),
				Lng.str("Asteroids will eventually respawn (if enabled in server settings)."),
				Lng.str("Piracy can be a worthwhile source of income."),
				Lng.str("Asteroid types are distributed based on heat."),
//                Lng.str("There are other galaxies to explore!"), Let's not encourage this one
				Lng.str("Exploration can often be rewarding."),
				Lng.str("You can visit every star in the sky."),
				Lng.str("You can customize your ship's thrust output, increasing its speed, rotation, etc."),
				Lng.str("Thicker armor adds extra strength to your armor values."),
				Lng.str("Winning a race and want to stay that way?\nUse the tractor beam to slow your opponents!"),
				Lng.str("Warp Gates allow instantaneous travel across vast distances."),
				Lng.str("Enemies can't see through cloaks!"),
				Lng.str("Jump Inhibitors are useful for ambushes."),
				Lng.str("Is something moving too quickly?\nUse beams for fast damage."),
				Lng.str("Fortress shields got you down?\nSome weapons are more suited to take out shields than others."),
				Lng.str("You are safe within the boundaries of Trading Guild shops\n...provided they like you."),
				Lng.str("Scavengers may not care about style, but other players might."),
				Lng.str("Larger ships are more powerful, but much harder to move."),
				Lng.str("Rotated Gravity Modules also influence their gravity direction."),
				Lng.str("You can use Gravity Modules to construct gravity lifts!"),
				Lng.str("A little decoration goes a long way."),
				Lng.str("Rail Rotators are useful for decoration, too!"),
				Lng.str("Rails allow you to move (and rotate) portions of your ship or its interior."),
				Lng.str("You can customize your controls using the options menu."),
				Lng.str("Holding Left Control and [%s] will allow you to save and load specific fleet commands for easy ordering.", KeyboardMappings.RADIAL_MENU.getKeyChar()),
				Lng.str("Holding [%s] displays the quick radial menu.", KeyboardMappings.RADIAL_MENU.getKeyChar()),
				Lng.str("Pressing [%s] while in Build Mode gives you access to Advanced Build Mode.", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar()),
				Lng.str("Advanced Build Mode ([%s]) allows you to copy/paste.", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar()),
				Lng.str("Advanced Build Mode ([%s]) symmetry planes reduce your work by up to 8x!", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar()),
				Lng.str("Advanced Build Mode ([%s]) allows you to place multiple blocks at once.", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar()),
				Lng.str("In Advanced Build Mode ([%s]), the arrow points to the front of the ship.", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar()),
				Lng.str("Some Weapons are better in some situations than others. Relying on just one weapon type is dangerous!"),
				Lng.str("Lock-on missiles (beam + missile) only rarely miss their targets."),
				Lng.str("Use swarm missiles (missile + missile) with caution: they don't discriminate."),
				Lng.str("Combine weapons to get different effects."),
				Lng.str("Beams lose some of their power at longer distances."),
				Lng.str("Warheads may detonate if damaged. Use caution!"),
				Lng.str("Basic Armor is very light and has very little Armor.\nHowever, it has a lot of HP and excels at absorbing missile damage."),
				Lng.str("Standard Armor has a good balance of Armor and HP, but weighs more than Basic Armor."),
				Lng.str("Advanced Armor has the most Armor, but the least HP. It is also the heaviest armor type."),
				Lng.str("Placing thick walls of armor will add more Armor HP to your ship, but will also weigh it down significantly."),
				Lng.str("Missiles may be destroyed with a well-placed cannon shot."),
				Lng.str("The larger a missile's warhead, the higher its HP."),
				Lng.str("Turrets can be made to be Point-defense by setting them to target missiles."),
				Lng.str("Jammers interfere with hostile AI's targeting abilities and can make you invisible to enemy radar."),
				Lng.str("Circuitry in StarMade is Turing-complete: you can make anything!"),
				Lng.str("Activators and buttons adjacent to basic rails can detect objects moving on that rail."),
				Lng.str("You may specify names for Inner Ship Remotes, which will display on the hotbar."),
				Lng.str("Buttons/Activators copy adjacent rail types and orientations to the rails they are linked to."),
				Lng.str("You can copy one Display Module to another using adjacent buttons/activators"),
				Lng.str("Sensors can detect the state of doors, useful for hangar/airlock indicators."),
				Lng.str("Sensors used on thrusters can detect your speed, which is useful for engine decoration."),
				Lng.str("Sensors can compare two Display Modules' contents. Useful for passwords, commands, etc."),
				Lng.str("Multiple buttons may toggle a single Flip-Flop (e.g. for light switches)"),
				Lng.str("Delays can be chained to achieve longer delays."),
				Lng.str("A cross-connected Delay and Not-Signal form a basic half second clock."),
				Lng.str("You can use Rail Speed Controllers to set rotation speeds."),
//                Lng.str("As of June 1st, 2017, the StarMade Launcher consists of 10,688 lines of code."), That's the old launcher, nobody cares about that one anymore
				Lng.str("If you find a bug, be sure to report it on phab.starma.de!"),
				Lng.str("As always, thank you for playing StarMade!\n\t~ The Schine Team"),
				Lng.str("You can rotate orientable blocks before placing them in Advanced Build Mode by using the mouse wheel and holding [%s].", KeyboardMappings.BUILD_MODE_FIX_CAM),
				Lng.str("Gas Giants and Planets can generate passive resource income for your empire\nwith Ambient Gas Extractors and Deep Core Extractors respectively."),
//				Lng.str("You can use a Factory Manager to automate the management and production of resources.\nThey can even be used with Shipyards to automate ship production!"),
//				Lng.str("You can connect a Factory Manager to a Display Module to see the status of your factories,\nand what resources are still needed to reach the current production target."),
//				Lng.str("You can remotely control a nearby ship using a Remote Control Module that is set to the same frequency as the target ship's Remote Access Point.\nYou can combine this with Inner-Ship Remotes for manual fleet control."),
//				Lng.str("By setting a \"Home Station\" for a fleet, you can have the fleet automatically return to that station when idle or damaged.\nIf that station has a Fleet Manager and Shipyard setup, the fleet can even repair itself!"),
//				Lng.str("You can use a Fleet Manager to automatically manage your fleets via logic or GUI.\nThey can even be used with Shipyards to automatically repair and resupply your fleets!"),
//				Lng.str("Factory Managers can be used to automatically restock your supplies and manage resource production."),
				Lng.str("You can use Decorative Consoles to control various systems in your ship simply by connecting them to the desired system.\nYou can even use them to remotely access your ship's core!")
		));
	}

	public String getRandomTip() {
		int i = new Random().nextInt(tips.size());
		return tips.get(i);
	}

	public LoadingScreenDetailed() {
		initLoadingTips();
		System.err.println("[INIT] Creating Loading Screen");
		loadInfo = new GUITextOverlay(FontLibrary.FontSize.BIG_30, null);
		System.err.println("[INIT] Creating Loading Message");
		loadMessage = new GUITextOverlay(FontLibrary.FontSize.BIG_20, null);
		System.err.println("[INIT] Creating Light");
		light = new Light(0);
		modMainInfo = new GUITextOverlay(FontLibrary.FontSize.BIG_20, null);
		modSecondaryInfo = new GUITextOverlay(FontLibrary.FontSize.BIG_20, null);
		starloaderVersion = new GUITextOverlay(FontLibrary.FontSize.BIG_20, null);
		smTitle = new GUITextOverlay(FontLibrary.FontSize.BIG_30, null);
		loadingScreenNote = new GUITextOverlay(FontLibrary.FontSize.SMALL_15, null);
		versionOverlay = new GUITextOverlay(FontLibrary.FontSize.SMALL_15, null);
	}

	@Override
	public void drawLoadingScreen() {
		if(!init) onInit();
		draw2d();
		draw3d();
	}

	private void draw3d() {

		Mesh mesh = Controller.getResLoader().getMesh("3dLogo");

		GUIElement.enableOrthogonal3d();
		GlUtil.glPushMatrix();

		//draw on top of screen
		//GlUtil.translateModelview(GLFrame.getWidth() - 170, GLFrame.getHeight() - 140, 0);
		GlUtil.translateModelview(-50, 0, 0); //I'm just gonna move this offscreen for now

		//y axis has to be flipped (cause: orthographic projection used)
		GlUtil.scaleModelview(40f, -40f, 40f);

		GlUtil.glColor4f(0.3f, 0.3f, .3f, 1.0f);
		GL11.glColorMask(true, true, true, true);
		GlUtil.glDepthMask(true);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_NORMALIZE);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GL20.glUseProgram(0);

		FloatBuffer dif = GlUtil.getDynamicByteBuffer(4 * 4, 0).asFloatBuffer();
		dif.put(0.6f);
		dif.put(0.6f);
		dif.put(0.6f);
		dif.put(0.9f);
		dif.rewind();

		FloatBuffer spec = GlUtil.getDynamicByteBuffer(4 * 4, 1).asFloatBuffer();
		spec.put(0.9f);
		spec.put(0.9f);
		spec.put(0.9f);
		spec.put(0.9f);
		spec.rewind();

		IntBuffer shine = GlUtil.getDynamicByteBuffer(4 * 4, 2).asIntBuffer();
		shine.put(66);
		shine.put(66);
		shine.put(66);
		shine.put(66);
		shine.rewind();

		GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, dif);
		GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, spec);
		GL11.glMaterialiv(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, shine);

		//			mesh.draw();
		//		mesh.getMaterial().attach(0);
		//			GlUtil.glDisable(GL11.GL_LIGHTING);
		light.setAmbience(new Vector4f(0.38f, .38f, .38f, 1f));
		light.setDiffuse(new Vector4f(.68f, .68f, .68f, 1f));
		light.setSpecular(new Vector4f(.99f, .99f, .99f, 1f));
		light.setPos(0, 0, 100);
		//		light.attachSimple();
		light.draw();
		//		((Mesh)mesh.getChilds().get(1)).getMaterial().setAmbient(new float[]{0,0,0,0});
		//		((Mesh)mesh.getChilds().get(1)).getMaterial().setDiffuse(new float[]{0,0,0,0});
		//		((Mesh)mesh.getChilds().get(1)).getMaterial().setSpecular(new float[]{0,0,0,0});
//			Quat4f q = new Quat4f();
//			Matrix3f r = new Matrix3f();
//			r.rotY(time*0.01f);
//			Quat4fTools.set(r, q);
//			System.err.println(((Mesh)mesh.getChilds().get(1)).getRot4()+"; \n"+((Mesh)mesh.getChilds().get(1)).getTransform().basis);
		//		((Mesh)mesh.getChilds().get(0)).setQuatRot(new Vector4f(q.x, q.y, q.z, q.w));
		mesh.getChilds().get(1).setRot(0, time, 0f);
		GlUtil.glPushMatrix();
		mesh.getChilds().get(1).transform();
		((Mesh) mesh.getChilds().get(1)).drawVBO();

		GlUtil.glPopMatrix();
		//		((Mesh)mesh.getChilds().get(1)).loadVBO(true);
		//		((Mesh)mesh.getChilds().get(1)).renderVBO();
		//		((Mesh)mesh.getChilds().get(1)).unloadVBO(true);

		//		mesh.getMaterial().detach();

		GlUtil.glPopMatrix();
		GUIElement.disableOrthogonal();

		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_NORMALIZE);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		//		Mesh m = Controller.getResLoader().getMesh("3dLogo");
		//
		//		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		//		GlUtil.glPushMatrix();
		//		GlUtil.glLoadIdentity();
		//
		//		float aspect = (float) GLFrame.getWidth() / (float) GLFrame.getHeight(); //1.333333333333333333333333f
		//
		//		GlUtil.gluPerspective(Controller.projectionMatrix, 60,
		//				aspect, 0.1f, 400, true);
		//
		//		GlUtil.glColor4f(0.2f, 1.0f, 0.3f, 1f);
		//
		//
		//		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		//		GlUtil.glPushMatrix();
		//		GlUtil.glLoadIdentity();
		//
		//		GlUtil.lookAt (
		//				0.0f, 0.0f, 30.0f,
		//				-20.0f, -10f, 0.0f,
		//				0.0f, 1.0f, 0.0f);
		//
		//
		//		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		//		GlUtil.glEnable(GL11.GL_LIGHTING);
		//		light.draw();
		//
		//		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		//
		//		m.setRot(90, 0, 0);
		//		m.draw();
		////		((Mesh)m.getChilds().get(1)).drawVBO();
		//
		//
		//		GlUtil.glPopMatrix();
		//		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		//		GlUtil.glPopMatrix();
		//		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		//
		//		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		//		GlUtil.glDisable(GL11.GL_LIGHTING);
	}

	private void draw2d() {
		//INSERTED CODE
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClearColor(Palette.BACKGROUND.color.x, Palette.BACKGROUND.color.y, Palette.BACKGROUND.color.z, Palette.BACKGROUND.color.w);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		///
		// switch to projection mode
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		// save previous matrix which contains the
		GlUtil.glPushMatrix();
		// settings for the perspective projection
		// reset matrix
		GlUtil.glLoadIdentity();
		// set a 2D orthographic projection
		GLU.gluOrtho2D(0, GLFrame.getWidth(), GLFrame.getHeight(), 0);
		// invert the y axis, down is positive
		// mover the origin from the bottom left corner
		// to the upper left corner
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glLoadIdentity();

		GlUtil.glPushMatrix();
		logo.setPos(30, GLFrame.getHeight() - logo.getHeight(), 0);
		logo.draw();

		logoSM.setPos(GLFrame.getWidth() - logoSM.getWidth() + 106, GLFrame.getHeight() - logoSM.getHeight() + 68, 0);
		logoSM.draw();
		GlUtil.glPopMatrix();

		//		GLUT glut = new GLUT();
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);

		float x = 60;
		float y = 60;
		GlUtil.glColor4f(0.9f, 0.9f, 0.9f, 0.8f);
		GlUtil.glColor4f(0.2f, 0.9f, 0.2f, 0.9f);
		y += 30;

		GUIElement.enableOrthogonal();

		loadInfo.setPos(10, 10, 0);
		loadMessage.setPos(100, 200, 0);

		modMainInfo.setPos(60, 400, 0);
		modSecondaryInfo.setPos(85, 435, 0);
		starloaderVersion.setPos(320, GLFrame.getHeight() - 50, 0);
		loadInfo.setColor(Palette.DEFAULT.color);
		loadMessage.setColor(Palette.DEFAULT.color);
		smTitle.setPos(60, 60, 0);
		smTitle.setColor(Palette.DEFAULT.color);
		versionOverlay.setPos(30, GLFrame.getHeight() - 70, 0);
		versionOverlay.setColor(Palette.DEFAULT.color);
		loadingScreenNote.setPos(30, GLFrame.getHeight() - 50, 0);
		loadingScreenNote.setColor(Palette.DEFAULT.color);

		//change advice message every ~5 seconds
		if(loadMessage.getText().isEmpty() || time - adviceStartTime > 5000) {
			adviceStartTime = time;
			loadMessage.setText(new ArrayList<>());
			loadMessage.getText().add(Lng.str("Advice:"));
			String[] t = getRandomTip().split("\n");
			for(String s : t) loadMessage.getText().add(s);
		}

		loadInfo.getText().clear();
		loadInfo.getText().add(Controller.getResLoader().getLoadString());
		String s = serverMessage;
		if(s != null) {
			loadInfo.getText().add(s);
		}
		//INSERTED CODE
		drawBG();
		modMainInfo.getText().clear();
		modMainInfo.setColor(Palette.IMPORTANT.color);
		modMainInfo.setTextSimple(modMainStatus);

		modSecondaryInfo.getText().clear();
		modSecondaryInfo.setColor(Palette.MILD.color);
		modSecondaryInfo.setTextSimple(modSecondaryStatus);

//        starloaderVersion.setColor(Palette.SL_INFO.color);
//        starloaderVersion.setTextSimple("StarLoader v" + StarLoader.version + " " + StarLoader.versionName);

//        starloaderVersion.draw();
		modSecondaryInfo.draw();
		modMainInfo.draw();
		///

		loadInfo.draw();
		loadMessage.draw();

		//smTitle.draw();
		loadingScreenNote.draw();
		versionOverlay.draw();

		GUIElement.disableOrthogonal();

		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.printGlErrorCritical();
	}

	private void loadBG() {
		try {
			File randomScreen = lastScreen;
			if(randomScreen == null) {
				randomScreen = ScreenshotManager.getRandomLoadingScreen();
				lastScreen = randomScreen;
			}
			if(GLFrame.getWidth() <= 0 || GLFrame.getHeight() <= 0) return;
			BufferedImage image = null;
			try {
				assert randomScreen != null;
				image = ImageIO.read(randomScreen);
			} catch(IOException exception) {
				exception.printStackTrace();
			}
			BufferedImage resized = new BufferedImage(GLFrame.getWidth(), GLFrame.getHeight(), image.getType());
			Graphics2D g = resized.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, 0, 0, GLFrame.getWidth(), GLFrame.getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			g.dispose();

			Image smLogoImage = ImageIO.read(new FileExt("./data/image-resource/sm_logo.png"));
			Graphics2D g2d = resized.createGraphics();
			g2d.drawImage(smLogoImage, GLFrame.getWidth() - smLogoImage.getWidth(null) - 30, GLFrame.getHeight() - smLogoImage.getHeight(null) - 100, null);
			g2d.dispose();

			bgSprite = new Sprite(TextureSwapper.getTextureFromImage(resized, "loading-screen-bg", true, true));
			bgSprite.onInit();
			bgSprite.setPos(0, 0, 0);
			timer = UPDATE_TIME;

			loadingScreenName = randomScreen.getName().substring(0, randomScreen.getName().lastIndexOf('.'));
			if(randomScreen.getPath().contains("loading-screens")) loadingScreenName = "Screenshot provided by community";
			loadingScreenNote.setTextSimple(loadingScreenName);
			versionOverlay.setTextSimple("StarMade v" + VersionContainer.VERSION.toString());
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	private void drawBG() {
		if(bgSprite == null || timer <= 0) {
			loadBG();
			timer = UPDATE_TIME;
			bgSprite.setTint(new Vector4f(1, 1, 1, 1.0f));
		}
		if(timer > 0) {
			timer -= 10;
			if(timer <= 500) {
				//Fade out
				Vector4f tint = new Vector4f(1, 1, 1, timer / 500.0f);
				bgSprite.setTint(tint);
				if(loadingScreenNote != null) loadingScreenNote.setColor(tint);
//				if(versionOverlay != null) versionOverlay.setColor(tint);
			} else if(timer >= UPDATE_TIME - 500) {
				//Fade in
				Vector4f tint = new Vector4f(1, 1, 1, (UPDATE_TIME - timer) / 500.0f);
				bgSprite.setTint(tint);
				if(loadingScreenNote != null) loadingScreenNote.setColor(tint);
//				if(versionOverlay != null) versionOverlay.setColor(tint);
			}
		}
		bgSprite.draw();
	}

	private void onInit() {
		initLoadingTips();
		loadBG();
//		System.err.println("[INIT] Creating Loading Screen");
		loadInfo = new GUITextOverlay(FontLibrary.FontSize.BIG_30, null);
//		System.err.println("[INIT] Creating Loading Message");
		loadMessage = new GUITextOverlay(FontLibrary.FontSize.BIG_20, null);
//		System.err.println("[INIT] Creating Light");

		light = new Light(0);
		loadInfo.onInit();
		loadMessage.onInit();

		modMainInfo.onInit();
		modSecondaryInfo.onInit();
		starloaderVersion.onInit();
		bgSprite.onInit();
		smTitle.onInit();
		smTitle.setTextSimple("StarMade");
		loadingScreenNote.onInit();
		loadingScreenNote.setTextSimple(loadingScreenName);
		logo = Controller.getResLoader().getSprite("schine");
		logoSM = Controller.getResLoader().getSprite("SM_logo_white_nostar");
		versionOverlay.onInit();
		versionOverlay.setTextSimple("StarMade v" + VersionContainer.VERSION.toString());
		init = true;
	}

	@Override
	public void loadInitialResources() throws IOException, ResourceException {
		// load logo first
		Controller.getResLoader().getImageLoader().loadImage(DataUtil.dataPath + "./image-resource/schine.png", "schine");
		Controller.getResLoader().getImageLoader().loadImage(DataUtil.dataPath + "./image-resource/loadingscreen-background.png", "loadingscreen-background");
		Controller.getResLoader().getImageLoader().loadImage(DataUtil.dataPath + "./image-resource/SM_logo_white_nostar.png", "SM_logo_white_nostar");
		Controller.getResLoader().loadModelDirectly("3dLogo", "./models/3Dlogo/", "3Dlogo");
		GlUtil.printGlError();
		System.out.println("[GLFrame] loading content data");
		Controller.getResLoader().setLoadString("preparing data");
	}

	@Override
	public void update(Timer timer) {
		float t = (Math.min(3f, timer.getDelta()) * 500f);
		time += t;
	}

	@Override
	public void handleException(Exception e) {
		if(mainMenu == null) {
			GLFrame.processErrorDialogException(e, null);
		} else {
			mainMenu.errorDialog(e);
		}
	}

	public GameMainMenuController getMainMenu() {
		return mainMenu;
	}

	public void setMainMenu(GameMainMenuController mainMenu) {
		this.mainMenu = mainMenu;
	}
}
