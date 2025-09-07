package org.schema.game.client.view.effects;

import java.util.ArrayList;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.Sprite;

public class OcclusionLensflare implements Drawable {

	public float sizeMult = 1;
	public float extraScale = 6;
	public boolean drawLensFlareEffects = true;
	public boolean depthTest;
	Vector3f camPos = new Vector3f();
	Vector3f m_LightSourcePos = new Vector3f();
	Vector3f vLightSourceToCamera = new Vector3f();
	Vector3f ptIntersect = new Vector3f();
	Vector3f vLightSourceToIntersect = new Vector3f();
	Vector3f pt = new Vector3f();
	ArrayList<FlareElement> elements = new ArrayList<FlareElement>(16);

	float length;
	private Sprite sprite;
	private int pointer;
	private Vector4f mainColor = new Vector4f();
	public OcclusionLensflare() {
		for (int i = 0; i < 16; i++) {
			elements.add(new FlareElement());
		}
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		assert (sprite != null);
		pointer = 0;
		m_LightSourcePos.set(getLightPos());
		camPos.set(Controller.getCamera().getPos());

		vLightSourceToCamera.sub(camPos, m_LightSourcePos);       // Lets Compute The Vector That Points To
		// The Camera From The Light Source.

		length = vLightSourceToCamera.length();          // Save The Length We Will Need It In A Minute

		ptIntersect.scale(length, Controller.getCamera().getForward());// = m_DirectionVector * Length;           // Now Lets Find A Point Along The Cameras Direction
		// Vector That We Can Use As An Intersection Point
		// Lets Translate Down This Vector The Same Distance
		// That The Camera Is. Away From The Light Source.
		ptIntersect.add(camPos);

		vLightSourceToIntersect.sub(ptIntersect, m_LightSourcePos);   // Lets Compute The Vector That Points To The Intersect
		// Point From The Light Source
		length = vLightSourceToIntersect.length();           // Save The Length We Will Need It Later
		vLightSourceToIntersect.normalize();                // Normalize The Vector So Its Unit Length

		// Render The Large Hazy Glow
		if (mainColor.x == 1 && mainColor.y == 1 && mainColor.z == 1) {
			RenderBigGlow(0.60f, 0.60f, 0.7f, mainColor.w * 1.0f, m_LightSourcePos, 16.0f * sizeMult);
			// Render The Streaks
			RenderStreaks(0.60f, 0.60f, 0.8f, 1.0f, m_LightSourcePos, 16.0f * sizeMult);
		} else {
			RenderBigGlow(mainColor.x * 0.80f, mainColor.y * 0.80f, mainColor.z * 0.8f, mainColor.w * 1.0f, m_LightSourcePos, 16.0f * sizeMult);
			// Render The Streaks
			RenderStreaks(mainColor.x * 0.80f, mainColor.y * 0.80f, mainColor.z * 0.8f, 1.0f, m_LightSourcePos, 16.0f * sizeMult);
		}

		// Render The Small Glow
		RenderGlow(0.8f, 0.8f, 1.0f, 0.9f, m_LightSourcePos, 3.5f * sizeMult);

		if (drawLensFlareEffects) {
			length *= 2;

			pt.scale((length * 0.1f), vLightSourceToIntersect);
			;     // Lets Compute A Point That Is 20%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderGlow(0.9f, 0.6f, 0.4f, 0.5f, pt, 0.6f);       // Render The Small Glow

			pt.scale((length * 0.15f), vLightSourceToIntersect);
			;    // Lets Compute A Point That Is 30%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderHalo(0.8f, 0.5f, 0.6f, 0.5f, pt, 1.7f);       // Render The Halo

			pt.scale((length * 0.175f), vLightSourceToIntersect);
			;   // Lets Compute A Point That Is 35%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderHalo(0.9f, 0.2f, 0.1f, 0.5f, pt, 0.83f);      // Render The Halo

			pt.scale((length * 0.285f), vLightSourceToIntersect);
			;   // Lets Compute A Point That Is 57%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderHalo(0.7f, 0.7f, 0.4f, 0.5f, pt, 1.6f);       // Render The Halo

			pt.scale((length * 0.2755f), vLightSourceToIntersect);
			;  // Lets Compute A Point That Is 55.1%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderGlow(0.9f, 0.9f, 0.2f, 0.5f, pt, 0.8f);       // Render The Small Glow

			pt.scale((length * 0.4775f), vLightSourceToIntersect);
			;  // Lets Compute A Point That Is 95.5%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderGlow(0.93f, 0.82f, 0.73f, 0.5f, pt, 1.0f);    // Render The Small Glow

			pt.scale((length * 0.49f), vLightSourceToIntersect);
			;    // Lets Compute A Point That Is 98%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderHalo(0.7f, 0.6f, 0.5f, 0.5f, pt, 1.4f);       // Render The Halo

			pt.scale((length * 0.65f), vLightSourceToIntersect);
			;    // Lets Compute A Point That Is 130%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderGlow(0.7f, 0.8f, 0.3f, 0.5f, pt, 1.8f);       // Render The Small Glow

			pt.scale((length * 0.63f), vLightSourceToIntersect);
			;    // Lets Compute A Point That Is 126%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderGlow(0.4f, 0.3f, 0.2f, 0.5f, pt, 1.4f);       // Render The Small Glow

			pt.scale((length * 0.8f), vLightSourceToIntersect);
			;     // Lets Compute A Point That Is 160%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderHalo(0.7f, 0.5f, 0.5f, 0.5f, pt, 1.4f);       // Render The Halo

			pt.scale((length * 0.7825f), vLightSourceToIntersect);
			;  // Lets Compute A Point That Is 156.5%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderGlow(0.8f, 0.5f, 0.1f, 0.5f, pt, 0.6f);       // Render The Small Glow

			pt.scale((length * 1.0f), vLightSourceToIntersect);
			;     // Lets Compute A Point That Is 200%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderHalo(0.5f, 0.5f, 0.7f, 0.5f, pt, 1.7f);       // Render The Halo

			pt.scale((length * 0.975f), vLightSourceToIntersect);
			;   // Lets Compute A Point That Is 195%
			pt.add(m_LightSourcePos);                 // Away From The Light Source In The
			// Direction Of The Intersection Point

			RenderGlow(0.4f, 0.1f, 0.9f, 0.5f, pt, 2.0f);       // Render The Small Glow
		} else {
			for (int i = pointer; i < elements.size(); i++) {
				elements.get(i).canDraw = false;
			}
		}
		sprite.blendFunc = Sprite.BLEND_SEPERATE_NORMAL;
		sprite.setDepthTest(depthTest);

		Sprite.draw3D(sprite, elements, Controller.getCamera());
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		this.sprite = Controller.getResLoader().getSprite("lens_flare-4x1-c-");
		assert (sprite != null);
	}

	public Vector3f getLightPos() {
		return AbstractScene.mainLight.getPos();
	}

	private FlareElement flare(float r, float g, float b, float a,
	                           Vector3f pt, float scale) {
		FlareElement f = elements.get(pointer++);
		f.canDraw = true;
		f.pos.set(pt);
		f.scale = scale * extraScale;
		f.color.set(r, g, b, a);
		return f;
	}

	private void RenderBigGlow(float r, float g, float b, float a,
	                           Vector3f pt, float scale) {
		FlareElement f = flare(r, g, b, a, pt, scale);
		f.subSprite = 3;
	}

	private void RenderGlow(float r, float g, float b, float a,
	                        Vector3f pt, float scale) {
		FlareElement f = flare(r, g, b, a, pt, scale);
		f.subSprite = 1;
	}

	private void RenderHalo(float r, float g, float b, float a,
	                        Vector3f pt, float scale) {
		FlareElement f = flare(r, g, b, a, pt, scale);
		f.subSprite = 0;
	}

	private void RenderStreaks(float r, float g, float b, float a,
	                           Vector3f pt, float scale) {
		FlareElement f = flare(r, g, b, a, pt, scale);
		f.subSprite = 2;
	}

	public void setFillRate(float f) {
	}

	public void setMainColor(Vector4f color) {
		this.mainColor.set(color);
	}

	private class FlareElement implements PositionableSubColorSprite {
		public Vector4f color = new Vector4f();
		Vector3f pos = new Vector3f();
		float scale;
		int subSprite = 0;
		private boolean canDraw = true;

		@Override
		public Vector4f getColor() {
			return color;
		}

		@Override
		public Vector3f getPos() {
			return pos;
		}

		@Override
		public float getScale(long time) {
			return scale;
		}

		@Override
		public int getSubSprite(Sprite sprite) {
			return subSprite;
		}

		@Override
		public boolean canDraw() {
			return canDraw;
		}
	}

}
