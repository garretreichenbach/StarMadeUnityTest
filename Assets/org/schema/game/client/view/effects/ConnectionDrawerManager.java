package org.schema.game.client.view.effects;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.*;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import java.util.Map.Entry;

public class ConnectionDrawerManager implements Drawable {

	private final Object2ObjectOpenHashMap<SegmentController, ConnectionDrawer> map = new Object2ObjectOpenHashMap<SegmentController, ConnectionDrawer>();
	private final GameClientState state;

	private float conTime;

	public ConnectionDrawerManager(GameClientState state) {
		this.state = state;
	}

	@Override
	public void cleanUp() {
		for(ConnectionDrawer e : map.values()){
			e.cleanUp();
		}
	}

	@Override
	public void draw() {

		ShaderLibrary.tubesShader.loadWithoutUpdate();
		double serverRunningTime = state.getController().getServerRunningTime() % 5000;

		float t = (float) (serverRunningTime / 5000);
		GlUtil.updateShaderFloat(ShaderLibrary.tubesShader, "time", t);
		for (ConnectionDrawer s : map.values()) {
			if (!s.getSegmentController().isCloakedFor(state.getCurrentPlayerObject())) {
				s.draw();
			}
		}

		ShaderLibrary.tubesShader.unloadWithoutExit();
		GlUtil.glColor4fForced(1, 1, 1, 1);

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {

	}

	public void update(Timer timer) {
		conTime += timer.getDelta() * 2f;
		conTime -= (int) conTime;
	}

	public void onConnectionChanged(SendableSegmentController sendableSegmentController) {
		ConnectionDrawer connectionDrawer = map.get(sendableSegmentController);
		if (connectionDrawer != null) {
			connectionDrawer.flagUpdate();
		} else {
			System.err.println("[CLIENT][ConnectionDrawer] WARNING segController to update not found!!!!!!!!!!! searching " + sendableSegmentController);
		}
	}

	public void updateEntities() {
		for (SimpleTransformableSendableObject s : state.getCurrentSectorEntities().values()) {
			if (s instanceof Planet || s instanceof PlanetIco || s instanceof SpaceStation || s instanceof Ship) {
				if (!map.containsKey(s)) {
					ConnectionDrawer connectionDrawer = new ConnectionDrawer((SegmentController) s);
					map.put((SegmentController) s, connectionDrawer);
				}
			}
		}
		ObjectIterator<Entry<SegmentController, ConnectionDrawer>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<SegmentController, ConnectionDrawer> s = iterator.next();
			if (s.getKey().getSectorId() != state.getCurrentSectorId() || !s.getKey().getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(s.getKey().getId())) {
				ConnectionDrawer connectionDrawer = map.get(s);
				if(connectionDrawer != null){
					connectionDrawer.cleanUp();
				}
				iterator.remove();
			}
		}

	}

	public void clear() {
		cleanUp();
		map.clear();
	}

}
