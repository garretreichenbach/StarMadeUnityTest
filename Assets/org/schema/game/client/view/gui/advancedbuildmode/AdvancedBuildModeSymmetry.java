package org.schema.game.client.view.gui.advancedbuildmode;

import org.schema.game.client.controller.manager.ingame.SymmetryPlanes;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.CheckboxCallback;
import org.schema.game.client.view.gui.advanced.tools.CheckboxResult;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class AdvancedBuildModeSymmetry extends AdvancedBuildModeGUISGroup{


	

	public AdvancedBuildModeSymmetry(AdvancedGUIElement e) {
		super(e);
	}
	
	private class SymResult extends ButtonResult{

		private final int mode;

		public SymResult(int mode) {
			this.mode = mode;
		}

		@Override
		public HButtonColor getColor() {
			switch (mode) {
			case (SymmetryPlanes.MODE_XY):
				return HButtonColor.BLUE;
			case (SymmetryPlanes.MODE_XZ):
				return HButtonColor.GREEN;
			case (SymmetryPlanes.MODE_YZ):
				return HButtonColor.RED;
			}	
			
			throw new RuntimeException("Mode fail: "+mode);
		}

		@Override
		public ButtonCallback initCallback() {
			return new ButtonCallback() {
				
				@Override
				public void pressedRightMouse() {
					getSymmetryPlanes().setPlaceMode(0);					
				}
				
				@Override
				public void pressedLeftMouse() {
//					System.err.println("DKLD "+getSymmetryPlanes().getPlaceMode()+"; "+getSymmetryPlanes().isXyPlaneEnabled());
					if (getSymmetryPlanes().getPlaceMode() == 0) {
						switch (mode) {
							case (SymmetryPlanes.MODE_XY):
								if (getSymmetryPlanes().isXyPlaneEnabled()) {
									getSymmetryPlanes().setXyPlaneEnabled(false);
								} else {
									getSymmetryPlanes().setPlaceMode(mode);
								}
								break;
							case (SymmetryPlanes.MODE_XZ):
								if (getSymmetryPlanes().isXzPlaneEnabled()) {
									getSymmetryPlanes().setXzPlaneEnabled(false);
								} else {
									getSymmetryPlanes().setPlaceMode(mode);
								}
								break;
							case (SymmetryPlanes.MODE_YZ):
								if (getSymmetryPlanes().isYzPlaneEnabled()) {
									getSymmetryPlanes().setYzPlaneEnabled(false);
								} else {
									getSymmetryPlanes().setPlaceMode(mode);
								}
								break;
						}
					} else {
						getSymmetryPlanes().setPlaceMode(0);
					}
					
				}
			};
		}

		@Override
		public String getName() {
			if (getSymmetryPlanes().getPlaceMode() == mode) {
				return Lng.str("*click on block*");
			} else {
				switch (mode) {
					case (SymmetryPlanes.MODE_XY):
						if (getSymmetryPlanes().isXyPlaneEnabled()) {
							return Lng.str("Unset XY");
						} else {
							return Lng.str("XY");
						}
					case (SymmetryPlanes.MODE_XZ):
						if (getSymmetryPlanes().isXzPlaneEnabled()) {
							return Lng.str("Unset XZ");
						} else {
							return Lng.str("XZ");
						}
					case (SymmetryPlanes.MODE_YZ):
						if (getSymmetryPlanes().isYzPlaneEnabled()) {
							return Lng.str("Unset YZ");
						} else {
							return Lng.str("YZ");
						}
				}
				throw new RuntimeException("Mode fail: "+mode);
			}
		}

		@Override
		public boolean isHighlighted() {
			switch (mode) {
				case (SymmetryPlanes.MODE_XY):
					if (getSymmetryPlanes().isXyPlaneEnabled()) {
						return true;
					}
					break;
				case (SymmetryPlanes.MODE_XZ):
					if (getSymmetryPlanes().isXzPlaneEnabled()) {
						return true;
					}
					break;
				case (SymmetryPlanes.MODE_YZ):
					if (getSymmetryPlanes().isYzPlaneEnabled()) {
						return true;
					}
					break;
			}
			return false;
		}
	}
	
	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
		GUITextOverlay l2 = new GUITextOverlay(getState());
		addButton(pane.getContent(0), 0, 0, new SymResult(SymmetryPlanes.MODE_XY));
		addButton(pane.getContent(0), 1, 0, new SymResult(SymmetryPlanes.MODE_XZ));
		addButton(pane.getContent(0), 2, 0, new SymResult(SymmetryPlanes.MODE_YZ));

		//only for testing
		addButton(pane.getContent(0), 0, 1, new ButtonResult() {
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedLeftMouse() {
						int value = getSymmetryPlanes().getXyExtraDist();
						getSymmetryPlanes().setXyExtraDist(value == 0 ? 1 : 0);
					}

					@Override
					public void pressedRightMouse() {
					}

				};

			}

			@Override
			public String getName() {
				return Lng.str("XY ODD");
			}

			@Override
			public boolean isHighlighted() {
				return getSymmetryPlanes().getXyExtraDist() == 1;
			}
		});

		//only for testing
		addButton(pane.getContent(0), 1, 1, new ButtonResult() {
			@Override
			public HButtonColor getColor() {
				return HButtonColor.GREEN;
			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedLeftMouse() {
						int value = getSymmetryPlanes().getXzExtraDist();
						getSymmetryPlanes().setXzExtraDist(value == 0 ? 1 : 0);
					}

					@Override
					public void pressedRightMouse() {
					}

				};

			}

			@Override
			public String getName() {
				return Lng.str("XZ ODD");
			}

			@Override
			public boolean isHighlighted() {
				return getSymmetryPlanes().getXzExtraDist() == 1;
			}
		});

		//only for testing
		addButton(pane.getContent(0), 2, 1, new ButtonResult() {
			@Override
			public HButtonColor getColor() {
				return HButtonColor.RED;
			}

			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedLeftMouse() {
						int value = getSymmetryPlanes().getYzExtraDist();
						getSymmetryPlanes().setYzExtraDist(value == 0 ? 1 : 0);
					}

					@Override
					public void pressedRightMouse() {
					}

				};

			}

			@Override
			public String getName() {
				return Lng.str("YZ ODD");
			}

			@Override
			public boolean isHighlighted() {
				return getSymmetryPlanes().getYzExtraDist() == 1;
			}
		});
		
		pane.addNewTextBox(UIScale.getUIScale().scale(30));
//		addCheckbox(pane.getContent(1), 0, 0, new CheckboxResult() {
//			
//			@Override
//			public CheckboxCallback initCallback() {
//				return null;
//			}
//			
//			@Override
//			public String getName() {
//				return "Block-centered Symmetry";
//			}
//			
//			@Override
//			public boolean getDefault() {
//				return getSymmetryPlanes().getExtraDist() == 0;
//			}
//
//			@Override
//			public boolean getCurrentValue() {
//				return getSymmetryPlanes().getExtraDist() == 0;
//			}
//
//			@Override
//			public void setCurrentValue(boolean b) {
//				getSymmetryPlanes().setExtraDist(b ? 0 : 1);		
//			}
//			@Override
//			public String getToolTipText() {
//				return Lng.str("puts symmetry planes in the middle of block");
//			}
//		});
		addCheckbox(pane.getContent(1), 0, 0, new CheckboxResult() {
			
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			
			@Override
			public String getName() {
				return Lng.str("Mirror cubic blocks");
			}
			
			@Override
			public boolean getDefault() {
				return getSymmetryPlanes().isMirrorCubeShapes();
			}
			
			@Override
			public boolean getCurrentValue() {
				return getSymmetryPlanes().isMirrorCubeShapes();
			}
			
			@Override
			public void setCurrentValue(boolean b) {
				getSymmetryPlanes().setMirrorCubeShapes(b);
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Rotates cubic blocks on the other side of the plane\nto mirror the blocks you place");
			}
		});
		addCheckbox(pane.getContent(1), 0, 1, new CheckboxResult() {
			
			@Override
			public CheckboxCallback initCallback() {
				return null;
			}
			
			@Override
			public String getName() {
				return Lng.str("Mirror non-cubic blocks");
			}
			
			@Override
			public boolean getDefault() {
				return getSymmetryPlanes().isMirrorNonCubicShapes();
			}
			
			@Override
			public boolean getCurrentValue() {
				return getSymmetryPlanes().isMirrorNonCubicShapes();
			}
			
			@Override
			public void setCurrentValue(boolean b) {
				getSymmetryPlanes().setMirrorNonCubicShapes(b);
			}
			@Override
			public String getToolTipText() {
				return Lng.str("Rotates non-cubic blocks on the other side of the plane\nto mirror the blocks you place");
			}
		});
	}

	
	@Override
	public String getId() {
		return "BSYMMETRY";
	}

	@Override
	public String getTitle() {
		return Lng.str("Symmetry");
	}

	


}
