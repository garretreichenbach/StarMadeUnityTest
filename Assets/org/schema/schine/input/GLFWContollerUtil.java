package org.schema.schine.input;

import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_10;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_11;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_12;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_13;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_14;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_15;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_2;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_3;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_4;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_5;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_6;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_7;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_8;
import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_9;
import static org.lwjgl.glfw.GLFW.glfwJoystickPresent;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GLFWContollerUtil {
	public static int[] JOYSTICKS = new int[] {
			GLFW_JOYSTICK_1,
			GLFW_JOYSTICK_2,
			GLFW_JOYSTICK_3,
			GLFW_JOYSTICK_4,
			GLFW_JOYSTICK_5,
			GLFW_JOYSTICK_6,
			GLFW_JOYSTICK_7,
			GLFW_JOYSTICK_8,
			GLFW_JOYSTICK_9,
			GLFW_JOYSTICK_10,
			GLFW_JOYSTICK_11,
			GLFW_JOYSTICK_12,
			GLFW_JOYSTICK_13,
			GLFW_JOYSTICK_14,
			GLFW_JOYSTICK_15
	};
	
	
	public static List<GameController> getControllers(){
		ObjectArrayList<GameController> s = new ObjectArrayList<>();
		for(int id : JOYSTICKS) {
			if(glfwJoystickPresent(id)) {
				GameController c = new GameController(id);
				s.add(c);
			}
		}
		return s;
	}
}
