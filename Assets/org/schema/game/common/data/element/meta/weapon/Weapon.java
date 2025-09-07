package org.schema.game.common.data.element.meta.weapon;

import org.schema.common.FastMath;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;

import javax.vecmath.Vector3f;

public abstract class Weapon extends MetaObject {
	public enum WeaponSubType {
		LASER((short) 1),
		HEAL((short) 2),
		POWER_SUPPLY((short) 3),
		MARKER((short) 4),
		ROCKET_LAUNCHER((short) 5),
		SNIPER_RIFLE((short) 6),
		GRAPPLE((short) 7),
		TORCH((short) 8),
		TRANSPORTER_MARKER((short) 9);

		public final short type;

		WeaponSubType(short type) {
			this.type = type;
		}

		public static WeaponSubType getById(short id) {
			for(WeaponSubType t : values()) {
				if(t.type == id) {
					return t;
				}
			}
			throw new NullPointerException("Illegal sub type: " + id);
		}

		public static short[] getTypes() {
			short[] st = new short[values().length];
			for(int i = 0; i < st.length; i++) {
				WeaponSubType t = values()[i];
				st[i] = t.type;
			}
			return st;
		}
	}

	protected final InterEffectSet attackEffectSet;
	private short type;

	public long getWeaponUsableId() {
		return getId();
	}

	public Weapon(int id, short type) {
		super(id);
		this.type = type;
		this.attackEffectSet = new InterEffectSet();
		setupEffectSet(attackEffectSet);
	}

	protected abstract void setupEffectSet(InterEffectSet s);

	public static Weapon instantiate(int id, short type) {
		return instantiate(id, WeaponSubType.getById(type));
	}

	@Override
	public short[] getSubTypes() {
		return WeaponSubType.getTypes();
	}

	public static Weapon instantiate(int id, WeaponSubType type) {
		return switch(type) {
			case LASER -> new LaserWeapon(id);
			case HEAL -> new HealBeam(id);
			case MARKER -> new MarkerBeam(id);
			case POWER_SUPPLY -> new PowerSupplyBeam(id);
			case ROCKET_LAUNCHER -> new RocketLauncherWeapon(id);
			case SNIPER_RIFLE -> new SniperRifle(id);
			case GRAPPLE -> new GrappleBeam(id);
			case TORCH -> new TorchBeam(id);
			case TRANSPORTER_MARKER -> new TransporterBeaconBeam(id);
		};
		//		throw new IllegalArgumentException("type not known: " + type);
	}

	public abstract void fire(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, boolean addButton, boolean removeButton, Timer timer);

	public abstract void fire(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, Vector3f dir, boolean addButton, boolean removeButton, Timer timer);

	@Override
	public DialogInput getEditDialog(GameClientState state, final AbstractControlManager parent, Inventory openedFrom) {
		return new PlayerGameOkCancelInput("Weapon_getEditDialog", state, Lng.str("Item"), toDetailedString()) {
			@Override
			public boolean isOccluded() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public void pressedOK() {
				deactivate();
			}
		};
	}

	@Override
	public MetaObjectType getObjectBlockType() {
		return MetaObjectType.WEAPON;
	}

	@Override
	public int getPermission() {
		return NO_EDIT_PERMISSION;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.meta.MetaObject#getSubObjectId()
	 */
	@Override
	public short getSubObjectId() {
		assert (type != 0);
		return type;
	}

	public WeaponSubType getSubObjectType() {
		return WeaponSubType.getById(getSubObjectId());
	}

	@Override
	public boolean isValidObject() {
		return true;
	}

	@Override
	public int getExtraBuildIconIndex() {
		return type - 1;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.meta.MetaObject#drawPossibleOverlay(org.schema.game.client.view.gui.inventory.InventorySlotOverlayElement)
	 */
	@Override
	public void drawPossibleOverlay(GUIOverlay reload, Inventory inventory) {
		if(getSubObjectId() == WeaponSubType.ROCKET_LAUNCHER.type) {
			RocketLauncherWeapon r = (RocketLauncherWeapon) this;
			long nextShot = r.getVol_lastShot() + r.reload;
			if(System.currentTimeMillis() - r.getVol_lastShot() < r.reload) {
				int base = 2;
				float max = 8;
				float percent = 0;
				int duration = (int) (nextShot - System.currentTimeMillis());
				percent = 1.0f - ((float) duration / (float) r.reload);
				int sprite = base;
				if(duration > 0) {
					//							System.err.println("duration "+duration+" "+d.getCurrentReloadTime()+"; %"+percent+"; "+(base + percent * max)+" --> "+FastMath.clamp(base + percent * max, base, base+max));
					sprite = (int) FastMath.floor(FastMath.clamp(base + percent * max, base, base + max));
				}
				reload.setSpriteSubIndex(sprite);
				reload.draw();
			}
		} else if(getSubObjectId() == WeaponSubType.SNIPER_RIFLE.type) {
			SniperRifle r = (SniperRifle) this;
			long nextShot = r.reloadCallback.getNextShoot();
			if(System.currentTimeMillis() < nextShot) {
				int base = 2;
				float max = 8;
				float percent = 0;
				int duration = (int) (nextShot - System.currentTimeMillis());
				percent = 1.0f - ((float) duration / (float) r.reloadCallback.getCurrentReloadTime());
				int sprite = base;
				if(duration > 0) {
					//							System.err.println("duration "+duration+" "+d.getCurrentReloadTime()+"; %"+percent+"; "+(base + percent * max)+" --> "+FastMath.clamp(base + percent * max, base, base+max));
					sprite = (int) FastMath.floor(FastMath.clamp(base + percent * max, base, base + max));
				}
				reload.setSpriteSubIndex(sprite);
				reload.draw();
			}
		} else if(getSubObjectId() == WeaponSubType.GRAPPLE.type) {
			GrappleBeam r = (GrappleBeam) this;
			long nextShot = r.reloadCallback.getNextShoot();
			if(System.currentTimeMillis() < nextShot) {
				int base = 2;
				float max = 8;
				float percent = 0;
				int duration = (int) (nextShot - System.currentTimeMillis());
				percent = 1.0f - ((float) duration / (float) r.reloadCallback.getCurrentReloadTime());
				int sprite = base;
				if(duration > 0) {
					//							System.err.println("duration "+duration+" "+d.getCurrentReloadTime()+"; %"+percent+"; "+(base + percent * max)+" --> "+FastMath.clamp(base + percent * max, base, base+max));
					sprite = (int) FastMath.floor(FastMath.clamp(base + percent * max, base, base + max));
				}
				reload.setSpriteSubIndex(sprite);
				reload.draw();
			}
		} else if(getSubObjectId() == WeaponSubType.TORCH.type) {
			TorchBeam r = (TorchBeam) this;
			long nextShot = r.reloadCallback.getNextShoot();
			if(System.currentTimeMillis() < nextShot) {
				int base = 2;
				float max = 8;
				float percent = 0;
				int duration = (int) (nextShot - System.currentTimeMillis());
				percent = 1.0f - ((float) duration / (float) r.reloadCallback.getCurrentReloadTime());
				int sprite = base;
				if(duration > 0) {
					//							System.err.println("duration "+duration+" "+d.getCurrentReloadTime()+"; %"+percent+"; "+(base + percent * max)+" --> "+FastMath.clamp(base + percent * max, base, base+max));
					sprite = (int) FastMath.floor(FastMath.clamp(base + percent * max, base, base + max));
				}
				reload.setSpriteSubIndex(sprite);
				reload.draw();
			}
		}
	}

	@Override
	public boolean drawUsingReloadIcon() {
		return true;
	}

	protected abstract String toDetailedString();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(getSubObjectType() == WeaponSubType.LASER) {
			return Lng.str("Laser\n(right click to view)");
		} else if(getSubObjectType() == WeaponSubType.HEAL) {
			return Lng.str("Healing Beam\n(right click to view)");
		} else if(getSubObjectType() == WeaponSubType.MARKER) {
			return Lng.str("Marker Beam\n%s\n(right click to view)", ((MarkerBeam) this).realName);
		} else if(getSubObjectType() == WeaponSubType.POWER_SUPPLY) {
			return Lng.str("Power Supply Beam\n(right click to view)");
		} else if(getSubObjectType() == WeaponSubType.ROCKET_LAUNCHER) {
			return Lng.str("Rocket Launcher\n(right click to view)");
		} else if(getSubObjectType() == WeaponSubType.SNIPER_RIFLE) {
			return Lng.str("Sniper Rifle\n(right click to view)");
		} else if(getSubObjectType() == WeaponSubType.GRAPPLE) {
			return Lng.str("Grapple\n(right click to view)");
		} else if(getSubObjectType() == WeaponSubType.TORCH) {
			return Lng.str("Torch\n(right click to view)");
		} else if(getSubObjectType() == WeaponSubType.TRANSPORTER_MARKER) {
			return Lng.str("Transporter Beacon\n%s\n(right click to view)", ((TransporterBeaconBeam) this).realName);
		} else {
			return Lng.str("unknown weapon type (%s)", getSubObjectType());
		}
	}

	public InterEffectSet getEffectSet() {
		return attackEffectSet;
	}

	public MetaWeaponEffectInterface getMetaWeaponEffect() {
		return null;
	}

	public HitType getHitType() {
		return HitType.WEAPON;
	}

	public boolean isIgnoringShields() {
		return false;
	}
}
