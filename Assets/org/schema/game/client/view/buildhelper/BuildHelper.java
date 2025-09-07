package org.schema.game.client.view.buildhelper;

import java.lang.reflect.Field;
import java.util.Locale;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerThreadProgressInput;
import org.schema.game.client.controller.ThreadCallback;
import org.schema.game.client.controller.manager.ingame.BuildToolsManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advanced.tools.SliderCallback;
import org.schema.game.client.view.gui.advanced.tools.SliderResult;
import org.schema.game.client.view.gui.advancedbuildmode.AdvancedBuildModeGUISGroup;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public abstract class BuildHelper implements Runnable, ThreadCallback {

	public final Transform localTransform = new Transform();

	public final Transformable transformable;

	public boolean placed;

	public Vector3i placedPos = new Vector3i();

	protected float percent;

	private boolean finished;

	private boolean initialized;

	public LongIterator iterator;

	protected int buffer;

	public BuildHelper(Transformable transformable) {
		super();
		this.transformable = transformable;
		localTransform.setIdentity();
	}

	public abstract BuildHelperFactory getType();

	public void reset() {
		synchronized (this) {
			iterator = null;
		}
		localTransform.setIdentity();
		finished = false;
		placed = false;
		percent = 0;
	}

	public abstract void create();

	protected abstract void drawLocal();

	public void draw() {
		if (initialized && finished && buffer != 0) {
			GlUtil.glPushMatrix();
			GlUtil.glMultMatrix(transformable.getWorldTransform());
			GlUtil.glMultMatrix(localTransform);
			drawLocal();
			GlUtil.glPopMatrix();
		}
	}

	private boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public abstract void clean();

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		create();
	}

	@Override
	public float getPercent() {
		return percent;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.ThreadCallback#isFinished()
	 */
	@Override
	public boolean isFinished() {
		return finished;
	}

	public abstract LongOpenHashSet getPoses();

	public boolean contains(int x, int y, int z) {
		long index = ElementCollection.getIndex(x - placedPos.x, y - placedPos.y, z - placedPos.z);
		return getPoses().contains(index);
	}

	public boolean contains(long blockIndex) {
		int x = ElementCollection.getPosX(blockIndex);
		int y = ElementCollection.getPosY(blockIndex);
		int z = ElementCollection.getPosZ(blockIndex);
		long index = ElementCollection.getIndex(x - placedPos.x, y - placedPos.y, z - placedPos.z);
		return getPoses().contains(index);
	}

	public void showProcessingDialog(final GameClientState state, final BuildToolsManager buildToolsManager, boolean placeMode) {
		finished = false;
		clean();
		Thread t = new Thread(this);
		t.start();
		(new PlayerThreadProgressInput("BuildToolsPanel_CALCULATING", state, Lng.str("Calculating Shape"), Lng.str("calculating..."), this) {

			@Override
			public void onDeactivate() {
				// make OpenGL
				System.err.println("[CLIENT] dialog->onFinished()->start(): " + BuildHelper.this);
				BuildHelper.this.onFinished();
				System.err.println("[CLIENT] dialog->onFinished()->end(): " + BuildHelper.this);
				buildToolsManager.setBuildHelper(BuildHelper.this);
				System.err.println("[CLIENT] using build helper: " + BuildHelper.this);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void pressedOK() {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(299);
	}

	public void onPressedOk(BuildToolsManager buildToolsManager) {
		showProcessingDialog(buildToolsManager.getState(), buildToolsManager, false);
	}

	public void onPressedOk(PlayerGameOkCancelInput playerGameOkCancelInput, BuildToolsManager buildToolsManager) {
		playerGameOkCancelInput.deactivate();
		showProcessingDialog(playerGameOkCancelInput.getState(), buildToolsManager, false);
	}

	public GUIElement getPanel(final GameClientState state, GUIContentPane pane, final AdvancedBuildModeGUISGroup group) {
		GUIAnchor container = pane.getContent(1);
		group.removeAllFrom(container);
		Field[] fields = getClass().getFields();
		GUIElementList list = new GUIElementList(state);
		int n = 0;
		for (int i = 0; i < fields.length; i++) {
			final Field f = fields[i];
			final BuildHelperVar annotation = f.getAnnotation(BuildHelperVar.class);
			if (annotation != null) {
				if (annotation.type().toLowerCase(Locale.ENGLISH).equals("float")) {
					group.addSlider(container, 0, n++, new SliderResult() {

						@Override
						public SliderCallback initCallback() {
							return value -> {
								try {
									f.setFloat(BuildHelper.this, value);
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								}
							};
						}

						@Override
						public String getToolTipText() {
							return Lng.str("Customize build helper");
						}

						@Override
						public String getName() {
							return annotation.name().toString();
						}

						@Override
						public float getMin() {
							return annotation.min();
						}

						@Override
						public float getMax() {
							return annotation.max();
						}

						@Override
						public float getDefault() {
							try {
								return f.getFloat(BuildHelper.this);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
							return 0;
						}
					});
				}
			}
		}
		return null;
	}

	public void setFinished(boolean finished) {
		// try {
		// throw new Exception("FINISHED: "+finished+"; "+this.getClass().getSimpleName());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		this.finished = finished;
	}

	public void recreateIterator() {
		synchronized (this) {
			if (iterator != null) {
				iterator = getPoses().iterator();
			}
		}
	}

	public void iterate(LongOpenHashSet open, LongArrayList openList) {
		synchronized (this) {
			if (iterator == null) {
				iterator = getPoses().iterator();
			}
			while (iterator.hasNext()) {
				long pIn = iterator.nextLong();
				int x = ElementCollection.getPosX(pIn) + placedPos.x;
				int y = ElementCollection.getPosY(pIn) + placedPos.y;
				int z = ElementCollection.getPosZ(pIn) + placedPos.z;
				long cPos = ElementCollection.getIndex(x, y, z);
				if (!open.contains(cPos)) {
					open.add(cPos);
					openList.add(cPos);
					break;
				}
			}
		}
	}
}
