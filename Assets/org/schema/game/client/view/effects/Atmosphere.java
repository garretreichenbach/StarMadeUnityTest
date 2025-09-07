package org.schema.game.client.view.effects;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.input.Keyboard;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;

public class Atmosphere implements Shaderable, Drawable {

	//	private Shader ShaderLibrary.skyFromAtmo = ShaderLibrary.skyFromAtmo;
	//	private Shader ShaderLibrary.skyFromSpace = ShaderLibrary.skyFromSpace;

	private final GameClientState state;
	private float m_Kr;
	private float m_Kr4PI;
	private float m_Km;
	private float m_Km4PI;
	private float m_ESun;
	private float m_fInnerRadius;
	private float m_fOuterRadius;
	@SuppressWarnings("unused")
	private float m_fScale;
	private float[] m_fWavelength = new float[3];
	private float[] m_fWavelength4 = new float[3];
	private float m_fRayleighScaleDepth;
	private Mesh pSphere;
	private boolean init;
	private PlanetType type;
	private float currentRadius = 0;
	private boolean drawError;

	private Vector3i currentSec = new Vector3i();
	public Atmosphere(GameClientState state) {
		this.state = state;
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		drawError = Keyboard.isKeyDown(GLFW.GLFW_KEY_F2);

		if (EngineSettings.G_ATMOSPHERE_SHADER.getObject() == AtmosphereShaderSetting.NONE) {
			return;
		}
		if (state.getPlayer() == null) {
			return;
		}
		ShaderLibrary.skyFromAtmo.setShaderInterface(this);
		ShaderLibrary.skyFromSpace.setShaderInterface(this);
		Vector3i s = state.getPlayer().getCurrentSector();

		String planetUI = SimpleTransformableSendableObject.EntityType.PLANET_ICO.dbPrefix + "CORE_" + s.x + "_" + s.y + "_" + s.z;

		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(planetUI);

		if (!(sendable instanceof PlanetIco)) {
			if(!currentSec.equals(s)) {
				System.err.println("[CLIENT][ATHMOSPHERE] cannot draw. no core: " + sendable);
			}
			currentSec.set(s);
			return;
		}
		currentSec.set(s);
		PlanetIco core = ((PlanetIco) sendable);
		//ENTITY_PLANETCORE_8_8_5
		//		handleInput();

		float atmosScale = core.radius * 2.0f;

		if (!init || currentRadius != atmosScale) {
			System.err.println("[CLIENT][ATHMOSPHERE] initializing athomsphere: " + core + "; RADIUS: " + core.radius);
			currentRadius = atmosScale;
			onInit();
		}
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		//		handleInput();
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		pSphere.loadVBO(true);

		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		boolean inside = isInside();
		if (inside) {
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
			ShaderLibrary.skyFromAtmo.load();
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
			GL11.glCullFace(GL11.GL_FRONT);
			//
			drawPass();
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
		} else {
			//			GL11.glCullFace(GL11.GL_BACK);
			GL11.glFrontFace(GL11.GL_CW);
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
			ShaderLibrary.skyFromSpace.load();
			drawPass();
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
			//			GL11.glCullFace(GL11.GL_BACK);
			//			drawPass();
		}
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}

		GL11.glCullFace(GL11.GL_BACK);
		GL11.glFrontFace(GL11.GL_CCW);
		GlUtil.glDisable(GL11.GL_BLEND);
		if (inside) {
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
			ShaderLibrary.skyFromAtmo.unload();
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
		} else {
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
			ShaderLibrary.skyFromSpace.unload();
			if (drawError) {
				GlUtil.printGlErrorCritical();
			}
		}
		pSphere.unloadVBO(true);
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		if (EngineSettings.G_ATMOSPHERE_SHADER.getObject() == AtmosphereShaderSetting.NONE) {
			return;
		}
		m_Kr = 0.0025f;    //0.0025f;	// Rayleigh scattering constant
		m_Km = 0.0010f;    //0.0010f	// Mie scattering constant

		m_Kr4PI = m_Kr * 4.0f * (FastMath.PI);
		m_Km4PI = m_Km * 4.0f * (FastMath.PI);
		m_ESun = 40.0f;//20.0 // Sun brightness constant
		//				m_fInnerRadius = 10.0f; //orig 10.0
		//				m_fOuterRadius = 10.25f;//+300.0f; //orig 10.25
		m_fInnerRadius = currentRadius - 60.0F; //orig 10.0
		m_fOuterRadius = currentRadius; //orig 10.25
		m_fScale = 1f / (m_fOuterRadius - m_fInnerRadius);

		m_fWavelength[0] = 0.650f;        // 650 nm for red
		m_fWavelength[1] = 0.570f;        // 570 nm for green
		m_fWavelength[2] = 0.475f;        // 475 nm for blue

		m_fWavelength4[0] = FastMath.pow(m_fWavelength[0], 4.0f);
		m_fWavelength4[1] = FastMath.pow(m_fWavelength[1], 4.0f);
		m_fWavelength4[2] = FastMath.pow(m_fWavelength[2], 4.0f);

		m_fRayleighScaleDepth = 0.25f; //orig 0.25
		pSphere = (Mesh) Controller.getResLoader().getMesh("GeoSphere").getChilds().get(0);
		init = true;
	}

	private void drawPass() {

		GlUtil.glPushMatrix();
		pSphere.renderVBO();//m_fOuterRadius, 100, 50
		GlUtil.glPopMatrix();

	}

	public boolean isInside() {
		Vector3f cPos = new Vector3f(Controller.getCamera().getPos());
		//		cPos.y += m_fInnerRadius+yTranslate;
		return cPos.length() < m_fOuterRadius;
	}

	@Override
	public void onExit() {
		
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}

		float normalization = 1f / 300f;
		GlUtil.updateShaderFloat(shader, "meshScale", normalization * m_fOuterRadius);
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderVector4f(shader, "tint", type.atmosphereInner);
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		Vector3f camPos = new Vector3f(Controller.getCamera().getPos());

		GlUtil.updateShaderVector3f(shader, "v3CameraPos", camPos);
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		//confirmed to be dir and not pos...
		Vector3f lpos = new Vector3f(AbstractScene.mainLight.getPos());
		lpos.sub(camPos);
		lpos.normalize();
		GlUtil.updateShaderVector3f(shader, "v3LightPos", lpos);
		//		shader->SetUniformParameter3f("v3LightPos", m_vLightDirection.x, m_vLightDirection.y, m_vLightDirection.z);
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderVector3f(shader, "v3InvWavelength", 1f / m_fWavelength4[0], 1f / m_fWavelength4[1], 1f / m_fWavelength4[2]);
		//		shader->SetUniformParameter3f("v3InvWavelength", 1/m_fWavelength4[0], 1/m_fWavelength4[1], 1/m_fWavelength4[2]);

		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "fCameraHeight", camPos.length() /*Math.max(1, camPos.y)*/);
		//		shader->SetUniformParameter1f("fCameraHeight", vCamera.Magnitude());

		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "fCameraHeight2", camPos.lengthSquared()/*camPos.lengthSquared()/2f*/ /*(Math.max(1, camPos.y) )* (Math.max(1, camPos.y) )*/);
		//		shader->SetUniformParameter1f("fCameraHeight2", vCamera.MagnitudeSquared());
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "fInnerRadius", m_fInnerRadius);
		//		shader->SetUniformParameter1f("fInnerRadius", m_fInnerRadius);

		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "fInnerRadius2", m_fInnerRadius * m_fInnerRadius);
		//		shader->SetUniformParameter1f("fInnerRadius2", m_fInnerRadius*m_fInnerRadius);
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.updateShaderFloat(shader, "fOuterRadius", m_fOuterRadius);
		//		shader->SetUniformParameter1f("fOuterRadius", m_fOuterRadius);

		GlUtil.updateShaderFloat(shader, "fOuterRadius2", m_fOuterRadius * m_fOuterRadius);
		//		shader->SetUniformParameter1f("fOuterRadius2", m_fOuterRadius*m_fOuterRadius);

		GlUtil.updateShaderFloat(shader, "fKrESun", m_Kr * m_ESun);
		//		shader->SetUniformParameter1f("fKrESun", m_Kr*m_ESun);

		GlUtil.updateShaderFloat(shader, "fKmESun", m_Km * m_ESun);
		//		shader->SetUniformParameter1f("fKmESun", m_Km*m_ESun);

		GlUtil.updateShaderFloat(shader, "fKr4PI", m_Kr4PI);
		//		shader->SetUniformParameter1f("fKr4PI", m_Kr4PI);

		GlUtil.updateShaderFloat(shader, "fKm4PI", m_Km4PI);
		//		shader->SetUniformParameter1f("fKm4PI", m_Km4PI);

		GlUtil.updateShaderFloat(shader, "fScale", m_fScale);
		//		shader->SetUniformParameter1f("fScale", 1.0f / (m_fOuterRadius - m_fInnerRadius));

		GlUtil.updateShaderFloat(shader, "fScaleDepth", m_fRayleighScaleDepth);
		//		shader->SetUniformParameter1f("fScaleDepth", m_fRayleighScaleDepth);

		GlUtil.updateShaderFloat(shader, "fScaleOverScaleDepth", m_fScale / m_fRayleighScaleDepth);
		//		shader->SetUniformParameter1f("fScaleOverScaleDepth", (1.0f / (m_fOuterRadius - m_fInnerRadius)) / m_fRayleighScaleDepth);
		if (drawError) {
			GlUtil.printGlErrorCritical();
		}
		//		GlUtil.updateShaderFloat(shader, "g", m_g);
		//		//		shader->SetUniformParameter1f("g", m_g);
		//
		//		GlUtil.updateShaderFloat(shader, "g2", m_g*m_g);
		//		//		shader->SetUniformParameter1f("g2", m_g*m_g);

	}

	public void setPlanetType(PlanetType planetType) {
		this.type = planetType;
	}

}
