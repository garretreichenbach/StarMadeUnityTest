package api.utils.gui;

import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.input.InputState;

@Deprecated
public class SimpleGUIVerticalButtonPane extends GUIAnchor {

    private GUITextButton[] buttons;
    private int spacing;

    public SimpleGUIVerticalButtonPane(InputState state, float width, float height, int spacing) {
        super(state, width, height);
        this.spacing = spacing;
    }

    public SimpleGUIVerticalButtonPane(InputState state, float width, float height) {
        this(state, width, height, 2);
    }

    public void addButton(GUITextButton button) {
        if(buttons == null) {
            button.setHeight((this.getHeight() - (spacing * 2)));
            button.setPos(spacing, spacing, 0);
            button.setMouseUpdateEnabled(true);
            buttons = new GUITextButton[] {button};
            attach(button);
        } else {
            button.setMouseUpdateEnabled(true);
            GUITextButton[] newButtons = new GUITextButton[buttons.length + 1];
            System.arraycopy(buttons, 0, newButtons, 0, buttons.length);
            newButtons[newButtons.length - 1] = button;
            for(int i = 0; i < newButtons.length; i ++) {
                newButtons[i].setHeight((this.getHeight() / newButtons.length) - (spacing * 2));
                newButtons[i].setPos(spacing, spacing + ((newButtons[i].getHeight() + spacing) * i), 0);
            }
            this.buttons = newButtons;
        }
        attach(button);
    }
}