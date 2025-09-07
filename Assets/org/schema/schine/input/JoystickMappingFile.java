package org.schema.schine.input;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.common.ParseException;
import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class JoystickMappingFile {

	public static final byte version = 0;
	private static boolean dirty;
	private final Object2ObjectOpenHashMap<KeyboardMappings, JoystickButtonMapping> mappings = new Object2ObjectOpenHashMap<KeyboardMappings, JoystickButtonMapping>();
	private final Object2ObjectAVLTreeMap<JoystickAxisMapping, JoystickAxisSingleMap> axis = new Object2ObjectAVLTreeMap<JoystickAxisMapping, JoystickAxisSingleMap>();
	public String joystickFilePath = "./joystick.cfg";
	public boolean axisEvent;
	private JoystickButtonMapping leftMouse;
	private JoystickButtonMapping rightMouse;

	public JoystickMappingFile() {
		super();

		for (JoystickAxisMapping j : JoystickAxisMapping.values()) {
			axis.put(j, new JoystickAxisSingleMap());
		}
		for (KeyboardMappings j : KeyboardMappings.values()) {
			mappings.put(j, new JoystickButtonMapping());
		}
		setLeftMouse(new JoystickButtonMapping());
		setRightMouse(new JoystickButtonMapping());
		//read the prefabs now
	}

	public static int getSelectedControllerIndex() {
		return (Integer) EngineSettings.C_SELECTED_JOYSTICK.getInt();
	}

	public static boolean ok() {
		return Controller.getControllerInput().getActiveController() != null && GLFW.glfwJoystickPresent(Controller.getControllerInput().getActiveController().joystick);
	}

	public static String getName() {
		return GLFW.glfwGetJoystickName(Controller.getControllerInput().getActiveController().joystick);
	}
	public static FloatBuffer getAxes() {
		FloatBuffer b = GLFW.glfwGetJoystickAxes(Controller.getControllerInput().getActiveController().joystick);
		b.rewind();
		return b;
	}
	public static ByteBuffer getButtons() {
		ByteBuffer b = GLFW.glfwGetJoystickButtons(Controller.getControllerInput().getActiveController().joystick);
		b.rewind();
		return b;
	}

	public static void printAxisString() {

		if (ok()) {
			FloatBuffer axes = getAxes();
			for (int i = 0; i < axes.limit(); i++) {
				
				System.err.println("AXIS[" + i + "] #"+i+": " + axes.get(i));
			}
		}
	}

	public static JoystickButtonMapping getPressedButton() {
		if (ok()) {
			ByteBuffer buttons = getButtons();
			for (int i = 0; i < buttons.limit(); i++) {
				if (buttons.get(i) == GLFW.GLFW_PRESS) {
					JoystickButtonMapping m = new JoystickButtonMapping();
					m.buttonId = i;
					m.buttonName = "Button "+i;
					return m;
				}
			}
		}
		return null;
	}

	public void write(String to) throws IOException {
		if (ok()) {
			BufferedWriter bf = null;
			try {
				System.err.println("[JOYSTICK] writing config " + to);
				File f = new FileExt(to);
				f.delete();
				f.createNewFile();
				bf = new BufferedWriter(new FileWriter(f));
				bf.write("#version = " + version);
				bf.newLine();

				bf.write("#name = " + getName());
				bf.newLine();
				for (Entry<KeyboardMappings, JoystickButtonMapping> e : mappings.entrySet()) {
					bf.write("BUTTON#" + e.getKey().name() + " = " + e.getValue().buttonId);
					bf.newLine();
				}
				bf.write("MOUSE#LEFTMOUSE = " + leftMouse.buttonId);
				bf.newLine();

				bf.write("MOUSE#RIGHTMOUSE = " + rightMouse.buttonId);
				bf.newLine();

				for (Entry<JoystickAxisMapping, JoystickAxisSingleMap> e : axis.entrySet()) {
					bf.write("AXIS#" + e.getKey().name() + " = " + e.getValue().mapping + "#" + e.getValue().inverted + "#" + e.getValue().sensivity);
					bf.newLine();
				}

				bf.flush();
			} finally {
				if (bf != null) {
					bf.close();
				}
			}
		} else {
			System.err.println("not writing joystick config: no joystick available");
		}

	}

	private void read(String from) throws IOException, ParseException {
		if(true) {
			System.err.println("CURRENTLY NOT READING JOYSTICK OPTIONS!!!!!!!!!!!!!!!!");
			return;
		}
		
		System.err.println("[JOYSTICK] reading config " + from);
		BufferedReader bf = null;
		try {
			File f = new FileExt(from);
			bf = new BufferedReader(new FileReader(f));
			String line = null;
			int i = 0;
			String first = bf.readLine();
			if (first == null || !first.startsWith("#version")) {
				throw new ParseException("cannot parse " + f.getAbsolutePath() + ": no version info");
			}
			i++;

			String sec = bf.readLine();
			if (sec == null || !sec.startsWith("#name")) {
				throw new ParseException("cannot parse " + f.getAbsolutePath() + ": no name info");
			}
			i++;
			int buttonCount = getButtons().limit();
			while ((line = bf.readLine()) != null) {

				try {
					if (line.startsWith("MOUSE#LEFTMOUSE")) {
						String[] split = line.split(" = ", 2);
						String name = split[0];
						String value = split[1].trim();
						try {
							int buttonId = Integer.parseInt(value);
							JoystickButtonMapping joystickButtonMapping = new JoystickButtonMapping();
							if (ok() && buttonId >= 0 && buttonId < buttonCount) {
								joystickButtonMapping.buttonId = buttonId;
								joystickButtonMapping.buttonName ="Button "+(buttonId);
							}
							setLeftMouse(joystickButtonMapping);

						} catch (NumberFormatException e) {
							e.printStackTrace();
							throw new ParseException("cannot parse " + f.getAbsolutePath() + ": button index must be number: " + value + " (for " + name + ")");
						}
					} else if (line.startsWith("MOUSE#RIGHTMOUSE")) {
						String[] split = line.split(" = ", 2);
						String name = split[0];
						String value = split[1].trim();
						try {
							int buttonId = Integer.parseInt(value);
							JoystickButtonMapping joystickButtonMapping = new JoystickButtonMapping();
							if (ok() && buttonId >= 0 && buttonId < buttonCount) {
								joystickButtonMapping.buttonId = buttonId;
								joystickButtonMapping.buttonName = "Button "+(buttonId);
							}
							setRightMouse(joystickButtonMapping);

						} catch (NumberFormatException e) {
							e.printStackTrace();
							throw new ParseException("cannot parse " + f.getAbsolutePath() + ": button index must be number: " + value + " (for " + name + ")");
						}
					} else if (line.startsWith("BUTTON#")) {
						line = line.substring("BUTTON#".length());
						String[] split = line.split(" = ", 2);
						String name = split[0];
						String value = split[1].trim();
						KeyboardMappings mapping;
						try {
							mapping = KeyboardMappings.valueOf(name);
							if (mapping == null) {
								throw new ParseException("cannot parse " + f.getAbsolutePath() + ": unknown name: " + name);
							}
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
							throw new ParseException("cannot parse " + f.getAbsolutePath() + ": unknown name: " + name);
						}
						try {
							int buttonId = Integer.parseInt(value);
							JoystickButtonMapping joystickButtonMapping = new JoystickButtonMapping();
							if (ok() && buttonId >= 0 && buttonId < buttonCount) {
								joystickButtonMapping.buttonId = buttonId;
								joystickButtonMapping.buttonName = "Button "+(buttonId);
							}
							mappings.put(mapping, joystickButtonMapping);

						} catch (NumberFormatException e) {
							e.printStackTrace();
							throw new ParseException("cannot parse " + f.getAbsolutePath() + ": button index must be number: " + value + " (for " + name + ")");
						}

					} else if (line.startsWith("AXIS#")) {
						line = line.substring("AXIS#".length());
						String[] split = line.split(" = ", 2);
						String name = split[0];

						JoystickAxisMapping mapper = JoystickAxisMapping.valueOf(name);
						if (mapper == null) {
							throw new ParseException("cannot parse " + f.getAbsolutePath() + ": unknown name: " + name);
						}

						try {
							String[] values = split[1].split("#");
							int mapping = Integer.parseInt(values[0]);
							boolean inverted = Boolean.parseBoolean(values[1]);
							float sensivity = Float.parseFloat(values[2]);

							JoystickAxisSingleMap m = new JoystickAxisSingleMap();
							m.mapping = mapping;
							m.inverted = inverted;

							axis.put(mapper, m);

						} catch (Exception e) {
							e.printStackTrace();
							throw new ParseException("cannot parse " + f.getAbsolutePath() + ": malformed line: " + line);
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
//					GLFrame.processErrorDialogExceptionWithoutReportWithContinue(e);
				}
				i++;
			}

		} finally {
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		write();
	}

	public void write() throws IOException {
		write(joystickFilePath);
		JoystickMappingFile.dirty = false;
	}

	public void read() throws IOException, ParseException {
		if (((new FileExt(joystickFilePath)).exists())) {
			read(joystickFilePath);
		} else {
			System.out.println("[JOYSTICK] cannot read default joystick file because it does not exist");
		}
		JoystickMappingFile.dirty = false;
	}

	public JoystickMapping getButtonFor(KeyboardMappings mapping) {
		return mappings.get(mapping);
	}

	public void setAxis(JoystickAxisMapping map, int assignedTo) {
		axis.get(map).mapping = assignedTo;
		JoystickMappingFile.dirty = true;
	}

	public void init() {
		Controller.getControllerInput().init();
		if (getSelectedControllerIndex() >= 0) {
			Controller.getControllerInput().select(getSelectedControllerIndex());
		} else {
			Controller.getControllerInput().delesect();
		}
		try {
			read();
		} catch (IOException e) {
			e.printStackTrace();
			File f = new FileExt(joystickFilePath);
			if (f.exists()) {
				f.delete();
			}
		}
	}

	public void updateInput() {
		if (ok()) {
		}
	}

	public boolean isAxisInverted(JoystickAxisMapping key) {
		return axis.get(key).inverted;
	}

	public void invertedAxis(JoystickAxisMapping key, boolean b) {
		axis.get(key).inverted = b;
	}

	public double getAxis(JoystickAxisMapping map) {
		if (!ok()) {
			return 0;
		}
		if (!axisEvent) {
			return 0;
		}

		if (axis.get(map).mapping == -1) {
			return 0;
		}
		double a = (isAxisInverted(map) ? -1 : 1) * getAxes().get(axis.get(map).mapping) * axis.get(map).sensivity;
		return Math.abs(a) > 0.2d ? a : .0d;
	}

//	public boolean next() {
//		if (ok()) {
//			boolean next = Controllers.next();
//			if (next && Controllers.getEventSource() != get()) {
//				return next();
//			} else {
//				return next;
//			}
//		}
//		return false;
//	}

	public boolean isKeyboardButtonDown(KeyboardMappings map) {
		return mappings.get(map).isDown();
	}

	/**
	 * @return the mappings
	 */
	public Object2ObjectOpenHashMap<KeyboardMappings, JoystickButtonMapping> getMappings() {
		return mappings;
	}

	/**
	 * @return the axis
	 */
	public Object2ObjectAVLTreeMap<JoystickAxisMapping, JoystickAxisSingleMap> getAxis() {
		return axis;
	}

	public float getSensivity(JoystickAxisMapping key) {
		return axis.get(key).sensivity;
	}

	public void modSensivity(JoystickAxisMapping key, float by) {
		setSensivity(key, axis.get(key).sensivity + by);
	}
	public void setSensivity(JoystickAxisMapping key, float by) {
		axis.get(key).sensivity = by;
		
		int t = FastMath.round(axis.get(key).sensivity * 10f);
		axis.get(key).sensivity = (t) / 10f;
		if (axis.get(key).sensivity <= 0) {
			axis.get(key).sensivity = 0.1f;
		}
		
	}

	/**
	 * @return the leftMouse
	 */
	public JoystickButtonMapping getLeftMouse() {
		return leftMouse;
	}

	/**
	 * @param leftMouse the leftMouse to set
	 */
	public void setLeftMouse(JoystickButtonMapping leftMouse) {
		//		System.err.println("SETTING JLEFT: "+leftMouse+" on "+this);
		JoystickMappingFile.dirty = true;
		this.leftMouse = leftMouse;
	}

	/**
	 * @return the rightMouse
	 */
	public JoystickButtonMapping getRightMouse() {

		return rightMouse;
	}

	/**
	 * @param rightMouse the rightMouse to set
	 */
	public void setRightMouse(JoystickButtonMapping rightMouse) {
		//		System.err.println("SETTING JRIGHT: "+rightMouse+" on "+this);
		JoystickMappingFile.dirty = true;
		this.rightMouse = rightMouse;
	}

	public static int getAxesCount() {
		return getAxes().limit();
	}

	public static String getAxisName(int assignedTo) {
		return "Axis "+assignedTo;
	}

	public static int getButtonCount() {
		return getButtons().limit();
	}

	public static String getButtonName(int index) {
		return "Button "+index;
	}

	public static float getAxisValue(int index) {
		return getAxes().get(index);
	}

	public static boolean isButtonPressed(int index) {
		return getButtons().get(index) == GLFW.GLFW_PRESS;
	}

}
