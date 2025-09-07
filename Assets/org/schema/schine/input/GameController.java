package org.schema.schine.input;

import org.schema.schine.common.language.Lng;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class GameController {

	public GameController(int id) {
		this.joystick = id;
	}

	public final int joystick;


	public String getName() {
		return glfwGetJoystickName(joystick);
	}
	public String getUID() {

		return glfwGetJoystickGUID(joystick);
	}
	public boolean isGamepad() {

		return glfwJoystickIsGamepad(joystick);
	}
	public enum HatDirection{
		CENTERED(GLFW_HAT_CENTERED),
		UP(GLFW_HAT_UP),
		DOWN(GLFW_HAT_DOWN),
		LEFT(GLFW_HAT_LEFT),
		RIGHT(GLFW_HAT_RIGHT),
		RIGHT_UP(GLFW_HAT_RIGHT_UP),
		RIGHT_DOWN(GLFW_HAT_RIGHT_DOWN),
		LEFT_UP(GLFW_HAT_LEFT_UP),
		LEFT_DOWN(GLFW_HAT_LEFT_DOWN);

		public final int code;

		HatDirection(int code) {
			this.code = code;
		}

		public String getName() {
			return switch(this) {
				case CENTERED -> Lng.str("Centered");
				case DOWN -> Lng.str("Down");
				case LEFT -> Lng.str("Left");
				case LEFT_DOWN -> Lng.str("Left Down");
				case LEFT_UP -> Lng.str("Left Up");
				case RIGHT -> Lng.str("Right");
				case RIGHT_DOWN -> Lng.str("Right Down");
				case RIGHT_UP -> Lng.str("Right Up");
				case UP -> Lng.str("Up");
			};
		}
	}
	/**
	 *

		@Nullable
		@NativeType(value="unsigned char const *")


		Returns the state of all hats of the specified joystick.

		This function returns the state of all hats of the specified joystick. Each element in the array is one of the following values:

		 Name                | Value
		 ------------------- | ------------------------------
		 GLFW_HAT_CENTERED   | 0
		 GLFW_HAT_UP         | 1
		 GLFW_HAT_RIGHT      | 2
		 GLFW_HAT_DOWN       | 4
		 GLFW_HAT_LEFT       | 8
		 GLFW_HAT_RIGHT_UP   | GLFW_HAT_RIGHT | GLFW_HAT_UP
		 GLFW_HAT_RIGHT_DOWN | GLFW_HAT_RIGHT | GLFW_HAT_DOWN
		 GLFW_HAT_LEFT_UP    | GLFW_HAT_LEFT  | GLFW_HAT_UP
		 GLFW_HAT_LEFT_DOWN  | GLFW_HAT_LEFT  | GLFW_HAT_DOWN

		The diagonal directions are bitwise combinations of the primary (up, right, down and left) directions and you can test for these individually by ANDingit with the corresponding direction.

		 if (hats[2] & GLFW_HAT_RIGHT)
		 {
		     // State of hat 2 could be right-up, right or right-down
		 }

		If the specified joystick is not present this function will return NULL but will not generate an error. This can be used instead of first calling JoystickPresent.


		Note
		�This function must only be called from the main thread.
		�The returned array is allocated and freed by GLFW. You should not free it yourself. It is valid until the specified joystick is disconnected, thisfunction is called again for that joystick or the library is terminated.

	 * @return
	 */
	public ByteBuffer getHats(){
		return glfwGetJoystickHats(joystick);
	}
	/**
	 *
	 * 		 Name
		 -------------------
		 GLFW_HAT_CENTERED
		 GLFW_HAT_UP
		 GLFW_HAT_RIGHT
		 GLFW_HAT_DOWN
		 GLFW_HAT_LEFT
		 GLFW_HAT_RIGHT_UP
		 GLFW_HAT_RIGHT_DOWN
		 GLFW_HAT_LEFT_UP
		 GLFW_HAT_LEFT_DOWN
	 * @param value
	 * @param direction
	 * @return true if hat it in the direction
	 */
	public boolean isHatInDirection(int value, int direction) {
		ByteBuffer hats = getHats();
		hats.rewind();
		return value < hats.remaining() && ((int)hats.get(value) & direction) == direction;
	}

	/**
	 *

		@Nullable
		@NativeType(value="unsigned char const *")


		Returns the state of all buttons of the specified joystick. Each element in the array is either PRESS or RELEASE.

		For backward compatibility with earlier versions that did not have GetJoystickHats, the button array also includes all hats, each represented as fourbuttons. The hats are in the same order as returned by GetJoystickHats and are in the order up, right, down and left. To disable these extrabuttons, set the JOYSTICK_HAT_BUTTONS init hint before initialization.

		If the specified joystick is not present this function will return NULL but will not generate an error. This can be used instead of first calling JoystickPresent.

		The returned array is allocated and freed by GLFW. You should not free it yourself. It is valid until the specified joystick is disconnected, thisfunction is called again for that joystick or the library is terminated.

		This function must only be called from the main thread.

	 * @return
	 */
	public ByteBuffer getButtons(){
		return glfwGetJoystickButtons(joystick);
	}
	/**
	 * @Nullable
		@NativeType(value="float const *")


		Returns the values of all axes of the specified joystick. Each element in the array is a value between -1.0 and 1.0.

		If the specified joystick is not present this function will return NULL but will not generate an error. This can be used instead of first calling JoystickPresent.

		The returned array is allocated and freed by GLFW. You should not free it yourself. It is valid until the specified joystick is disconnected, thisfunction is called again for that joystick or the library is terminated.

		This function must only be called from the main thread.

	 * @return
	 */
	public FloatBuffer getAxes(){
		return glfwGetJoystickAxes(joystick);
	}
	public boolean isButtonDown(int value) {
		ByteBuffer buttons = getButtons();
		buttons.rewind();
		return value < buttons.remaining() && buttons.get(value) == GLFW_PRESS;
	}
}
