package org.schema.game.client.controller;

import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.manager.ingame.SegmentBuildController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.GUITextInput;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class PlayerBlockTypeDropdownInput extends PlayerGameDropDownInput {

	public ObjectArrayList<GUIElement> additionalElements;

	private GUITextInput guiTextInput;

	private PlayerTextInputBar playerTextInputBar;

	private Object info;

	public PlayerBlockTypeDropdownInput(String windowId, GameClientState state, Object info, ObjectArrayList<GUIElement> additionalElements) {
		this(windowId, state, info, additionalElements, GUIInputPanel.SMALL_PANEL);
	}

	public PlayerBlockTypeDropdownInput(String windowId, GameClientState state, Object info, ObjectArrayList<GUIElement> additionalElements, int panelStyle) {
		super(windowId, state, info, 32);
		this.additionalElements = additionalElements;
		this.info = info;
		guiTextInput = new GUITextInput(180, 20, state);
		playerTextInputBar = new PlayerTextInputBar(getState(), 30, this.getInputPanel(), this.guiTextInput) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public boolean onInput(String entry) {
				return false;
			}
		};
		guiTextInput.setPreText(Lng.str("Dropdown Filter: "));
		guiTextInput.setDrawCarrier(true);
		guiTextInput.setPos(20, 0, 0);
		guiTextInput.dependend = ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(0);
		guiTextInput.dependendWidthOffset = -40;
		getInputPanel().getContent().attach(guiTextInput);
		GUITextButton selectLookingAt = new GUITextButton(getState(), 150, 24, ColorPalette.OK, Lng.str("Select looking at"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(210);
					Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
					Vector3f camTo = new Vector3f(camPos);
					Vector3f forw = new Vector3f(Controller.getCamera().getForward());
					if (Float.isNaN(forw.x)) {
						return;
					}
					forw.normalize();
					forw.scale(SegmentBuildController.EDIT_DISTANCE);
					camTo.add(forw);
					ClosestRayResultCallback c = ((PhysicsExt) getState().getPhysics()).testRayCollisionPoint(camPos, camTo, false, getState().getCharacter(), null, true, true, false);
					if (c != null && c.hasHit() && c instanceof CubeRayCastResult) {
						CubeRayCastResult cc = (CubeRayCastResult) c;
						SegmentPiece p = new SegmentPiece(cc.getSegment(), cc.getCubePos());
						if (ElementKeyMap.isValidType(p.getType())) {
							ElementInformation i = ElementKeyMap.getInfo(p.getType());
							setSelectedUserPointer(i);
							System.err.println("[DROPDOWN] set type by pick: " + p.getType());
						}
					}
				}
			}
		});
		selectLookingAt.setPos(5, 85, 0);
		getInputPanel().getContent().attach(selectLookingAt);
		updateDropdown("");
	}

	public PlayerBlockTypeDropdownInput(String windowId, GameClientState state, Object info, int panelStyle) {
		this(windowId, state, info, null, panelStyle);
	}

	public PlayerBlockTypeDropdownInput(String windowId, GameClientState state, Object info) {
		this(windowId, state, info, null);
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if (isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
			deactivate();
			return;
		}
		playerTextInputBar.getTextInput().handleKeyEvent(e);
		updateDropdown(playerTextInputBar.getText());
	}

	@Override
	public void handleCharEvent(KeyEventInterface e) {
		playerTextInputBar.getTextInput().handleCharEvent(e);
	}

	private void updateDropdown(String text) {
		// getInputPanel().getContent().detach(guiTextInput);
		update(getState(), info, 32, "", getElements(getState(), text, additionalElements));
	// getInputPanel().getContent().attach(guiTextInput);
	}

	public List<GUIElement> getElements(GameClientState state, String contain, ObjectArrayList<GUIElement> additionalElements) {
		ObjectArrayList<GUIElement> g = new ObjectArrayList<GUIElement>();
		if (additionalElements != null) {
			g.addAll(additionalElements);
		}
		int i = 0;
		for (ElementInformation info : ElementKeyMap.sortedByName) {
			if (contain.trim().length() == 0 || info.getName().toLowerCase(Locale.ENGLISH).contains(contain.trim().toLowerCase(Locale.ENGLISH))) {
				GUIAnchor guiAnchor = new GUIAnchor(state, UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(32));
				g.add(guiAnchor);
				GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, state);
				t.setTextSimple(info.getName());
				guiAnchor.setUserPointer(info);
				GUIBlockSprite b = new GUIBlockSprite(state, info.getId());
				b.getScale().set(0.5f, 0.5f, 0.5f);
				guiAnchor.attach(b);
				t.getPos().x = UIScale.getUIScale().scale(50);
				t.getPos().y = UIScale.getUIScale().scale(7);
				guiAnchor.attach(t);
				i++;
			}
		}
		return g;
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public void pressedOK(GUIListElement current) {
		onOk((ElementInformation) current.getContent().getUserPointer());
		deactivate();
	}

	public abstract void onOk(ElementInformation info);
}
