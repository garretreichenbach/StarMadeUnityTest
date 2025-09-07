package org.schema.game.client.view.gui.fleet;

import api.common.GameClient;
import api.utils.game.PlayerUtils;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.view.gui.RadialMenu;
import org.schema.game.client.view.gui.RadialMenuDialog;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Fleet Quick Menu Dialog.
 * <p>Allows players to pre-define specific fleet commands for easy access.</p>
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class FleetQuickMenuDialog extends RadialMenuDialog implements GUIActivationCallback {

	private static FleetQuickMenuConfig config;

	public FleetQuickMenuDialog() {
		super(GameClient.getClientState());
	}

	public static String parseArgs(String t, FleetCommandData commandData) {
		String[] split = t.split(", ");
		commandData.args = new Object[split.length];
		for(int i = 0; i < split.length; i++) {
			try {
				commandData.args[i] = Integer.parseInt(split[i]);
				continue;
			} catch(Exception ignored) {}
			try {
				commandData.args[i] = Double.parseDouble(split[i]);
				continue;
			} catch(Exception ignored) {}
			try {
				commandData.args[i] = Long.parseLong(split[i]);
				continue;
			} catch(Exception ignored) {}
			try {
				if(split[i].contains("~")) {
					Vector3i currentSector = GameClient.getClientState().getPlayer().getCurrentSector();
					String[] coords = split[i].split(",");
					for(int j = 0; j < coords.length; j++) {
						if(coords[j].equals("~")) coords[j] = String.valueOf(currentSector.getCoord(j));
						else if(coords[j].startsWith("~")) coords[j] = String.valueOf(currentSector.getCoord(j) + Integer.parseInt(coords[j].substring(1)));
					}
					commandData.args[i] = Vector3i.parseVector3iFree(coords[0] + "," + coords[1] + "," + coords[2]);
				} else commandData.args[i] = Vector3i.parseVector3iFree(split[i]);
				continue;
			} catch(Exception ignored) {}
			try {
				if(split[i].contains("~")) {
					Vector3i currentSector = GameClient.getClientState().getPlayer().getCurrentSector();
					String[] coords = split[i].split(" ");
					for(int j = 0; j < coords.length; j++) {
						if(coords[j].equals("~")) coords[j] = String.valueOf(currentSector.getCoord(j));
						else if(coords[j].startsWith("~")) coords[j] = String.valueOf(currentSector.getCoord(j) + Integer.parseInt(coords[j].substring(1)));
					}
					commandData.args[i] = Vector3i.parseVector3iFree(coords[0] + " " + coords[1] + " " + coords[2]);
				} else commandData.args[i] = Vector3i.parseVector3iFree(split[i]);
				continue;
			} catch(Exception ignored) {}
			try {
				if(split[i].toLowerCase(Locale.ENGLISH).equals("true") || split[i].toLowerCase(Locale.ENGLISH).equals("false")) {
					commandData.args[i] = Boolean.parseBoolean(split[i]);
					continue;
				}
			} catch(Exception ignored) {}
			commandData.args[i] = split[i];
		}
		return t;
	}

	@Override
	public RadialMenu createMenu(RadialMenuDialog radialMenuDialog) {
		RadialMenu menu = new RadialMenu(getState(), "FleetQuickMenu", radialMenuDialog, 800, 600, 50, FontLibrary.FontSize.BIG_20);
		if(config == null) loadConfig();
		for(int i = 0; i < config.getFleetCommandData().length; i ++) {
			final FleetCommandData fleetCommandData = config.getFleetCommandData()[i];
			if(fleetCommandData != null) {
				final int finalI = i;
				menu.addItem(fleetCommandData.display, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							if(fleetCommandData.fleetDbId == -1) {
								deactivate();
								(new FleetCommandDataSetDialog(fleetCommandData, finalI)).activate();
							} else {
								try {
									parseArgs(fleetCommandData.argsString, fleetCommandData);
									getState().getFleetManager().getByFleetDbId(fleetCommandData.fleetDbId).sendFleetCommand(FleetCommandTypes.valueOf(fleetCommandData.command.toUpperCase(Locale.ENGLISH)), fleetCommandData.args);
								} catch(Exception exception) {
									exception.printStackTrace();
									PlayerUtils.sendMessage(getState().getPlayer(), "Failed to send fleet command!");
								}
							}
						} else if(event.pressedRightMouse()) {
							(new PlayerOkCancelInput("CONFIRM_COMMAND_DELETION", getState(), Lng.str("Remove Command"), Lng.str("Are you sure you want to remove this entry?")) {
								@Override
								public void onDeactivate() {

								}

								@Override
								public void pressedOK() {
									config.getFleetCommandData()[finalI] = new FleetCommandData("EMPTY", -1, null);
									saveConfig();
									deactivate();
									createMenu(FleetQuickMenuDialog.this);
								}
							}).activate();
						}
					}

					@Override
					public boolean isOccluded() {
						return !FleetQuickMenuDialog.this.isActive(getState());
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState state) {
						return FleetQuickMenuDialog.this.isActive(getState());
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
			}
		}
		return menu;
	}

	@Override
	public boolean isVisible(InputState state) {
		return true;
	}

	@Override
	public boolean isActive(InputState state) {
		return super.isActive();
	}

	public static void loadConfig() {
		try {
			File configFile = new File("fleet-quick-menu.json");
			if(!configFile.exists()) {
				config = new FleetQuickMenuConfig();
				saveConfig();
			} else config = new Gson().fromJson(new String(Files.readAllBytes(configFile.toPath())), FleetQuickMenuConfig.class);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void saveConfig() {
		File configFile = getConfigFile();
		if(configFile.exists()) configFile.delete();
		try {
			configFile.createNewFile();
			OutputStream outputStream = Files.newOutputStream(configFile.toPath());
			Gson gson = new Gson();
			outputStream.write(gson.toJson(config).getBytes());
			outputStream.flush();
			outputStream.close();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public static File getConfigFile() {
		File configFile = new File("fleet-quick-menu.json");
		try {
			if(!configFile.exists()) {
				configFile.createNewFile();
				writeDefaultConfig(configFile);
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return configFile;
	}

	private static void writeDefaultConfig(File configFile) {
		//Create new json
		Gson gson = new Gson();
		String json = gson.toJson(new FleetQuickMenuConfig());
		try {
			OutputStream fileOutputStream = Files.newOutputStream(configFile.toPath());
			fileOutputStream.write(json.getBytes());
			fileOutputStream.close();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public static class FleetQuickMenuConfig {

		private final FleetCommandData[] fleetCommandData = new FleetCommandData[10];

		public FleetQuickMenuConfig() {
			for(int i = 0; i < fleetCommandData.length; i ++) fleetCommandData[i] = new FleetCommandData("EMPTY", -1, null);
		}

		public FleetCommandData[] getFleetCommandData() {
			return fleetCommandData;
		}
	}

	public static class FleetCommandData implements Serializable {

		private String display;
		private long fleetDbId;
		private String command;
		public String argsString;
		private Object[] args;

		public FleetCommandData(String display, long fleetDbId, String command, Object... args) {
			this.display = display;
			this.fleetDbId = fleetDbId;
			this.command = command;
			this.args = args;
		}

		public String getDisplay() {
			return display;
		}

		public long getFleetDbId() {
			return fleetDbId;
		}

		public String getCommand() {
			return command;
		}

		public Object[] getArgs() {
			return args;
		}
	}

	public static class FleetCommandDataSetDialog extends DialogInput {

		private final FleetQuickCommandPanel inputPanel;

		public FleetCommandDataSetDialog(FleetCommandData commandData, int index) {
			super(GameClient.getClientState());
			inputPanel = new FleetQuickCommandPanel(this, getState(), commandData, index);
		}

		@Override
		public FleetQuickCommandPanel getInputPanel() {
			return inputPanel;
		}

		@Override
		public void onDeactivate() {

		}
	}

	public static class FleetQuickCommandPanel extends GUIElement implements GUIActiveInterface {

		private final List<GUIElement> toCleanUp = new ObjectArrayList<>();
		private final FleetCommandData commandData;

		private final FleetCommandDataSetDialog dialog;
		private GUIMainWindow mainPanel;
		private final int index;

		public FleetQuickCommandPanel(FleetCommandDataSetDialog dialog, InputState state, FleetCommandData commandData, int index) {
			super(state);
			this.dialog = dialog;
			this.commandData = commandData;
			this.index = index;
		}

		@Override
		public void onInit() {
			mainPanel = new GUIMainWindow(getState(), 500, 300, 500, 400, "FLEET_QUICK_MENU_SET_COMMAND");
			mainPanel.onInit();
			mainPanel.setPos(500, 300, 0);
			mainPanel.setWidth(500);
			mainPanel.setHeight(400);
			mainPanel.clearTabs();

			final GUIContentPane contentPane = mainPanel.addTab(Lng.str("Set Command"));
			{
				GUIActivatableTextBar nameBar = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.MEDIUM_15, 64, 1, Lng.str("Command Alias"), contentPane.getContent(0), new TextCallback() {
					@Override
					public String[] getCommandPrefixes() {
						return new String[0];
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return null;
					}

					@Override
					public void onFailedTextCheck(String msg) {

					}

					@Override
					public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {

					}

					@Override
					public void newLine() {

					}
				}, contentPane.getTextboxes().get(0), t -> {
					commandData.display = t;
					return t;
				});
				contentPane.getContent(0).attach(nameBar);
				nameBar.setText(commandData.getDisplay());
				nameBar.setDeleteOnEnter(false);
				contentPane.setTextBoxHeightLast((int) nameBar.getHeight());

				contentPane.addNewTextBox(32);
				ArrayList<GUIElement> fleetElements = new ArrayList<>();
				for(Fleet fleet : GameClient.getClientState().getFleetManager().getAvailableFleetsClient()) {
					final GUIAnchor anchor = new GUIAnchor(getState(), 10, 24);
					GUITextOverlayTableDropDown dropDown = new GUITextOverlayTableDropDown(10, 10, getState());
					dropDown.setTextSimple(fleet.getName());
					dropDown.setPos(4, 4, 0);
					dropDown.setUserPointer(fleet.dbid);
					anchor.setUserPointer(fleet.dbid);
					anchor.attach(dropDown);
					fleetElements.add(anchor);
				}
				final GUIDropDownList fleetList = new GUIDropDownList(getState(), (int) contentPane.getContent(1).getWidth(), 24, 400, element -> {
					try {
						commandData.fleetDbId = (long) element.getContent().getUserPointer();
					} catch(Exception ignored) {}
				}, fleetElements);
				fleetList.onInit();
				fleetList.dependend = contentPane.getContent(1);
				contentPane.getContent(1).attach(fleetList);
				contentPane.setTextBoxHeightLast((int) fleetList.getHeight());

				contentPane.addNewTextBox(32);
				final ArrayList<GUIElement> commandElements = new ArrayList<>();
				float commandElementHeight = 0;
				for(FleetCommandTypes commandType : FleetCommandTypes.values()) {
					final GUIAnchor anchor = new GUIAnchor(getState(), 10, 24);
					GUITextOverlayTableDropDown dropDown = new GUITextOverlayTableDropDown(10, 10, getState());
					dropDown.setTextSimple(commandType.name().replaceAll("_", " "));
					dropDown.setPos(4, 4, 0);
					dropDown.setUserPointer(commandType);
					anchor.setUserPointer(commandType);
					anchor.attach(dropDown);
					commandElements.add(anchor);
					commandElementHeight += anchor.getHeight();
				}
				final GUIDropDownList commandList = new GUIDropDownList(getState(), (int) contentPane.getContent(2).getWidth(), 24, (int) commandElementHeight, element -> {
					try {
						if(element == null) commandData.command = "IDLE";
						else commandData.command = ((FleetCommandTypes) element.getContent().getUserPointer()).name();
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}, commandElements);
				commandList.onInit();
				commandList.dependend = contentPane.getContent(2);
				contentPane.getContent(2).attach(commandList);
				contentPane.setTextBoxHeightLast((int) commandList.getHeight());

				contentPane.addNewTextBox(32);
				GUIActivatableTextBar argumentsBar = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.MEDIUM_15, 64, 1, Lng.str("Command Arguments"), contentPane.getContent(3), new TextCallback() {
					@Override
					public String[] getCommandPrefixes() {
						return new String[0];
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) {
						return null;
					}

					@Override
					public void onFailedTextCheck(String msg) {

					}

					@Override
					public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {

					}

					@Override
					public void newLine() {

					}
				}, contentPane.getTextboxes().get(3), t -> {
					//if(t.isEmpty()) return t;
					//return parseArgs(t);
					commandData.argsString = t;
					return t;
				});
				contentPane.getContent(3).attach(argumentsBar);
				argumentsBar.setText(Arrays.toString(commandData.getArgs()));
				argumentsBar.setDeleteOnEnter(false);
				contentPane.setTextBoxHeightLast((int) argumentsBar.getHeight());

				contentPane.addNewTextBox(32);
				GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, contentPane.getContent(4));
				buttonPane.onInit();
				buttonPane.addButton(0, 0, Lng.str("ADD COMMAND"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							if(!fleetList.isEmpty() && commandData.fleetDbId == -1) {
								try {
									commandData.fleetDbId = (long) fleetList.get(0).getContent().getUserPointer();
									commandData.command = commandList.get(0).getContent().getUserPointer().toString();
								} catch(Exception ignored) {}
							}
							config.fleetCommandData[index] = commandData;
							saveConfig();
							dialog.deactivate();
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				buttonPane.addButton(1, 0, Lng.str("CANCEL"), GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) dialog.deactivate();
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}, new GUIActivationCallback() {
					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return true;
					}
				});
				contentPane.getContent(4).attach(buttonPane);
				contentPane.setTextBoxHeightLast((int) buttonPane.getHeight());
				toCleanUp.add(mainPanel);
			}
			mainPanel.activeInterface = this;
		}

		@Override
		public void draw() {
			if(mainPanel == null) onInit();
			GlUtil.glPushMatrix();
			transform();
			mainPanel.draw();
			GlUtil.glPopMatrix();
		}

		@Override
		public void cleanUp() {
			for(GUIElement element : toCleanUp) element.cleanUp();
			toCleanUp.clear();
			mainPanel.cleanUp();
		}

		@Override
		public boolean isInside() {
			return mainPanel.isInside();
		}

		@Override
		public float getWidth() {
			return 0;
		}

		@Override
		public float getHeight() {
			return 0;
		}
	}
}
