package org.schema.game.common.controller.elements.power.reactor.tree;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schema.common.JsonSerializable;
import org.schema.common.SerializationInterface;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitUnit;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.elements.power.reactor.tree.graph.ReactorGraphContainerElementInformation;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.blockeffects.config.EffectConfigElement;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;
import org.schema.schine.resource.tag.TagSerializableLongSet;
import org.schema.schine.sound.controller.AudioController;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ReactorElement implements SerializationInterface, TagSerializable, JsonSerializable {

	public static final short NOT_GENERAL_CHAMBER = -1;

	public static final short INVALID_TYPE = -2;

	public static final short CHAMBER_TREE_INVALID = -3;

	public static final short CHAMBER_TOO_SMALL = -4;

	public static final short INVALID_CONDUIT = -5;

	public static final short CHAMBER_MUTUALLY_EXCLUSIVE = -6;

	private static final float BOOT_TIME = 15;

	public short type;

	public TagSerializableLongSet chamber;

	public List<ReactorElement> children = new ObjectArrayList<ReactorElement>();

	public ReactorTree root;

	public ReactorElement parent;

	private int size;

	private long chamIdPos;

	private int actualSize;

	private float bootStatus = 0;

	public boolean validConduit;

	private static byte TAG_VERSION = 0;
	
	public ReactorElement() {
		
	}
	
	public ReactorElement(JSONObject json) {
		fromJson(json);
	}

	void create(ConduitCollectionManager conduits, ReactorTree reactorTree, ReactorElement parent, ReactorChamberUnit cham, Set<ReactorChamberUnit> used) {
		assert (cham != null);
		assert (cham.getNeighboringCollection() != null);
		this.parent = parent;
		this.root = reactorTree;
		this.type = cham.getClazzId();
		this.size = cham.size();
		this.actualSize = cham.size();
		chamber = new TagSerializableLongSet(cham.getNeighboringCollection());
		this.chamIdPos = cham.idPos;
		for(ConduitUnit c : conduits.getElementCollections()) {
			if(c.getConnectedReactors().size() == 0 && c.getConnectedChambers().contains(cham)) {
				for(ReactorChamberUnit ru : c.getConnectedChambers()) {
					if(!used.contains(ru)) {
						used.add(ru);
						ReactorElement child = new ReactorElement();
						child.validConduit = c.isValidConduit();
						child.create(conduits, reactorTree, this, ru, used);
						children.add(child);
					}
				}
			}
		}
	}

	public long calculateLocalHp(int size) {
		return size * ElementKeyMap.getInfo(type).reactorHp;
	}

	public void print(int lvl) {
		StringBuffer b = new StringBuffer();
		for(int i = 0; i < lvl; i++) {
			b.append("  ");
		}
		b.append("- CHAMBER: " + ElementKeyMap.toString(type) + "; SetSize: " + (chamber != null ? String.valueOf(chamber.size()) : "n/a on client") + "; ActualSize/Size: " + actualSize + "/" + size);
		System.err.println(b);
		for(ReactorElement c : children) {
			c.print(lvl + 1);
		}
	}

	@Override
	public String toString() {
		return "ReactorElement[" + (isGeneral() ? "*" : "") + getInfo().getName() + "]";
	}

	public ShortList getPossibleSpecifications() {
		ShortArrayList l = new ShortArrayList();
		if(!isValidType()) {
			l.add(INVALID_TYPE);
		} else {
			if(!isValidTreeNodeBySize()) {
				l.add(CHAMBER_TOO_SMALL);
			} else if(isMutuallyExclusiveToOthers()) {
				l.add(CHAMBER_MUTUALLY_EXCLUSIVE);
			} else if(!validConduit) {
				l.add(INVALID_CONDUIT);
			} else {
				ElementInformation info = getInfo();
				if(info.isReactorChamberGeneral()) {
					if(parent == null) {
						// all level-ones possible here
						ShortSet rl = info.chamberChildren;
						l.addAll(rl);
					} else {
						if(checkParentSpecification(false)) {
							ShortSet rl = new ShortOpenHashSet();
							// parent specific types possible
							parent.getInfo().getChamberChildrenOnLevel(rl);
							for(short s : rl) {
								if(!root.existsMutuallyExclusiveFor(s) && s != parent.getInfo().chamberUpgradesTo) {
									l.add(s);
								}
							}
						} else {
							l.add(CHAMBER_TREE_INVALID);
						}
					}
				} else {
					l.add(NOT_GENERAL_CHAMBER);
				}
			}
		}
		return l;
	}

	private boolean checkParentSpecification(boolean ownSpecification) {
		if(!isValidType()) {
			return false;
		}
		if(!validConduit) {
			return false;
		}
		if(parent == null) {
			return true;
		}
		final ElementInformation parentInfo = ElementKeyMap.getInfo(parent.type);
		if(parentInfo.isReactorChamberSpecific()) {
			if(ownSpecification) {
				if(root.containsTypeExcept(chamIdPos, type)) {
					return false;
				}
				if(parentInfo.chamberUpgradesTo == type) {
					// upgradable
					return parent.checkParentSpecification(true);
				}
				short upgradedRoot = getInfo().getChamberUpgradedRoot();
				if(!parentInfo.isChamberChildrenUpgradableContains(upgradedRoot)) {
					// parent doesnt belong to us
					return false;
				}
			}
			return parent.checkParentSpecification(true);
		}
		return false;
	}

	public void convertToClientRequest(short s) {
		root.pw.convertRequest(chamIdPos, s);
	}

	public boolean isGeneral() {
		return isValidType() && getInfo().isReactorChamberGeneral();
	}

	public boolean isValidType() {
		return ElementKeyMap.isValidType(type);
	}

	public int getMinBlocksNeeded() {
		return root.pw.getNeededMinForReactorLevel(root.getSizeInital());
	}

	public int getMaxBlocksNeeded() {
		return root.pw.getNeededMaxForReactorLevel(root.getSizeInital());
	}

	public boolean isValidTreeNodeBySize() {
		return root.pw.isChamberValid(root.getSizeInital(), size);
	}

	public boolean isValidTreeNode() {
		return root.isWithinCapacity() && isValidType() && validConduit && isPermittedOnThisEntity() && isValidTreeNodeBySize() && !isMutuallyExclusiveToOthers() && getInfo().isReactorChamberSpecific() && checkParentSpecification(true);
	}

	private boolean isPermittedOnThisEntity() {
		return getInfo().isChamberPermitted(root.pw.getSegmentController().getType());
	}

	public boolean isParentValidTreeNode() {
		return checkParentSpecification(false);
	}

	public int getSize() {
		return size;
	}

	public boolean isUnitPartOfTree(ReactorChamberUnit e) {
		if(chamIdPos == e.idPos) {
			return true;
		}
		for(ReactorElement c : children) {
			if(c.isUnitPartOfTree(e)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(chamIdPos);
		b.writeShort(type);
		b.writeInt(size);
		b.writeInt(actualSize);
		b.writeFloat(bootStatus);
		b.writeShort(children.size());
		for(int i = 0; i < children.size(); i++) {
			children.get(i).serialize(b, isOnServer);
		}
		b.writeBoolean(validConduit);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		chamIdPos = b.readLong();
		type = b.readShort();
		size = b.readInt();
		actualSize = b.readInt();
		bootStatus = b.readFloat();
		int cSize = b.readShort();
		for(int i = 0; i < cSize; i++) {
			ReactorElement e = new ReactorElement();
			e.parent = this;
			e.root = this.root;
			e.deserialize(b, updateSenderStateId, isOnServer);
			children.add(e);
		}
		validConduit = b.readBoolean();
	}

	@Override
	public void fromTagStructure(Tag iTag) {
		Tag[] t = iTag.getStruct();
		byte version = t[0].getByte();
		chamIdPos = t[1].getLong();
		type = t[2].getShort();
		size = t[3].getInt();
		actualSize = t[4].getInt();
		chamber = (TagSerializableLongSet) t[5].getValue();
		Tag[] cTags = t[6].getStruct();
		for(int i = 0; i < cTags.length - 1; i++) {
			ReactorElement e = new ReactorElement();
			e.parent = this;
			e.root = this.root;
			e.fromTagStructure(cTags[i]);
			children.add(e);
		}
		if(t.length > 7 && t[7].getType() == Type.FLOAT) {
			// remove check on release of reactors
			bootStatus = t[7].getFloat();
		}
		if(t.length > 8 && t[8].getType() == Type.FLOAT) {
			// remove check on release of reactors
			validConduit = t[8].getBoolean();
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag vTag = new Tag(Type.BYTE, null, TAG_VERSION);
		Tag idTag = new Tag(Type.LONG, null, chamIdPos);
		Tag typeTag = new Tag(Type.SHORT, null, type);
		Tag sizeTag = new Tag(Type.INT, null, size);
		Tag actSizeTag = new Tag(Type.INT, null, actualSize);
		Tag chamSet = new Tag(Type.SERIALIZABLE, null, chamber);
		Tag[] childTags = new Tag[children.size() + 1];
		childTags[childTags.length - 1] = FinishTag.INST;
		for(int i = 0; i < childTags.length - 1; i++) {
			childTags[i] = children.get(i).toTagStructure();
		}
		Tag btStat = new Tag(Type.FLOAT, null, bootStatus);
		Tag validConduitTag = new Tag(Type.BYTE, null, validConduit ? (byte) 1 : (byte) 0);
		return new Tag(Type.STRUCT, null, new Tag[]{vTag, idTag, typeTag, sizeTag, actSizeTag, chamSet, new Tag(Type.STRUCT, null, childTags), btStat, validConduitTag, FinishTag.INST});
	}

	public ReactorElement onBlockKilledServer(Damager from, short type, long index, Long2IntMap changedModules) {
		if(this.type == type && chamber.contains(index)) {
			onChamberHit(index);
			changedModules.put(chamIdPos, actualSize);
			// if(root.isActiveTree() && root.pw.isInstable()){
			// checkExplosion(from, type, index);
			// }
			return this;
		}
		for(int i = 0; i < children.size(); i++) {
			ReactorElement hit = children.get(i).onBlockKilledServer(from, type, index, changedModules);
			if(hit != null) {
				return hit;
			}
		}
		return null;
	}

	// private void checkExplosion(Damager from, short type, long pos) {
	// if(root.pw.getSegmentController().getUpdateTime() - lastHit > 1000){
	// lastHit = root.pw.getSegmentController().getUpdateTime();
	// 
	// if(!root.existsExplosion(from, type, pos)){
	// for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberManager, VoidElementManager<ReactorChamberUnit, ReactorChamberManager>> r : root.pw.getChambers()){
	// if(r.getElementID() == type){
	// List<ReactorChamberUnit> col = r.getCollectionManager().getElementCollections();
	// for(int i = 0; i < col.size(); i ++){
	// ReactorChamberUnit u = col.get(i);
	// if(u.getNeighboringCollection().contains(pos)){
	// root.explodeReactor(from, type, pos, u);
	// break;
	// }
	// }
	// break;
	// }
	// }
	// 
	// }
	// }
	// }
	private void onChamberHit(long index) {
		chamber.remove(index);
		actualSize = chamber.size();
	}

	public boolean isDamaged() {
		return actualSize < size;
	}

	public boolean isDamagedRec() {
		if(isDamaged()) {
			return true;
		}
		for(int i = 0; i < children.size(); i++) {
			boolean d = children.get(i).isDamagedRec();
			if(d) {
				return true;
			}
		}
		return false;
	}

	public boolean applyReceivedSizeChange(long moduleId, final int newSize, ReactorTree reactorTree) {
		if(chamIdPos == moduleId) {
			final int oldSize = this.actualSize;
			this.actualSize = newSize;
			reactorTree.onChamberReceivedSizeUpdate(this, oldSize, newSize);
			return true;
		}
		for(int i = 0; i < children.size(); i++) {
			boolean d = children.get(i).applyReceivedSizeChange(moduleId, newSize, reactorTree);
			if(d) {
				return true;
			}
		}
		return false;
	}

	public void fillEffectGroups(ConfigPool pool, ShortSet appliedEffectGroups, int appliesToFilter) {
		if(isValidTreeNode() && !isGeneral() && isBooted()) {
			ElementInformation info = getInfo();
			addConfigGroups(pool, appliedEffectGroups, info, appliesToFilter);
			for(int i = 0; i < children.size(); i++) {
				children.get(i).fillEffectGroups(pool, appliedEffectGroups, appliesToFilter);
			}
		}
	}

	private void addConfigGroups(ConfigPool pool, ShortSet appliedEffectGroups, ElementInformation info, int appliesToFilter) {
		if(info.isChamberUpgraded()) {
			ElementInformation parent = ElementKeyMap.getInfo(info.chamberParent);
			addConfigGroups(pool, appliedEffectGroups, parent, appliesToFilter);
		}
		if(info.chamberAppliesTo == appliesToFilter) {
			for(String cf : info.chamberConfigGroupsLowerCase) {
				ConfigGroup configGroup = pool.poolMapLowerCase.get(cf);
				if(configGroup != null) {
					appliedEffectGroups.add(configGroup.ntId);
				} else {
					System.err.println("POOL: " + pool.poolMapLowerCase.keySet());
					throw new RuntimeException("[REACTORTREE] WARNING: Effect Group \"" + cf + "\" doesn't exist in config pool (referenced by " + ElementKeyMap.toString(type) + ") ");
				}
			}
		}
	}

	public float getCapacityRecursivelyUpwards() {
		return getChamberCapacity() + (parent != null ? parent.getCapacityRecursivelyUpwards() : 0f);
	}

	public float getCapacityRecursively() {
		float chamberCapacity = getChamberCapacity();
		for(ReactorElement c : children) {
			if(!c.isGeneral()) {
				chamberCapacity += c.getCapacityRecursively();
			}
		}
		return chamberCapacity;
	}

	public ElementInformation getInfo() {
		return ElementKeyMap.getInfo(type);
	}

	public float getChamberCapacity() {
		return (isValidType() && !isGeneral()) ? getInfo().getChamberCapacityWithUpgrades() : 0;
	}

	public long calculateMaxHpRecursively() {
		if(!isValidTreeNode()) {
			return 0;
		}
		long maxHp = calculateLocalHp(this.size);
		for(ReactorElement c : children) {
			maxHp += c.calculateMaxHpRecursively();
		}
		return maxHp;
	}

	public long calculateHpRecursively() {
		if(!isValidTreeNode()) {
			return 0;
		}
		long hp = calculateLocalHp(this.actualSize);
		for(ReactorElement c : children) {
			hp += c.calculateHpRecursively();
		}
		return hp;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean containsElement(short specifiedChamberBlockId) {
		if(isValidTreeNode() && type == specifiedChamberBlockId) {
			return true;
		}
		for(ReactorElement c : children) {
			if(c.containsElement(specifiedChamberBlockId)) {
				return true;
			}
		}
		return false;
	}

	public float getSizePercent() {
		return (float) actualSize / (float) size;
	}

	public int getActualSize() {
		return actualSize;
	}

	public long getId() {
		return chamIdPos;
	}

	public ReactorElement getChamber(long reactorIdPos) {
		if(reactorIdPos == this.chamIdPos) {
			return this;
		}
		for(ReactorElement c : children) {
			ReactorElement e = c.getChamber(reactorIdPos);
			if(e != null) {
				return e;
			}
		}
		return null;
	}

	public void popupSpecifyTileDialog(final InputState state) {
		final PlayerOkCancelInput ip = new PlayerOkCancelInput("REACTOR_SPECIFY_D", state, UIScale.getUIScale().scale(900), UIScale.getUIScale().scale(600), Lng.str("Specify"), "", FontStyle.big) {

			@Override
			public void pressedOK() {
				if(ElementKeyMap.isValidType(ReactorGraphContainerElementInformation.selected)) {
					convertToClientRequest(ReactorGraphContainerElementInformation.selected);
				}
				deactivate();
			}

			@Override
			public void onDeactivate() {
			}
		};
		ip.getInputPanel().onInit();
		ip.getInputPanel().getButtonOK().activationInterface = () -> ElementKeyMap.isValidType(ReactorGraphContainerElementInformation.selected);
		final GUIDialogWindow w = (GUIDialogWindow) ip.getInputPanel().getBackground();
		w.getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().P_SMALL_PANE_HEIGHT);
		w.getMainContentPane().addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIGraph treeGraph = root.getTreeGraph(getInfo(), ReactorElement.this, ip, w.getMainContentPane().getContent(1));
		treeGraph.onInit();
		GUIScrollablePanel pTree = new GUIScrollablePanel(10, 10, w.getMainContentPane().getContent(1), state);
		pTree.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
		pTree.setContent(treeGraph);
		w.getMainContentPane().getContent(1).attach(pTree);
		ShortList possible = getPossibleSpecifications();
		final GUITextOverlay l = new GUITextOverlay(FontSize.MEDIUM_15, state);
		final int MAX_DESCRIPTION_HEIGHT = UIScale.getUIScale().scale(240);
		for(final short s : possible) {
			if(s == ReactorElement.CHAMBER_TREE_INVALID) {
				l.setTextSimple(Lng.str("Chamber chain invalid"));
				l.setColor(1, 0.3f, 0.3f, 1.0f);
			} else if(s == ReactorElement.INVALID_TYPE) {
				l.setTextSimple(Lng.str("Invalid Chamber Type"));
				l.setColor(1, 0.3f, 0.3f, 1.0f);
			} else if(s == ReactorElement.CHAMBER_TOO_SMALL) {
				l.setTextSimple(Lng.str("Chamber too small! Needs at least %s blocks", getMinBlocksNeeded()));
				l.setColor(1, 0.3f, 0.3f, 1.0f);
			} else if(s == ReactorElement.INVALID_CONDUIT) {
				l.setTextSimple(Lng.str("Invalid conduit! Conduit connects more than two chambers/reacor! Must be chamber<->chamber or main<->chamber!"));
				l.setColor(1, 0.3f, 0.3f, 1.0f);
			} else if(s == ReactorElement.CHAMBER_MUTUALLY_EXCLUSIVE) {
				l.setTextSimple(Lng.str("Chamber is mutually exclusive with another chamber!"));
				l.setColor(1, 0.3f, 0.3f, 1.0f);
			} else if(s == ReactorElement.NOT_GENERAL_CHAMBER) {
				l.setTextSimple(Lng.str("Must be general chamber"));
				l.setColor(1, 0.3f, 0.3f, 1.0f);
			} else {
				l.setTextSimple(new Object() {

					@Override
					public String toString() {
						if(ReactorGraphContainerElementInformation.selected == 0) {
							return Lng.str("No options available to specify this chamber!");
						} else {
							ElementInformation info = ElementKeyMap.getInfo(ReactorGraphContainerElementInformation.selected);
							String description = info.getDescriptionIncludingChamberUpgraded();
							if(description.equals(Lng.str("undefined description"))) {
								description = "";
							}
							return info.getName() + " \n" + (description.length() > 0 ? (description + "\n") : "") + info.getChamberEffectInfo(((GameClientState) state).getConfigPool());
						}
					}
				});
			}
			l.onInit();
			final GUIAnchor c = new GUIAnchor(state) {

				private int sel = -1;

				@Override
				public void draw() {
					super.draw();
					if(ReactorGraphContainerElementInformation.selected != sel) {
						if(sel == 0) {
							w.getMainContentPane().setTextBoxHeight(0, 26);
							setHeight(20);
						} else {
							l.updateTextSize();
							w.getMainContentPane().setTextBoxHeight(0, Math.min(MAX_DESCRIPTION_HEIGHT + 12, l.getTextHeight() + 12));
							setHeight(l.getTextHeight() + 4);
							setWidth(l.getMaxLineWidth() + 4);
						}
						sel = ReactorGraphContainerElementInformation.selected;
					}
				}
			};
			l.getPos().x = 4;
			l.getPos().y = 4;
			c.attach(l);
			GUIScrollablePanel p = new GUIScrollablePanel(10, 10, w.getMainContentPane().getContent(0), state);
			p.setContent(c);
			p.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
			p.onInit();
			w.getMainContentPane().getContent(0).attach(p);
			// w.getMainContentPane().setListDetailMode(w.getMainContentPane().getTextboxes().get(1));
			ip.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(901);
		}
		// final PlayerButtonTilesInput tileInput = new PlayerButtonTilesInput("REACTOR_TILE_SPECIFY", state, 800, 600, Lng.str("Specify"), 200, 80) {
		// 
		// @Override
		// public void onDeactivate() {
		// 
		// }
		// 
		// @Override
		// protected void init(String windowid, int initialWidth, int initialHeight, InputState state, Object info,
		// int tilesWidth, int tilesHeight) {
		// super.init(windowid, initialWidth, initialHeight, state, info, tilesWidth, tilesHeight);
		// ((GUIDialogWindow)getInputPanel().getBackground()).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(100));
		// ((GUIDialogWindow)getInputPanel().getBackground()).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(350));
		// ((GUIDialogWindow)getInputPanel().getBackground()).getMainContentPane().setDividerDetail(0);
		// GUIGraph treeGraph = root.getTreeGraph(
		// state,
		// getInfo(),
		// root,
		// ReactorElement.this,
		// getInputPanel());
		// treeGraph.onInit();
		// 
		// GUIScrollablePanel p = new GUIScrollablePanel(10, 10, ((GUIDialogWindow)getInputPanel().getBackground()).getMainContentPane().getContent(1), state);
		// p.setContent(treeGraph);
		// p.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
		// p.onInit();
		// 
		// ((GUIDialogWindow)getInputPanel().getBackground()).getMainContentPane().getContent(1).attach(p);
		// 
		// }
		// 
		// 
		// };
		// ShortList possible = getPossibleSpecifications();
		// 
		// for(final short s : possible){
		// if(s == ReactorElement.CHAMBER_TREE_INVALID){
		// tileInput.addTile(Lng.str("ERROR"), Lng.str("Chamber chain invalid"), HButtonColor.RED, new GUICallback() {
		// 
		// @Override
		// public boolean isOccluded() {
		// return !tileInput.isActive();
		// }
		// 
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// tileInput.deactivate();
		// }
		// }
		// }, new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		// @Override
		// public boolean isActive(InputState state) {
		// return tileInput.isActive();
		// }
		// });
		// }else if(s == ReactorElement.INVALID_TYPE){
		// tileInput.addTile(Lng.str("ERROR"), Lng.str("Invalid Chamber Type"), HButtonColor.RED, new GUICallback() {
		// 
		// @Override
		// public boolean isOccluded() {
		// return !tileInput.isActive();
		// }
		// 
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// tileInput.deactivate();
		// }
		// }
		// }, new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		// @Override
		// public boolean isActive(InputState state) {
		// return tileInput.isActive();
		// }
		// });
		// }else if(s == ReactorElement.CHAMBER_TOO_SMALL){
		// tileInput.addTile(Lng.str("ERROR"), Lng.str("Chamber too small! Needs at least %s blocks", getMinBlocksNeeded()), HButtonColor.RED, new GUICallback() {
		// 
		// @Override
		// public boolean isOccluded() {
		// return !tileInput.isActive();
		// }
		// 
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// tileInput.deactivate();
		// }
		// }
		// }, new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		// @Override
		// public boolean isActive(InputState state) {
		// return tileInput.isActive();
		// }
		// });
		// }else if(s == ReactorElement.INVALID_CONDUIT){
		// tileInput.addTile(Lng.str("ERROR"), Lng.str("Invalid conduit! Conduit connects more than two chambers/reacor! Must be chamber<->chamber or main<->chamber!", getMinBlocksNeeded()), HButtonColor.RED, new GUICallback() {
		// 
		// @Override
		// public boolean isOccluded() {
		// return !tileInput.isActive();
		// }
		// 
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// tileInput.deactivate();
		// }
		// }
		// }, new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		// @Override
		// public boolean isActive(InputState state) {
		// return tileInput.isActive();
		// }
		// });
		// }else if(s == ReactorElement.NOT_GENERAL_CHAMBER){
		// tileInput.addTile(Lng.str("ERROR"), Lng.str("Must be general chamber"), HButtonColor.RED, new GUICallback() {
		// 
		// @Override
		// public boolean isOccluded() {
		// return !tileInput.isActive();
		// }
		// 
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// tileInput.deactivate();
		// }
		// }
		// }, new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		// @Override
		// public boolean isActive(InputState state) {
		// return tileInput.isActive();
		// }
		// });
		// }else{
		// final boolean exists = root.containsType(s);
		// 
		// final ElementInformation info = ElementKeyMap.getInfo(s);
		// tileInput.addTile(info.getName(),
		// (!info.isChamberPermitted(root.pw.getSegmentController().getType()) ? Lng.str("Not permitted on %s!\n", root.pw.getSegmentController().getType().getName()) : "")+
		// (exists ? Lng.str("Already Exists!\n") : "")+info.getChamberEffectInfo(((GameClientState)state).getConfigPool()),
		// HButtonColor.BLUE, new GUICallback() {
		// 
		// @Override
		// public boolean isOccluded() {
		// return !tileInput.isActive();
		// }
		// 
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// convertToClientRequest(s);
		// tileInput.deactivate();
		// }
		// 
		// }
		// }, new GUIActivationCallback() {
		// @Override
		// public boolean isVisible(InputState state) {
		// return true;
		// }
		// @Override
		// public boolean isActive(InputState state) {
		// return tileInput.isActive() && !exists && info.isChamberPermitted(root.pw.getSegmentController().getType());
		// }
		// });
		// }
		// }
		// tileInput.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
	}

	public boolean containsTypeExcept(long chamIdPos, short type) {
		if(!isGeneral() && this.chamIdPos != chamIdPos && this.type == type) {
			return true;
		}
		for(ReactorElement c : children) {
			boolean e = c.containsTypeExcept(chamIdPos, type);
			if(e) {
				return true;
			}
		}
		return false;
	}

	public boolean containsType(short type) {
		if(!isGeneral() && this.type == type) {
			return true;
		}
		for(ReactorElement c : children) {
			boolean e = c.containsType(type);
			if(e) {
				return true;
			}
		}
		return false;
	}

	public void removeExitingTypes(ShortArrayList l) {
		if(!isGeneral()) {
			for(int i = 0; i < l.size(); i++) {
				if(l.getShort(i) == type) {
					l.remove(i);
					i--;
				}
			}
		}
		for(ReactorElement c : children) {
			c.removeExitingTypes(l);
		}
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("version", TAG_VERSION);
		json.put("id", chamIdPos);
		json.put("type", type);
		json.put("size", size);
		json.put("actualSize", actualSize);
		json.put("bootStatus", bootStatus);
		json.put("validConduit", validConduit);
		JSONArray childrenArray = new JSONArray();
		for(ReactorElement c : children) childrenArray.put(c.toJson());
		json.put("children", childrenArray);
		json.put("chamber", chamber.toJson());
		return json;
	}

	@Override
	public void fromJson(JSONObject json) {
		byte version = (byte) json.getInt("version");
		chamIdPos = json.getLong("id");
		type = (short) json.getInt("type");
		size = json.getInt("size");
		actualSize = json.getInt("actualSize");
		bootStatus = (float) json.getDouble("bootStatus");
		validConduit = json.getBoolean("validConduit");
		JSONArray childrenArray = json.getJSONArray("children");
		for(int i = 0; i < childrenArray.length(); i++) {
			ReactorElement c = new ReactorElement();
			c.fromJson(childrenArray.getJSONObject(i));
			c.parent = this;
			c.root = root;
			children.add(c);
		}
		chamber = new TagSerializableLongSet();
		chamber.fromJson(json.getJSONObject("chamber"));
	}

	public enum BootStatusReturn {

		ALL_BOOTED,
		CHANGED,
		UNCHANGED
	}

	public BootStatusReturn updateBooted(Timer t) {
		BootStatusReturn status = BootStatusReturn.ALL_BOOTED;
		if(bootStatus > 0) {
			bootStatus = Math.max(0, bootStatus - t.getDelta());
			return isBooted() ? BootStatusReturn.CHANGED : BootStatusReturn.UNCHANGED;
		} else {
			for(ReactorElement c : children) {
				status = c.updateBooted(t);
				if(status != BootStatusReturn.ALL_BOOTED) {
					// only fill up one per update
					break;
				}
			}
		}
		return status;
	}

	public void resetBooted() {
		if(isGeneral()) {
			bootStatus = 0;
		} else {
			bootStatus = BOOT_TIME;
		}
	}

	public float getAccumulatedBootUp() {
		float rc = bootStatus;
		for(ReactorElement c : children) {
			rc += c.getAccumulatedBootUp();
		}
		return rc;
	}

	public void distributeBootUp(float bootupPerSpecific) {
		if(!isGeneral()) {
			bootStatus = bootupPerSpecific;
		}
		for(ReactorElement c : children) {
			c.distributeBootUp(bootupPerSpecific);
		}
	}

	public int getSpecificCountRec() {
		int rc = isGeneral() ? 0 : 1;
		for(ReactorElement c : children) {
			rc += c.getSpecificCountRec();
		}
		return rc;
	}

	public boolean isBooted() {
		return bootStatus <= 0;
	}

	public float getBootStatus() {
		return bootStatus;
	}

	public float getBootStatusPercent() {
		return bootStatus / BOOT_TIME;
	}

	public void setBootStatus(float bootStatus) {
		this.bootStatus = bootStatus;
	}

	public void resetBootedRecursive() {
		if(isValidTreeNode()) {
			resetBooted();
			for(ReactorElement c : children) {
				c.resetBootedRecursive();
			}
		}
	}

	public void setBootedRecursive() {
		setBooted();
		for(ReactorElement c : children) {
			c.setBootedRecursive();
		}
	}

	public void setBooted() {
		bootStatus = 0;
	}

	public boolean isOrIsChildOfGeneral(short id) {
		return getTypeGeneral().id == id;
	}

	public ElementInformation getTypeGeneral() {
		if(isGeneral()) {
			return getInfo();
		} else {
			return ElementKeyMap.getInfo(getInfo().chamberRoot);
		}
	}

	private boolean isGeneralChain(short oType) {
		return type == oType && (parent == null || parent.isValidTreeNode() || parent.isGeneralChain(type));
	}

	public boolean isGeneralChain() {
		return isGeneral() && (parent == null || parent.isValidTreeNode() || parent.isGeneralChain(type));
	}

	public void getAllReactorElementsWithConfig(ConfigPool pool, StatusEffectType t, Collection<ConfigGroup> out) {
		if(isValidTreeNode() && !isGeneral() && isBooted()) {
			ElementInformation info = getInfo();
			boolean f = true;
			while(f || info.isChamberUpgraded()) {
				if(!info.isChamberUpgraded()) {
					f = false;
				}
				for(String cf : info.chamberConfigGroupsLowerCase) {
					ConfigGroup configGroup = pool.poolMapLowerCase.get(cf);
					if(configGroup != null) {
						for(EffectConfigElement e : configGroup.elements) {
							if(e.getType() == t) {
								out.add(configGroup);
							}
						}
					}
				}
				if(info.isChamberUpgraded()) {
					info = ElementKeyMap.getInfo(info.chamberParent);
				}
			}
			;
		}
		for(ReactorElement c : children) {
			c.getAllReactorElementsWithConfig(pool, t, out);
		}
	}

	public boolean isAllValidOrUnspecified() {
		if(!(isValidTreeNode() || isGeneralChain())) {
			return false;
		}
		for(ReactorElement c : children) {
			if(!c.isAllValidOrUnspecified()) {
				return false;
			}
		}
		return true;
	}

	public boolean isAllValid() {
		if(!isValidTreeNode()) {
			return false;
		}
		for(ReactorElement c : children) {
			if(!c.isAllValid()) {
				return false;
			}
		}
		return true;
	}

	public boolean isMutuallyExclusiveToOthers() {
		return root.existsMutuallyExclusiveFor(this.type);
	}

	public boolean isMutuallyExclusiveTo(short type) {
		return (isValidType() && getInfo().isThisOrParentChamberMutuallyExclusive(type)) || (ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).isThisOrParentChamberMutuallyExclusive(this.type));
	}

	public boolean isMutuallyExclusiveToRecusive(short type) {
		if(isMutuallyExclusiveTo(type)) {
			return true;
		}
		for(ReactorElement c : children) {
			if(c.isMutuallyExclusiveTo(type)) {
				return true;
			}
		}
		return false;
	}
}
