package org.schema.game.common.controller.elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.schema.common.util.data.DataUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.schine.resource.FileExt;

public abstract class ModuleStatistics<R extends SegmentController, E extends ManagerContainer<R>> {

	protected final R segmentController;
	protected final E managerContainer;
	public EntityIndexScore totalRealScore;
	public EntityIndexScore totalPotentialScore;

	public ModuleStatistics(R segmentController, E managerContainer) {
		this.segmentController = segmentController;
		this.managerContainer = managerContainer;
	}

	public abstract double calculateDangerIndex();

	public abstract double calculateMobilityIndex();

	public abstract double calculateSurvivabilityIndex();

	public abstract CompiledScript getCompiledScript();

	public abstract void setCompiledScript(CompiledScript c);

	public abstract long getScriptChanged();

	public abstract void setScriptChanged(long c);

	public EntityIndexScore calculateIndex() throws Exception {

		File f = new FileExt("." + File.separator + DataUtil.dataPath + "script" + File.separator + getScript());

		CompiledScript compiledScript = getCompiledScript();
		if (compiledScript == null || getScriptChanged() < f.lastModified()) {
			System.err.println("[LUA] LOADING: " + getScript());
			Globals globals = JsePlatform.standardGlobals();
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine scriptEngine = mgr.getEngineByName("luaj");

			BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
			compiledScript = ((Compilable) scriptEngine).compile(bufferedReader);

			setScriptChanged(f.lastModified());
			setCompiledScript(compiledScript);
			bufferedReader.close();
		}
		Bindings sb = new SimpleBindings();
		compiledScript.eval(sb); // Put the Lua functions into the sb environment
		LuaValue self = CoerceJavaToLua.coerce(this); // Java to Lua
		LuaValue binding = CoerceJavaToLua.coerce(sb); // Java to Lua

		LuaFunction calcLuaFunc = (LuaFunction) sb.get("calcuateScore"); // Get Lua function

		LuaValue b = calcLuaFunc.call(self, binding); // Call the function
		EntityIndexScore ownScore = (EntityIndexScore) CoerceLuaToJava.coerce(b, EntityIndexScore.class);

		calcRec(calcLuaFunc, ownScore, binding);

		this.totalRealScore = new EntityIndexScore();
		this.totalPotentialScore = new EntityIndexScore();

		LuaFunction rAccum = (LuaFunction) sb.get("accumulateScores");
		rAccum.call(self, b, binding); // Call the function

		LuaFunction rRes = (LuaFunction) sb.get("calculateResultScores");
		rRes.call(self, binding); // Call the function

		
		System.err.println("SAVED BLUEPRINT SCORE: "+totalRealScore);
		
		return totalRealScore;

	}
//	public static final Object2ObjectOpenHashMap<Class<? extends ModuleStatistics>, CompiledScript> scripts = new Object2ObjectOpenHashMap<Class<? extends ModuleStatistics>, CompiledScript>();

	void calcRec(LuaFunction calcLuaFunc,
	             EntityIndexScore in, LuaValue binding) {
		for (RailRelation e : segmentController.railController.next) {
			SegmentController sm = e.docked.getSegmentController();

			if (sm instanceof Ship) {
				ShipManagerModuleStatistics statisticsManager = ((Ship) sm).getManagerContainer().getStatisticsManager();
				LuaValue statistics = CoerceJavaToLua.coerce(statisticsManager);
				LuaValue b = calcLuaFunc.call(statistics, binding); // Call the function

				EntityIndexScore ret = (EntityIndexScore) CoerceLuaToJava.coerce(b, EntityIndexScore.class);

				in.children.add(ret);

				statisticsManager.calcRec(calcLuaFunc, ret, binding);
			}
		}
	}

	public abstract String getScript();

	public int getTotalElements() {
		return segmentController.getTotalElements();
	}
	public double getHitpoints() {
		return segmentController.getHpController().getMaxHp();
	}

//	public double getArmorHitpoints() {
//		return segmentController.getHpController().getMaxArmorHp();
//	}

	public abstract double getJumpDriveIndex();

	public abstract double getThrust();
	
	public abstract double getMining();

	public abstract double getShields();

	public double getMass() {
		return segmentController.getMass();
	}

	public boolean isDocked() {
		return segmentController.railController.isDockedAndExecuted();
	}

	public boolean isTurret() {
		return segmentController.railController.isDockedAndExecuted() && segmentController.railController.isTurretDocked();
	}

	public double calculateWeaponDamageIndex() {
		double w = 0;
		for (ManagerModule<?, ?, ?> m : managerContainer.modules) {
			w += m.calculateWeaponDamageIndex();
		}

		return w;
	}

	public double calculateWeaponRangeIndex() {
		double w = 0;
		for (ManagerModule<?, ?, ?> m : managerContainer.modules) {
			w += m.calculateWeaponRangeIndex();
		}

		return w;
	}

	public double calculateWeaponHitPropabilityIndex() {
		double w = 0;
		for (ManagerModule<?, ?, ?> m : managerContainer.modules) {
			w += m.calculateWeaponHitPropabilityIndex();
		}

		return w;
	}

	public double calculateWeaponSpecialIndex() {
		double w = 0;
		for (ManagerModule<?, ?, ?> m : managerContainer.modules) {
			w += m.calculateWeaponSpecialIndex();
		}
		return w;
	}

	public double calculateWeaponPowerConsumptionPerSecondIndex() {
		double w = 0;
		for (ManagerModule<?, ?, ?> m : managerContainer.modules) {
			w += m.calculateWeaponPowerConsumptionPerSecondIndex();
		}
		return w;
	}

	public double getPowerRecharge() {
		if (managerContainer instanceof PowerManagerInterface) {
			PowerManagerInterface p = (PowerManagerInterface) managerContainer;

			return p.getPowerAddOn().getRechargeForced();

		}
		return 0;
	}

	public double getMaxPower() {
		if (managerContainer instanceof PowerManagerInterface) {
			PowerManagerInterface p = (PowerManagerInterface) managerContainer;
			return p.getPowerAddOn().getMaxPowerWithoutDock();

		}
		return 0;
	}

	public double calculateSupportIndex() {
		double w = 0;
		for (ManagerModule<?, ?, ?> m : managerContainer.modules) {
			w += m.calculateSupportIndex();
		}
		return w;
	}

	public double calculateStealthIndex(double scoreForConstant) {
		double w = 0;
		for (ManagerModule<?, ?, ?> m : managerContainer.modules) {
			w += m.calculateStealthIndex(scoreForConstant);
		}
		return w;
	}

}
