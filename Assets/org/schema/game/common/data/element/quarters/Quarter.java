package org.schema.game.common.data.element.quarters;

import api.common.GameClient;
import api.element.block.Blocks;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.quarters.crew.CrewMember;
import org.schema.game.common.data.element.quarters.crew.CrewPositionalInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.Vector4f;
import java.util.HashSet;

public abstract class Quarter implements TagSerializable, CrewPositionalInterface {

	private static final byte VERSION = 0;
	private final SegmentController segmentController;
	private final Long2ObjectOpenHashMap<Quarter> childConnections = new Long2ObjectOpenHashMap<>();
	private final ObjectOpenHashSet<CrewMember> crew = new ObjectOpenHashSet<>();
	public SegmentPiece firstGround;
	protected ConfigGroup configGroup;
	protected ConfigGroup damagedConfigGroup;
	private Area area = new Area();
	private QuarterStatus status = QuarterStatus.OK;
	private float integrity = 1.0f;
	private int priority;
	private long index;
	private boolean reinitializing;
	private boolean reinitializingDone;
	private boolean changed;

	protected Quarter(SegmentController s) {
		segmentController = s;
	}

	public static void addConnection(Quarter parent, Quarter child) {
		parent.childConnections.put(child.index, child);
	}

	public static Quarter loadFromTag(SegmentController c, Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		byte version = (byte) t[0].getValue();
		byte typeByte = (byte) t[1].getValue();
		Quarter instance = QuarterType.values()[typeByte].getInstance(c);
		instance.fromTagStructure(tag);
		return instance;
	}

	public abstract QuarterType getType();

	public abstract int getMaxDim();

	public abstract int getMinCrew();

	public abstract int getMaxCrew();

	public abstract void update(Timer timer);

	public abstract void forceUpdate();

	public abstract ConfigGroup createConfigGroup();

	public abstract ConfigGroup createDamagedConfigGroup();

	public void applyEffects() {
		if(!segmentController.getConfigManager().getPermanentEffects().contains(getConfigGroup()))
			segmentController.getConfigManager().addEffectAndSend(getConfigGroup(), true, segmentController.getNetworkObject());
	}

	public ConfigGroup getConfigGroup() {
		if(status == QuarterStatus.OK) {
			if(configGroup == null) configGroup = createConfigGroup();
			return configGroup;
		} else if(status == QuarterStatus.DAMAGED) {
			if(damagedConfigGroup == null) damagedConfigGroup = createDamagedConfigGroup();
			return damagedConfigGroup;
		} else return null;
	}

	public float getCombatEfficiency() {
		int efficiency = 0;
		for(int i = 0; i < crew.size(); i++) efficiency += crew.get(i).getCombatSkill();
		return efficiency / (float) crew.size();
	}

	public float getEngineeringEfficiency() {
		int efficiency = 0;
		for(int i = 0; i < crew.size(); i++) efficiency += crew.get(i).getEngineeringSkill();
		return efficiency / (float) crew.size();
	}

	public float getPhysicsEfficiency() {
		int efficiency = 0;
		for(int i = 0; i < crew.size(); i++) efficiency += crew.get(i).getPhysicsSkill();
		return efficiency / (float) crew.size();
	}

	public float getBiologyEfficiency() {
		int efficiency = 0;
		for(int i = 0; i < crew.size(); i++) efficiency += crew.get(i).getBiologySkill();
		return efficiency / (float) crew.size();
	}

	public final void openGUI(SegmentPiece segmentPiece, PlayerState playerState) {
		(new DialogInput(GameClient.getClientState()) {

			private GUIMainWindow inputPanel;

			@Override
			public void onDeactivate() {
				inputPanel.cleanUp();
			}

			@Override
			public GUIElement getInputPanel() {
				if(inputPanel == null) inputPanel = createGUI(segmentPiece, playerState, this);
				if(inputPanel == null) {
					//Todo: Remove this once all guis are implemented
					GUIMainWindow panel = new GUIMainWindow(GameClient.getClientState(), 750, 500, "DEFAULT") {

						@Override
						public void onInit() {
							super.onInit();
							GUIContentPane contentPane = addTab("DEFAULT");
							contentPane.setTextBoxHeightLast(300);
						}
					};
					panel.setCallback(this);
					panel.onInit();
					return panel;
				}
				return inputPanel;
			}
		}).activate();
	}

	public abstract GUIMainWindow createGUI(SegmentPiece segmentPiece, PlayerState playerState, DialogInput dialogInput);

	public ObjectOpenHashSet<CrewMember> getCrew() {
		return crew;
	}

	public QuarterStatus getStatus() {
		return status;
	}

	public void setStatus(QuarterStatus status) {
		this.status = status;
	}

	public float getIntegrity() {
		return integrity;
	}

	public void setIntegrity(float integrity) {
		this.integrity = integrity;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Long2ObjectOpenHashMap<Quarter> getChildConnections() {
		return childConnections;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		byte version = (byte) t[0].getValue();
		byte typeByte = (byte) t[1].getValue();
		area.min.set((Vector3i) t[2].getValue());
		area.max.set((Vector3i) t[3].getValue());
		if(t.length > 4) {
			status = QuarterStatus.valueOf((String) t[4].getValue());
			integrity = (Float) t[5].getValue();
			priority = (Integer) t[6].getValue();
			index = (Long) t[7].getValue();
			HashSet<Quarter> children = new HashSet<>();
			Tag.listFromTagStruct(children, t[8]);
			for(Quarter child : children) {
				if(child != null) childConnections.put(child.index, child);
			}
			/*int size = (Integer) t[9].getValue();
			Tag list = t[10];
			for(int i = 0; i < size; i++) {
				int id = (Integer) ((Tag[]) list.getValue())[i].getValue();
				childConnections.put(id, null);
			}
			for(Map.Entry<Integer, Quarter> e : childConnections.entrySet()) {
				if(e.getValue() == null) {
					Quarter q = segmentController.getQuarterManager().getQuartersById().get(e.getKey());
					if(q != null) e.setValue(q);
				}
			}*/
			fromTagExtra(t[11]);
		}
	}

	public abstract Tag toTagExtra();

	public abstract void fromTagExtra(Tag tag);

	@Override
	public final Tag toTagStructure() {
		Tag version = new Tag(Tag.Type.BYTE, "version", VERSION);
		Tag type = new Tag(Tag.Type.BYTE, "type", (byte) getType().ordinal());
		Tag min = new Tag(Tag.Type.VECTOR3i, "min", area.min);
		Tag max = new Tag(Tag.Type.VECTOR3i, "max", area.max);
		Tag statusTag = new Tag(Tag.Type.STRING, "status", status.name());
		Tag integrityTag = new Tag(Tag.Type.FLOAT, "integrity", integrity);
		Tag priorityTag = new Tag(Tag.Type.INT, "priority", priority);
		Tag indexTag = new Tag(Tag.Type.LONG, "index", index);
		Tag children = Tag.listToTagStruct(new HashSet<>(childConnections.values()), "children");
		Tag extra = toTagExtra();
		return new Tag(Tag.Type.STRUCT, null, new Tag[]{version, type, min, max, statusTag, integrityTag, priorityTag, indexTag, children, extra, FinishTag.INST});
	}

	@Override
	public SegmentController getSegmentController() {
		return segmentController;
	}

	public void reinitialize() {
		reinitializing = true;
//		getSegmentController().getState().getThreadPoolLogins().execute(new Runnable() {
//
//			@Override
//			public void run() {
//				calculateFirstGround();
//
//				reinitializingDone = true;
//				reinitializing = false;
//			}
//
//		});
		reinitializingDone = true;
		reinitializing = false;
	}

	public boolean isReinitialing() {
		return reinitializing;
	}

	public boolean reinitializingDone() {
		return reinitializingDone;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public void calculateFirstGround() {
		int midX = area.min.x + (area.max.x - area.min.x) / 2;
		int midZ = area.min.z + (area.max.z - area.min.z) / 2;
		for(int y = area.min.y; y <= area.max.y; y++) {
			//Todo: Factor in gravity direction somehow? Probably better to use some sort of physics bounding box idk
			SegmentPiece p = segmentController.getSegmentBuffer().getPointUnsave(midX, y, midZ);
			SegmentPiece p1 = segmentController.getSegmentBuffer().getPointUnsave(midX, y - 1, midZ);
			SegmentPiece p2 = segmentController.getSegmentBuffer().getPointUnsave(midX, y - 2, midZ);
			if(p != null && p.getType() == Element.TYPE_NONE && p1 != null && p1.getType() == Element.TYPE_NONE && p2 != null && p2.getType() == Element.TYPE_NONE) {
				firstGround = p;
				return;
			}
		}
	}

	public SegmentPiece getFirstGround() {
		if(firstGround == null) calculateFirstGround();
		return firstGround;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
		changed = true;
	}

	public void addCrew(CrewMember c) {
		c.setPositional(this);
		crew.add(c);
		changed = true;
	}

	public void removeCrew(CrewMember c) {
		crew.remove(c);
		c.setPositional(null);
		changed = true;
	}

	public boolean hasAnyCrewAvailable() {
		if(GameClient.getClientState() != null) {
			for(AiInterfaceContainer container : GameClient.getClientPlayerState().getPlayerAiManager().getCrew()) {
				if(container instanceof CrewMember)
					return ((CrewMember) container).getAssignedQuarter(segmentController.getQuarterManager()) == null;
				else return true;
			}
		}
		return false;
	}

	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "Quarter [index=" + index + ", area=" + area + ", status=" + status + ", integrity=" + integrity + ", priority=" + priority + ", childConnections=" + childConnections + "]";
	}

	public enum QuarterStatus {
		OK(new Vector4f(0.0f, 255.0f, 30.0f, 0.75f)),
		DAMAGED(new Vector4f(255.0f, 255.0f, 0.0f, 0.75f)),
		DESTROYED(new Vector4f(255.0f, 0.0f, 0.0f, 0.75f));

		public final Vector4f color;

		QuarterStatus(Vector4f color) {
			this.color = color;
		}

		public float getEfficiencyModifier() {
			return switch(this) {
				case OK -> 1.0f;
				case DAMAGED -> 0.5f;
				case DESTROYED -> 0.0f;
			};
		}
	}

	public enum QuarterType {
		BRIDGE(BridgeQuarter::new, Blocks.PLACEHOLDER.getId()),
		CANTEEN(CanteenQuarter::new, Blocks.PLACEHOLDER.getId()),
		CARGO(CargoQuarter::new, Blocks.PLACEHOLDER.getId()),
		GUARD(GuardQuarter::new, Blocks.PLACEHOLDER.getId()),
		LIVING(LivingQuarter::new, Blocks.PLACEHOLDER.getId()),
		MEDICAL(MedicalQuarter::new, Blocks.PLACEHOLDER.getId()),
		RECREATION(RecreationQuarter::new, Blocks.PLACEHOLDER.getId()),
		ENGINEERING(EngineeringQuarter::new, Blocks.PLACEHOLDER.getId()),
		TRAINING(TrainingQuarter::new, Blocks.PLACEHOLDER.getId()),
		GUNNERY(GunneryQuarter::new, Blocks.PLACEHOLDER.getId()),
		HANGAR(HangarQuarter::new, Blocks.PLACEHOLDER.getId()),
		SHIPYARD(ShipyardQuarter::new, Blocks.PLACEHOLDER.getId());

		public final short id;
		private final QuarterFactory f;

		QuarterType(QuarterFactory f, short id) {
			this.f = f;
			this.id = id;
		}

		public static String[] getStrings() {
			String[] s = new String[values().length];
			for(int i = 0; i < values().length; i++) s[i] = values()[i].name();
			return s;
		}

		public static QuarterFactory getFromBlock(short id) {
			return null;
		/*	return switch(id) {
				case Blocks.PLACEHOLDER -> BRIDGE.f;
				case Blocks.PLACEHOLDER -> CANTEEN.f;
				case Blocks.PLACEHOLDER -> CARGO.f;
				case Blocks.PLACEHOLDER -> GUARD.f;
				case Blocks.PLACEHOLDER -> LIVING.f;
				case Blocks.PLACEHOLDER -> MEDICAL.f;
				case Blocks.PLACEHOLDER -> RECREATION.f;
				case Blocks.PLACEHOLDER -> ENGINEERING.f;
				case Blocks.PLACEHOLDER -> TRAINING.f;
				case Blocks.PLACEHOLDER -> GUNNERY.f;
				case Blocks.PLACEHOLDER -> HANGAR.f;
				case Blocks.PLACEHOLDER -> SHIPYARD.f;
				default -> null;
			};*/
		}

		public Quarter getInstance(SegmentController c) {
			return f.getQuarter(c);
		}
	}
}