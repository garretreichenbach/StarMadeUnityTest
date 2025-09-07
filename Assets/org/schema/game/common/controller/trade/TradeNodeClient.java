package org.schema.game.common.controller.trade;


import api.common.GameClient;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.map.MapControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.gamemap.entry.AbstractMapEntry;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.effects.Indication;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.fleet.FleetMember.FleetMemberMapIndication;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.SelectableSprite;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class TradeNodeClient extends TradeNodeStub{
	public class PriceChangeListener extends GUIObservable{
		public void onChanged(){
			System.err.println("[CLIENT] Fire Prices Changed. Oberservers "+listener.size()+"; "+TradeNodeClient.this+"; "+getLoadedShop());
			notifyObservers();
		}
	}
	public class StockChangeListener extends GUIObservable{
		public void onChanged(){
			notifyObservers();
		}
	}
	private final GameClientState state;
	public TradeNodeMapIndication mapEntry;
	public PriceChangeListener priceChangeListener;

	private List<TradePriceInterface> tranPrices = new ObjectArrayList<TradePriceInterface>();

	public boolean dirty = true;

	private boolean requested;


	public TradeNodeClient(GameClientState state){
		this.state = state;
		mapEntry = new TradeNodeMapIndication();

	}
	public void modCredits(long credits) {
		assert(false):"Cannot mod credits on client";
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		super.deserialize(b, updateSenderStateId, isOnServer);

	}

	public ShopInterface getLoadedShop(){
		synchronized(state){
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(getEntityDBId());

			if(sendable != null && sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)sendable).getManagerContainer() instanceof ShopInterface){
				ShopInterface s = (ShopInterface)((ManagedSegmentController<?>)sendable).getManagerContainer();
				if(s.isValidShop() && !s.getSegmentController().isClientCleanedUp()){
					return s;
				}else{
//					System.err.println("NOT VALID SHOP OR CLEAN: "+s.getSegmentController());
				}
			}else{
//				System.err.println("NOTNONSONSONSONSONON "+getEntityDBId());
			}
		}
		return null;
	}
	public ShopInterface getLoadedShopIgnoreValid(){
		synchronized(state){
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(getEntityDBId());
			if(sendable != null && sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)sendable).getManagerContainer() instanceof ShopInterface){
				ShopInterface s = (ShopInterface)((ManagedSegmentController<?>)sendable).getManagerContainer();
				return s;
			}
		}
		return null;
	}
	@Override
	public double getVolume() {
		ShopInterface loadedShop = getLoadedShop();
		if(loadedShop != null){
			super.setVolume(loadedShop.getShopInventory().getVolume());
			return loadedShop.getShopInventory().getVolume();
		}
		return super.getVolume();
	}

	@Override
	public double getCapacity() {
		ShopInterface loadedShop = getLoadedShop();
		if(loadedShop != null){
			super.setCapacity(loadedShop.getShopInventory().getCapacity());
			return loadedShop.getShopInventory().getCapacity();
		}
		return super.getCapacity();
	}

	@Override
	public long getTradePermission() {
		ShopInterface loadedShop = getLoadedShopIgnoreValid();
		if(loadedShop != null){
			super.setTradePermission(loadedShop.getPermissionToTrade());
			return loadedShop.getPermissionToTrade();
		}
		return super.getTradePermission();
	}

	@Override
	public Vector3i getSystem() {
		ShopInterface loadedShop = getLoadedShopIgnoreValid();
		if(loadedShop != null){
			if(super.getSystem() == null){
				super.setSystem(new Vector3i());
			}
			return loadedShop.getSegmentController().getSystem(super.getSystem());
		}
		return super.getSystem();
	}
	@Override
	public Vector3i getSector() {
		ShopInterface loadedShop = getLoadedShopIgnoreValid();
		if(loadedShop != null){
			if(super.getSector() == null){
				super.setSector(new Vector3i());
			}
			return loadedShop.getSegmentController().getSector(sector);
		}
		return super.getSector();
	}

	@Override
	public Set<String> getOwners() {
		ShopInterface loadedShop = getLoadedShopIgnoreValid();
		if(loadedShop != null){

			return loadedShop.getShopOwners();
		}
		return super.getOwners();
	}

	@Override
	public String getStationName() {
		ShopInterface loadedShop = getLoadedShopIgnoreValid();
		if(loadedShop != null){
			super.setStationName(loadedShop.getSegmentController().getRealName());
			return loadedShop.getSegmentController().getRealName();
		}
		return super.getStationName();
	}

	@Override
	public int getFactionId() {
		ShopInterface loadedShop = getLoadedShopIgnoreValid();
		if(loadedShop != null){
			super.setFactionId(loadedShop.getSegmentController().getFactionId());
			return loadedShop.getSegmentController().getFactionId();
		}
		return super.getFactionId();
	}

	@Override
	public long getCredits() {
		ShopInterface loadedShop = getLoadedShopIgnoreValid();
		if(loadedShop != null){
			super.setCredits(loadedShop.getCredits());
			return loadedShop.getCredits();
		}else{
//			System.err.println("SHOP "+getEntityDBId()+" NOT LOADED "+state.getLocalAndRemoteObjectContainer().getDbObjects());
		}
		return super.getCredits();
	}

	public List<TradePriceInterface> getTradePricesClient() {
		ShopInterface loadedShop = getLoadedShopIgnoreValid();
		if(loadedShop != null){
			tranPrices.clear();
			List<TradePriceInterface> pricesRep = loadedShop.getShoppingAddOn().getPricesRep();
			tranPrices.addAll(pricesRep);

			System.err.println("[CLIENT] GOT LOADED PRICES OF NTID: "+loadedShop.getSegmentController().getId()+"; "+pricesRep );

			return tranPrices;
		}
		if(dirty){
			requested = true;
			state.getController().getClientChannel().requestTradePrices(getEntityDBId());
			dirty = false;
		}
		return tranPrices;
	}


	public void receiveTradePrices(List<TradePriceInterface> tranPrices){
		this.tranPrices = tranPrices;
		priceChangeListener.onChanged();
		requested = false;
	}

	public boolean isRefreshing(){
		return getLoadedShop() == null && requested;
	}
	@Override
	public void updateWith(TradeNodeStub dequeue) {
		super.updateWith(dequeue);
		priceChangeListener.onChanged();
	}
	public boolean isRequesting() {
		return getLoadedShop() == null && requested && tranPrices.isEmpty();
	}

	public class TradeNodeMapIndication extends AbstractMapEntry implements SelectableSprite {
		private Indication indication;

		private Vector4f color = new Vector4f(0.9f, 0.8f, 0.2f, 0.8f);

		private Vector3f posf = new Vector3f();

		private boolean drawIndication;

		public Vector3i currentSystemInMap = new Vector3i();

		private float selectDepth;
		@Override
		public void drawPoint(boolean colored, int filter, Vector3i selectedSector) {
			if (colored) {
				float alpha = 1f;
				if (!include(filter, selectedSector)) {
					alpha = 0.1f;
				}
				GlUtil.glColor4f(0.9f, 0.1f, 0.1f, alpha);
			}
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex3f(getPos().x, getPos().y, getPos().z);
			GL11.glEnd();
		}



		@Override
		public Indication getIndication(Vector3i system) {
			Vector3f pos = getPos();
			if (indication == null) {
				Transform t = new Transform();
				t.setIdentity();
				indication = new ConstantIndication(t, Lng.str("Trade Node ")+getStationName() + "\n(Shift-Click for options)");
			}
			indication.setText(Lng.str("Trade Node ")+getStationName() + "\n(Shift-Click for options)");
			indication.getCurrentTransform().origin.set
			(pos.x - GameMapDrawer.halfsize, pos.y - GameMapDrawer.halfsize, pos.z - GameMapDrawer.halfsize);
			return indication;

		}

		@Override
		public int getType() {
			return EntityType.SPACE_STATION.ordinal();
		}

		@Override
		public void setType(byte type) {
		}

		@Override
		public boolean include(int filter, Vector3i selectedSector) {
			return true;
		}

		@Override
		public Vector4f getColor() {
			return color;
		}

		@Override
		public float getScale(long time) {
			if(!currentSystemInMap.equals(getSystem())){
				return 2.0f;
			}
			return 0.2f;
		}

		@Override
		public int getSubSprite(Sprite sprite) {
			return 12;
		}

		@Override
		public boolean canDraw() {
			return GameClient.getClientState().getController().getClientGameData().hasWaypointAt(getSector()); //Prioritize Custom Waypointsreturn true;
		}

		@Override
		public Vector3f getPos() {
			posf.set((getSector().x / VoidSystem.SYSTEM_SIZEf) * 100f, (getSector().y / VoidSystem.SYSTEM_SIZEf) * 100f, (getSector().z / VoidSystem.SYSTEM_SIZEf)*100f);
			return posf;
		}

		/**
		 * @return the drawIndication
		 */
		@Override
		public boolean isDrawIndication() {
			return drawIndication && state.getController().getClientChannel().getGalaxyManagerClient().isSectorVisiblyClientIncludingLastVisited(getSector());
		}

		/**
		 * @param drawIndication the drawIndication to set
		 */
		@Override
		public void setDrawIndication(boolean drawIndication) {
			this.drawIndication = drawIndication;
		}


		@Override
		protected void decodeEntryImpl(DataInputStream stream) throws IOException {
		}
		@Override
		public void encodeEntryImpl(DataOutputStream buffer) throws IOException {
		}



		@Override
		public float getSelectionDepth() {
			return selectDepth;
		}

		@Override
		public void onSelect(float depth) {
			drawIndication = true;
			this.selectDepth = depth;
			MapControllerManager.selected.add(this);

		}

		@Override
		public void onUnSelect() {
			drawIndication = false;
			MapControllerManager.selected.remove(this);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (int) getEntityDBId();
		}

		@Override
		public boolean isSelectable() {
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof FleetMemberMapIndication){
				return ((TradeNodeMapIndication)obj).getEntityId() == getEntityId();
			}

			return false;
		}
		private long getEntityId(){
			return getEntityDBId();
		}
	}

	public void spawnPriceListener() {
		priceChangeListener = new PriceChangeListener();
	}
}
