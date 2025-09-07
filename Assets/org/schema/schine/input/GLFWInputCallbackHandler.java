package org.schema.schine.input;

import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWJoystickCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

public interface GLFWInputCallbackHandler {

	GLFWCharCallbackI getCharCallback();

	GLFWKeyCallbackI getKeyCallback();

	GLFWScrollCallbackI getScrollCallback();

	GLFWMouseButtonCallbackI getMouseButtonCallback();

	GLFWCursorPosCallbackI getMouseCursorCallback();

	GLFWJoystickCallbackI getJoystickCallback();

}
