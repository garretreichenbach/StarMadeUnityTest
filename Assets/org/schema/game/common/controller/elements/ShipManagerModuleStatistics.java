package org.schema.game.common.controller.elements;

import java.io.File;

import javax.script.CompiledScript;

import org.schema.game.common.controller.Ship;

public class ShipManagerModuleStatistics extends ModuleStatistics<Ship, ShipManagerContainer> {
	public static long lastModified;
	public static CompiledScript script;

	public ShipManagerModuleStatistics(Ship segmentController,
	                                   ShipManagerContainer managerContainer) {
		super(segmentController, managerContainer);
	}

	@Override
	public double calculateDangerIndex() {
				return 0;
	}

	@Override
	public double calculateMobilityIndex() {
				return 0;
	}

	@Override
	public double calculateSurvivabilityIndex() {
				return 0;
	}

	@Override
	public synchronized CompiledScript getCompiledScript() {
		return script;
	}

	@Override
	public synchronized void setCompiledScript(CompiledScript c) {
		script = c;
	}

	@Override
	public synchronized long getScriptChanged() {
		return lastModified;
	}

	@Override
	public synchronized void setScriptChanged(long c) {
		lastModified = c;
	}

	@Override
	public String getScript() {
		return File.separator + "statistics" + File.separator + "ship-index-calculation.lua";
	}

	@Override
	public double getJumpDriveIndex() {
		return managerContainer.getJumpDrive().getCollectionManagers().size();
	}

	@Override
	public double getThrust() {
		return managerContainer.getThrusterElementManager().getActualThrust();
	}

	@Override
	public double getShields() {
		return managerContainer.getShieldAddOn().getShieldCapacity();
	}

	@Override
	public double getMining() {
		return managerContainer.getSalvage().getElementManager().getMiningScore();
	}

}
