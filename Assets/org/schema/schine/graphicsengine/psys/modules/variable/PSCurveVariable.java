package org.schema.schine.graphicsengine.psys.modules.variable;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Locale;

import org.schema.schine.graphicsengine.spline.Spline2DAlt;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class PSCurveVariable implements PSVariable<Float> {
	private boolean useIntegral;
	private ArrayList<Point2D.Double> points = new ArrayList();
	private ArrayList<Point2D.Double> pointsSecond = new ArrayList();
	private float base = 1;

	private Spline2DAlt sA;
	private Spline2DAlt sB;

	public PSCurveVariable() {
		super();
		reset();
		assert (sA != null);
		assert (sB != null);
	}

	public PSCurveVariable(double base) {
		this();
		this.base = (float) base;
	}

	private static void parsePoints(Node cp, ArrayList<Double> p) {

		NodeList childNodes = cp.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {

				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("point")) {
					p.add(parsePoint(item));
				}
			}
		}

	}

	private static Double parsePoint(Node cp) {
		int index = Integer.parseInt(cp.getAttributes().getNamedItem("index").getNodeValue());
		NodeList childNodes = cp.getChildNodes();
		Double point = new Double();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("x")) {
					point.x = Float.parseFloat(item.getTextContent());
				}
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("y")) {
					point.y = Float.parseFloat(item.getTextContent());
				}
			}
		}

		return point;
	}

	protected boolean valid() {
		return sA != null && sB != null;
	}

	@Override
	public Float get(float lifetime) {
		assert (sA != null);
		assert (sB != null);
		if (useIntegral) {

			float upper = (float) sA.getValueY(lifetime);
			float lower = (float) sB.getValueY(lifetime);

			if (lower > upper) {
				float tmp = upper;
				upper = lower;
				lower = tmp;
			}
			float dist = upper - lower;
			float val = (float) (Math.random() * dist);

			float result = lower + val;

			return (float) (base * result);
		} else {
			return (float) (base * sA.getValueY(lifetime));
		}
	}

	@Override
	public void set(PSVariable<Float> var) {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.psys.modules.variable.PSVariable#appendXML(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public void appendXML(Object m, Element element) {
		element.setAttribute("integral", String.valueOf(useIntegral));
		element.setAttribute("base", String.valueOf(base));
		{
			Element root = element.getOwnerDocument().createElement("Points0");
			for (int i = 0; i < points.size(); i++) {
				Double p = points.get(i);
				Element point = element.getOwnerDocument().createElement("Point");
				point.setAttribute("index", String.valueOf(i));
				Element x = element.getOwnerDocument().createElement("X");
				Element y = element.getOwnerDocument().createElement("Y");
				root.appendChild(point);
				point.appendChild(x);
				point.appendChild(y);

				x.setTextContent(String.valueOf((float) p.x));
				y.setTextContent(String.valueOf((float) p.y));

			}
			element.appendChild(root);
		}
		{
			Element root = element.getOwnerDocument().createElement("Points1");
			for (int i = 0; i < pointsSecond.size(); i++) {
				Double p = pointsSecond.get(i);
				Element point = element.getOwnerDocument().createElement("Point");
				point.setAttribute("index", String.valueOf(i));
				Element x = element.getOwnerDocument().createElement("X");
				Element y = element.getOwnerDocument().createElement("Y");
				root.appendChild(point);
				point.appendChild(x);
				point.appendChild(y);

				x.setTextContent(String.valueOf((float) p.x));
				y.setTextContent(String.valueOf((float) p.y));

			}
			element.appendChild(root);
		}

	}

	@Override
	public void parse(Node r) {
		useIntegral = Boolean.parseBoolean(r.getAttributes().getNamedItem("integral").getNodeValue());
		base = Float.parseFloat(r.getAttributes().getNamedItem("base").getNodeValue());

		points.clear();

		if (useIntegral) {
			pointsSecond.clear();
		}

		NodeList childNodes = r.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				System.err.println("PARSING: " + item.getNodeName());
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("points0")) {
					parsePoints(item, points);
				}
				if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("points1")) {
					parsePoints(item, pointsSecond);
				}

			}
		}

		revalidate();
	}

	public abstract String getName();

	public abstract Color getColor();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @return the useIntegral
	 */
	public boolean isUseIntegral() {
		return useIntegral;
	}

	/**
	 * @param useIntegral the useIntegral to set
	 */
	public void setUseIntegral(boolean useIntegral) {
		this.useIntegral = useIntegral;
	}

	public void reset() {
		points.clear();
		pointsSecond.clear();
		initPoints();
		revalidate();
	}

	public void initPoints() {
		points.add(new Point2D.Double(0, 1));
		points.add(new Point2D.Double(1, 1));
		pointsSecond.add(new Point2D.Double(0, 0));
		pointsSecond.add(new Point2D.Double(1, 0));
		revalidate();

	}

	/**
	 * @return the base
	 */
	public float getBase() {
		return base;
	}

	/**
	 * @param base the base to set
	 */
	public void setBase(float base) {
		this.base = base;
	}

	public void revalidate() {
		if (points.size() < 2) {
			throw new IllegalArgumentException("points0 have to be more then 1, but are " + points.size());
		}
		if (pointsSecond.size() < 2) {
			throw new IllegalArgumentException("points1 have to be more then 1, but are " + pointsSecond.size());
		}
		sA = new Spline2DAlt(points);
		sB = new Spline2DAlt(pointsSecond);
	}

	/**
	 * @return the points
	 */
	public ArrayList<Point2D.Double> getPoints() {
		return points;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(ArrayList<Point2D.Double> points) {
		this.points = points;
		revalidate();
	}

	/**
	 * @return the pointsSecond
	 */
	public ArrayList<Point2D.Double> getPointsSecond() {
		return pointsSecond;
	}

	/**
	 * @param pointsSecond the pointsSecond to set
	 */
	public void setPointsSecond(ArrayList<Point2D.Double> pointsSecond) {
		this.pointsSecond = pointsSecond;
		revalidate();
	}

}
