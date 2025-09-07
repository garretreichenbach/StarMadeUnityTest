using System;
using UnityEngine; // For Debug.Assert

// Placeholders for custom types
namespace Org.Schema.Schine.Input
{
    public enum InputType
    {
        MOUSE,
        MOUSE_WHEEL,
        // Add other types as needed
    }

    public class InputAction
    {
        public InputType Type;
        public int Key;
        public Mouse.MouseWheelDir WheelDir;

        public InputAction(InputType type, int key)
        {
            Type = type;
            Key = key;
        }

        public InputAction(InputType type, Mouse.MouseWheelDir wheelDir)
        {
            Type = type;
            WheelDir = wheelDir;
        }
    }

    public class KeyboardEvent
    {
        public int key;
        public int actionState; // Assuming this holds GLFW_PRESS/RELEASE like values

        public KeyboardEvent() { }

        public bool isPressed()
        {
            // Assuming GLFW.GLFW_PRESS is 1 and GLFW.GLFW_RELEASE is 0
            return actionState == 1; // Placeholder for GLFW_PRESS
        }

        public bool isInputType(InputType t)
        {
            // This method needs to be implemented in the actual KeyboardEvent class
            return false;
        }

        public void checkSpecialKeysDownMod(int mods) { Debug.Log("KeyboardEvent.checkSpecialKeysDownMod called."); }
        public void checkTriggeredMappings(InputType t) { Debug.Log("KeyboardEvent.checkTriggeredMappings called."); }
    }

    public class Mouse
    {
        public enum MouseWheelDir
        {
            MOUSE_WHEEL_UP,
            MOUSE_WHEEL_DOWN
        }
    }
}

namespace Org.Schema.Schine.GraphicsEngine.Core
{
    public class MouseEvent : Org.Schema.Schine.Input.KeyboardEvent
    {
        public int Dx;
        public int Dy;
        public int X, Y;
        public int DWheel;
        protected readonly InputType MouseInputType;

        public MouseEvent(InputType tp)
        {
            // base() calls the constructor of the base class (KeyboardEvent)
            Debug.Assert(tp == InputType.MOUSE || tp == InputType.MOUSE_WHEEL, "InputType must be MOUSE or MOUSE_WHEEL");
            MouseInputType = tp;
        }

        public override InputType getType()
        {
            return MouseInputType;
        }

        public override InputAction generateInputAction()
        {
            Debug.Assert(isInputType(InputType.MOUSE) || isInputType(InputType.MOUSE_WHEEL), "InputType must be MOUSE or MOUSE_WHEEL");

            if (MouseInputType == InputType.MOUSE)
            {
                // button
                return new InputAction(InputType.MOUSE, key);
            }
            else
            {
                // wheel
                return new InputAction(InputType.MOUSE_WHEEL, DWheel > 0 ? Mouse.MouseWheelDir.MOUSE_WHEEL_UP : Mouse.MouseWheelDir.MOUSE_WHEEL_DOWN);
            }
        }

        public bool ReleasedLeftMouse()
        {
            // Assuming GLFW.GLFW_RELEASE is 0
            return !isPressed() && key == (int)MouseButton.MouseLeft && MouseInputType == InputType.MOUSE;
        }

        public bool PressedLeftMouse()
        {
            // Assuming GLFW.GLFW_PRESS is 1
            return isPressed() && key == (int)MouseButton.MouseLeft && MouseInputType == InputType.MOUSE;
        }

        public bool PressedMiddleMouse()
        {
            return isPressed() && key == (int)MouseButton.MouseMiddle && MouseInputType == InputType.MOUSE;
        }

        public bool PressedRightMouse()
        {
            return isPressed() && key == (int)MouseButton.MouseRight && MouseInputType == InputType.MOUSE;
        }

        public override string ToString()
        {
            return $"MouseEvent [button={key}, pressed={isPressed()}, dx={Dx}, dy={Dy}, x={X}, y={Y}, dWheel={DWheel}]";
        }

        public override bool isInputType(InputType t)
        {
            return t == MouseInputType;
        }

        public void GenerateMouseClick(MouseButton button)
        {
            // This method was empty in Java, keeping it empty for now.
        }

        public void GenerateMouseRelease(MouseButton button)
        {
            key = (int)button;
            actionState = 0; // Placeholder for GLFW_RELEASE
        }

        public static MouseEvent GenerateEvent(MouseButton button, bool pressed)
        {
            return GenerateEvent(button, pressed, 0);
        }

        public static MouseEvent GenerateEvent(MouseButton button, bool pressed, int mods)
        {
            MouseEvent e = new MouseEvent(InputType.MOUSE);
            e.key = (int)button;
            e.actionState = pressed ? 1 : 0; // Placeholder for GLFW_PRESS/RELEASE
            e.checkSpecialKeysDownMod(mods);
            e.checkTriggeredMappings(InputType.MOUSE);
            return e;
        }
    }
}