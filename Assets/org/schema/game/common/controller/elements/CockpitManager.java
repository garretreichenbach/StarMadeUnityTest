package org.schema.game.common.controller.elements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.manager.ingame.ship.ShipExternalFlightController;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.client.view.camera.ShipOffsetCameraViewable;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.network.objects.NetworkShip;
import org.schema.game.network.objects.remote.RemoteCockpitManager;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.LongTransformPair;
import org.schema.schine.network.objects.remote.RemoteLongTransformationPair;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;
import org.schema.schine.resource.tag.TagSerializableLong2TransformMap;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CockpitManager implements TagSerializable, SerializationInterface{
	private static final byte TAG_VERSION = 0;
	private final TagSerializableLong2TransformMap metaCockpitMap;
	private final TagSerializableLong2TransformMap cockpits;
	private final ShipManagerContainer c;
	private int cockpitIndex = -1;
	private Vector3i cockpitTmp = new Vector3i();
	
	public CockpitManager(ShipManagerContainer c) {
		this.c = c;
		
		metaCockpitMap = new TagSerializableLong2TransformMap();
		metaCockpitMap.ignoreIdentTransforms = true;
		cockpits = new TagSerializableLong2TransformMap();
		cockpits.ignoreIdentTransforms = true;
		
		Transform id = new Transform(TransformTools.ident);
		metaCockpitMap.defaultReturnValue(id);
	}
	
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		
		metaCockpitMap.serialize(b, isOnServer);
		cockpits.serialize(b, isOnServer);
	}
	
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		
		//for the client, we want the both the server cockpits and its meta to be put in the clients
		//meta map, so that they can be called when the block gets loaded on client
		metaCockpitMap.deserialize(b, updateSenderStateId, isOnServer);
		
		TagSerializableLong2TransformMap cp = new TagSerializableLong2TransformMap();
		cp.deserialize(b, updateSenderStateId, isOnServer);
		metaCockpitMap.putAll(cp);
	}
	
	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] s = tag.getStruct();
		byte version = s[0].getByte();
		metaCockpitMap.putAll((TagSerializableLong2TransformMap)s[1].getValue());
		cockpits.putAll((TagSerializableLong2TransformMap)s[2].getValue());
	}
	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[] { 
				new Tag(Type.BYTE, null, TAG_VERSION),
				new Tag(Type.SERIALIZABLE, null, metaCockpitMap),
				new Tag(Type.SERIALIZABLE, null, cockpits),
				FinishTag.INST
		});
	}
	/**
	 * @return the cockpits
	 */
	public Long2ObjectLinkedOpenHashMap<Transform> getCockpits() {
		return cockpits;
	}


	public void removeCockpit(long absIndex) {
		cockpits.remove(absIndex);
		metaCockpitMap.remove(absIndex);
	}
	public void addCockpit(long absIndex) {
		cockpits.put(absIndex, new Transform(metaCockpitMap.remove(absIndex)));		
	}


	public void initFromNetworkObject(NetworkShip s) {
		for(int i = 0; i < s.cockpitManagerBuffer.getReceiveBuffer().size(); i++) {
			s.cockpitManagerBuffer.getReceiveBuffer().get(i); //nothing to do since it already deserilized directly into here
		}
	}


	public void updateToFullNetworkObject(NetworkShip s) {
		if(cockpits.size() > 0 || metaCockpitMap.size() > 0) {
			s.cockpitManagerBuffer.add(new RemoteCockpitManager(this, s));
		}
	}


	public void handleKeyEvent(KeyEventInterface e, ShipExternalFlightController s) {
		if (!cockpits.isEmpty() && s.getEntered().getType() == ElementKeyMap.CORE_ID) {
			if (e.isTriggered(KeyboardMappings.SWITCH_COCKPIT_PREVIOUS)) {
				if (cockpitIndex == 0) {
					cockpitIndex = -1;
					resetShipViewPos(s);
				} else {
					if (cockpitIndex < 0) {
						cockpitIndex = cockpits.size() - 1;
					} else {
						cockpitIndex--;
					}
					setShipViewPos(s);
				}

			} else if (e.isTriggered(KeyboardMappings.SWITCH_COCKPIT_NEXT)) {
				if (cockpitIndex + 1 >= cockpits.size()) {
					cockpitIndex = -1;
					resetShipViewPos(s);
				} else {
					if (cockpitIndex < 0) {
						cockpitIndex = 0;
					} else {
						cockpitIndex++;
					}
					setShipViewPos(s);
				}
			}
		}
	}


	public StateInterface getState() {
		return c.getState();
	}


	public void onSwitch(ShipExternalFlightController s) {
		if (cockpitIndex >= 0 && cockpitIndex < cockpits.size()) {
			setShipViewPos(s);
		} else {
			cockpitIndex = -1;
			resetShipViewPos(s);
		}		
	}
	private void resetShipViewPos(ShipExternalFlightController s) {
		System.out.println("SHIPCAMERA: resetShipViewPos called");
		InShipControlManager inShipControlManager = s.getState().getGlobalGameControlManager().
				getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
		s.shipCamera = new InShipCamera(inShipControlManager.getShipControlManager(), Controller.getCamera(), s.getEntered());
		s.shipCamera.setCameraStartOffset(0f);
		Controller.setCamera(s.shipCamera);
		((InShipCamera) s.shipCamera).docked =
				s.getEntered().getSegmentController().getDockingController().isDocked() || s.getEntered().getSegmentController().railController.isDockedOrDirty();

	}
	private void setShipViewPos(ShipExternalFlightController s) {
		long cockpitVal = getCurrentCockpitBlock();
		Vector3i v = ElementCollection.getPosFromIndex(cockpitVal, new Vector3i());

		SegmentPiece pointUnsave;
		pointUnsave = s.getShip().getSegmentBuffer().getPointUnsave(v);
		if (pointUnsave != null) {
			InShipControlManager inShipControlManager = s.getState().getGlobalGameControlManager().
					getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
			s.shipCamera = new InShipCamera(inShipControlManager.getShipControlManager(), Controller.getCamera(), pointUnsave);
			s.shipCamera.setCameraStartOffset(0f);
			Controller.setCamera(s.shipCamera);

			v.x -= Segment.HALF_DIM;
			v.y -= Segment.HALF_DIM;
			v.z -= Segment.HALF_DIM;
			
			((ShipOffsetCameraViewable) s.shipCamera.getViewable()).getPosMod().set(v);

			
			Transform t = getTransform(s.getState().getPlayer().getCockpit().block);
			if (!s.getState().getPlayer().getCockpit().equalsBlockPos(v)) {
				s.getState().getPlayer().getCockpit().changeBlockClient(v, t, s.shipCamera);
			}

			
			
		} else {
			resetCockpitIndex();
			resetShipViewPos(s);
		}

	}



	public void updateLocal(ShipExternalFlightController s, Camera shipCamera, Timer timer) {
		if (cockpitIndex >= 0) {
			cockpitTmp.set(((ShipOffsetCameraViewable) shipCamera.getViewable()).getPosMod());

			cockpitTmp.x += Segment.HALF_DIM;
			cockpitTmp.y += Segment.HALF_DIM;
			cockpitTmp.z += Segment.HALF_DIM;
			
			if ( !cockpits.containsKey(ElementCollection.getIndex(cockpitTmp))) {
				cockpitIndex = -1;
				InShipControlManager inShipControlManager = s.getState().getGlobalGameControlManager().
						getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager();
				Vector3i v = inShipControlManager.getEntered().getAbsolutePos(new Vector3i());
				
				v.x -= Segment.HALF_DIM;
				v.y -= Segment.HALF_DIM;
				v.z -= Segment.HALF_DIM;
				
				((ShipOffsetCameraViewable) shipCamera.getViewable()).getPosMod().set(v);
				
			}
			if (!s.getState().getPlayer().getCockpit().equalsBlockPos(cockpitTmp)) {
				Transform t = getTransform(ElementCollection.getIndex(cockpitTmp));
				s.getState().getPlayer().getCockpit().changeBlockClient(cockpitTmp, t, s.shipCamera);
			}
		} else {
			if (!s.getState().getPlayer().getCockpit().equalsBlockPos(Ship.core)) {
				Transform t = getTransform(ElementCollection.getIndex(Ship.core));
				s.getState().getPlayer().getCockpit().changeBlockClient(Ship.core, t, s.shipCamera);
			}
		}		
	}

	public Transform getTransform(long block) {
		Transform t = cockpits.get(block);
		if(t == null) {
			t = new Transform();
			t.setIdentity();
		}else {
			t = new Transform(t);
		}
		return t;
	}
	public long getCurrentCockpitBlock() {
		if(cockpitIndex < 0 || cockpits.isEmpty() || (cockpitIndex >= cockpits.size())) {
			return ElementCollection.getIndex(Ship.core);
		}
		LongBidirectionalIterator iterator = cockpits.keySet().iterator();
		
		
		long val = iterator.nextLong();
		for(int i = 0; i < cockpitIndex; i++) {
			val = iterator.nextLong();
		}
		return val;
	}


	public void resetCockpitIndex() {
		cockpitIndex = -1;		
	}


	public void setTransformFor(Cockpit cockpit, Transform trans) {
		cockpits.put(cockpit.block, new Transform(trans));
		cockpit.changeBlockClient(ElementCollection.getPosFromIndex(cockpit.block, new Vector3i()), new Transform(trans), null);
	}


	public void updateFromNetworkObject() {
		//check for changes
		ObjectArrayList<RemoteLongTransformationPair> r = c.getSegmentController().getNetworkObject().cockpitManagerUpdateBuffer.getReceiveBuffer();
		for(int i = 0; i < r.size(); i++) {
			LongTransformPair lt = r.get(i).get();
			cockpits.put(lt.l, new Transform(lt.t));
			if(c.isOnServer()) {
				//deligate to clients
				send(lt.l, lt.t);
			}
		}
	}
	public void send(long block, Transform t) {
		c.getSegmentController().getNetworkObject().cockpitManagerUpdateBuffer.add(new RemoteLongTransformationPair(new LongTransformPair(block, new Transform(t)), c.getSegmentController().getNetworkObject()));
	}

}
