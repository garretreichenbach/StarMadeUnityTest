package org.schema.schine.common.util;


import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.Keyboard;

public class BlendTool {

	private static int sfactors[] = new int[]{
			GL11.GL_ZERO,
			GL11.GL_ONE,
			GL11.GL_SRC_COLOR,
			GL11.GL_ONE_MINUS_SRC_COLOR,
			GL11.GL_DST_COLOR,
			GL11.GL_ONE_MINUS_DST_COLOR,
			GL11.GL_SRC_ALPHA,
			GL11.GL_ONE_MINUS_SRC_ALPHA,
			GL11.GL_DST_ALPHA,
			GL11.GL_ONE_MINUS_DST_ALPHA,
			GL11.GL_SRC_ALPHA_SATURATE
	};
	private static String sfactorsString[] = new String[]{
			"GL_ZERO",
			"GL_ONE",
			"GL_SRC_COLOR",
			"GL_ONE_MINUS_SRC_COLOR",
			"GL_DST_COLOR",
			"GL_ONE_MINUS_DST_COLOR",
			"GL_SRC_ALPHA",
			"GL_ONE_MINUS_SRC_ALPHA",
			"GL_DST_ALPHA",
			"GL_ONE_MINUS_DST_ALPHA",
			"GL_SRC_ALPHA_SATURATE"
	};
	private static int dfactors[] = new int[]{
			GL11.GL_ZERO,
			GL11.GL_ONE,
			GL11.GL_SRC_COLOR,
			GL11.GL_ONE_MINUS_SRC_COLOR,
			GL11.GL_DST_COLOR,
			GL11.GL_ONE_MINUS_DST_COLOR,
			GL11.GL_SRC_ALPHA,
			GL11.GL_ONE_MINUS_SRC_ALPHA,
			GL11.GL_DST_ALPHA,
			GL11.GL_ONE_MINUS_DST_ALPHA,
	};
	private static String dfactorsString[] = new String[]{
			"GL_ZERO",
			"GL_ONE",
			"GL_SRC_COLOR",
			"GL_ONE_MINUS_SRC_COLOR",
			"GL_DST_COLOR",
			"GL_ONE_MINUS_DST_COLOR",
			"GL_SRC_ALPHA",
			"GL_ONE_MINUS_SRC_ALPHA",
			"GL_DST_ALPHA",
			"GL_ONE_MINUS_DST_ALPHA",
			"GL_CONSTANT_COLOR",
			"GL_ONE_MINUS_CONSTANT_COLOR",
			"GL_CONSTANT_ALPHA",
			"GL_ONE_MINUS_CONSTANT_ALPHA"
	};
	private int source = 1;
	private int dest = 1;
	private boolean kLast;

	public void apply() {
		GlUtil.glBlendFunc(sfactors[source], dfactors[dest]);
	}

	public void checkKeyboard() {

		if (!kLast) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
				switchSource(1);
				kLast = true;
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
				switchSource(-1);
				kLast = true;
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_UP)) {
				switchDest(1);
				kLast = true;
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
				switchDest(-1);
				kLast = true;
			}

		}
		if (!Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT) &&
				!Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT) &&
				!Keyboard.isKeyDown(GLFW.GLFW_KEY_UP) &&
				!Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN)
				) {
			kLast = false;
		}

	}

	public void switchDest(int i) {
		dest = FastMath.cyclicModulo((dest + i), dfactors.length);
		System.err.println("SWITCHED " + this);
	}

	public void switchSource(int i) {
		source = FastMath.cyclicModulo((source + i), sfactors.length);

		System.err.println("SWITCHED " + this);
	}

	@Override
	public String toString() {
		return "[ " + sfactorsString[source] + ", " + dfactorsString[dest] + " ]";
	}
}
