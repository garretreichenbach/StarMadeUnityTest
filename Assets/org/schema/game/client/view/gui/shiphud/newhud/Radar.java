package org.schema.game.client.view.gui.shiphud.newhud;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.MinimapMode;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

import com.bulletphysics.linearmath.Transform;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class Radar extends GUIElement {

	private static final float TITLE_DRAWN_OPAQUE = 4;
	private static final float TITLE_DRAWN_BLEND = 1.3f;
	private static final float TITLE_DRAWN_TOTAL = TITLE_DRAWN_OPAQUE + TITLE_DRAWN_BLEND;
	Transform t = new Transform();
	private GameClientState state;
	private GUITextOverlay location;

	private float displayed;
	private Transform inv = new Transform();
	private Vector3f c = new Vector3f();
	private GUIOverlay panel;
	private GUIOverlay panelLarge;
	private int listSmall;
	private int listLarge;
	private Vector4f tint = new Vector4f();
	private GUITextOverlay secString;
	//INSERTED CODE
	public GUITextOverlay getLocation() {
		return location;
	}
	///

	public Radar(InputState state) {
		super(state);
		this.state = (GameClientState) state;
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (EngineSettings.MINIMAP_MODE.getObject() == MinimapMode.OFF) {
			return;
		}
		GlUtil.glPushMatrix();
		transform();
		int textDiff = UIScale.getUIScale().scale(-18);
		int lineHeight = UIScale.getUIScale().scale(18);

		GUIOverlay p;
		if (EngineSettings.MINIMAP_MODE.getObject() == MinimapMode.SMALL) {
			panel.draw();
			drawCircleHUD(false);
			p = panel;

		} else {
			panelLarge.draw();
			drawCircleHUD(true);
			p = panelLarge;
		}

		secString.getPos().x = (int) (p.getWidth() / 2 - secString.getMaxLineWidth() / 2);
		secString.getPos().y = p.getHeight() + textDiff;

		int extra = 0;
		if ((int) ((p.getWidth() / 2) + (location.getMaxLineWidth() / 2)) > p.getWidth()) {
			extra = (int) p.getWidth() - (int) ((p.getWidth() / 2) + (location.getMaxLineWidth() / 2));
		}

		location.getPos().x = (int) (p.getWidth() / 2 - location.getMaxLineWidth() / 2) + extra;
		location.getPos().y = p.getHeight() + lineHeight + textDiff;

		if (displayed < TITLE_DRAWN_TOTAL) {
			if (displayed > TITLE_DRAWN_OPAQUE) {
				float tAlph = displayed - TITLE_DRAWN_OPAQUE;
				float pAlph = tAlph / TITLE_DRAWN_BLEND;
				secString.getColor().a = 1.0f - pAlph;
			} else {
				secString.getColor().a = 1.0f;
			}
			secString.draw();
		} else {
			secString.getColor().a = 0;
		}

		for (AbstractSceneNode f : getChilds()) {
			f.draw();
		}
		GlUtil.glPopMatrix();

	}

	@Override
	public void onInit() {
		this.panel = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_Map-2x1-gui-"), getState());
		this.panelLarge = new GUIOverlay(Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_MapLarge-2x1-gui-"), getState());

		this.secString = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		this.secString.setTextSimple(Lng.str("SECTOR"));
		this.location = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		this.location.setTextSimple(new Object() {
			@Override
			public String toString() {
				if (((GameClientState) getState()).getPlayer().isInTutorial()) {
					return "<Tutorial>";
				}
				if (((GameClientState) getState()).getPlayer().isInPersonalSector()) {
					return Lng.str("<Personal>");
				}
				if (((GameClientState) getState()).getPlayer().isInTestSector()) {
					return Lng.str("<Test>");
				}
				return ((GameClientState) getState()).getPlayer().getCurrentSector().toStringPure();
			}
		});

		attach(secString);
		attach(location);
	}

	private int createBgCircles(float radius) {

		int list = GL11.glGenLists(1);

		GL11.glNewList(list, GL11.GL_COMPILE);

		float s = 1.0f;
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GlUtil.glColor4f(1, 1, 1, 0.5f);
		for (float i = 0; i < FastMath.TWO_PI; i += FastMath.PI / 24f) { //<-- Change this Value
			GL11.glVertex3f(FastMath.cos(i) * radius * s, 0, FastMath.sin(i) * radius * s);
		}
		GL11.glVertex3f(FastMath.cos(0) * radius * s, 0, FastMath.sin(0) * radius * s);
		GL11.glEnd();

		s = 0.85f;
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GlUtil.glColor4f(1, 1, 1, 0.1f);
		for (float i = 0; i < FastMath.TWO_PI; i += FastMath.PI / 24f) { //<-- Change this Value
			GL11.glVertex3f(FastMath.cos(i) * radius * s, 0, FastMath.sin(i) * radius * s);
		}
		GL11.glVertex3f(FastMath.cos(0) * radius * s, 0, FastMath.sin(0) * radius * s);
		GL11.glEnd();

		s = 0.6f;
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GlUtil.glColor4f(1, 1, 1, 0.1f);
		for (float i = 0; i < FastMath.TWO_PI; i += FastMath.PI / 24f) { //<-- Change this Value
			GL11.glVertex3f(FastMath.cos(i) * radius * s, 0, FastMath.sin(i) * radius * s);
		}
		GL11.glVertex3f(FastMath.cos(0) * radius * s, 0, FastMath.sin(0) * radius * s);
		GL11.glEnd();

		s = 0.3f;
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GlUtil.glColor4f(1, 1, 1, 0.1f);
		for (float i = 0; i < FastMath.TWO_PI; i += FastMath.PI / 24f) { //<-- Change this Value
			GL11.glVertex3f(FastMath.cos(i) * radius * s, 0, FastMath.sin(i) * radius * s);
		}
		GL11.glVertex3f(FastMath.cos(0) * radius * s, 0, FastMath.sin(0) * radius * s);
		GL11.glEnd();

		GL11.glEndList();

		return list;
	}

	private void drawBgCircles(boolean large) {

		float radius;
		if (large) {
			radius = UIScale.getUIScale().scale(120);
			if (listLarge != 0) {
				GL11.glCallList(listLarge);
			} else {
				listLarge = createBgCircles(radius);
			}
		} else {
			radius = UIScale.getUIScale().scale(60);
			if (listSmall != 0) {
				GL11.glCallList(listSmall);
			} else {
				listSmall = createBgCircles(radius);
			}
		}

	}

	private void drawCircleHUD(boolean large) {
		if (state.getCurrentPlayerObject() == null) {
			return;
		}
		GlUtil.glEnable(GL11.GL_BLEND);
		GL11.glLineWidth(1);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		GlUtil.glPushMatrix();

		Transform model = new Transform();
		Controller.getMat(Controller.modelviewMatrix, model);
		GUIElement.enableOrthogonal3d();

		t.setIdentity();
//		t.basis.set(state.getCurrentPlayerObject().getWorldTransform().basis);
//		t.basis.invert();
		t.basis.rotX(0.368f);
		GlUtil.glMultMatrix(model);
		GlUtil.translateModelview(getWidth() / 2, getHeight() / 2, 0);
		GlUtil.glMultMatrix(t);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);

		drawBgCircles(large);

		PlayerInteractionControlManager playerIntercationManager = ((GameClientState) getState()).
				getGlobalGameControlManager().
				getIngameControlManager().
				getPlayerGameControlManager().
				getPlayerIntercationManager();

		SimpleTransformableSendableObject selectedEntity = playerIntercationManager.getSelectedEntity();

		if (state.getCurrentPlayerObject() instanceof PlayerCharacter) {
			t.setIdentity();
			((PlayerCharacter) state.getCurrentPlayerObject()).getOwnerState().getWordTransform(t);
			t.origin.set(state.getCurrentPlayerObject().getWorldTransform().origin);
		} else {
			t.set(state.getCurrentPlayerObject().getWorldTransform());
		}
		t.inverse();

		float size;

		if (large) {
			size = UIScale.getUIScale().scale(120);
		} else {
			size = UIScale.getUIScale().scale(60);
		}

		GL11.glBegin(GL11.GL_LINES);
		for (SimpleTransformableSendableObject o : state.getCurrentSectorEntities().values()) {

			if(NavigationControllerManager.isVisibleRadar(o)){
				c.set(o.getWorldTransformOnClient().origin);
				t.transform(c);
				c.scale(0.02f);
				if (c.length() < size / 1.2f) {
	//				HudIndicatorOverlay.getColor(o, tint, selectedEntity == o, state);
	//				c.x = (size/2-c.x);
	//				c.y = (size/2-c.x);
	//				c.z = (size/2-c.z);
	//				GlUtil.glColor4f(tint.x,tint.y,tint.z,tint.w);
					GlUtil.glColor4f(0.3f, 0.3f, 0.7f, 0.5f);
					GL11.glVertex3f(-c.x, -c.y, c.z);
					GL11.glVertex3f(-c.x, 0, c.z);
					GlUtil.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
					GL11.glVertex3f(-c.x, 0, c.z);
					GL11.glVertex3f(0, 0, 0);
				}
			}
		}
		GlUtil.glColor4f(1, 1, 1, 1);
		GL11.glEnd();

		GL11.glPointSize(2);
		GL11.glBegin(GL11.GL_POINTS);
		for (SimpleTransformableSendableObject o : state.getCurrentSectorEntities().values()) {
			
			if(NavigationControllerManager.isVisibleRadar(o)){
				HudIndicatorOverlay.getColor(o, tint, o == selectedEntity, (GameClientState) getState());
				tint.w = 0.8f;

				c.set(o.getWorldTransformOnClient().origin);
				t.transform(c);
				c.scale(0.02f);
				if (c.length() < size / 1.2f) {
	//				HudIndicatorOverlay.getColor(o, tint, selectedEntity == o, state);
	//				c.x = (size/2-c.x);
	//				c.y = (size/2-c.x);
	//				c.z = (size/2-c.z);
	//				GlUtil.glColor4f(tint.x,tint.y,tint.z,tint.w);
					GlUtil.glColor4f(tint);
					GL11.glVertex3f(-c.x, -c.y, c.z);
				}
			}
		}
		GlUtil.glColor4f(1, 1, 1, 1);
		GL11.glEnd();

		GUIElement.disableOrthogonal();

		GlUtil.glPopMatrix();

	}

	@Override
	public float getHeight() {
		if (EngineSettings.MINIMAP_MODE.getObject() == MinimapMode.LARGE) {
			return panelLarge.getHeight();
		} else {
			return panel.getHeight();
		}
	}

	@Override
	public float getWidth() {
		if (EngineSettings.MINIMAP_MODE.getObject() == MinimapMode.LARGE) {
			return panelLarge.getWidth();
		} else {
			return panel.getWidth();
		}
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		displayed += timer.getDelta();
	}

	public void resetDrawn() {
		displayed = 0;
	}

}
