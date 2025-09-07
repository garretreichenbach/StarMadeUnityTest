package org.schema.schine.graphicsengine.psys.modules;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.psys.ParticleContainer;
import org.schema.schine.graphicsengine.psys.ParticleSystemConfiguration;
import org.schema.schine.graphicsengine.psys.modules.iface.ParticleStartInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.BooleanInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.DropDownInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.StringPair;
import org.schema.schine.graphicsengine.psys.modules.variable.VarInterface;
import org.schema.schine.graphicsengine.psys.modules.variable.XMLSerializable;

import com.bulletphysics.linearmath.Transform;

public class ShapeModule extends ParticleSystemModule implements ParticleStartInterface {

	public static int SPHERE = 0;
	public static int HEMISPHERE = 1;
	public static int CONE = 2;
	public static int BOX = 3;
	public static int MESH = 4;
	@XMLSerializable(name = "angle", type = "float")
	float angle;
	@XMLSerializable(name = "radius", type = "float")
	float radius;
	@XMLSerializable(name = "emitFromShell", type = "boolean")
	boolean emitFromShell = true;
	@XMLSerializable(name = "randomDirection", type = "boolean")
	boolean randomDirection;
	@XMLSerializable(name = "boxX", type = "float")
	float boxX = 1;
	@XMLSerializable(name = "boxY", type = "float")
	float boxY = 1;
	@XMLSerializable(name = "boxZ", type = "float")
	float boxZ = 1;
	@XMLSerializable(name = "mesh", type = "string")
	String mesh = "";
	@XMLSerializable(name = "type", type = "int")
	private int type;

	public ShapeModule(ParticleSystemConfiguration sys) {
		super(sys);
	}

	@Override
	protected JPanel getConfigPanel() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int i = 0;
		addRow(p, i++, new DropDownInterface(
				new StringPair("sphere", SPHERE),
				new StringPair("hemisphere", HEMISPHERE),
				new StringPair("cone", CONE),
				new StringPair("box", BOX),
				new StringPair("mesh", MESH)
		) {

			@Override
			public String getName() {
				return "Shape";
			}

			@Override
			public int getCurrentIndex() {
				return type;
			}

			@Override
			public void set(StringPair selectedItem) {
				type = selectedItem.val;
			}
		});
		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "angle";
			}

			@Override
			public Float get() {
				return angle;
			}

			@Override
			public void set(String f) {
				angle = Math.max(0, Math.min(1, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 0f;
			}
		});
		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "radius";
			}

			@Override
			public Float get() {
				return radius;
			}

			@Override
			public void set(String f) {
				radius = Math.max(0, Math.min(1, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 0f;
			}
		});

		addRow(p, i++, new BooleanInterface() {

			@Override
			public boolean get() {
				return emitFromShell;
			}

			@Override
			public void set(boolean selected) {
				emitFromShell = selected;
			}

			@Override
			public String getName() {
				return "emit from shell";
			}

		});
		addRow(p, i++, new BooleanInterface() {

			@Override
			public void set(boolean selected) {
				randomDirection = selected;
			}

			@Override
			public String getName() {
				return "random Direction";
			}

			@Override
			public boolean get() {
				return randomDirection;
			}
		});

		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "boxX";
			}

			@Override
			public Float get() {
				return boxX;
			}

			@Override
			public void set(String f) {
				boxX = Math.max(0, Math.min(1, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 0f;
			}
		});
		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "boxY";
			}

			@Override
			public Float get() {
				return boxY;
			}

			@Override
			public void set(String f) {
				boxY = Math.max(0, Math.min(1, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 0f;
			}
		});
		addRow(p, i++, new VarInterface<Float>() {
			@Override
			public String getName() {
				return "boxZ";
			}

			@Override
			public Float get() {
				return boxZ;
			}

			@Override
			public void set(String f) {
				boxZ = Math.max(0, Math.min(1, Float.parseFloat(f)));
			}

			@Override
			public Float getDefault() {
				return 0f;
			}
		});
		addRow(p, i++, new VarInterface<String>() {
			@Override
			public String getName() {
				return "mesh";
			}

			@Override
			public String get() {
				return mesh;
			}

			@Override
			public void set(String f) {
				mesh = f;
			}

			@Override
			public String getDefault() {
				return "";
			}
		});
		return p;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.ParticleSystemModule#getName()
	 */
	@Override
	public String getName() {
		return "Shape";
	}

	@Override
	public void onParticleSpawn(ParticleContainer newParticle, Transform systemTransform) {
		newParticle.position.set(systemTransform.origin);
	}

}
