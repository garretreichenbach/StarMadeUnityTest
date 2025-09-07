package org.schema.schine.input;

import java.util.List;
import java.util.Locale;

import org.schema.common.XMLSerializationInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseButton;
import org.schema.schine.input.GameController.HatDirection;
import org.schema.schine.input.Keyboard.KeyboardControlKey;
import org.schema.schine.input.Mouse.MouseWheelDir;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InputAction implements XMLSerializationInterface{
	public final InputType type;
	
	public final List<InputAction> modifierInput;
	public final int value;
	public final int joystickIndex;
	public final HatDirection hatDirection;
	public final MouseWheelDir wheelDirection;

	private final KeyboardControlKey controlKey;
	
	/**
	 * Deep copy of an input action
	 * @param cpy
	 */
	public InputAction(InputAction cpy) {
		this.type = cpy.type;
		this.value = cpy.value;
		this.joystickIndex = cpy.joystickIndex;
		this.hatDirection = cpy.hatDirection;
		this.wheelDirection = cpy.wheelDirection;
		this.controlKey = null;
		this.modifierInput = cpy.modifierInput != null ? new ObjectArrayList<InputAction>(cpy.modifierInput.size()) : null;
		if(cpy.modifierInput != null) {
			for(InputAction c : cpy.modifierInput) {
				this.modifierInput.add(new InputAction(c));
			}
		}
	}
	/**
	 * for buttons combinations of keyboard
	 * @param type (must be InputType.KEYBOARD)
	 * @param value keyboard key
	 * @param modifier the key action to hold (e.g. crtl)
	 */
	public InputAction(InputType type, int value, InputAction ... modifier) {
		assert(type == InputType.MOUSE || type == InputType.KEYBOARD):type.name();
		this.type = type;
		this.value = value;
		this.joystickIndex = -1;
		this.hatDirection = null;
		this.wheelDirection = null;
		this.controlKey = null;
		this.modifierInput = new ObjectArrayList<InputAction>(modifier);
	}
	/**
	 * for buttons combinations of keyboard
	 * @param type (must be InputType.KEYBOARD)
	 * @param button MouseButton pressed
	 */
	public InputAction(MouseButton button) {
		this.type = InputType.MOUSE;
		this.value = button.button;
		this.joystickIndex = -1;
		this.hatDirection = null;
		this.wheelDirection = null;
		this.controlKey = null;
		this.modifierInput = null;
	}
	/**
	 * for buttons combinations of keyboard
	 * @param type (must be InputType.KEYBOARD)
	 * @param button MouseButton pressed
	 * @param modifier the key action to hold (e.g. crtl)
	 */
	public InputAction(MouseButton button, InputAction ... modifier) {
		this.type = InputType.MOUSE;
		this.value = button.button;
		this.joystickIndex = -1;
		this.hatDirection = null;
		this.wheelDirection = null;
		this.controlKey = null;
		this.modifierInput = new ObjectArrayList<InputAction>(modifier);
	}
	/**
	 * special keys with multiple versions (e.g. left and right control)
	 * @param type (must be InputType.KEYBOARD_MOD)
	 * @param value keyboard key
	 * @param modifier the key action to hold (e.g. crtl)
	 */
	public InputAction(InputType type, KeyboardControlKey controlKey) {
		assert(type == InputType.KEYBOARD_MOD):type.name();
		this.type = type;
		this.value = controlKey.ordinal();
		this.joystickIndex = -1;
		this.hatDirection = null;
		this.wheelDirection = null;
		this.controlKey = controlKey;
		this.modifierInput = null;
	}
	/**
	 * For joystick buttons
	 * @param type
	 * @param value
	 * @param joystickIndex
	 */
	public InputAction(InputType type, int value, int joystickIndex) {
		assert(type == InputType.JOYSTICK):type.name();
		this.type = type;
		this.value = value;
		this.joystickIndex = joystickIndex;
		this.hatDirection = null;
		this.wheelDirection = null;
		this.controlKey = null;
		this.modifierInput = null;
	}
	/**
	 * For joystick hats
	 * @param type
	 * @param value
	 * @param joystickIndex
	 * @param hatDir
	 */
	public InputAction(InputType type, int value, int joystickIndex, HatDirection hatDir) {
		assert(type == InputType.JOYSTICK_HAT);
		this.type = type;
		this.value = value;
		this.joystickIndex = joystickIndex;
		this.hatDirection = hatDir;
		this.wheelDirection = null;
		this.controlKey = null;
		this.modifierInput = null;
	}
	/**
	 * For Mouse wheel actions
	 * @param type
	 * @param value
	 * @param dir
	 */
	public InputAction(InputType type, MouseWheelDir dir) {
		assert(type == InputType.MOUSE_WHEEL):type.name();
		this.type = type;
		this.value = dir.dir;
		this.joystickIndex = -1;
		this.hatDirection = null;
		this.wheelDirection = dir;
		this.controlKey = null;
		this.modifierInput = null;
	}
	/**
	 * For Mouse wheel actions with mod
	 * @param type
	 * @param value
	 * @param dir
	 */
	public InputAction(InputType type, MouseWheelDir dir, InputAction ... modifier) {
		assert(type == InputType.MOUSE_WHEEL):type.name();
		this.type = type;
		this.value = dir.dir;
		this.joystickIndex = -1;
		this.hatDirection = null;
		this.wheelDirection = dir;
		this.controlKey = null;
		this.modifierInput = new ObjectArrayList<InputAction>(modifier);
	}
	public static InputAction readAction(Node mainNode) throws InputTypeParseException {
		assert(mainNode.getNodeType() == Node.ELEMENT_NODE && mainNode.getNodeName().toLowerCase(Locale.ENGLISH).equals(nodeName.toLowerCase(Locale.ENGLISH))):mainNode.getNodeName();
		Node tpNd = mainNode.getAttributes().getNamedItem(typeNode);
		if(tpNd == null) {
			throw new InputTypeParseException("No 'type' attribute on "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
		}
		InputType type = InputType.strMap.get(tpNd.getNodeValue().toUpperCase(Locale.ENGLISH));
		
		if(type == null) {
			throw new InputTypeParseException("Unknown type '"+tpNd.getNodeValue()+"'; Must be either: "+InputType.strMap.keySet()+" on "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
		}
		
		NodeList cn = mainNode.getChildNodes();
		
		List<InputAction> modifierInput = new ObjectArrayList<>();
		int value = Integer.MIN_VALUE;
		int joystickIndex = Integer.MIN_VALUE;
		HatDirection hatDirection = null;
		MouseWheelDir wheelDirection = null;
		
		for(int i = 0; i < cn.getLength(); i++) {
			Node node = cn.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				switch(node.getNodeName().toLowerCase(Locale.ENGLISH)) {
				case "joystickindex":
					try {
						joystickIndex = Integer.parseInt(node.getTextContent());
					} catch (NumberFormatException e) {
						throw new InputTypeParseException("Invalid number format for JoystickIndex in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName(), e);
					}
					break;
				case "joystickhatdir":
					try {
						joystickIndex = Integer.parseInt(node.getTextContent());
					} catch (NumberFormatException e) {
						throw new InputTypeParseException("Invalid number format for JoystickIndex in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName(), e);
					}
					try {
						int ht = Integer.parseInt(node.getTextContent());
						for(HatDirection c : HatDirection.values()) {
							if(c.code == ht) {
								hatDirection = c;
								break;
							}
						}
						if(hatDirection == null) {
							throw new InputTypeParseException("Invalid hatDirection for JoystickHatDirection in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
						}
					} catch (NumberFormatException e) {
						throw new InputTypeParseException("Invalid number format for JoystickHatDirection in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName(), e);
					}
					break;
				case "value":
					try {
						
						if(type == InputType.KEYBOARD) {
							value = Keyboard.getKeyFromName(node.getTextContent());
						}else {
							value = Integer.parseInt(node.getTextContent().trim());
						}
						
					} catch (NumberFormatException e) {
						throw new InputTypeParseException("Invalid number format for Value in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName(), e);
					}
					break;
				case "modifier":
					NodeList mc = node.getChildNodes();
					for(int o = 0; o < mc.getLength(); o++) {
						if(mc.item(o).getNodeType() == Node.ELEMENT_NODE && mc.item(o).getNodeName().toLowerCase(Locale.ENGLISH).equals(nodeName.toLowerCase(Locale.ENGLISH))) {
							modifierInput.add(readAction(mc.item(o)));
						}
					}
					break;
				}
			}
		}
		if(value == Integer.MIN_VALUE) {
			throw new InputTypeParseException("No value for action in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
		}
		switch(type) {
		case JOYSTICK:
			if(joystickIndex == Integer.MIN_VALUE) {
				throw new InputTypeParseException("No joystick index for joystick action in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
			}
			return new InputAction(type, value, joystickIndex);
		case JOYSTICK_HAT:
			if(joystickIndex == Integer.MIN_VALUE) {
				throw new InputTypeParseException("No joystick index for joystick hat action in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
			}
			if(hatDirection == null) {
				throw new InputTypeParseException("No joystick hat direction for joystick hat action in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
			}
			return new InputAction(type, value, joystickIndex);
		case KEYBOARD, MOUSE:
			if(!modifierInput.isEmpty()) {
				return new InputAction(type, value, modifierInput.toArray(new InputAction[modifierInput.size()]));
			}else {
				return new InputAction(type, value);
			}
		case KEYBOARD_MOD:
			return new InputAction(type, KeyboardControlKey.values()[value]); //value is ordinal
		case MOUSE_WHEEL:
			if(value != -1 && value != 1) {
				throw new InputTypeParseException("Mouse wheel direction must be either -1 or 1 in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
			} else if(!modifierInput.isEmpty()) {
				return new InputAction(type, value == -1 ? MouseWheelDir.MOUSE_WHEEL_DOWN : MouseWheelDir.MOUSE_WHEEL_UP, modifierInput.toArray(new InputAction[modifierInput.size()]));
			} else {
				return new InputAction(type, value == -1 ? MouseWheelDir.MOUSE_WHEEL_DOWN : MouseWheelDir.MOUSE_WHEEL_UP);
			}
		default:
			throw new InputTypeParseException("Invallid type "+type+"; in "+mainNode.getNodeName()+"; "+mainNode.getParentNode().getNodeName());
		}
	}
	public static final String nodeName = "Input";
	private static final String typeNode = "type";
	private static final String joyIndexNode = "JoystickIndex";
	private static final String joyHatNode = "JoystickHatDir";
	private static final String valueNode = "Value";
	private static final String modifierNode = "Modifier";
	
	public Node getUID(Document doc){
		Element e = doc.createElement("Input");
		
		Attr typeAtt = doc.createAttribute(typeNode);
		typeAtt.setNodeValue(type.prefix);
		e.setAttributeNode(typeAtt);
		
		if(type == InputType.JOYSTICK || type == InputType.JOYSTICK_HAT) {
			Element ji = doc.createElement(joyIndexNode);
			ji.setTextContent(String.valueOf(joystickIndex));
			e.appendChild(ji);
		}
		
		if(type == InputType.JOYSTICK_HAT) {
			Element ji = doc.createElement(joyHatNode);
			ji.setTextContent(String.valueOf(hatDirection.code));
			e.appendChild(ji);
		}
		
		
		
		Element val = doc.createElement(valueNode);
		if(type == InputType.KEYBOARD) {
			val.setTextContent(Keyboard.getKeyNameUnique(value));
		}else {
			val.setTextContent(String.valueOf(value));
		}
		e.appendChild(val);
		
		if(type == InputType.KEYBOARD_MOD) {
			val.appendChild(doc.createComment(controlKey.getName()));
		}
		
		
		if(modifierInput != null) {
			for(InputAction o : modifierInput) {
				Element mod = doc.createElement(modifierNode);
				mod.appendChild(o.getUID(doc));
				e.appendChild(mod);
			}
		}
		return e;
	}
	public boolean isModifierDown() {
		if(modifierInput != null) {
			for(InputAction a : modifierInput) {
				if(!a.isDown()) {
					return false;
				}
			}
			return true;
		}else {
			//modifier considered pressed if it doesn't exist
			return true;
		}
	}
	public boolean isDown() {
		
		//modifier has to be down too, if set
		if(!isModifierDown()) {
			return false;
		}
		return switch(type) {
			case JOYSTICK -> joystickIndex >= 0 && joystickIndex < GameControllerInput.joysticks.size() && GameControllerInput.joysticks.get(joystickIndex).isButtonDown(value);
			case KEYBOARD -> Keyboard.isKeyDown(value);
			case KEYBOARD_MOD -> Keyboard.isModDown(controlKey);
			case JOYSTICK_HAT -> joystickIndex >= 0 && joystickIndex < GameControllerInput.joysticks.size() && GameControllerInput.joysticks.get(joystickIndex).isHatInDirection(value, hatDirection.code);
			case MOUSE -> Mouse.isDown(value);
			case MOUSE_WHEEL -> false;
			default -> throw new RuntimeException("Invalid Type: " + type);
		};
	}
	@Override
	public void parseXML(Node node) {
		throw new RuntimeException("Invalid Function Call. Use static readAction(Node)");
	}
	@Override
	public Node writeXML(Document doc, Node parent) {
		Node uid = getUID(doc);
		parent.appendChild(uid);
		return uid;
	}
	public String getModifiers() {
		if(modifierInput == null || modifierInput.isEmpty()) {
			return "";
		}else {
			StringBuffer b = new StringBuffer();
			for(int i = 0; i < modifierInput.size(); i++) {
				InputAction a = modifierInput.get(i);
				b.append(" + "+a.getName());
			}
			return b.toString();
		}
	}
	public String getName() {
		return switch(type) {
			case BLOCK -> throw new RuntimeException("Invalid Action");
			case JOYSTICK -> Lng.str("Controller(%s) Button %s", joystickIndex, value) + getModifiers();
			case JOYSTICK_HAT -> Lng.str("Controller(%s) Hat %s", joystickIndex, hatDirection.getName()) + getModifiers();
			case KEYBOARD_MOD -> Lng.str("ControlKey(%s)", controlKey.getName()) + getModifiers();
			case KEYBOARD -> Keyboard.getKeyTranslated(value) + getModifiers();
			case MOUSE -> MouseButton.values()[value].getName() + getModifiers();
			case MOUSE_WHEEL -> wheelDirection.getName() + getModifiers();
			default -> throw new RuntimeException("Invalid Type: " + type);
		};
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((controlKey == null) ? 0 : controlKey.hashCode());
		result = prime * result + ((hatDirection == null) ? 0 : hatDirection.hashCode());
		result = prime * result + joystickIndex;
		result = prime * result + ((modifierInput == null) ? 0 : modifierInput.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + value;
		result = prime * result + ((wheelDirection == null) ? 0 : wheelDirection.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InputAction other = (InputAction) obj;
		if (controlKey != other.controlKey)
			return false;
		if (hatDirection != other.hatDirection)
			return false;
		if (joystickIndex != other.joystickIndex)
			return false;
		if (modifierInput == null) {
			if (other.modifierInput != null)
				return false;
		} else if (!modifierInput.equals(other.modifierInput))
			return false;
		if (type != other.type)
			return false;
		if (value != other.value)
			return false;
		if (wheelDirection != other.wheelDirection)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "InputAction["+getName()+"]";
	}
	
	
}