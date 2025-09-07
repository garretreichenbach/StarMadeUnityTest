package org.schema.game.common.controller.elements.armorhp;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;

import static java.lang.Math.max;
import static org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator.lerp;

/**
 * CollectionManager for ArmorHP.
 * <p>Rather than storing each block index, this just stores the type and count for efficiency.</p>
 *
 * @author TheDerpGamer
 */
public class ArmorHPCollection extends ElementCollectionManager<ArmorHPUnit, ArmorHPCollection, VoidElementManager<ArmorHPUnit, ArmorHPCollection>> {

	private static final long UPDATE_FREQUENCY = 5000;
	private static final long REGEN_FREQUENCY = 1000;

	//	private final Short2IntArrayMap blockMap = new Short2IntArrayMap(); By all accounts, this SHOULD work, but it doesn't because... Java reasons?
	private final short[] armorTypes;
	private final int[] armorCounts;

	private double currentHP;
	private double maxHP;
	private boolean flagCollectionChanged;
	private boolean updateMaxOnly;
	private long lastUpdate;
	private long lastRegen;
	private boolean regenEnabled;

	public ArmorHPCollection(SegmentController segmentController, VoidElementManager<ArmorHPUnit, ArmorHPCollection> armorHPManager) {
		super(ElementKeyMap.CORE_ID, segmentController, armorHPManager);
		armorTypes = ElementKeyMap.getAllArmorBlocks();
		armorCounts = new int[armorTypes.length];
	}

	public static ArmorHPCollection getCollection(SegmentController segmentController) {
		if(segmentController instanceof ManagedUsableSegmentController<?> managedUsableSegmentController) return managedUsableSegmentController.getManagerContainer().getArmorHP().getCollectionManager();
		else return null;
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<ArmorHPUnit> getType() {
		return ArmorHPUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public ArmorHPUnit getInstance() {
		return new ArmorHPUnit();
	}

	@Override
	protected void onChangedCollection() {
		if(!getSegmentController().isOnServer()) ((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer().managerChanged(this);
		flagCollectionChanged = true;
	}

	@Override
	public void update(Timer timer) {
		if(currentHP < maxHP && maxHP > 0) {
			flagCollectionChanged = false; //This should prevent people from just placing blocks to get their HP back
			return;
		}

		if((flagCollectionChanged || (maxHP <= 0 && hasAnyArmorBlocks())) && getSegmentController().isFullyLoadedWithDock()) recalcHP();
		if(System.currentTimeMillis() - lastUpdate >= UPDATE_FREQUENCY) {
			lastUpdate = System.currentTimeMillis();
			regenEnabled = getSegmentController().getConfigManager().apply(StatusEffectType.ARMOR_HP_REGENERATION, 1.0f) > 1.0f;
		}

		if(System.currentTimeMillis() - lastRegen >= REGEN_FREQUENCY && regenEnabled && currentHP < maxHP) {
			lastRegen = System.currentTimeMillis();
			doRegen();
		}
	}

	public void doRegen() {
		double regen = getSegmentController().getConfigManager().apply(StatusEffectType.ARMOR_HP_REGENERATION, 1.0f) * maxHP;
		currentHP = Math.min(maxHP, currentHP + regen);
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("HP Status"), StringTools.formatPointZero(currentHP) + "/" + StringTools.formatPointZero(maxHP) + " [" + getHPPercent() * 100 + "%]")
		};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Armor HP System");
	}

	private int getCount(short type) {
		for(int i = 0; i < armorTypes.length; i++) {
			if(armorTypes[i] == type) return armorCounts[i];
		}
		return 0;
	}

	private void setCount(short type, int count) {
		for(int i = 0; i < armorTypes.length; i++) {
			if(armorTypes[i] == type) {
				armorCounts[i] = count;
				if(count < 0) {
					System.err.println("[WARNING][ArmorHPCollection]: Negative armor count detected, resetting to 0.\nThis is likely a sign of a deeper issue with the entity, and should not be ignored!\nEntity: " + getSegmentController().getName() + " (" + getSegmentController().getUniqueIdentifier() + ")");
					armorCounts[i] = 0;
				}
				break;
			}
		}
	}

	private boolean hasAnyArmorBlocks() {
		for(int count : armorCounts) {
			if(count > 0) return true;
		}
		return false;
	}

	/**
	 * Recalculates the current and maximum HP (Hit Points) of the armor.
	 * <p>
	 * This method resets the current and maximum HP to zero, then iterates through all armor types
	 * to calculate the new HP values based on the armor's multiplier
	 * </p>
	 * <p>
	 * If the updateMaxOnly flag is set, only the maximum HP is updated. The method ensures that
	 * the current HP does not exceed the maximum HP and that neither value is negative.
	 * </p>
	 */
	public void recalcHP() {
		// Reset current and maximum HP to zero
		currentHP = 0;
		maxHP = 0;

		// Get the armor multiplier
		float armorMult = VoidElementManager.ARMOR_HP_MULTIPLIER;

		// Iterate through all armor types to calculate HP values
		for(short type : armorTypes) {
			if(type != 0) {
				int count = getCount(type);
				if(!updateMaxOnly) {
					// Update current HP based on armor value and count
					currentHP += (ElementKeyMap.getInfo(type).getArmorValue() * armorMult) * count;
				}
				// Update maximum HP based on armor value and count
				maxHP += (ElementKeyMap.getInfo(type).getArmorValue() * armorMult) * count;
			}
		}

		// Ensure current HP and maximum HP are not negative
		if(currentHP < 0) currentHP = 0;
		if(maxHP < 0) maxHP = 0;

		// Ensure current HP does not exceed maximum HP
		if(currentHP > maxHP) currentHP = maxHP;

		// Reset flags and update the last update time
		flagCollectionChanged = false;
		updateMaxOnly = false;
		lastUpdate = System.currentTimeMillis();
	}

	public double getCurrentHP() {
		return currentHP;
	}

	public double getMaxHP() {
		return maxHP;
	}

	public double getHPPercent() {
		if(maxHP == 0 || currentHP == 0) return 0;
		else return currentHP / maxHP;
	}

	public void setCurrentHP(double hp) {
		double prev = currentHP;
		currentHP = max(0, Math.min(maxHP, hp));
		if(!isOnServer() && currentHP == 0 && prev > 0) {
			if(getSegmentController().isClientOwnObject()) {
				//TODO play "0022_spaceship user - ship turbulence medium.ogg" & "0022_spaceship user - int. cockpit alarm 1 (loop)"
				long index = getInstance().idPos;
				Vector3f pos = new Vector3f(getSegmentController().getWorldTransform().origin);
				AudioController.fireAudioEvent("0022_spaceship user - ship turbulence medium", AudioController.ent(getSegmentController(), pos, index, 5.0f));
				AudioController.fireAudioEvent("0022_spaceship user - int. cockpit alarm 1 (loop)", AudioController.ent(getSegmentController(), pos, index, 5.0f));
				GameClientState.instance.message(new ServerMessage(Lng.astr("[WARNING] Armor integrity is fully compromised!"), ServerMessage.MESSAGE_TYPE_WARNING));
			}
		}
	}

	public void addBlock(long index, short type) {
		setCount(type, getCount(type) + 1);
		if(currentHP < maxHP) updateMaxOnly = true;
		flagCollectionChanged = true;
		try {
			if(rawCollection == null && type != 0) doAdd(index, type);
		} catch(Exception exception) { //I know this is terrible exception handling, but this whole process is shit and can randomly break, so it's better than crashing the game
			System.err.println("[ERROR][ArmorHPCollection]: Failed to add block of type " + type + " to entity " + getSegmentController().getName() + " (" + getSegmentController().getUniqueIdentifier() + ")");
			exception.printStackTrace();
		}
	}

	public void removeBlock(long index, short type) {
		setCount(type, getCount(type) - 1);
		flagCollectionChanged = true;
		try {
			if(rawCollection == null && type != 0) doRemove(index);
		} catch(Exception exception) { //I know this is terrible exception handling, but this whole process is shit and can randomly break, so it's better than crashing the game
			System.err.println("[ERROR][ArmorHPCollection]: Failed to remove block of type " + type + " from entity " + getSegmentController().getName() + " (" + getSegmentController().getUniqueIdentifier() + ")");
			exception.printStackTrace();
		}
	}

	/**
	 * @return the multiplier to armour absorption effectiveness at max, i.e. while the structure has 100% AHP.
	 */
	public float getArmorMaxAbsorptionFactor() {
		return getConfigManager().apply(StatusEffectType.ARMOR_HP_ABSORPTION, VoidElementManager.ARMOR_HP_MAX_ARMOR_EFFECTIVENESS_MULTIPLIER);
	}

	/**
	 * Calculate reduced damage to armour based on AHP level and native armour protection.
	 *
	 * @param dmgIn                       The original damage to the impacted armour block
	 * @param dmgAfterBaseArmorProtection The remaining damage from the shot after standard armour calculations (without AHP)
	 * @return The resulting damage value after AHP protection is factored in
	 */
	public float processDamageToArmor(float dmgIn, float dmgAfterBaseArmorProtection) {
		float dmgToAHP;
		float dmgOut;
		if(getHPPercent() > getBleedthroughThreshold()) {
			dmgOut = 0;
			dmgToAHP = dmgIn; //absorbed everything
			// Do note that this implementation creates armour gating.
			// Even if a single hit takes the armour HP well below the bleedthrough threshold, as long as the hit landed whilst above the threshold it will still be entirely absorbed as AHP damage. Not certain if this is desirable.
			// Todo: I'm sure this will have no negative consequences whatsoever for the game's balance and won't lead to an armor gating meta :clueless:
		} else if(dmgIn > 0 && currentHP > 0) {
			float armorStrengthMultiplier = lerp(1.0f, getArmorMaxAbsorptionFactor(), (float) (getHPPercent() / getBleedthroughThreshold())); //reduce added armour effectiveness based on HP remaining, approaching default armour behaviour as we approach zero
			float armorRatio = dmgAfterBaseArmorProtection / (dmgIn * armorStrengthMultiplier); //reduction scaled by strength multiplier. Note that if armorStrengthMultiplier is 1, this cancels out

			dmgToAHP = dmgAfterBaseArmorProtection - armorRatio; //deduct HP based on the damage absorbed by AHP
			dmgOut = dmgIn * armorRatio;
		} else {
			//no AHP left
			dmgOut = dmgAfterBaseArmorProtection;
			dmgToAHP = 0;
		}

		dmgToAHP *= VoidElementManager.ARMOR_HP_LOST_PER_DAMAGE_ABSORBED;
		setCurrentHP(max(0, currentHP - dmgToAHP));
		return dmgOut;
	}

	private double getBleedthroughThreshold() {
		return max(VoidElementManager.MIN_ARMOR_HP_BLEEDTHROUGH_START, getConfigManager().apply(StatusEffectType.ARMOR_HP_BLEEDTHROUGH_THRESHOLD, VoidElementManager.BASE_ARMOR_HP_BLEEDTHROUGH_START));
	}
}