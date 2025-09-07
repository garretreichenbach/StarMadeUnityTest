package org.schema.game.common.controller;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.generator.ShopCreatorThread;
import org.schema.game.common.controller.trade.TradeNode;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.inventory.*;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.network.objects.NetworkShop;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.remote.RemoteInventory;
import org.schema.game.network.objects.remote.RemoteInventoryClientAction;
import org.schema.game.network.objects.remote.RemoteInventoryMultMod;
import org.schema.game.network.objects.remote.RemoteInventorySlotRemove;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.AudioEntity;

import javax.vecmath.Vector4f;
import java.util.Map.Entry;
import java.util.Set;

public class ShopSpaceStation extends EditableSendableSegmentController implements TransientSegmentController, InventoryHolder, AudioEntity, ShopInterface {

	private final ShoppingAddOn shoppingAddOn;
	protected ShopInventory shopInventory;
	private ObjectArrayFIFOQueue<InventoryClientAction> clientInventoryActions = new ObjectArrayFIFOQueue<>();
	private boolean transientTouched;
	private boolean transientMoved;

	public ShopSpaceStation(StateInterface state) {
		super(state);
		shoppingAddOn = new ShoppingAddOn(this);
		shoppingAddOn.setBasePriceMult(0.5 + Math.random());
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.SHOP_SPACE_STATION;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.SHOP;
	}
	@Override
	public void sendHitConfirm(byte damageType) {
	}
	@Override
	public void getRelationColor(RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyShop);
			case FRIEND -> out.set(ColorPalette.allyShop);
			case NEUTRAL -> out.set(ColorPalette.neutralShop);
		}
		if(sameFaction) {
			out.set(ColorPalette.factionShop);
		}
		out.x += select;
		out.y += select;
		out.z += select;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		shopInventory = new ShopInventory(this, Long.MIN_VALUE);
		//		shopAIEntity = new ShopAIEntity(null, null, this);
		setMass(0);

		if (getRealName().equals("undef")) {
			setRealName("Shop");
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#onSectorInactiveClient()
	 */
	@Override
	public void onSectorInactiveClient() {
		super.onSectorInactiveClient();
		shoppingAddOn.onSectorInactiveClient();

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}
	@Override
	public boolean isVulnerable() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#cleanUpOnEntityDelete()
	 */
	@Override
	public void cleanUpOnEntityDelete() {

		super.cleanUpOnEntityDelete();

		shoppingAddOn.cleanUp();

	}

	@Override
	public void destroyPersistent() {
		super.destroyPersistent();

		// Update map for deleted ship
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
		((GameServerState) getState()).getGameMapProvider().updateMapForAllInSystem(sysPos);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);

		shoppingAddOn.initFromNetwokObject(from);



		if (!isOnServer()) {
			NetworkShop s = ((NetworkShop) from);

			ObjectArrayList<RemoteInventory> r = s.getInventoriesChangeBuffer().getReceiveBuffer();
			for(int i = 0; i < r.size(); i++){
				ShopInventory c = (ShopInventory) r.get(i).get();
				shopInventory = c;
			}

			if (s.getInventoryMultModBuffer().getReceiveBuffer().size() > 0) {
				for (RemoteInventoryMultMod ia : s.getInventoryMultModBuffer().getReceiveBuffer()) {
					shopInventory.handleReceived(ia.get(), s);
				}
			}
		}
	}

	@Override
	public String toNiceString() {
		return "Shop (" + getUniqueIdentifier().substring(12) + ")";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SendableSegmentController#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		NetworkShop s = ((NetworkShop) o);

		ObjectArrayList<RemoteInventory> r = s.getInventoriesChangeBuffer().getReceiveBuffer();
		for(int i = 0; i < r.size(); i++){
			if(r.get(i).get() instanceof ShopInventory){
				ShopInventory c = (ShopInventory) r.get(i).get();
				shopInventory = c;
			}
		}
		if (s.getInventoryMultModBuffer().getReceiveBuffer().size() > 0) {
			for (RemoteInventoryMultMod ia : s.getInventoryMultModBuffer().getReceiveBuffer()) {
				shopInventory.handleReceived(ia.get(), s);
			}
		}
		if (s.getInventorySlotRemoveRequestBuffer().getReceiveBuffer().size() > 0) {
			for (RemoteInventorySlotRemove ia : s.getInventorySlotRemoveRequestBuffer().getReceiveBuffer()) {
				shopInventory.removeSlot(ia.get().slot, isOnServer());
			}
		}
		if (s.getInventoryClientActionBuffer().getReceiveBuffer().size() > 0) {
			for (RemoteInventoryClientAction ia : s.getInventoryClientActionBuffer().getReceiveBuffer()) {
				InventoryClientAction inventoryClientAction = ia.get();
				synchronized (clientInventoryActions) {
					clientInventoryActions.enqueue(inventoryClientAction);
				}
			}
		}
		shoppingAddOn.receivePrices(false);
	}

	@Override
	public void updateToFullNetworkObject() {

		shopInventory.sendAll();
		super.updateToFullNetworkObject();
		long time = System.currentTimeMillis();

		shoppingAddOn.updateToFullNT();

	}

	@Override
	public void fillInventory(boolean send, boolean full) throws NoSlotFreeException {
		shoppingAddOn.fillInventory(send, full);
	}

	@Override
	public long getCredits() {
		return shoppingAddOn.getCredits();
	}

	/**
	 * @return the permissionToPurchase
	 */
	@Override
	public long getPermissionToPurchase() {
		return shoppingAddOn.getPermissionToPurchase();
	}

	@Override
	public long getPermissionToTrade() {
		return shoppingAddOn.getPermissionToTrade();
	}

	@Override
	public TradePriceInterface getPrice(short type, boolean buy){
		return shoppingAddOn.getPrice(type, buy);
	}

	/**
	 * @return the shopInventory
	 */
	@Override
	public ShopInventory getShopInventory() {
		return shopInventory;
	}

	@Override
	public Set<String> getShopOwners() {
		return shoppingAddOn.getOwnerPlayers();
	}

	/**
	 * @return the shoppingAddOn
	 */
	@Override
	public ShoppingAddOn getShoppingAddOn() {
		return shoppingAddOn;
	}

	@Override
	public void modCredits(long i) {
		shoppingAddOn.modCredits(i);
	}

	@Override
	public boolean isInfiniteSupply() {
		return shoppingAddOn.isInfiniteSupply();
	}

	@Override
	public boolean isAiShop() {
		return shoppingAddOn.isAIShop();
	}

	/**
	 * @param shopInventory the shopInventory to set
	 */
	public void setShopInventory(ShopInventory shopInventory) {
		this.shopInventory = shopInventory;
	}

	@Override
	public String getInsideSound() {
		return "0022_ambience loop - shop machinery (loop)";
	}

	@Override
	public float getInsideSoundPitch() {
		return 1;
	}

	@Override
	public float getInsideSoundVolume() {
		return 1;
	}

	@Override
	public String getOutsideSound() {
		return "0022_ambience loop - shop machinery (loop)";
	}

	@Override
	public float getOutsideSoundPitch() {
		return 1.0f;
	}

	@Override
	public float getOutsideSoundVolume() {
		return 1.5f;
	}

	@Override
	public float getSoundRadius() {
		return getBoundingBox().maxSize();
	}

	@Override
	public boolean isOwnPlayerInside() {
		return false;
	}

	@Override
	public boolean hasStructureAndArmorHP() {
		return false;
	}

	private final InventoryMap invMap = new InventoryMap();
	private boolean hasPricesUpdated;
	@Override
	public InventoryMap getInventories() {
		invMap.put(0, shopInventory);
		return invMap;
	}

	@Override
	public Inventory getInventory(long pos) {
		return shopInventory;
	}
	@Override
	public double getCapacityFor(Inventory inventory) {
		return shopInventory.getVolume();
	}
	@Override
	public void volumeChanged(double volumeBefore, double volumeNow) {
	}
	@Override
	public NetworkInventoryInterface getInventoryNetworkObject() {
		return getNetworkObject();
	}
	@Override
	public void sendInventoryErrorMessage(Object[] astr, Inventory inv) {
	}
	@Override
	public String printInventories() {
		return shopInventory.toString();
	}

	@Override
	public void sendInventoryModification(IntCollection slots, long parameter) {

		Inventory inventory = getInventory(parameter);
		if (inventory != null) {
			InventoryMultMod m = new InventoryMultMod(slots, inventory, parameter);

			getNetworkObject().getInventoryMultModBuffer().add(new RemoteInventoryMultMod(m, getNetworkObject()));
		} else {
			try {
				throw new IllegalArgumentException("[INVENTORY] Exception: tried to send inventory " + parameter);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void sendInventoryModification(int slot, long parameter) {

		IntArrayList l = new IntArrayList(1);
		l.add(slot);
		sendInventoryModification(l, parameter);
	}
	@Override
	public void sendInventorySlotRemove(int slot, long parameter) {
		Inventory inventory = getInventory(parameter);
		if (inventory != null) {
			getNetworkObject().getInventorySlotRemoveRequestBuffer().add(new RemoteInventorySlotRemove(
					new InventorySlotRemoveMod(slot, parameter), isOnServer()));
		} else {
			try {
				throw new IllegalArgumentException("[INVENTORY] Exception: tried to send inventory " + parameter);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public String getName() {
		return "Shop";
	}

	//	/* (non-Javadoc)
	//	 * @see org.schema.game.common.controller.SegmentController#fromTagStructure(org.schema.game.common.controller.io.Tag)
	//	 */
	//
	//	public int getPriceBasedOfQuantity(ElementInformation info, int wantedQuantity){
	//		// maximal stock of the shop per item
	//		float baseValue = getShopInventory().getMaxStock();
	//
	//		//modificator for additional
	//		float baseDiv = 1.01f;
	//
	//		//retrieve inventory slot for the item
	//		int slot = getShopInventory().getFirstSlot(info.getId());
	//
	//		//inventory slot < 0 if it doesnt exist
	//		int count = slot >= 0 ? getShopInventory().getCount(slot) : 0;
	//
	//		//lower limit count from 1 to maxStock
	//		float priceLimitA = Math.max(1, Math.min(baseValue, count));
	//		//calculate lower limit percentage of max stock
	//		float priceLimitPercentA = 1.0f - priceLimitA / baseValue;
	//
	//
	//
	//		//calculate difference of price: lowerLimit + differenceOfLimits/2
	//		float extraPriceDifference = baseDiv*priceLimitPercentA;
	//
	//		//calculate base price without any influence by stock or interest
	//		float basePrice =  (info.getPrice());
	//
	//
	//
	//		//calculate interest (stock demand)
	//		float additionalPrice = (info.getPrice() * extraPriceDifference);
	//
	//		//add both for final price
	//		int finalPrice = (int) (wantedQuantity * (basePrice+additionalPrice));
	//
	//
	////		if(info.getId() == ElementKeyMap.CORE_ID){
	////			if(Math.abs(wantedQuantity) > 1){
	////				System.err.println("QUANTITY PRICE FOR "+info.getName()+" Quant: "+wantedQuantity+": "+finalPrice+"; base: "+basePrice+"; add "+additionalPrice+"; AP "+priceLimitPercentA+"("+priceLimitA+"); BP "+priceLimitPercentB+"("+priceLimitB+");  price diff = "+extraPriceDifference);
	////			}
	////		}
	//
	//		return finalPrice;
	//
	//	}
	@Override
	public void fromTagStructure(Tag tag) {

		if ("ShopSpaceStation3".equals(tag.getName())) {
			Tag[] p = (Tag[]) tag.getValue();
			assert ("inv".equals(p[1].getName()) || "stash".equals(p[1].getName()));
			shopInventory.fromTagStructure(p[1]);

			shoppingAddOn.fromTagStructure(p[2]);

			//update missing only
			shoppingAddOn.updateServerPrices(false, true);

			super.fromTagStructure(p[0]);
		} else if ("ShopSpaceStation2".equals(tag.getName())) {
			Tag[] p = (Tag[]) tag.getValue();
			assert ("inv".equals(p[1].getName()) || "stash".equals(p[1].getName()));
			shopInventory.fromTagStructure(p[1]);

			shoppingAddOn.fromTagStructure(p[2]);

			//update missing only
			shoppingAddOn.updateServerPrices(false, true);

			if (this instanceof ManagedShop)
				((Tag[]) p[0].getValue())[7] = ((ManagedShop) this).getManagerContainer().toTagStructure();

			super.fromTagStructure(p[0]);
		} else if ("ShopSpaceStation1".equals(tag.getName())) {
			Tag[] p = (Tag[]) tag.getValue();
			assert ("inventory".equals(p[1].getName()));
			shopInventory.fromTagStructure(p[1]);

			shoppingAddOn.fromTagStructure(p[2], p[3], p[4]);

			if (this instanceof ManagedShop)
				((Tag[]) p[0].getValue())[7] = ((ManagedShop) this).getManagerContainer().toTagStructure();

			//update missing only
			shoppingAddOn.updateServerPrices(false, true);
			super.fromTagStructure(p[0]);
		} else if ("ShopSpaceStation0".equals(tag.getName())) {
			Tag[] p = (Tag[]) tag.getValue();
			assert ("inventory".equals(p[1].getName()));
			shopInventory.fromTagStructure(p[1]);

			if (this instanceof ManagedShop)
				((Tag[]) p[0].getValue())[7] = ((ManagedShop) this).getManagerContainer().toTagStructure();

			super.fromTagStructure(p[0]);
			shoppingAddOn.updateServerPrices(false, false);
		} else {

			if (this instanceof ManagedShop)
				((Tag[]) tag.getValue())[7] = ((ManagedShop) this).getManagerContainer().toTagStructure();

			super.fromTagStructure(tag);
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {

		return new Tag(Type.STRUCT, (this instanceof ManagedShop) ? "ShopSpaceStation3" : "ShopSpaceStation2", new Tag[]{
				super.toTagStructure(),
				shopInventory.toTagStructure(),
				shoppingAddOn.toTagStructure(),
				new Tag(Type.FINISH, "fin", null)});
	}

	@Override
	public NetworkShop getNetworkObject() {
		return (NetworkShop) super.getNetworkObject();
	}

	@Override
	protected String getSegmentControllerTypeString() {
		return "Shop";
	}



//	/* (non-Javadoc)
//	 * @see org.schema.game.common.controller.EditableSendableSegmentController#handleHit(javax.vecmath.Vector3f, com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback)
//	 */
//	@Override
//	public ParticleHitCallback handleHit(ParticleHitCallback c, Damager from, float damage, float damageBeforeShield, Vector3f startPos, Vector3f endPos, boolean shieldAbsorbed, short effectType, float effectRatio, float effectSize) {
//		//		System.err.println( getState()+" You attacked the shop (Cannot be destroyed): PREPARE TO DIE!!");
//
//		if (!isOnServer()) {
//			// prepare to add some explosions
//			GameClientState s = (GameClientState) getState();
//			s.getWorldDrawer().getExplosionDrawer().addExplosion(c.hitPointWorld);
//			Transform t = new Transform();
//			t.setIdentity();
//			t.origin.set(c.hitPointWorld);
//			((GameClientController)getState().getController()).queueTransformableAudio("0022_spaceship enemy - hit no explosion metallic impact on enemy ship", t, 2, 100);
//		} else {
//			shoppingAddOn.onHit(from);
//		}
//		c.hit = true;
//		c.addDamageDone(0);
//		return c;
//	}
//
//
//
//	/* (non-Javadoc)
//	 * @see org.schema.game.common.controller.EditableSendableSegmentController#handleBeamDamage(org.schema.game.common.controller.elements.BeamState, org.schema.game.common.controller.BeamHandlerContainer, long, javax.vecmath.Vector3f, javax.vecmath.Vector3f, org.schema.game.common.data.physics.CubeRayCastResult, org.schema.schine.graphicsengine.core.Timer)
//	 */
//	@Override
//	public int handleBeamDamage(
//			BeamState beam, int hits,
//			BeamHandlerContainer<? extends SimpleTransformableSendableObject> owner,
//			Vector3f from, Vector3f to,
//			CubeRayCastResult cubeResult, boolean ignoreShields, Timer timer) {
//
//		if (!isOnServer()) {
//		} else {
//			if (beam.getHandler().getBeamShooter() != null && beam.getHandler().getBeamShooter() instanceof Sendable) {
//				shoppingAddOn.onHit(beam.getHandler().getBeamShooter());
//			}
//		}
//		return 0;
//	}

	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkShop(getState(), this));
	}

	@Override
	protected void onCoreDestroyed(Damager from) {

	}

	@Override
	public void startCreatorThread() {
		if (getCreatorThread() == null) {

			setCreatorThread(new ShopCreatorThread(this));
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		long time = System.currentTimeMillis();

		if(isOnServer() && !hasPricesUpdated){
			shoppingAddOn.updateServerPrices(true, true);
			hasPricesUpdated = true;
		}

		if (!clientInventoryActions.isEmpty()) {
			assert (isOnServer());
			Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> moddedSlots = new Object2ObjectOpenHashMap<Inventory, IntOpenHashSet>();
			synchronized (clientInventoryActions) {
				while (!clientInventoryActions.isEmpty()) {
					try {
						InventoryClientAction d = clientInventoryActions.dequeue();

						assert (d.ownInventoryOwnerId == getId());

						Sendable s = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(d.otherInventoryOwnerId);
						if (s != null) {
							InventoryHolder holder;
							if (s instanceof ManagedSegmentController<?>) {
								holder = ((ManagedSegmentController<?>) s).getManagerContainer();
							} else {
								holder = (InventoryHolder) s;
							}

							Inventory inventory = getInventory(d.ownInventoryPosId);
							if (inventory != null) {
								inventory.doSwitchSlotsOrCombine(d.slot, d.otherSlot, d.subSlot, holder.getInventory(d.otherInventoryPosId), d.count, moddedSlots);
							} else {
								assert (false);
							}
						} else {
							assert (false);
						}

					} catch (InventoryExceededException e) {
						e.printStackTrace();
					}
				}
			}
			if (!moddedSlots.isEmpty()) {
				for (Entry<Inventory, IntOpenHashSet> e : moddedSlots.entrySet()) {
					e.getKey().sendInventoryModification(e.getValue());
				}
			}
		}

		shoppingAddOn.update(time);

		Starter.modManager.onSegmentControllerUpdate(this);
	}

	@Override
	public boolean isSalvagableFor(Salvager harvester, String[] cannotHitReason, Vector3i position) {
		return false;
	}

	@Override
	public boolean isTouched() {
//		return transientTouched; TODO: Doesn't work correctly and just results the shops to never respawn
		return true;
	}

	@Override
	public void setTouched(boolean b, boolean checkEmpty) {
		transientTouched = b;
	}

	@Override
	public boolean isMoved() {
		return transientMoved;
	}

	@Override
	public void setMoved(boolean b) {
		transientMoved = b;
	}

	@Override
	public boolean needsTagSave() {
		return transientMoved || transientTouched;
	}

	@Override
	public boolean isTradeNode() {
		return false;
	}

	@Override
	public TradeNode getTradeNode() {
		return null;
	}

	@Override
	public SegmentController getSegmentController() {
		return this;
	}

	@Override
	public boolean isValidShop() {
		return true;
	}

	@Override
	public boolean isNPCHomeBase() {
		return false;
	}

	@Override
	public int getPriceString(ElementInformation info, boolean purchase) {
		return shoppingAddOn.getPriceString(info, purchase);
	}

	@Override
	public boolean wasValidTradeNode() {
		return true;
	}
	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}
	private DamageBeamHitHandler damageBeamHitHandler = new DamageBeamHitHandlerSegmentController();
	public DamageBeamHitHandler getDamageBeamHitHandler() {
		return damageBeamHitHandler;
	}
	public boolean canBeDamagedBy(Damager from, DamageDealerType beam) {
		shoppingAddOn.onHit(from);
		return false;
	}
}
