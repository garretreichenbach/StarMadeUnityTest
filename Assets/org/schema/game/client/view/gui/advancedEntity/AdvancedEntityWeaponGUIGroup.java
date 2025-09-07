package org.schema.game.client.view.gui.advancedEntity;

import java.util.Collection;
import java.util.List;

import org.schema.game.client.data.CollectionManagerChangeListener;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.ButtonCallback;
import org.schema.game.client.view.gui.advanced.tools.ButtonResult;
import org.schema.game.client.view.gui.advanced.tools.DropdownCallback;
import org.schema.game.client.view.gui.advanced.tools.DropdownResult;
import org.schema.game.client.view.gui.advanced.tools.StatLabelResult;
import org.schema.game.common.controller.SegNotifyType;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.FiringUnit;
import org.schema.game.common.controller.elements.UsableControllableFiringElementManager;
import org.schema.game.common.controller.elements.combination.Combinable;
import org.schema.game.common.controller.elements.combination.CombinationSettings;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class AdvancedEntityWeaponGUIGroup<
		E extends FiringUnit<E, CM, EM>, 
		CM extends ControlBlockElementCollectionManager<E, CM, EM>, 
		EM extends UsableControllableFiringElementManager<E, CM, EM>,
		M extends Modifier,
		C extends CombinationSettings> extends AdvancedEntityGUIGroup implements CollectionManagerChangeListener{

	
	public E unit;
	private long lastCheckCS;
	private C cachedCC;
	
	
	public List<CM> getCollectionManagers(){
		return getEm().getCollectionManagers();
	}
	
	protected abstract C getWeaponCombiSettingsRaw(); 
	
	public C getWeaponCombiSettings() {
		if(getState().updateTime-lastCheckCS > 500) {
			
			cachedCC = getWeaponCombiSettingsRaw();
			lastCheckCS = getState().getUpdateTime();
		}
		return cachedCC;
	}
	
	public AdvancedEntityWeaponGUIGroup(AdvancedGUIElement e) {
		super(e);
		
		getState().getController().addCollectionManagerChangeListener(this);
	}
	public abstract EM getEm();
	
	public CM selectedCollectionManager;
	public E selectedElement;
	
	private boolean dirtyModuleDropdownDirty;
	private M cachedModifier;
	private long lastCheck;
	public short getModuleType() {
		return getEm().controllingId;
	}
	public short getComputerType() {
		return getEm().controllerId;
	}
	public void selectSelectedWeapon() {
		if(selectedCollectionManager != null) {
			getPlayerInteractionControlManager().setSelectedBlockByActiveController(selectedCollectionManager.getControllerElement());
		}
	}
	public M getCombiValue() {
		
		if(getState().updateTime-lastCheck > 500) {
		
			ControlBlockElementCollectionManager<?, ?, ?> s = selectedCollectionManager.getSupportCollectionManager();
			if(s != null) {
				cachedModifier = (M) ((Combinable)getEm()).getAddOn().getGUI(selectedCollectionManager, selectedElement, s, selectedCollectionManager.getEffectCollectionManager());
			}else {
				cachedModifier = null;
			}
			lastCheck = getState().getUpdateTime();
		}
		return cachedModifier;
	}
	public int getComputerCount() {
		return getTypeCount(getComputerType());
	}
	public int getModuleCount() {
			return getTypeCount(getModuleType());
	}
	public void combineWith(CM other) {
		if(selectedCollectionManager != null) {
			getSegCon().getControlElementMap().switchControllerForElement(selectedCollectionManager.getControllerIndex(), other.getControllerIndex(), other.getControllerElement().getType());
		}
	}
	public void addWeaponPanel(GUIContentPane pane, int x, int y) {
		addButton(pane.getContent(0), x, y, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					
					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						if(getState().getPlayerInputs().isEmpty()) {
							getPlayerGameControlManager().weaponAction();
						}
					}
				};
			}
			
			@Override
			public boolean isHighlighted() {
				return getPlayerGameControlManager().getWeaponControlManager().isTreeActive();
			}

			@Override
			public String getName() {
				return Lng.str("Assign & Comb.");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
	}
	public int addWeaponBlockIcons(GUIContentPane pane, int x, int y) {
		addStatLabel(pane.getContent(0), x, y++, new StatLabelResult() {
			
			@Override
			public String getName() {
				return Lng.str("Computer:");
			}
			
			@Override
			public String getValue() {
				return ElementKeyMap.getInfo(getComputerType()).getName();
			}
			
			@Override
			public int getStatDistance() {
				return 100;
			}
		});
		addStatLabel(pane.getContent(0), x, y++, new StatLabelResult() {
			
			@Override
			public String getName() {
				return Lng.str("Module:");
			}
			
			@Override
			public String getValue() {
				return ElementKeyMap.getInfo(getModuleType()).getName();
			}
			
			@Override
			public int getStatDistance() {
				return 100;
			}
		});
		
		
		addWeaponBlockIcon(pane, x, y, new Object() {
			public String toString() {
				if(getMan() != null && getEm() != null) {
					return Lng.str("Computer (%s)", ElementKeyMap.getInfo(getComputerType()).getName());
				}
				return "";
			}
		}, new InitInterface() {
		
			public short getType() {
				if(isInit()) {
					return getComputerType();
				}else {
					return ElementKeyMap.CORE_ID;
				}
			}
			@Override
			public boolean isInit() {
				return getMan() != null && getEm() != null;
			}
		});
		
		addWeaponBlockIcon(pane, x+1, y++, new Object() {
			public String toString() {
				if(getMan() != null && getEm() != null) {
					return Lng.str("Module (%s)", ElementKeyMap.getInfo(getModuleType()).getName());
				}
				return "";
			}
		}, new InitInterface() {
			public short getType() {
				if(isInit()) {
					return getModuleType();
				}else {
					return ElementKeyMap.CORE_ID;
				}
			}
			@Override
			public boolean isInit() {
				return getMan() != null && getEm() != null;
			}
		});
		
		
		return y;
	}
	
	public void addAddButton(GUIContentPane pane, int x, int y) {
		addButton(pane.getContent(0), x, y, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
						
					}
					
					@Override
					public void pressedLeftMouse() {
						resetQueue();
						selectSelectedWeapon();
						promptBuild(getModuleType(), 1, Lng.str("Build one or more %s as the weapon output.\nGroups of blocks are combined into a stronger output.\nThe bigger the group, the stronger the weapon.",ElementKeyMap.getInfo(getModuleType()).getName()));
					}
				};
			}
			
			
			@Override
			public boolean isActive() {
				return super.isActive() && getState().getPlayer() != null && getState().getPlayer().getInventory().existsInInventory(getModuleType()) && canQueue(getModuleType(), 1);
			}


			@Override
			public String getToolTipText() {
				return Lng.str("You need %s blocks to add to this weapon!",ElementKeyMap.getInfo(getComputerType()).getName());
			}


			@Override
			public String getName() {
				return Lng.str("Add modules");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
	}
	public void addSelectButton(GUIContentPane pane, int x, int y) {
		addButton(pane.getContent(0), x, y, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
						
					}
					
					@Override
					public void pressedLeftMouse() {
						selectSelectedWeapon();
					}
				};
			}
			
			@Override
			public String getName() {
				return Lng.str("Select");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}
		});
	}
	public void addBuildButton(GUIContentPane pane, GUIDockableDirtyInterface dInt, int x, int y) {
		addButton(pane.getContent(0), x, y, new ButtonResult() {
			
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}
					
					@Override
					public void pressedLeftMouse() {
						resetQueue();
						promptBuild(getComputerType(), 1, Lng.str("First, build a %s",ElementKeyMap.getInfo(getComputerType()).getName()));
						promptBuild(getModuleType(), 1, Lng.str("Now, build one or more %s as the weapon barrel.\nGroups of blocks are combined into a stronger output.\nThe bigger the group, the stronger the weapon.",ElementKeyMap.getInfo(getModuleType()).getName()));
					}
				};
			}
			
			@Override
			public String getName() {
				return Lng.str("Build");
			}
			
			@Override
			public HButtonColor getColor() {
				return HButtonColor.BLUE;
			}

			@Override
			public boolean isActive() {
				return super.isActive() && canQueue(getComputerType(), 1) && canQueue(getModuleType(), 1);
			}

			
			
			
		});
	}
	public boolean isSlave(CM t) {
		return getMan() != null && getMan().getSlavesAndEffects().contains(t.getControllerIndex4());
	}
	public class WeaponSelectDropdownResult extends DropdownResult implements DrawerObserver{
		private ObjectArrayList<GUIElement> list;
		private boolean dirty;
		private EM lastMan;
		private List<CM> collectionManagers;
		

		@Override
		public DropdownCallback initCallback() {
			
			
			
			return value -> {
				if(value instanceof ControlBlockElementCollectionManager<?, ?, ?>) {
					AdvancedEntityWeaponGUIGroup.this.selectedCollectionManager = ((CM)(value));
					dirtyModuleDropdownDirty = true;
				}
//					AdvancedEntityReactor.this.selectedReactor = ((ReactorTree)((GUIElement)value).getUserPointer());
			};
		}
		
		
		@Override
		public void update(Timer timer) {
			
			if(getMan() != null && getEm() != lastMan) {
				dirty = true;
				if(lastMan != null) {
					lastMan.deleteObserver(this);
				}
				lastMan = getEm();
				lastMan.addObserver(this);
			}
			
			super.update(timer);
		}

		@Override
		public String getToolTipText() {
			return "Available Computers";
		}
		
		@Override
		public String getName() {
			return "Select";
		}
		
		@Override
		public Collection<? extends GUIElement> getDropdownElements(final GUIElement dep) {
			if(getMan() == null) {
				return new ObjectArrayList<GUIElement>();
			}
			list = new ObjectArrayList<GUIElement>();
			
			
			collectionManagers = getCollectionManagers();
			ReactorSet reactorSet = getMan().getPowerInterface().getReactorSet();
			
			boolean refreshDef = true;
			for(CM t : collectionManagers) {
				
				String nm = Lng.str("%s (size: %d)", getSystemNameShort(), t.getTotalSize());
				if(isSlave(t)) {
					nm += " " + Lng.str("(slaved)");
				}
				GUIAnchor guiAnchor = new GUIAnchor(getState(), 200, 20);
				guiAnchor.setUserPointer(t);
				list.add(guiAnchor);

				
				final GUITextOverlay td = new GUITextOverlay(FontSize.MEDIUM_15, getState()){

					@Override
					public void draw() {
						if(dep != null){
							limitTextWidth = (int) (dep.getWidth());
						}
						super.draw();
					}
					
				};
				td.setTextSimple(nm);
				
				td.getPos().x = 3;
				td.getPos().y = 2;

				guiAnchor.attach(td);
				
				
				if(t == selectedCollectionManager) {
					refreshDef = false;
				}
			}
			
			if(refreshDef) {
				if(getDefault() != null) {
					selectedCollectionManager = (CM)((GUIAnchor) getDefault()).getUserPointer();
				}else {
					selectedCollectionManager = null;
				}
			}
			if(selectedCollectionManager != null) {
				change(selectedCollectionManager);
			}
			dirtyModuleDropdownDirty = true;
			dirty = false;
			
			return list;
		}
		
		@Override
		public Object getDefault() {
			if(selectedCollectionManager != null && collectionManagers != null && collectionManagers.contains(selectedCollectionManager)){
				for(int i = 0; i < list.size(); i++) {
					if(list.get(i).getUserPointer() == selectedCollectionManager) {
						return list.get(i);
					}
				}
			}
			if(list == null || collectionManagers == null || collectionManagers.isEmpty()) {
				return null;
			}
			return list.size() > 0 ? list.get(0) : null;
		}
		
		@Override
		public boolean needsListUpdate() {
			return dirty;
		}

		@Override
		public void flagListNeedsUpdate(boolean b) {
			dirty = b;				
		}


		@Override
		public void update(DrawerObservable observer, Object userdata, Object message) {
			if(userdata == SegNotifyType.SHIP_ELEMENT_CHANGED) {
				dirty = true;
			}
		}


	}
	public class ModuleSelectDropdown extends DropdownResult{
		private ObjectArrayList<GUIElement> list;
		
		private EM lastMan;
		private List<E> elements;
		
		
		@Override
		public DropdownCallback initCallback() {
			
			
			
			return value -> {
				if(value instanceof FiringUnit<?, ?, ?>) {
					AdvancedEntityWeaponGUIGroup.this.selectedElement = ((E)(value));
				}
//					AdvancedEntityReactor.this.selectedReactor = ((ReactorTree)((GUIElement)value).getUserPointer());
			};
		}
		
		
		
		@Override
		public String getToolTipText() {
			return "Available Computers";
		}
		
		@Override
		public String getName() {
			return "Select";
		}
		
		@Override
		public Collection<? extends GUIElement> getDropdownElements(final GUIElement dep) {
			if(getMan() == null || selectedCollectionManager == null) {
				return new ObjectArrayList<GUIElement>();
			}
			list = new ObjectArrayList<GUIElement>();
			
			
			elements = selectedCollectionManager.getElementCollections();
			ReactorSet reactorSet = getMan().getPowerInterface().getReactorSet();
			
			for(E t : elements) {
				
				
				String nm = Lng.str("%s (size: %d)", getOutputNameShort(), t.size());
				GUIAnchor guiAnchor = new GUIAnchor(getState(), 200, 20);
				guiAnchor.setUserPointer(t);
				list.add(guiAnchor);
				
				
				final GUITextOverlay td = new GUITextOverlay(FontSize.MEDIUM_15, getState()){
					
					@Override
					public void draw() {
						if(dep != null){
							limitTextWidth = (int) (dep.getWidth());
						}
						super.draw();
					}
					
				};
				td.setTextSimple(nm);
				
				td.getPos().x = 3;
				td.getPos().y = 2;
				
				guiAnchor.attach(td);
				
				
				
			}
			boolean foundSel = false;
			if(selectedElement != null && selectedElement.elementCollectionManager == selectedCollectionManager) {
				for(E t : elements) {
					if(t.idPos == selectedElement.idPos) {
						selectedElement = t;
						foundSel = true;
						break;
					}
				}
			}
			if(!foundSel) {
				selectedElement = null;
			}
			if(selectedElement == null) {
				if(getDefault() != null) {
					selectedElement = (E)((GUIAnchor) getDefault()).getUserPointer();
				}else {
					selectedElement = null;
				}
			}
			dirtyModuleDropdownDirty = false;
			return list;
		}
		
		



		@Override
		public Object getDefault() {
			if(list == null || selectedCollectionManager == null || list.isEmpty()) {
				return null;
			}
			return list.size() > 0 ? list.get(0) : null;
		}
		
		@Override
		public boolean needsListUpdate() {
			return dirtyModuleDropdownDirty;
		}
		
		@Override
		public void flagListNeedsUpdate(boolean b) {
			dirtyModuleDropdownDirty = b;				
		}
		
		
		
		
	}
	
	@Override
	public void onChange(ElementCollectionManager<?, ?, ?> col) {
		if(col == selectedCollectionManager) {
			dirtyModuleDropdownDirty = true;
		}
	}

	public abstract String getSystemNameShort();
	public abstract String getOutputNameShort();
}
