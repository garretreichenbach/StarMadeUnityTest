package org.schema.game.client.controller.manager.ingame.navigation;

import org.schema.game.common.controller.*;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.space.PlanetIcoCore;

public abstract class NavigationFilter {

	public static final long POW_SHOP = 1;
	public static final long POW_SPACESTATION = 4;
	public static final long POW_FLOATINGROCK = 8;
	public static final long POW_PLANET = 16;
	public static final long POW_SHIP = 32;
	public static final long POW_PLAYER = 64;
	public static final long POW_TURRET = 128;
	public static final long POW_DOCKED = 256;
	public static final long POW_PLANET_CORE = 512;

	public NavigationFilter() {

	}

	public NavigationFilter(NavigationFilter c) {
		this.setFilter(c.getFilter());
	}

	public boolean isFiltered(long f) {
		return (getFilter() & f) == f;
	}

	public boolean isDisplayed(SimpleTransformableSendableObject s) {
		if(getFilter() == Long.MAX_VALUE) {
			return true;
		} else if(getFilter() == 0) {
			return false;
		} else if(s instanceof Ship && (getFilter() & POW_SHIP) == POW_SHIP) {

			if(!((Ship) s).railController.isDockedAndExecuted() && !((Ship) s).getDockingController().isDocked()) {
				return true;
			} else if(((Ship) s).railController.isDockedAndExecuted() && ((Ship) s).railController.isTurretDocked() && (getFilter() & POW_TURRET) == POW_TURRET) {
				return true;
			} else if(((Ship) s).railController.isDockedAndExecuted() && !((Ship) s).railController.isTurretDocked() && (getFilter() & POW_DOCKED) == POW_DOCKED) {
				return true;
			} else {
				((Ship) s).getDockingController();
				if(((Ship) s).getDockingController().isDocked() && DockingController.isTurretDocking((((Ship) s).getDockingController().getDockedOn().to)) && (getFilter() & POW_TURRET) == POW_TURRET) {
					return true;
				} else {
					((Ship) s).getDockingController();
					if(((Ship) s).getDockingController().isDocked() && !DockingController.isTurretDocking((((Ship) s).getDockingController().getDockedOn().to)) && (getFilter() & POW_DOCKED) == POW_DOCKED) {
						return true;
					}
				}
			}
			return false;
		} else if(s instanceof Ship && !((Ship) s).getDockingController().isDocked() && !((Ship) s).railController.isDockedAndExecuted() && (getFilter() & POW_SHIP) == POW_SHIP) {
			return true;
		} else if(s instanceof Ship && (getFilter() & POW_SHIP) == POW_SHIP) {
			return true;
		} else if(s instanceof AbstractCharacter<?> && (getFilter() & POW_PLAYER) == POW_PLAYER) {
			return true;
		} else if(s instanceof FloatingRock && (getFilter() & POW_FLOATINGROCK) == POW_FLOATINGROCK) {
			return true;
		} else if((s instanceof PlanetIco || s instanceof GasPlanet) && (getFilter() & POW_PLANET) == POW_PLANET) {
			return true;
		} else if(s instanceof ShopSpaceStation && (getFilter() & POW_SHOP) == POW_SHOP) {
			return true;
		} else if(s instanceof SpaceStation && (getFilter() & POW_SPACESTATION) == POW_SPACESTATION) {
			return true;
		} else return ((s instanceof PlanetIcoCore || s instanceof GasPlanet) && (getFilter() & POW_PLANET_CORE) == POW_PLANET_CORE);
	}

	public void setFilter(boolean b, long filter) {
		if(b) {
			setFilter(this.getFilter() | filter);
		} else {
			setFilter(this.getFilter() & ~filter);
		}
	}

	/**
	 * @return the filter
	 */
	public abstract long getFilter();

	/**
	 * @param filter the filter to set
	 */
	public abstract void setFilter(long filter);

}
