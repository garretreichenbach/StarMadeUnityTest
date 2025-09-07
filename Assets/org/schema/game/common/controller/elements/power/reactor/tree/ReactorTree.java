package org.schema.game.common.controller.elements.power.reactor.tree;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schema.common.FastMath;
import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ModuleExplosion.ExplosionCause;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.MainReactorUnit;
import org.schema.game.common.controller.elements.power.reactor.PowerImplementation;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitUnit;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberPreset;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement.BootStatusReturn;
import org.schema.game.common.controller.elements.power.reactor.tree.graph.ReactorGraphGlobal;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializableLongSet;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ReactorTree implements SerializationInterface {
	public TagSerializableLongSet main;
	public List<ReactorElement> children = new ObjectArrayList<ReactorElement>();
	private long id;
	public final PowerInterface pw;
	private int size;
	private int actualSize;
	private static byte TAG_VERSION = 1;

	private long centerOfMass;

	private long hp;
	private long maxHp;

	private ShortSet appliedEffectGroups = new ShortOpenHashSet();
	private ShortSet appliedEffectSectorGroups = new ShortOpenHashSet();
	private float chamberCapacity;
	private boolean bootStatus = true;
	private double integrity;
	private final Matrix3f bonusMatrix = new Matrix3f();

	private final Object2ObjectOpenHashMap<String, ReactorChamberPreset> presets = new Object2ObjectOpenHashMap<>();
	private String currentPreset = "Default";

	public Object2ObjectOpenHashMap<String, ReactorChamberPreset> getPresets() {
		return presets;
	}

	public ReactorChamberPreset getCurrentPreset() {
		return presets.get(currentPreset);
	}

	public String getCurrentPresetName() {
		return currentPreset;
	}

	public ObjectArrayList<ReactorElement> getActiveOrUnspecifiedChambers() {
		ObjectArrayList<ReactorElement> activeReactorElements = new ObjectArrayList<>();
		for(ReactorElement chamber : children) {
			if(chamber.isAllValidOrUnspecified()) activeReactorElements.add(chamber);
		}
		return activeReactorElements;
	}

	public ObjectArrayList<ReactorElement> getActiveChambers() {
		ObjectArrayList<ReactorElement> activeReactorElements = new ObjectArrayList<>();
		for(ReactorElement chamber : children) {
			if(chamber.isAllValid()) activeReactorElements.add(chamber);
		}
		return activeReactorElements;
	}

	public void addPreset(ReactorChamberPreset preset) {
		presets.put(preset.getName(), preset);
		loadPreset(preset.getName());
	}

	public void removePreset(String name) {
		presets.remove(name);
		if(presets.isEmpty()) createDefaultPreset();
	}

	private void createDefaultPreset() {
		ReactorChamberPreset preset = new ReactorChamberPreset("Default", new ObjectArrayList<>(children));
		presets.put("Default", preset);
		loadPreset("Default");
	}

	public void loadPreset(String name) {
		if(currentPreset.equals(name)) return;
		ReactorChamberPreset preset = presets.get(name);
		if(preset == null) return;
		preset.applyTo(this);
		currentPreset = name;
	}

	public ReactorTree(PowerInterface pw) {
		this.pw = pw;
		bonusMatrix.setIdentity();
		if(presets.isEmpty()) createDefaultPreset();
	}

	public void build(MainReactorUnit r) {
		main = new TagSerializableLongSet(r.getNeighboringCollection());
		this.size = main.size();
		this.actualSize = main.size();
		this.integrity = r.getIntegrity();
		this.id = r.idPos;
		setAllBooted();

		ConduitCollectionManager conduits = pw.getConduits();
		Set<ReactorChamberUnit> used = new ObjectOpenHashSet<ReactorChamberUnit>();
		for(ConduitUnit c : conduits.getElementCollections()) {
			if(c.getConnectedReactors().contains(r)) {
				addChildFirstLevel(conduits, r, c, used);
			}
		}
		calculateCapacity();
		fillConfig();
		calculateAllMaxHp();
		calculateAllCurrentHp();

		centerOfMass = ElementCollection.getIndex(r.getCoMOrigin());
	}

	private void addChildFirstLevel(ConduitCollectionManager conduits, MainReactorUnit r, ConduitUnit c, Set<ReactorChamberUnit> used) {
		for(ReactorChamberUnit cham : c.getConnectedChambers()) {
			if(!used.contains(cham)) {
				assert (cham != null);
				used.add(cham);
				ReactorElement e = new ReactorElement();
				e.root = this;
				e.validConduit = c.isValidConduit();
				used.add(cham);
				e.create(conduits, this, null, cham, used);
				children.add(e);
			}
		}
	}

	private void calculateAllMaxHp() {
		this.maxHp = calculateLocalHp(this.size);
		for(ReactorElement c : children) {
			this.maxHp += c.calculateMaxHpRecursively();
		}
	}

	private void calculateAllCurrentHp() {
		long oldHp = this.hp;
		this.hp = calculateLocalHp(this.actualSize);
		for(ReactorElement c : children) {
			this.hp += c.calculateHpRecursively();
		}
		if(oldHp != this.hp) {
			flagHpChanged();
		}
	}

	public long calculateLocalHp(int size) {
		return size * ElementKeyMap.getInfo(ElementKeyMap.REACTOR_MAIN).reactorHp;
	}

	private void calculateCapacity() {
		this.chamberCapacity = 0;
		for(ReactorElement c : children) {
			if(!c.isGeneral()) {
				this.chamberCapacity += c.getCapacityRecursively();
			}
		}
//		System.err.println("CALCULATED CAPACITY ::: "+chamberCapacity);
	}

	public boolean isWithinCapacity() {
		return this.chamberCapacity <= 1.009;
	}

	public void print() {
		System.err.println("Reactor Tree: " + id + " SetSize: " + (main != null ? String.valueOf(main.size()) : "n/a on client") + "; ActualSize/Size: " + actualSize + "/" + size + "; Capacity used: " + chamberCapacity);
		int lvl = 0;
		for(ReactorElement c : children) {
			c.print(lvl);
		}
	}

	public boolean isUnitPartOfTree(ReactorChamberUnit e) {
		for(ReactorElement c : children) {
			if(c.isUnitPartOfTree(e)) {
				return true;
			}
		}
		return false;
	}

	public String getName() {
		return "Reactor[" + this.id + "]";
	}

	public String getDisplayName() {
		return Lng.str("Reactor [size: %s]", this.size);
	}

	public void boot() {
		pw.boot(this);
	}

	public long getId() {
		return this.id;
	}

	public boolean isActiveTree() {
		return pw.isActiveReactor(this);
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(id);
		b.writeInt(size);
		b.writeInt(actualSize);
		b.writeLong(centerOfMass);
		b.writeDouble(integrity);
		if(hasModifiedBonusMatrix()) {
			b.writeBoolean(true);
			b.writeFloat(bonusMatrix.m00);
			b.writeFloat(bonusMatrix.m01);
			b.writeFloat(bonusMatrix.m02);
			b.writeFloat(bonusMatrix.m10);
			b.writeFloat(bonusMatrix.m11);
			b.writeFloat(bonusMatrix.m12);
			b.writeFloat(bonusMatrix.m20);
			b.writeFloat(bonusMatrix.m21);
			b.writeFloat(bonusMatrix.m22);
		} else {
			b.writeBoolean(false);
		}

		b.writeShort(children.size());
		for(int i = 0; i < children.size(); i++) {
			children.get(i).serialize(b, isOnServer);
		}

	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		appliedEffectGroups.clear();
		this.id = b.readLong();
		this.size = b.readInt();
		this.actualSize = b.readInt();
		this.centerOfMass = b.readLong();
		this.integrity = b.readDouble();

		boolean bonusByte = b.readBoolean();
		if(bonusByte) {
			bonusMatrix.m00 = b.readFloat();
			bonusMatrix.m01 = b.readFloat();
			bonusMatrix.m02 = b.readFloat();
			bonusMatrix.m10 = b.readFloat();
			bonusMatrix.m11 = b.readFloat();
			bonusMatrix.m12 = b.readFloat();
			bonusMatrix.m20 = b.readFloat();
			bonusMatrix.m21 = b.readFloat();
			bonusMatrix.m22 = b.readFloat();
		}

		int cSize = b.readShort();

		for(int i = 0; i < cSize; i++) {
			ReactorElement e = new ReactorElement();
			e.root = this;
			e.deserialize(b, updateSenderStateId, isOnServer);
			children.add(e);
		}
		pw.reactorTreeReceived(this);
		calculateAllMaxHp();
		calculateAllCurrentHp();
	}

	public void onConfigPoolReceived() {
		calculateCapacity();
		fillConfig();
	}

	public void fromTagStructure(Tag iTag) {
		Tag[] t = iTag.getStruct();
		byte version = t[0].getByte();
		id = t[1].getLong();
		size = t[2].getInt();
		actualSize = t[3].getInt();
		main = (TagSerializableLongSet) t[4].getValue();
		Tag[] cTags = t[5].getStruct();
		for(int i = 0; i < cTags.length - 1; i++) {
			ReactorElement e = new ReactorElement();
			e.root = this;
			e.fromTagStructure(cTags[i]);
			children.add(e);
		}

		if(t.length > 6 && t[6].getType() == Tag.Type.LONG) {
			centerOfMass = t[6].getLong();
		} else {
			//can be removed after release of power update
			Vector3f centerOfMassUnweighted = new Vector3f();
			for(long l : main) {
				centerOfMassUnweighted.x += ElementCollection.getPosX(l);
				centerOfMassUnweighted.y += ElementCollection.getPosY(l);
				centerOfMassUnweighted.z += ElementCollection.getPosZ(l);
			}
			centerOfMassUnweighted.x /= main.size();
			centerOfMassUnweighted.y /= main.size();
			centerOfMassUnweighted.z /= main.size();

			int cX = FastMath.round(centerOfMassUnweighted.x);
			int cY = FastMath.round(centerOfMassUnweighted.y);
			int cZ = FastMath.round(centerOfMassUnweighted.z);
			centerOfMass = ElementCollection.getIndex(cX, cY, cZ);
		}
		if(t.length > 7 && t[7].getType() == Tag.Type.DOUBLE) {
			integrity = t[7].getDouble();
		}
		if(t.length > 8 && t[8].getType() == Tag.Type.MATRIX3f) {
			bonusMatrix.set(t[8].getMatrix3f());
		}
		if(t.length > 9 && t[9].getType() == Tag.Type.STRING) {
			deserializePresets(t[9]);
		}

		calculateCapacity();
		calculateAllMaxHp();
		calculateAllCurrentHp();
	}

	private Tag serializePresets() {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for(ReactorChamberPreset preset : presets.values()) array.put(preset.toJson());
		json.put("array", array);
		return new Tag(Tag.Type.STRING, "Presets", json.toString());
	}

	private void deserializePresets(Tag tag) {
		JSONObject json = new JSONObject(tag.getString());
		JSONArray array = json.getJSONArray("array");
		for(int i = 0; i < array.length(); i++) {
			ReactorChamberPreset preset = new ReactorChamberPreset(array.getJSONObject(i));
			presets.put(preset.getName(), preset);
		}
		if(presets.isEmpty()) createDefaultPreset();
	}

	public Tag toTagStructure() {
		Tag vTag = new Tag(Tag.Type.BYTE, null, TAG_VERSION);
		Tag idTag = new Tag(Tag.Type.LONG, null, id);
		Tag sizeTag = new Tag(Tag.Type.INT, null, size);
		Tag actualSizeTag = new Tag(Tag.Type.INT, null, size);
		Tag mainSetTag = new Tag(Tag.Type.SERIALIZABLE, null, main);
		Tag[] childTags = new Tag[children.size() + 1];
		childTags[childTags.length - 1] = FinishTag.INST;
		for(int i = 0; i < childTags.length - 1; i++) {
			childTags[i] = children.get(i).toTagStructure();
		}
		return new Tag(Tag.Type.STRUCT, null, new Tag[]{
				vTag,
				idTag,
				sizeTag,
				actualSizeTag,
				mainSetTag,
				new Tag(Tag.Type.STRUCT, null, childTags),
				new Tag(Tag.Type.LONG, null, centerOfMass),
				new Tag(Tag.Type.DOUBLE, null, integrity),
				//only save if not identity
				hasModifiedBonusMatrix() ? new Tag(Tag.Type.MATRIX3f, null, bonusMatrix) : new Tag(Tag.Type.BYTE, null, (byte) 0),
				serializePresets(),

				FinishTag.INST
		});
	}

	private void flagHpChanged() {
	}

	public boolean onBlockKilledServer(Damager from, final short type, final long index, final Long2IntMap changedModules) {
		if(main.contains(index)) {
			onMainReactorHit(index);
			changedModules.put(id, actualSize);
//			if(isActiveTree() && pw.getStabilizerEfficiencyTotal() < VoidElementManager.REACTOR_EXPLOSION_STABILITY){
//				checkExplosion(from, type, index);
//			}

			return true;
		}
		return onOtherChamberHit(from, type, index, changedModules);
	}

	//	private void checkExplosion(Damager from, short type, long pos) {
//		if(pw.getSegmentController().getUpdateTime() - lastHit > 1000){
//			lastHit = pw.getSegmentController().getUpdateTime();
//			
//			if(!existsExplosion(from, type, pos)){
//				for(int i = 0; i < pw.getMainReactors().size(); i ++){
//					MainReactorUnit p = pw.getMainReactors().get(i);
//					if(p.getNeighboringCollection().contains(pos)){
//						explodeReactor(from, type, pos, p);
//						break;
//					}
//				}
//			}
//		}
//	}
	public boolean existsExplosion(Damager from, short type, long pos) {
		return pw.getManagerContainer().existsExplosion(from, type, pos);

	}

	public void explodeReactor(Damager from, short type, long pos, ElementCollection<?, ?, ?> p) {
		long explosionRate = (long) ((1d / Math.max(0.0000001d, VoidElementManager.REACTOR_EXPLOSION_RATE)) * 1000d);
		long radLong =
				(long) (VoidElementManager.REACTOR_EXPLOSION_RADIUS_PER_BLOCKS * this.size);
		int rad = (int) Math.min(VoidElementManager.REACTOR_EXPLOSION_RADIUS_MAX,
				Math.min(64, Math.max(3, radLong)));
		long damageLong =
				(long) (VoidElementManager.REACTOR_EXPLOSION_DAMAGE_PER_BLOCKS * this.size
						* Math.max(0.0, (1.0 - pw.getStabilizerEfficiencyTotal() * VoidElementManager.REACTOR_EXPLOSION_STABILITY_LOSS_MULT)));
		int damage = (int) Math.min(VoidElementManager.REACTOR_EXPLOSION_DAMAGE_MAX,
				Math.min(Integer.MAX_VALUE, Math.max(0, damageLong)));

		long pCount = (long) (VoidElementManager.REACTOR_EXPLOSION_COUNT_PER_BLOCKS * this.size);
		int max = (int) (Math.min(1d, VoidElementManager.REACTOR_EXPLOSION_COUNT_PERCENT) * this.size);

		int amount = Math.max(1, (int) Math.min(max, pCount));

		//System.out.println("Reactor Explosion! reactor count: " + this.size +  " Damage: " + damage + " Count: " + pCount + " stabilizationMult: " + (1.0f - pw.getStabilizerEfficiencyTotal()));
		p.explodeOnServer(amount, pos, type, explosionRate, rad, damage, true, ExplosionCause.STABILITY, from);
	}

	private boolean onOtherChamberHit(Damager from, final short type, final long index, final Long2IntMap changedModules) {
		for(int i = 0; i < children.size(); i++) {
			ReactorElement hit = children.get(i).onBlockKilledServer(from, type, index, changedModules);
			if(hit != null) {
				//lost 1 block worth of hp from a chamber block
				hp -= Math.max(0, hit.calculateLocalHp(1));
				flagHpChanged();
				return true;
			}
		}
		return false;
	}

	private void onMainReactorHit(long index) {
		int oldSize = actualSize;
		main.remove(index);
		actualSize = main.size();
		changeHp(null, oldSize, actualSize);
	}

	public boolean isDamagedChambers() {
		for(int i = 0; i < children.size(); i++) {
			boolean d = children.get(i).isDamagedRec();
			if(d) {
				return true;
			}
		}
		return false;
	}

	public boolean isDamagedAny() {
		return isDamagedMain() || isDamagedChambers();
	}

	public boolean isDamagedMain() {
		boolean damaged = actualSize < size;
		return damaged;
	}

	public boolean applyReceivedSizeChange(long moduleId, int actualSize) {
		//on client when received chamber block kills
		if(id == moduleId) {
			int oldSize = this.actualSize;
			this.actualSize = actualSize;
			if(this.actualSize != oldSize) {
				changeHp(null, oldSize, this.actualSize);

			}
			return true;
		}
		for(int i = 0; i < children.size(); i++) {
			boolean d = children.get(i).applyReceivedSizeChange(moduleId, actualSize, this);
			if(d) {
				return true;
			}
		}
		return false;
	}

	public void onChamberReceivedSizeUpdate(ReactorElement reactorElement, int oldSize, int newSize) {
		//on client when received chamber block kills
		if(oldSize != newSize) {
			changeHp(reactorElement, oldSize, newSize);
		}
	}

	public void changeHp(ReactorElement reactorElement, int oldSize, int newSize) {
		if(reactorElement != null) {
			//chamber change
			this.hp -= reactorElement.calculateLocalHp(oldSize);
			this.hp += reactorElement.calculateLocalHp(newSize);
		} else {
			//main reactor change
			this.hp -= calculateLocalHp(oldSize);
			this.hp += calculateLocalHp(newSize);
		}
		if(oldSize != newSize) {
			flagHpChanged();
		}
	}

	public void getAppliedConfigGroups(ShortList out) {
		out.addAll(appliedEffectGroups);
	}

	public void getAppliedConfigSectorGroups(ShortList out) {
		out.addAll(appliedEffectSectorGroups);
	}

	public int getSizeInital() {
		return size;
	}

	public int getLevelReadable() {
		return PowerImplementation.convertLinearLvl(getLevel());
	}

	public int getLevel() {
		return PowerImplementation.getReactorLevel(size);
	}

	public int getMinLvlSize() {
		return PowerImplementation.getMinNeededFromReactorLevelRaw(getLevel());
	}

	public int getMaxLvlSize() {
		return PowerImplementation.getMinNeededFromReactorLevelRaw(getLevel() + 1);
	}

	public int getMinChamberSize() {
		return pw.getNeededMinForReactorLevel(size);
	}

	public float getChamberCapacity() {
		return chamberCapacity;
	}

	public long getHp() {
		return hp;
	}

	public long getMaxHp() {
		return maxHp;
	}

	public GUIGraph getTreeGraph(ElementInformation root, ReactorElement onNode, DialogInput ip, GUIElement dependent) {
		if(!root.isReactorChamberGeneral()) {
			throw new IllegalArgumentException("Not a general chamber (" + id + ") " + getName());
		}
		ReactorGraphGlobal g = new ReactorGraphGlobal(this, dependent);
		g.ip = ip;
		g.updateGraph(false, onNode, root);

		return g.getGraph();
	}

	public GUIGraph getTreeGraphCurrent(ElementInformation elementInformation, GUIElement dependent) {
		ReactorGraphGlobal g = new ReactorGraphGlobal(this, dependent);
		ObjectOpenHashSet<ElementInformation> generals = new ObjectOpenHashSet<ElementInformation>();
		if(elementInformation == null) {
			for(ReactorElement e : children) {
				generals.add(e.getTypeGeneral());
			}
		} else {
			generals.add(elementInformation);
		}
		ElementInformation[] a = new ElementInformation[generals.size()];
		generals.toArray(a);
		g.updateGraph(true, null, a);

		return g.getGraph();
	}

	public boolean existsMutuallyExclusiveFor(short type) {
		for(ReactorElement c : children) {
			if(c.isMutuallyExclusiveToRecusive(type)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsElementAnyChild(short specifiedChamberBlockId) {
		if(containsElement(specifiedChamberBlockId)) {
			return true;
		}
		ElementInformation info = ElementKeyMap.getInfo(specifiedChamberBlockId);
		for(short s : info.chamberChildren) {
			if(containsElementAnyChild(s)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsElement(short specifiedChamberBlockId) {
		for(ReactorElement c : children) {
			if(c.containsElement(specifiedChamberBlockId)) {
				return true;
			}
		}
		return false;
	}

	public ReactorElement getChamber(long reactorIdPos) {
		for(ReactorElement c : children) {
			ReactorElement e = c.getChamber(reactorIdPos);
			if(e != null) {
				return e;
			}
		}
		return null;
	}

	public boolean containsTypeExcept(long chamIdPos, short type) {
		for(ReactorElement c : children) {
			boolean e = c.containsTypeExcept(chamIdPos, type);
			if(e) {
				return true;
			}
		}
		return false;
	}

	public void removeExitingTypes(ShortArrayList l) {
		for(ReactorElement c : children) {
			c.removeExitingTypes(l);
		}
	}

	public void resetBootedRecursive() {
		bootStatus = false;
		for(ReactorElement c : children) {
			c.resetBootedRecursive();
		}
		fillConfig();
	}

	private void setAllBooted() {
		bootStatus = true;
		for(ReactorElement c : children) {
			c.setBootedRecursive();
		}
	}

	public boolean updateBooted(Timer t) {
		for(ReactorElement c : children) {
			BootStatusReturn updateBooted = c.updateBooted(t);
			if(updateBooted == BootStatusReturn.UNCHANGED) {

				return false;
			}
			if(updateBooted == BootStatusReturn.CHANGED) {
				//refill configuration based on boot status
				fillConfig();
				return false;
			}
		}
		if(bootStatus == false) {
			//refill configuration based on boot status
			fillConfig();
		}
		bootStatus = true;
		return true;
	}

	public void fillConfig() {
		final ConfigPool configPool = pw.getConfigPool();
		appliedEffectGroups.clear();
		if(isChamberCapacityIsOk()) {
//			System.err.println("****APPLIYING "+pw.getSegmentController()+" -> "+pw.getSegmentController().getState()+"; "+getChamberCapacity());
			for(ReactorElement c : children) {
				c.fillEffectGroups(configPool, appliedEffectGroups, ElementInformation.CHAMBER_APPLIES_TO_SELF);
			}
		}
		appliedEffectSectorGroups.clear();
		if(isChamberCapacityIsOk()) {

			for(ReactorElement c : children) {
				c.fillEffectGroups(configPool, appliedEffectSectorGroups, ElementInformation.CHAMBER_APPLIES_TO_SECTOR);
			}
		}
	}

	public boolean containsType(short type) {
		for(ReactorElement c : children) {
			boolean e = c.containsType(type);
			if(e) {
				return true;
			}
		}
		return false;
	}

	public int getSize() {
		return size;
	}

	public double getHpPercent() {
		if(maxHp == 0) {
			return 0;
		}
		return (double) hp / (double) maxHp;
	}

	public void getAllReactorElementsWithConfig(ConfigPool configPool, StatusEffectType t, Collection<ConfigGroup> out) {
		if(isChamberCapacityIsOk()) {
			for(ReactorElement c : children) {
				c.getAllReactorElementsWithConfig(configPool, t, out);
			}
		}
	}

	public boolean isChamberCapacityIsOk() {
		return isWithinCapacity();
	}

	public int getActualSize() {
		return actualSize;
	}

	public float getReactorCapacityOf(ElementInformation general) {
		float rc = 0;
		for(ReactorElement c : children) {
			if(c.getTypeGeneral().id == general.id) {
				rc += c.getCapacityRecursively();
			}
		}
		return rc;
	}

	public boolean isAnyChamberBootingUp() {
		return !bootStatus;
	}

	public float getAccumulatedBootUp() {
		float rc = 0;
		for(ReactorElement c : children) {
			rc += c.getAccumulatedBootUp();
		}
		return rc;
	}

	public void distributeBootUp(float bootupPerSpecific) {
		for(ReactorElement c : children) {
			c.distributeBootUp(bootupPerSpecific);
		}
	}

	public int getSpecificCountRec() {
		int rc = 0;
		for(ReactorElement c : children) {
			rc += c.getSpecificCountRec();
		}
		return rc;
	}

	//	public void effectHitOnServer(double damage) {
//		int lostBlocks = Math.min((int)Math.round(damage/(double)maxHp), actualSize);
//		int i = 0;
//		if(lostBlocks > 0){
//			int oldSize = actualSize;
//			LongIterator iterator = main.iterator();
//			while(iterator.hasNext() && i < lostBlocks){
//				iterator.remove();
//				i++;
//			}
//			actualSize = main.size();
//			changeHp(null, oldSize, actualSize);
//		}
//	}
	public long getCenterOfMass() {
		return centerOfMass;
	}

	public double getIntegrity() {
		return integrity;
	}

	public Matrix3f getBonusMatrix() {
		return bonusMatrix;
	}

	public boolean hasModifiedBonusMatrix() {
		return !TransformTools.ident.basis.equals(bonusMatrix);
	}

}
