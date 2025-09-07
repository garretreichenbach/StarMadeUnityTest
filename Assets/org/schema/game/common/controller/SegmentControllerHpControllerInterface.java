package org.schema.game.common.controller;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.network.objects.NetworkSegmentController;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.TagSerializable;

public interface SegmentControllerHpControllerInterface extends TagSerializable {

	public float onHullDamage(Damager damager, float hullDamage, short hitBlockType, DamageDealerType weaponType);

	public void forceDamage(float hullDamage);

	public void onAddedElementSynched(short newType);

	public void onAddedElementsSynched(int[] map, int[] oreCounts);
	
	public void updateLocal(Timer timer);

	public void updateFromNetworkObject(NetworkSegmentController s);

	public void initFromNetwork(NetworkSegmentController s);

	public void updateToNetworkObject();

	public void updateToFullNetworkObject();

	public void onRemovedElementSynched(short oldType);

	public void reboot(boolean fast);
	public boolean hadOldPowerBlocks();
	public void forceReset();

	public boolean isRebooting();

	public long getRebootTimeLeftMS();

	public long getRebootTimeMS();

	public void onElementDestroyed(Damager from, ElementInformation elementInformation, DamageDealerType weaponType, long weaponId);

	public void onManualRemoveBlock(ElementInformation elementInformation);

	public double getHpPercent();

	public void setHpPercent(float v);

	public void setRequestedTimeClient(boolean requestedTimeClient);

	public void setRebootTimeServerForced(long t);

	public long getHp();

	public long getMaxHp();

//	public long getArmorHp();
//
//	public void setArmorHp(long v);
//
//	public long getMaxArmorHp();
//	
//	public double getArmorHpPercent();
//
//	public void setArmorHpPercent(float v);
//
//	public long getShopArmorRepairCost();
//
//	public void repairArmor(boolean fast);
	
	
	

	public String getDebuffString();

	public void onManualAddBlock(ElementInformation elementInformation);


	public long getShopRebootCost();



	public boolean isRebootingRecoverFromOverheating();

	public float getSystemStabilityPenalty();
}
