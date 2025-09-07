package org.schema.game.client.view.gui.reactor;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIReactorNodeOptions extends GUIElement {

	private boolean init;

	private GUIHorizontalButtonTablePane buttons;

	private GUIAnchor stats;

	private ReactorElement node;

	private GUIElement dependent;

	private GUIElement activityElement;

	public GUIReactorNodeOptions(InputState state, GUIElement dependent, GUIElement activityElement) {
		super(state);
		this.activityElement = activityElement;
		this.dependent = dependent;
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		ReactorElement selected = getSelected();
		if (selected != this.node) {
			this.node = selected;
		}
		if (node == null) {
			return;
		}
		if (!init) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();
		this.buttons.draw();
		this.stats.setPos(0, buttons.getPos().y - 140, 0);
		this.stats.draw();
		GlUtil.glPopMatrix();
	}

	private ReactorElement getSelected() {
		if (GUIReactorTree.selected != Long.MIN_VALUE) {
			GameClientState s = ((GameClientState) getState());
			if (s.getCurrentPlayerObject() != null && s.getCurrentPlayerObject() instanceof SegmentController && s.getCurrentPlayerObject() instanceof ManagedSegmentController<?>) {
				SegmentController c = (SegmentController) s.getCurrentPlayerObject();
				PowerInterface powerInterface = ((ManagedSegmentController<?>) c).getManagerContainer().getPowerInterface();
				ReactorElement chamber = powerInterface.getChamber(GUIReactorTree.selected);
				if (chamber != null) {
					return chamber;
				}
			}
		}
		return null;
	}

	public GUIAnchor createStats() {
		GUIAnchor c = new GUIAnchor(getState(), 10, 100) {

			@Override
			public void draw() {
				super.draw();
				setWidth(GUIReactorNodeOptions.this.getWidth());
			}
		};
		GUITextOverlayTable l = new GUITextOverlayTable(getState());
		l.setText(new ObjectArrayList());
		l.getText().add(new Object() {

			@Override
			public String toString() {
				return Lng.str("Name: %s", node.getInfo().getName());
			}
		});
		l.getText().add(new Object() {

			@Override
			public String toString() {
				return Lng.str("Size: %s", node.getSize());
			}
		});
		l.getText().add(new Object() {

			@Override
			public String toString() {
				return Lng.str("Minimum Size Required: %s", node.getMinBlocksNeeded());
			}
		});
		l.getText().add(new Object() {

			@Override
			public String toString() {
				return Lng.str("HP-Contribution: %s/%s", node.calculateLocalHp(node.getActualSize()), node.calculateLocalHp(node.getSize()));
			}
		});
		l.getText().add(new Object() {

			@Override
			public String toString() {
				return Lng.str("Condition: %s%% (missing: %s)", StringTools.formatPointZero(node.getSizePercent() * 100f), node.getSize() - node.getActualSize());
			}
		});
		c.attach(l);
		return c;
	}

	@Override
	public boolean isActive() {
		return super.isActive() && activityElement.isActive();
	}

	@Override
	public void onInit() {
		if (init) {
			return;
		}
		buttons = new GUIHorizontalButtonTablePane(getState(), 3, 1, this);
		buttons.onInit();
		buttons.addButton(1, 0, new Object() {

			@Override
			public String toString() {
				if (node == null) {
					return "";
				}
				ElementInformation info = ElementKeyMap.getInfo(node.type);
				if (info.isReactorChamberSpecific()) {
					return Lng.str("REVERT CHAMBER");
				} else {
					return Lng.str("SPECIFY CHAMBER");
				}
			}
		}, HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(667);
					if (node == null) {
						return;
					}
					ElementInformation info = ElementKeyMap.getInfo(node.type);
					if (info.isReactorChamberSpecific()) {
						node.convertToClientRequest((short) info.chamberRoot);
					} else {
						node.popupSpecifyTileDialog(getState());
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				if (node == null || node.root.pw.isAnyRebooting()) {
					return false;
				}
				return GUIReactorNodeOptions.this.isActive() && !node.root.pw.isAnyDamaged();
			}
		});
		GUIHorizontalArea bDown = buttons.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("Level Down");
			}
		}, HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(668);
					ElementInformation info = ElementKeyMap.getInfo(node.type);
					System.err.println("[CLIENT] DOWNGRADE REQUEST FROM " + info.getName() + " to " + ElementKeyMap.toString((short) info.chamberParent));
					node.convertToClientRequest((short) info.chamberParent);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				if (node == null || node.root.pw.isAnyRebooting()) {
					return false;
				}
				ElementInformation info = ElementKeyMap.getInfo(node.type);
				return GUIReactorNodeOptions.this.isActive() && !node.root.pw.isAnyDamaged() && info.isReactorChamberSpecific() && info.chamberParent != 0 && ElementKeyMap.getInfo(info.chamberParent).chamberUpgradesTo == info.id;
			}
		});
		bDown.setToolTip(new GUIToolTip(getState(), new Object() {

			@Override
			public String toString() {
				if (node == null || node.root.pw.isAnyRebooting()) {
					return "";
				}
				ElementInformation info = ElementKeyMap.getInfo(node.type);
				if (info.isChamberUpgraded() && ElementKeyMap.isValidType(info.chamberParent)) {
					ElementInformation d = ElementKeyMap.getInfo(info.chamberParent);
					return Lng.str("Downgrade to %s; RC freed: %s%%", d.getName(), StringTools.formatPointZero((info.chamberCapacity * 100f)));
				} else {
					return "";
				}
			}
		}, bDown));
		GUIHorizontalArea bUp = buttons.addButton(2, 0, new Object() {

			@Override
			public String toString() {
				return Lng.str("Level Up");
			}
		}, HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					ElementInformation info = ElementKeyMap.getInfo(node.type);
					node.convertToClientRequest((short) info.chamberUpgradesTo);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				if (node == null || node.root.pw.isAnyRebooting()) {
					return false;
				}
				ElementInformation info = ElementKeyMap.getInfo(node.type);
				return GUIReactorNodeOptions.this.isActive() && !node.root.pw.isAnyDamaged() && info.isReactorChamberSpecific() && info.chamberUpgradesTo != 0;
			}
		});
		bUp.setToolTip(new GUIToolTip(getState(), new Object() {

			@Override
			public String toString() {
				if (node == null || node.root.pw.isAnyRebooting()) {
					return "";
				}
				ElementInformation info = ElementKeyMap.getInfo(node.type);
				if (ElementKeyMap.isValidType(info.chamberUpgradesTo)) {
					ElementInformation d = ElementKeyMap.getInfo(info.chamberUpgradesTo);
					return Lng.str("Upgrade to %s; RC used: %s%%", d.getName(), StringTools.formatPointZero((d.chamberCapacity * 100f)));
				} else {
					return "";
				}
			}
		}, bUp));
		this.stats = createStats();
		this.stats.onInit();
		init = true;
	}

	@Override
	public float getWidth() {
		return dependent.getWidth();
	}

	@Override
	public float getHeight() {
		return dependent.getHeight();
	}
}
