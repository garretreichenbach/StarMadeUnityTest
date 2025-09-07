package org.schema.schine.tools.curve;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.OutputStream;
import java.util.ArrayList;

import org.schema.schine.graphicsengine.psys.modules.variable.PSCurveVariable;
import org.schema.schine.graphicsengine.spline.Spline2DAlt;

public class SplineCurve {

	private static final double CONTROL_POINT_SIZE = 12.0;

	// private Point2D control1 = ;
	// private Point2D control2 = ;
	private final PSCurveVariable var;
	private Point dragStart = null;
	private boolean isSaving = false;
	private PropertyChangeSupport support;
	private SplineDisplay display;

	SplineCurve(SplineDisplay display, PSCurveVariable var) {
		this.var = var;

		this.display = display;

		display.setEnabled(false);

		display.addMouseMotionListener(new ControlPointsHandler());
		display.addMouseListener(new SelectionHandler());

		support = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(String propertyName,
	                                      PropertyChangeListener listener) {
		support.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
	                                         PropertyChangeListener listener) {
		support.removePropertyChangeListener(propertyName, listener);
	}

	// public Point2D getControl1() {
	// return (Point2D) control1.clone();
	// }
	//
	// public Point2D getControl2() {
	// return (Point2D) control2.clone();
	// }
	//
	// public void setControl1(Point2D control1) {
	// support.firePropertyChange("control1",
	// (Point2D) this.control1.clone(),
	// (Point2D) control1.clone());
	// this.control1 = (Point2D) control1.clone();
	// repaint();
	// }
	//
	// public void setControl2(Point2D control2) {
	// support.firePropertyChange("control2",
	// (Point2D) this.control2.clone(),
	// (Point2D) control2.clone());
	// this.control2 = (Point2D) control2.clone();
	// repaint();
	// }

	synchronized void saveAsTemplate(OutputStream out) {
		// BufferedImage image = Java2dHelper.createCompatibleImage(getWidth(),
		// getHeight());
		// Graphics g = image.getGraphics();
		// isSaving = true;
		// setDrawText(false);
		// paint(g);
		// setDrawText(true);
		// isSaving = false;
		// g.dispose();
		//
		// BufferedImage subImage = image.getSubimage((int)
		// xPositionToPixel(0.0),
		// (int) yPositionToPixel(1.0),
		// (int) (xPositionToPixel(1.0) - xPositionToPixel(0.0)) + 1,
		// (int) (yPositionToPixel(0.0) - yPositionToPixel(1.0)) + 1);
		//
		// try {
		// ImageIO.write(subImage, "PNG", out);
		// } catch (IOException e) {
		// }
		//
		// image.flush();
		// subImage = null;
		// image = null;
	}

	public void paint(Graphics2D g2) {

		if (!isSaving) {
			paintControlPoints(g2, var.getPoints());
		}
		paintSpline(g2, var.getPoints());

		if (var.isUseIntegral()) {
			if (!isSaving) {
				paintControlPoints(g2, var.getPointsSecond());
			}
			paintSpline(g2, var.getPointsSecond());

			paintIntegral(g2, var.getPoints(), var.getPointsSecond());
		}

	}

	private void paintControlPoints(Graphics2D g2, ArrayList<Point2D.Double> po) {
		for (int i = 0; i < po.size(); i++) {
			paintControlPoint(g2, po.get(i));
		}
	}

	private void paintControlPoint(Graphics2D g2, Point2D control) {
		double origin_x = display.xPositionToPixel(control.getX());
		double origin_y = display.yPositionToPixel(control.getY());
		double pos = 0.0;// control == control1 ? 0.0 : 1.0;

		Ellipse2D outer = getDraggableArea(control);
		Ellipse2D inner = new Ellipse2D.Double(origin_x + 2.0
				- CONTROL_POINT_SIZE / 2.0, origin_y + 2.0 - CONTROL_POINT_SIZE
				/ 2.0, 8.0, 8.0);

		Area circle = new Area(outer);
		circle.subtract(new Area(inner));

		Stroke stroke = g2.getStroke();
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 5, new float[]{5, 5}, 0));
		g2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.4f));
		g2.drawLine(0, (int) origin_y, (int) origin_x, (int) origin_y);
		g2.drawLine((int) origin_x, (int) origin_y, (int) origin_x,
				display.getHeight());
		g2.setStroke(stroke);

		if (display.selected == control) {
			g2.setColor(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		} else {
			g2.setColor(new Color(0.8f, 0.8f, 0.8f, 0.6f));
		}
		g2.fill(inner);

		g2.setColor(var.getColor().darker());
		g2.fill(circle);

		// g2.drawLine((int) origin_x, (int) origin_y,
		// (int) xPositionToPixel(pos), (int) yPositionToPixel(pos));
	}

	private Ellipse2D getDraggableArea(Point2D control) {
		Ellipse2D outer = new Ellipse2D.Double(display.xPositionToPixel(control
				.getX()) - CONTROL_POINT_SIZE / 2.0,
				display.yPositionToPixel(control.getY()) - CONTROL_POINT_SIZE
						/ 2.0, CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);
		return outer;
	}

	private Spline2DAlt createSpline(ArrayList<Point2D.Double> po) {
		Point2D[] p = new Point2D[po.size()];
		for (int i = 0; i < po.size(); i++) {
			p[i] = po.get(i);
		}
		Spline2DAlt s = new Spline2DAlt(p);
		return s;
	}

	private void paintIntegral(Graphics2D g2, ArrayList<Point2D.Double> poA, ArrayList<Point2D.Double> poB) {

		Spline2DAlt sA = createSpline(poA);
		Spline2DAlt sB = createSpline(poB);
		g2.setColor(var.getColor());

		double step = 0.05;
		for (double i = 0; i <= 1d; i += step) {
			if (i == 1.0) {
				i = 1 - 0.00001d;
			}

			Polygon p = new Polygon();
//			g2.drawLine( 
//					(int) display.xPositionToPixel(sA.getValueX(i)), (int) display.yPositionToPixel(sA.getValueY(i)),
//					(int) display.xPositionToPixel(sB.getValueX(i)), (int) display.yPositionToPixel(sB.getValueY(i)));
			g2.drawLine(
					(int) display.xPositionToPixel(i), (int) display.yPositionToPixel(sA.getValueY(i)),
					(int) display.xPositionToPixel(i), (int) display.yPositionToPixel(sB.getValueY(i)));

			g2.fill(p);
		}
	}

	private void paintSpline(Graphics2D g2, ArrayList<Point2D.Double> po) {

		Spline2DAlt s = createSpline(po);
		g2.setColor(var.getColor());

		double step = 0.05;
		for (double i = 0; i <= 1d; i += step) {

//			g2.drawLine((int) display.xPositionToPixel(s.getValueX(i)),
//					(int) display.yPositionToPixel(s.getValueY(i)),
//					(int) display.xPositionToPixel(s.getValueX(i+step)),
//					(int) display.yPositionToPixel(s.getValueY(i+step)));
			g2.drawLine((int) display.xPositionToPixel(i),
					(int) display.yPositionToPixel(s.getValueY(i)),
					(int) display.xPositionToPixel(i + step),
					(int) display.yPositionToPixel(s.getValueY(i + step)));
		}
	}

	private int getSelectedIndex(ArrayList<Point2D.Double> po) {
		for (int i = 0; i < po.size(); i++) {
			if (display.selected == po.get(i)) {
				return i;
			}
		}
		return -1;
	}

	private void resetSelection() {
		Point2D oldSelected = display.selected;
		display.selected = null;

		if (oldSelected != null) {
			Rectangle bounds = getDraggableArea(oldSelected).getBounds();
			display.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	private class ControlPointsHandler extends MouseMotionAdapter {
		private void dragged(MouseEvent e, ArrayList<Double> points) {
			if (display.selected == null) {
				return;
			}
			int selectedIndex = getSelectedIndex(points);

			if (selectedIndex < 0) {
				return;
			}
			Point dragEnd = e.getPoint();

			double distanceX = display.xPixelToPosition(dragEnd.getX())
					- display.xPixelToPosition(dragStart.getX());

			double x = display.selected.getX() + distanceX;

			int i = getSelectedIndex(points);

			if (x < 0.0) {
				x = 0.0;
			} else if (x > 1.0) {
				x = 1.0;
			}

			double distanceY = display.yPixelToPosition(dragEnd.getY())
					- display.yPixelToPosition(dragStart.getY());

			double y = display.selected.getY() + distanceY;
			if (y < -1.0) {
				y = -1.0;
			} else if (y > 1.0) {
				y = 1.0;
			}

			if (selectedIndex == 0) {
				x = 0;
			} else if (selectedIndex == points.size() - 1) {
				x = 1;
			} else {
				if (x > points.get(selectedIndex + 1).x) {
					x = points.get(selectedIndex + 1).x;
				}
				if (x < points.get(selectedIndex - 1).x) {
					x = points.get(selectedIndex - 1).x;
				}
			}

			// System.err.println("X "+x+", Y "+y+";       "+distanceX+"; "+distanceY);
			Point2D selectedCopy = (Point2D) display.selected.clone();
			display.selected.setLocation(x, y);

			String sel = String.valueOf(selectedIndex + 1);

			support.firePropertyChange("control" + sel, selectedCopy,
					display.selected.clone());

			display.repaint();

			double xPos = display.xPixelToPosition(dragEnd.getX());
			double yPos = -display.yPixelToPosition(dragEnd.getY());

			// if (xPos >= 0.0 && xPos <= 1.0) {
			// System.err.println("DX "+xPos);
			dragStart.setLocation(dragEnd.getX(), dragStart.getY());
			// }
			// if (yPos >= 0.0 && yPos <= 1.0) {
			// System.err.println("DY "+xPos);
			dragStart.setLocation(dragStart.getX(), dragEnd.getY());
			// }

			var.revalidate();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			dragged(e, var.getPoints());
			if (var.isUseIntegral()) {
				dragged(e, var.getPointsSecond());
			}

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			for (Point2D p : var.getPoints()) {
				Ellipse2D area1 = getDraggableArea(p);

				if (area1.contains(e.getPoint())) {
					display.setCursor(Cursor
							.getPredefinedCursor(Cursor.HAND_CURSOR));
					return;
				}
			}
			if (var.isUseIntegral()) {
				for (Point2D p : var.getPointsSecond()) {
					Ellipse2D area1 = getDraggableArea(p);

					if (area1.contains(e.getPoint())) {
						display.setCursor(Cursor
								.getPredefinedCursor(Cursor.HAND_CURSOR));
						return;
					}
				}
			}

			display.setCursor(Cursor.getDefaultCursor());
		}
	}

	private class SelectionHandler extends MouseAdapter {

		private boolean handle(MouseEvent e, ArrayList<Double> points) {
			for (int i = 0; i < points.size(); i++) {
				Point2D p = points.get(i);
				Ellipse2D area1 = getDraggableArea(p);

				if (area1.contains(e.getPoint()) && display.lastEvent != e) {
					System.err.println("POINT INSIDE " + this);
					display.lastEvent = e;
					if (e.getButton() == 1) {
						display.selected = p;
						dragStart = e.getPoint();
						Rectangle bounds = area1.getBounds();
						display.repaint(bounds.x, bounds.y, bounds.width,
								bounds.height);
					} else {
						if (points.size() > 2 && i != 0
								&& i != points.size() - 1) {
							points.remove(p);
							Rectangle bounds = area1.getBounds();
							display.repaint();
						}
					}

					return true;
				}

			}
			if (display.lastEvent != e) {
				resetSelection();
			}
			if (e.getButton() != 1 && display.lastEvent != e) {

				Spline2DAlt s = createSpline(points);
//				float resA[] = new float[2];

				float xPos = (float) ((e.getPoint().x) / (double) display.getWidth());

				System.err.println("---------\nCHECKING: " + (e.getPoint().x) + " -> " + xPos);

//				s.getPositionAt(xPos, resA);

				double cursorY = (0.5 - (e.getPoint().y / (double) display.getHeight())) * 2.0;

				if (Math.abs(display.yPositionToPixel(s.getValueY(xPos)) - display.yPositionToPixel(cursorY)) < 15) {
					System.err.println("HIT");
					for (int i = 0; i < points.size() - 1; i++) {
						Double a = points.get(i);
						Double b = points.get(i + 1);

						if (a.x <= display.xPixelToPosition(e.getPoint().x) && b.x >= display.xPixelToPosition(e.getPoint().x)) {
							points.add(i + 1, new Point2D.Double(display.xPixelToPosition(e.getPoint().x), display.yPixelToPosition(e.getPoint().y)));
							display.repaint();
							display.lastEvent = e;
							return true;
						} else {
							System.err.println("NO siutable point found");
						}
					}

				} else {
//					System.err.println("RES: -> "+resA[0]+", "+resA[1]);

					System.err.println("DD: " + (0.5 - (e.getPoint().y / (double) display.getHeight())) * 2.0);
//					System.err.println("DISTANCE: "+(Math.abs(display.yPositionToPixel(resA[1]) - display.yPositionToPixel(cursorY))));
				}
			}

			return false;
		}

		@Override
		public void mousePressed(MouseEvent e) {

			ArrayList<Double> points = new ArrayList<Double>(var.getPoints());
			ArrayList<Double> pointsSecond = new ArrayList<Double>(var.getPointsSecond());

			if (!handle(e, points)) {
				if (var.isUseIntegral()) {
					handle(e, pointsSecond);
				}
			}

			var.setPoints(points);
			var.setPointsSecond(pointsSecond);

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			resetSelection();
		}
	}
}
