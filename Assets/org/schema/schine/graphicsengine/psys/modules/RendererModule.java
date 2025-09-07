package org.schema.schine.graphicsengine.psys.modules;

import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.psys.Particle;
import org.schema.schine.graphicsengine.psys.ParticleSystem;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.ParticleVertexBuffer;
import org.schema.schine.graphicsengine.psys.modules.variable.BooleanInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.DropDownInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.StringPair;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

public class RendererModule extends ParticleSystemModule {

	public static final int RENDER_MODE_BILLBOARD = 0;
	public static final int RENDER_MODE_STRETCHED_BILLBOARD = 1;
	public static final int RENDER_MODE_HORIZONTAL_BILLBOARD = 2;
	public static final int RENDER_MODE_VERTICAL_BILLBOARD = 3;
	public static final int RENDER_MODE_MESH = 4;
	private final Vector3f camPos = new Vector3f();
	@XMLSerializable(name = "renderMode", type = "int")
	int renderMode = RENDER_MODE_BILLBOARD;
	@XMLSerializable(name = "sort", type = "boolean")
	boolean sort = true;
	@XMLSerializable(name = "sortingFudge", type = "int")
	int sortingFudge;
	@XMLSerializable(name = "normalDirection", type = "float")
	float normalDirection = 1;
	@XMLSerializable(name = "cameraScale", type = "float")
	float cameraScale;
	@XMLSerializable(name = "speedScale", type = "float")
	float speedScale;
	@XMLSerializable(name = "lengthScale", type = "float")
	float lengthScale;
	@XMLSerializable(name = "material", type = "string")
	String material = "smoke_color";
	@XMLSerializable(name = "lighting", type = "boolean")
	boolean lighting = false;
	@XMLSerializable(name = "frustumCulling", type = "int")
	int frustumCulling = FrustumCullingMethod.SINGLE.ordinal();

	public RendererModule(ParticleSystemConfiguration sys) {
		super(sys);
		setEnabled(true);
	}

	@Override
	public boolean canDisable() {
		return false;
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;

		addRow(p, i++, new DropDownInterface(
				new StringPair("billboard", RENDER_MODE_BILLBOARD),
				new StringPair("stretched billboard", RENDER_MODE_STRETCHED_BILLBOARD),
				new StringPair("horizontal billboard", RENDER_MODE_HORIZONTAL_BILLBOARD),
				new StringPair("vertical billboard", RENDER_MODE_VERTICAL_BILLBOARD),
				new StringPair("mesh", RENDER_MODE_MESH)) {

			@Override
			public String getName() {
				return "Render Mode";
			}

			@Override
			public int getCurrentIndex() {
				return renderMode;
			}

			@Override
			public void set(StringPair selectedItem) {
				renderMode = selectedItem.val;
			}
		});
		addRow(p, i++, new VarInterface<String>() {

			@Override
			public String getName() {
				return "material";
			}

			@Override
			public String get() {
				return material;
			}

			@Override
			public void set(String f) {
				material = f;
			}

			@Override
			public String getDefault() {
				return "flare";
			}

		});
		addRow(p, i++, new BooleanInterface() {

			@Override
			public boolean get() {
				return sort;
			}

			@Override
			public void set(boolean selected) {
				sort = selected;
			}

			@Override
			public String getName() {
				return "sort";
			}

		});
		addRow(p, i++, new VarInterface<Integer>() {

			@Override
			public String getName() {
				return "sorting fudge";
			}

			@Override
			public Integer get() {
				return sortingFudge;
			}

			@Override
			public void set(String f) {
				sortingFudge = Math.max(0, Integer.parseInt(f));
			}

			@Override
			public Integer getDefault() {
				return 0;
			}

		});

		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "[stretch] camera scale";
			}

			@Override
			public Float get() {
				return cameraScale;
			}

			@Override
			public void set(String f) {
				cameraScale = Math.max(0, Float.parseFloat(f));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}
		});
		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "[stretch] speed scale";
			}

			@Override
			public Float get() {
				return speedScale;
			}

			@Override
			public void set(String f) {
				speedScale = Math.max(0, Float.parseFloat(f));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}
		});
		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "[stretch] length scale";
			}

			@Override
			public Float get() {
				return lengthScale;
			}

			@Override
			public void set(String f) {
				lengthScale = Math.max(0, Float.parseFloat(f));
			}

			@Override
			public Float getDefault() {
				return 1.0f;
			}
		});
		addRow(p, i++, new BooleanInterface() {

			@Override
			public void set(boolean selected) {
				lighting = selected;
			}

			@Override
			public String getName() {
				return "lighting";
			}

			@Override
			public boolean get() {
				return lighting;
			}
		});
		addRow(p, i++, new DropDownInterface(
				new StringPair("NONE", FrustumCullingMethod.NONE.ordinal()),
				new StringPair("SINGLE", FrustumCullingMethod.SINGLE.ordinal()),
				new StringPair("ACCURATE", FrustumCullingMethod.ACCURATE.ordinal())) {

			@Override
			public String getName() {
				return "frustum culling";
			}

			@Override
			public int getCurrentIndex() {
				return frustumCulling;
			}

			@Override
			public void set(StringPair selectedItem) {
				frustumCulling = selectedItem.val;
			}
		});

		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Renderer";
	}

	public void draw(ParticleSystem pSys, ParticleVertexBuffer vbo) {

		Sprite sprite = Controller.getResLoader().getSprite(material);

		if (sprite != null) {
			if (!lighting) {
				GlUtil.glDisable(GL11.GL_LIGHTING);
			}
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, sprite.getMaterial().getTexture().getTextureId());
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);

		}
		if (Controller.getCamera() != null) {
			camPos.set(Controller.getCamera().getPos());
		}
		if (sort) {
			for (int particleIndex = 0; particleIndex < sys.getParticleCount(); particleIndex++) {
				Particle.updateDistance(particleIndex, pSys.getRawParticles());
			}
			pSys.sort(sys.getParticleCount());
		}
		vbo.draw(pSys, FrustumCullingMethod.values()[frustumCulling]);

		if (sprite != null) {
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
			if (!lighting) {
				GlUtil.glEnable(GL11.GL_LIGHTING);
			}
		}
//		GL11.glBegin(GL11.GL_POINTS);
//		for(int i = 0; i < sys.getMaxParticles(); i++){
//			GL11.glVertex3f(Particle.getPosX(i, rawParticles), Particle.getPosY(i, rawParticles), Particle.getPosZ(i, rawParticles));
//		}
//		GL11.glEnd();
	}

	public enum FrustumCullingMethod {
		NONE,
		SINGLE,
		ACCURATE
	}
}
