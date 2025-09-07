package org.schema.schine.tools.curve;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;

public class SplineDisplay extends EquationDisplay {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<SplineCurve> splines = new ArrayList<SplineCurve>();
	public MouseEvent lastEvent;
	Point2D selected = null;

	public SplineDisplay(PSCurveVariable[] var) {
		super(0.0, 0.0,
				0, 1, -1, 1,
				0.5, 2,
				0.5, 2);
		for (int i = 0; i < var.length; i++) {
			splines.add(new SplineCurve(this, var[i]));
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.tools.curve.EquationDisplay#paintInformation(java.awt.Graphics2D)
	 */
	@Override
	protected void paintInformation(Graphics2D g2) {
		super.paintInformation(g2);

		for (int i = 0; i < splines.size(); i++) {
			splines.get(i).paint(g2);
		}
	}

}
