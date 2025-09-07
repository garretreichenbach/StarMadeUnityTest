package org.schema.game.client.view.shards;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.client.view.shader.CubeMeshQuadsShader13;
import org.schema.game.client.view.shader.CubeMeshQuadsShader13.CubeTexQuality;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.physics.RigidDebrisBody;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.shader.ShadowParams;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Map.Entry;

public class ShardDrawer implements Shaderable {

	public static int shardsAddedFromNTBlocks;
	public int slow;
	Object2ObjectOpenHashMap<Mesh, ObjectArrayList<Shard>> shards = new Object2ObjectOpenHashMap<Mesh, ObjectArrayList<Shard>>();
	private ShadowParams shadowParams;
	private ObjectArrayList<DelayedShard> shardsToAdd = new ObjectArrayList<DelayedShard>();

	private Vector3f tmpGravity = new Vector3f();
	private int shardsCurrent;

	public void voronoiBBShatter(PhysicsExt physics, Transform position, short s, int sectorId, Vector3f center, SimpleTransformableSendableObject gravity) {
		if (EngineSettings.D_LIFETIME_NORM.getInt() <= 0) {
			return;
		}
		if (s > 0) {
			Mesh smesh = Controller.getResLoader().getMesh("Debris00");
			for (int i = 0; i < smesh.getChilds().size(); i++) {
				Mesh mesh = (Mesh) smesh.getChilds().get(i);
				Transform shardTransform = new Transform(position);

				Vector3f initial = new Vector3f(mesh.getInitionPos());
				initial.scale(0.5f);
				shardTransform.basis.transform(initial);
				shardTransform.origin.add(initial);

				if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
					DebugPoint debugPoint = new DebugPoint(shardTransform.origin, new Vector4f(1, 0, 1, 1), 0.6f);
					debugPoint.LIFETIME = 10000;
					DebugDrawer.points.add(debugPoint);
				}
				mesh.shape.setMargin(0);
				mesh.shape.setUserPointer("shard");

				RigidDebrisBody bodyFromShape = (RigidDebrisBody) physics.getBodyFromShape(mesh.shape, 0.01f/*volume*matDensity*/, shardTransform);

				bodyFromShape.setRestitution(0.01f);

				if (gravity != null) {
					bodyFromShape.setSleepingThresholds(1.6f, 1.6f);
					tmpGravity.set(0, -9.81f, 0);
					gravity.getWorldTransform().basis.transform(tmpGravity);
					bodyFromShape.setGravity(tmpGravity);
					//					System.err.println("APPLYING GRAVITY: "+tmpGravity);
				} else {
					tmpGravity.set(0, 0, 0);
				}

				physics.addObject(bodyFromShape, CollisionFilterGroups.DEBRIS_FILTER, CollisionFilterGroups.ALL_FILTER);

				if (center != null) {
					Vector3f linVel = new Vector3f();
					linVel.sub(shardTransform.origin, center);

					if (linVel.lengthSquared() > 0) {
						linVel.x += (Math.random() - 0.5f) * 0.2f;
						linVel.y += (Math.random() - 0.5f) * 0.2f;
						linVel.z += (Math.random() - 0.5f) * 0.2f;
						linVel.normalize();
						linVel.scale(0.01f);
						bodyFromShape.applyCentralImpulse(linVel);
					}
				}

				GameClientState c = (GameClientState) physics.getState();
				c.getWorldDrawer().addShard(bodyFromShape, mesh, s, sectorId, new Vector3f(tmpGravity));
			}
		} else {
//			System.err.println("[CLIENT][SHATTER] unknown blocktype: "+s);
		}

	}

	public void draw() {
		shardsCurrent = 0;
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		ShaderLibrary.shardShader.setShaderInterface(this);
		ShaderLibrary.shardShader.load();
		GlUtil.glColor4f(1, 1, 1, 1);
		for (Entry<Mesh, ObjectArrayList<Shard>> e : shards.entrySet()) {
			e.getKey().loadVBO(true);

			for (int i = 0; i < e.getValue().size(); i++) {
				shardsCurrent++;
				e.getValue().get(i).drawBulk();
			}

			e.getKey().unloadVBO(true);
		}
		GlUtil.glColor4f(1, 1, 1, 1);

		ShaderLibrary.shardShader.unload();

		GlUtil.glDisable(GL11.GL_BLEND);
	}

	public void add(Shard s) {
		ObjectArrayList<Shard> objectArrayList = shards.get(s.hull);
		if (objectArrayList == null) {
			objectArrayList = new ObjectArrayList();
			shards.put(s.hull, objectArrayList);
		}
		objectArrayList.add(s);
	}

	public void update(Timer timer, GameClientState state) {
		Mesh smesh = Controller.getResLoader().getMesh("Debris00");
		assert(smesh != null);
		assert(((Mesh)smesh.getChilds().get(0)).shape != null);
		if (shardsToAdd.size() > 0) {
			DelayedShard remove = shardsToAdd.remove(0);
			if (slow != 0) {
				ObjectListIterator<DelayedShard> iterator = shardsToAdd.iterator();
				int i = 0;
				while(iterator.hasNext()){
					iterator.next();
					if(i > 0 && i%5 == 0){
						iterator.remove();
					}
					i++;
				}
			}
			remove.spawn();
		}

		for (Entry<Mesh, ObjectArrayList<Shard>> e : shards.entrySet()) {

			for (int i = 0; i < e.getValue().size(); i++) {
				boolean alive = e.getValue().get(i).update(timer, state, slow);
				if (!alive) {
					state.getPhysics().removeObject(e.getValue().get(i).body);
					e.getValue().remove(i);
					i--;
				}
			}

		}
	}
	public void onSimulationStepBurst() {
		for (Entry<Mesh, ObjectArrayList<Shard>> e : shards.entrySet()) {
			for (int i = 0; i < e.getValue().size(); i++) {
				e.getValue().get(i).body.noCollision = true;
			}
		}
	}
	@Override
	public void onExit() {
		CubeMeshQuadsShader13.unbindTextures();
	}

	@Override
	public void updateShader(DrawableScene scene) {
		
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		boolean wasRecompiled = false;
		// ShadowShader.putDepthMatrix(shader, true);
		if (shader.recompiled) {
			wasRecompiled = true;
			shader.recompiled = false;
		}
//		CubeMeshQuadsShader13.uploadMVP(shader);
		CubeMeshQuadsShader13.bindtextures(wasRecompiled, shader, CubeTexQuality.SELECTED);

		GlUtil.updateShaderFloat(shader, "extraAlpha", 1);

		if (shadowParams != null) {
			shadowParams.execute(shader);
		}

		GlUtil.updateShaderFloat(shader, "innerTexId", ElementKeyMap.getInfo(ElementKeyMap.TERRAIN_LAVA_ID).getTextureId(0));
		
		GlUtil.updateShaderVector3f(shader, "viewPos", Controller.getCamera().getPos());
		GlUtil.updateShaderVector3f(shader, "lightPos", MainGameGraphics.mainLight.getPos());
	}

	public void setShadow(ShadowParams shadowParams) {
		this.shadowParams = shadowParams;
	}

	public void voronoiBBShatterDelayed(PhysicsExt physics, Vector3f f,
	                                    short type, SendableSegmentController c, SimpleTransformableSendableObject<?> source) {

		if(shards.size() < 4 || (shardsToAdd.size() < 8 && Math.random() > (slow > 0 ? 0.95f : 0.8f))){
			if (c instanceof Planet || c instanceof PlanetIco) {
				source = c;
			}
			DelayedShard d = new DelayedShard(physics, f, type, c, source);
			shardsToAdd.add(d);
		}

	}

	private class DelayedShard {
		private PhysicsExt physics;
		private Vector3f f;
		private short type;
		private SimpleTransformableSendableObject<?> gravity;
		private SendableSegmentController c;

		public DelayedShard(PhysicsExt physics, Vector3f f, short type,
		                    SendableSegmentController c, SimpleTransformableSendableObject source) {
			super();
			this.physics = physics;
			this.f = f;
			this.type = type;
			this.c = c;
			this.gravity = source;
		}

		public void spawn() {
			c.getWorldTransformOnClient().transform(f);
			Transform tt = new Transform(c.getWorldTransformOnClient());
			tt.origin.set(f);
			voronoiBBShatter(physics, tt, type, c.getSectorId(), tt.origin, gravity);
		}

	}

	public boolean isEmpty() {
		return shardsCurrent == 0;
	}

	
}
