package org.schema.game.common.controller.elements.power;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;

public class PowerUnit extends ElementCollection<PowerUnit, PowerCollectionManager, VoidElementManager<PowerUnit, PowerCollectionManager>> {

	private double recharge;

	private static double f(double a) {
		return Math.max(0.1, 1 - 0.5 * Math.pow((2.0 * (1.0 - a)), 2.5));
	}

	public static double integrate(double a, double b) {
		int N = 10;                    // precision parameter
		double h = (b - a) / (N - 1);     // step size

		// 1/3 terms
		double sum = 1.0 / 3.0 * (f(a) + f(b));

		// 4/3 terms
		for (int i = 1; i < N - 1; i += 2) {
			double x = a + h * i;
			sum += 4.0 / 3.0 * f(x);
		}

		// 2/3 terms
		for (int i = 2; i < N - 1; i += 2) {
			double x = a + h * i;
			sum += 2.0 / 3.0 * f(x);
		}

		return sum * h;
	}
	@Override
	public boolean hasMesh(){
		return false;
	}
	/**
	 * @return the recharge
	 */
	public double getRecharge() {
		return recharge;
	}

	/**
	 * y = 1 - 0.5 * (2.0 * (1.0 - x))^2.5
	 */
	public void refreshPowerCapabilities() {
		recharge = getBBTotalSize();
		recharge = Math.max(0.3333f, Math.pow(recharge / 3d, 1.7d));
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		Vector3i dim = new Vector3i();
		dim.sub(getMax(new Vector3i()), getMin(new Vector3i()));
		return ControllerManagerGUI.create(state, Lng.str("Power Generator Module"), this,
				new ModuleValueEntry(Lng.str("Dimension"), dim),
				new ModuleValueEntry(Lng.str("Recharge"), StringTools.formatPointZero(recharge)));
	}
}