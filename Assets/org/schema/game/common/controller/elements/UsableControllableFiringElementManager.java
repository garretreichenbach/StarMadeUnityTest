package org.schema.game.common.controller.elements;

import java.util.List;

import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public abstract class UsableControllableFiringElementManager<E extends FiringUnit<E, CM, EM>, CM extends ControlBlockElementCollectionManager<E, CM, EM>, EM extends UsableControllableFiringElementManager<E, CM, EM>>
		extends UsableControllableElementManager<E, CM, EM> implements ManagerReloadInterface {

	public static final Vector4f reloadColor = new Vector4f(0.0F, 0.8F, 1.0F, 0.4F);
	public static final Vector4f disabledColor = new Vector4f(1.0F, 0.64F, 0.54F, 0.4F);
	public static final Vector4f activeColor = new Vector4f(0.0F, 1.0F, 0.2F, 0.4F);

	protected UsableControllableFiringElementManager(short controller, short controlling, SegmentController segmentController) {
		super(controller, controlling, segmentController);
	}

	public double calculateReload(E u) {
		return u.getReloadTimeMs();
	}

	@Override
	public boolean isCheckForUniqueConnections() {
		return true;
	}

	public static interface ReloadListener {
		public String onDischarged(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent);

		public String onReload(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent);

		public String onFull(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent, long controllerPos);

		public void drawForElementCollectionManager(InputState state, Vector3i iconPos, Vector3i iconSize,
		                                            Vector4f reloadcolor, long controllerPos);
	}

	public static class DrawReloadListener implements ReloadListener {

		@Override
		public String onDischarged(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor,
		                           boolean backwards, float percent) {
			drawReload(state, iconPos, iconSize, reloadColor, backwards, percent);
			return null;
		}

		@Override
		public String onReload(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor,
		                       boolean backwards, float percent) {
			drawReload(state, iconPos, iconSize, reloadColor, backwards, percent);
			return null;
		}

		@Override
		public String onFull(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor,
		                     boolean backwards, float percent, long controllerPos) {
			return null;
		}

		@Override
		public void drawForElementCollectionManager(InputState state, Vector3i iconPos, Vector3i iconSize,
		                                            Vector4f reloadcolor, long controllerPos) {

		}

	}

	public class AmmoWeaponDrawReloadListener implements UsableControllableFiringElementManager.ReloadListener {

		private final AmmoCapacityElementManager.WeaponType type;
		private final AmmoCapacityElementManager.WeaponCapacityReloadMode mode;
		private GUITextOverlay chargesText;

		private short lastDraw;

		public AmmoWeaponDrawReloadListener(AmmoCapacityElementManager.WeaponType type, AmmoCapacityElementManager.WeaponCapacityReloadMode mode) {
			this.type = type;
			this.mode = mode;
		}

		@Override
		public String onDischarged(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent) {
			if(chargesText == null) {
				chargesText = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, (InputState) getState());
				chargesText.onInit();
			}
			boolean drawOneCharge = true;
			if(lastDraw != state.getNumberOfUpdate()) {
				drawReload(state, iconPos, iconSize, reloadColor, backwards, percent, drawOneCharge, (int) getSegmentController().getAmmoCapacity(type), (int) getSegmentController().getAmmoCapacityMax(type), -1, chargesText);
				// only draw once
				lastDraw = state.getNumberOfUpdate();
			}
			return null;
		}

		@Override
		public String onReload(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent) {
			drawReload(state, iconPos, iconSize, reloadColor, backwards, percent);
			return null;
		}

		@Override
		public String onFull(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent, long controllerPos) {
			return null;
		}

		@Override
		public void drawForElementCollectionManager(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadcolor, long controllerPos) {
			if(chargesText == null) {
				chargesText = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, (InputState) getState());
				chargesText.onInit();
			}
			boolean drawOneCharge = true;
			boolean drCap = false;
			if(lastDraw != state.getNumberOfUpdate()) {
				drawReload(state, iconPos, iconSize, reloadColor, false, 1, drawOneCharge, (int) getSegmentController().getAmmoCapacity(type), (int) getSegmentController().getAmmoCapacityMax(type), -1, chargesText);
				// only draw once
				lastDraw = state.getNumberOfUpdate();
				drCap = true;
			}
			if(drCap && getSegmentController().getAmmoCapacity(type) < (int) getSegmentController().getAmmoCapacityMax(type) && mode == AmmoCapacityElementManager.WeaponCapacityReloadMode.ALL) {
				chargesText.setTextSimple(Lng.str("%s sec", (int) FastMath.ceil(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer().getAmmoCapacityTimer(type))));
				chargesText.getPos().x -= 10;
				chargesText.getPos().y -= 20;
				drawReloadText(state, iconPos, iconSize, chargesText);
			}
		}
	}

	public static class PrintReloadListener implements ReloadListener {

		@Override
		public String onDischarged(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor,
		                           boolean backwards, float percent) {

			return Lng.str("DISCHARGED");
		}

		@Override
		public String onReload(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor,
		                       boolean backwards, float percent) {
			return Lng.str("RELOADING (%s sec)", StringTools.formatPointZero(percent * 100d));
		}

		@Override
		public String onFull(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor,
		                     boolean backwards, float percent, long controllerPos) {
			return Lng.str("CHARGED");
		}

		@Override
		public void drawForElementCollectionManager(InputState state, Vector3i iconPos, Vector3i iconSize,
		                                            Vector4f reloadcolor, long controllerPos) {

		}

	}

	public String handleReload(Vector3i iconPos, Vector3i iconSize, long controllerPos, ReloadListener r) {
		try {
			boolean backwards = false;
			long time = System.currentTimeMillis();
			CM ec = getCollectionManagersMap().get(controllerPos);
			if(ec.getElementCollections().size() > 16) {
				float smallest = 10000000;
				List<E> elementCollections = ec.getElementCollections();
				final int size = Math.min(16, elementCollections.size());
				for(int i = 0; i < size; i++) {
					E d = elementCollections.get(i);
					if(!d.canUse(time, false)) {
						if(d.isUsingPowerReactors()) {
							if(d.getReactorReloadNeededFull() > 0) {
								float percent = 1.0f - (float) (d.getReactorReloadNeeded() / d.getReactorReloadNeededFull());
								smallest = Math.min(smallest, percent);
							}
						} else {
							if(d.getCurrentReloadTime() > 0) {
								int duration = (int) (d.getNextShoot() - time);
								float percent = ((float) duration / (float) d.getCurrentReloadTime());
								smallest = Math.min(smallest, percent);
							}
						}
					}
				}
				float percent = 1.0f - smallest;
				if(percent <= 0.000001) {
					return r.onDischarged((InputState) getState(), iconPos, iconSize, disabledColor, false, 1);
				} else if(smallest < 10000000) {
					return r.onReload((InputState) getState(), iconPos, iconSize, reloadColor, backwards, percent);
				} else {
					return r.onFull((InputState) getState(), iconPos, iconSize, reloadColor, backwards, 1f, controllerPos);
				}
			} else {
				List<E> elementCollections = ec.getElementCollections();
				final int size = elementCollections.size();
				String s = null;
				for(int i = 0; i < size; i++) {
					E d = elementCollections.get(i);

					if(!d.canUse(time, false)) {
						if(d.isUsingPowerReactors()) {
							if(d.getReactorReloadNeededFull() > 0) {
								float percent = 1.0f - (float) (d.getReactorReloadNeeded() / d.getReactorReloadNeededFull());

								if(percent <= 0.000001) {
									s = r.onDischarged((InputState) getState(), iconPos, iconSize, disabledColor, false, 1);
								} else {
									s = r.onReload((InputState) getState(), iconPos, iconSize, reloadColor, backwards, percent);
								}
							}
						} else {
							if(d.getCurrentReloadTime() > 0) {

								int duration = (int) (d.getNextShoot() - time);
								float percent = 1.0f - ((float) duration / (float) d.getCurrentReloadTime());
								s = r.onReload((InputState) getState(), iconPos, iconSize, reloadColor, backwards, percent);
							}
						}

					}
				}

				if(s != null) {
					return s;
				} else {
					return r.onFull((InputState) getState(), iconPos, iconSize, reloadColor, backwards, 1f, controllerPos);
				}
			}
		} finally {
			r.drawForElementCollectionManager((InputState) getState(), iconPos, iconSize, reloadColor, controllerPos);
		}
	}

	private static final DrawReloadListener drawReloadListener = new DrawReloadListener();
	private static final PrintReloadListener printReloadListener = new PrintReloadListener();

	@Override
	public String getReloadStatus(long id) {
		return handleReload(null, null, id, printReloadListener);
	}

	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		handleReload(iconPos, iconSize, controllerPos, drawReloadListener);
	}

	@Override
	public int getCharges() {
		return 0;
	}

	@Override
	public int getMaxCharges() {
		return 0;
	}

	public void onKilledBlock(long pos, short type, Damager from) {
		if(getSegmentController().isOnServer()) {
			assert (this instanceof BlockKillInterface);
			if(lowestIntegrity < VoidElementManager.INTEGRITY_MARGIN) {
				List<CM> cmList = getCollectionManagers();
				final int size = cmList.size();
				for(int i = 0; i < size; i++) {
					CM cm = cmList.get(i);
					cm.checkIntegrity(pos, type, from);
				}
			}
		}
	}

	public boolean isHandlingActivationForType(short type) {
		return type == controllingId;
	}
}


