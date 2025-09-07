package org.schema.schine.tools.gradient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.schema.schine.graphicsengine.psys.modules.variable.PSGradientVariable;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap.Entry;
import it.unimi.dsi.fastutil.floats.Float2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

public class LinearGradient extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private PSGradientVariable var;

	public LinearGradient(PSGradientVariable var) {
		super();
		this.var = var;
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		int rectWidth = getWidth();
		int rectHeight = getHeight();

		ObjectBidirectionalIterator<Entry<Color>> iterator = var.color.float2ObjectEntrySet().iterator();
		Entry<Color> prev = iterator.next();

		while (iterator.hasNext()) {
			Entry<Color> next = iterator.next();
			Rectangle2D rect2D = new Rectangle2D.Float(prev.getFloatKey() * rectWidth, 0,
					next.getFloatKey() * rectWidth, rectHeight);
			if (next.getValue().getAlpha() < 255) {
				g2.setPaint(Color.WHITE);
				g2.fill(rect2D);
			}
			GradientPaint p = new GradientPaint(prev.getFloatKey() * rectWidth, 0, prev.getValue(), next.getFloatKey() * rectWidth, 0, next.getValue());
			g2.setPaint(p);
			g2.fill(rect2D);

			prev = next;

		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(350, 30);
	}

	public void update(float lastColorValue, float newValue, Color background) {
		if (lastColorValue == 0) {
			lastColorValue += 0.1f;
		}
		if (lastColorValue == 1) {
			lastColorValue -= 0.1f;
		}
		if (newValue == 0) {
			newValue += 0.1f;
		}
		if (newValue == 1) {
			newValue -= 0.1f;
		}
		Float2ObjectRBTreeMap<Color> color = new Float2ObjectRBTreeMap<Color>(var.color);

		color.remove(lastColorValue);
		color.put(newValue, background);
		var.color = color;
		repaint();
	}
}
