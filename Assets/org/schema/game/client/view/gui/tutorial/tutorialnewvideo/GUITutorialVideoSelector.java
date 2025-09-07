package org.schema.game.client.view.gui.tutorial.tutorialnewvideo;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.game.client.controller.tutorial.newtut.TutorialVideoPlayer;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.MovieDialog;
import org.schema.game.common.util.DataUtil;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class GUITutorialVideoSelector extends GUIDialogWindow {

	private String[] descriptions = new String[] { Lng.str("Learn the basics of moevement and building."), Lng.str("Learn how to build power supply for a structure."), Lng.str("Learn how add thrust for your ship."), Lng.str("Faster and easier building with advanced build tools."), Lng.str("Protect your ship by adding shields.") };

	private String[] obsoleteFileNames = new String[] { ("00 - Fundamentals.mp4"), ("01 - Power Systems.mp4"), ("02 - Thruster Systems.mp4"), ("03 - Advanced Build Tools.mp4"), ("04 - Shield Systems.mp4"), ("05 - Basic Build Mode Tutorial.mp4"), ("07 - Power Systems.mp4") // ("09 - Shield Systems.mp4"),
	};

	private MovieDialog introDialog;

	private TutorialVideoPlayer diag;

	private Object2ObjectOpenHashMap<String, String> descs = new Object2ObjectOpenHashMap<String, String>();

	private Object2ObjectOpenHashMap<String, String> extra = new Object2ObjectOpenHashMap<String, String>();

	public GUITutorialVideoSelector(InputState state, int initialWidth, int initialHeight, int initialPosX, int initalPosY, GUIActiveInterface activeInterface, final TutorialVideoPlayer diag) {
		super(state, initialWidth, initialHeight, initialPosX, initalPosY, "TUT_VIDEO_SEL_DIA");
		onInit();
		this.diag = diag;
		this.activeInterface = activeInterface;
		File dir = new FileExt(DataUtil.dataPath + "video/tutorial/");
		if (!dir.exists()) {
			assert (false);
			return;
		}
		setTitle(Lng.str("Tutorials"));
		for (String s : obsoleteFileNames) {
			(new FileExt(dir, s)).delete();
		}
		GUITilePane<Object> tiles = new GUITilePane<Object>(state, getMainContentPane().getContent(0), 180, 105);
		tiles.onInit();
		GUIScrollablePanel sc = new GUIScrollablePanel(10, 10, getMainContentPane().getContent(0), state);
		sc.setContent(tiles);
		File[] files = dir.listFiles();
		assert (files.length > 0);
		Arrays.sort(files, 0, files.length, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		int i = 0;
		for (int j = 0; j < files.length; j++) {
			if (files[j].getName().endsWith(".mp4")) {
				addFileTile(tiles, files[j], i < descriptions.length ? descriptions[i] : Lng.str("Video description not available"));
				i++;
			}
		}
		getMainContentPane().getContent(0).attach(sc);
		getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(10));
		getMainContentPane().setListDetailMode(0, getMainContentPane().getTextboxes().get(0));
		getMainContentPane().addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIHorizontalButtonTablePane t = new GUIHorizontalButtonTablePane(getState(), 3, 1, getMainContentPane().getContent(1));
		t.onInit();
		t.addButton(0, 0, "PLACEHOLDER", HButtonType.BUTTON_BLUE_MEDIUM, null, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return false;
			}

			@Override
			public boolean isActive(InputState state) {
				return false;
			}
		});
		t.addButton(1, 0, Lng.str("RESET TUTORIALS"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUITutorialVideoSelector.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(732);
					((GameClientState) getState()).getController().getTutorialController().resetTutorials();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUITutorialVideoSelector.this.isActive();
			}
		});
		t.addButton(2, 0, Lng.str("CLOSE"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUITutorialVideoSelector.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(733);
					diag.deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUITutorialVideoSelector.this.isActive();
			}
		});
		getMainContentPane().getContent(1).attach(t);
		GUICheckBoxTextPair s = new GUICheckBoxTextPair(getState(), Lng.str("Show next startup"), 140, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				return EngineSettings.TUTORIAL_NEW.isOn();
			}

			@Override
			public void deactivate() {
				EngineSettings.TUTORIAL_NEW.setOn(false);
			}

			@Override
			public void activate() {
				EngineSettings.TUTORIAL_NEW.setOn(true);
			}
		};
		s.setPos(8, 4, 0);
		getMainContentPane().getContent(1).attach(s);
	}

	private void addFileTile(GUITilePane<Object> tiles, final File file, final String description) {
		final int width = 800;
		final int height = 600;
		final MovieDialog m;
		final String title = file.getName().substring(5, file.getName().lastIndexOf("."));
		if (file.getParentFile() != null) {
			File meta = new FileExt(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".xml");
			if (meta.exists()) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				try {
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(meta);
					NodeList rChi = doc.getChildNodes();
					for (int j = 0; j < rChi.getLength(); j++) {
						Node rr = rChi.item(j);
						if (rr.getNodeType() == Node.ELEMENT_NODE && rr.getNodeName().toLowerCase(Locale.ENGLISH).equals("meta")) {
							NodeList childNodes = rr.getChildNodes();
							for (int i = 0; i < childNodes.getLength(); i++) {
								Node item = childNodes.item(i);
								if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().toLowerCase(Locale.ENGLISH).equals("keybinds")) {
									extra.put(title, item.getTextContent());
								}
								if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().toLowerCase(Locale.ENGLISH).equals("description")) {
									descs.put(title, item.getTextContent());
								}
							}
						}
					}
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			m = new MovieDialog(getState(), "TUT_MOVIE", width, height, GLFrame.getWidth() / 2 - width / 2, GLFrame.getHeight() / 2 - height / 2, file) {

				@Override
				public void onDeactivate() {
					super.onDeactivate();
					if (this == introDialog) {
						EngineSettings.TUTORIAL_PLAY_INTRO.setOn(false);
					}
					((GameClientState) getState()).getController().getTutorialController().addWatched(title);
				}
			};
			m.setTitle(title);
			m.setExtraPanel(new AddTextBoxInterface() {

				@Override
				public int getHeight() {
					return 28;
				}

				@Override
				public GUIElement createAndAttach(GUIAnchor content) {
					GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 1, 1, content);
					p.onInit();
					p.addButton(0, 0, Lng.str("Play in background"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

						@Override
						public boolean isOccluded() {
							return !m.isActive();
						}

						@Override
						public void callback(GUIElement callingGuiElement, MouseEvent event) {
							if (event.pressedLeftMouse()) {
								m.setInBackground(true);
								m.deactivate();
								diag.deactivate();
//								((GameClientState) getState()).getController().getTutorialController().setBackgroundVideo(m);
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
								AudioController.fireAudioEventID(734);
							}
						}
					}, new GUIActivationCallback() {

						@Override
						public boolean isVisible(InputState state) {
							return true;
						}

						@Override
						public boolean isActive(InputState state) {
							return m.isActive();
						}
					});
					return p;
				}
			});
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		if (file.getName().startsWith("00 -")) {
			introDialog = m;
		}
		boolean watched = ((GameClientState) getState()).getController().getTutorialController().isWatched(title);
		HButtonColor t = HButtonColor.BLUE;
		GUITile addButtonTile = tiles.addButtonTile(title, descs.containsKey(title) ? descs.get(title) : description, t, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUITutorialVideoSelector.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					try {
						final MovieDialog mPl = new MovieDialog(getState(), "TUT_MOVIE", width, height, GLFrame.getWidth() / 2 - width / 2, GLFrame.getHeight() / 2 - height / 2, file) {

							@Override
							public void onDeactivate() {
								super.onDeactivate();
								if (this == introDialog) {
									EngineSettings.TUTORIAL_PLAY_INTRO.setOn(false);
								}
								((GameClientState) getState()).getController().getTutorialController().addWatched(title);
							}
						};
						mPl.setTitle(title);
						mPl.setExtraPanel(new AddTextBoxInterface() {

							@Override
							public int getHeight() {
								return 28;
							}

							@Override
							public GUIElement createAndAttach(GUIAnchor content) {
								GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 2, 1, content);
								p.onInit();
								final GUIScrollablePanel sc = new GUIScrollablePanel(10, 10, getState());
								GUICheckBoxTextPair s = new GUICheckBoxTextPair(getState(), Lng.str("Subtitles"), UIScale.getUIScale().scale(80), FontSize.SMALL_14, UIScale.getUIScale().h) {

									@Override
									public boolean isActivated() {
										return EngineSettings.SUBTITLES.isOn();
									}

									@Override
									public void deactivate() {
										EngineSettings.SUBTITLES.setOn(false);
									}

									@Override
									public void activate() {
										EngineSettings.SUBTITLES.setOn(true);
									}
								};
								s.setPos(UIScale.getUIScale().scale(8), UIScale.getUIScale().inset, 0);
								sc.setContent(s);
								sc.onInit();
								content.attach(sc);
								p.addButton(0, 0, "paceholder", HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

									@Override
									public boolean isOccluded() {
										return true;
									}

									@Override
									public void callback(GUIElement callingGuiElement, MouseEvent event) {
									}
								}, new GUIActivationCallback() {

									@Override
									public boolean isVisible(InputState state) {
										sc.setWidth(getWidth());
										sc.setHeight(getHeight());
										return false;
									}

									@Override
									public boolean isActive(InputState state) {
										return false;
									}
								});
								p.addButton(1, 0, Lng.str("Play in background"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

									@Override
									public boolean isOccluded() {
										return !mPl.isActive();
									}

									@Override
									public void callback(GUIElement callingGuiElement, MouseEvent event) {
										if (event.pressedLeftMouse()) {
											mPl.setInBackground(true);
											mPl.deactivate();
											diag.deactivate();
//											((GameClientState) getState()).getController().getTutorialController().setBackgroundVideo(mPl);
										}
									}
								}, new GUIActivationCallback() {

									@Override
									public boolean isVisible(InputState state) {
										return true;
									}

									@Override
									public boolean isActive(InputState state) {
										return mPl.isActive();
									}
								});
								return p;
							}
						});
						mPl.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(735);
					} catch (IOException e) {
						e.printStackTrace();
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
				return GUITutorialVideoSelector.this.isActive();
			}
		});
		((GUITileButtonDesc) addButtonTile).onDrawInterface = (button, descriptionText) -> {
			boolean watched1 = ((GameClientState) getState()).getController().getTutorialController().isWatched(title);
			HButtonType t1 = HButtonType.BUTTON_BLUE_MEDIUM;
			if (watched1) {
				// ALREADY WATCHED
				String des = "";
				if (!descriptionText.getText().get(0).toString().startsWith(des)) {
					descriptionText.setTextSimple(des + (descs.containsKey(title) ? descs.get(title) : description));
				}
				t1 = HButtonType.BUTTON_BLUE_DARK;
			} else {
				descriptionText.setTextSimple((descs.containsKey(title) ? descs.get(title) : description));
			}
			button.setDefaultType(t1);
		};
	}

	public void playIntroVideo() {
		if (introDialog != null) {
			introDialog.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(736);
		}
	}
}
