package org.schema.schine.tools.curve;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.Locale;

public class QuadtoTest extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Point2D.Double[] points = new Point2D.Double[0];
	Point2D.Double[] ctrls = new Point2D.Double[0];
	Line2D.Double[] tangents = new Line2D.Double[0];
	Path2D.Double path = new Path2D.Double();
	boolean addingPoints = false;
	boolean showLines = true;
	double skew = 0.5;
	private MouseListener ml = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (addingPoints) {
				addPoint(e.getPoint());
				repaint();
			} else {  // dump points
				for (int j = 0; j < points.length; j++) {
					System.out.printf("points[%d] = (%5.1f, %5.1f)%n",
							j, points[j].x, points[j].y);
				}
				System.out.println("--------------");
			}
		}
	};

	public QuadtoTest() {
		addMouseListener(ml);
	}

	public static void main(String[] args) {
		QuadtoTest test = new QuadtoTest();
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(test);
		f.add(test.getControls(), "Last");
		f.setSize(400, 400);
		f.setLocation(100, 100);
		f.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String ac = e.getActionCommand();
		if (ac.equals("ADD POINTS")) {
			addingPoints = ((AbstractButton) e.getSource()).isSelected();
		}
		if (ac.equals("FIT CURVE")) {
			fitCurve();
		}
		if (ac.equals("RESET")) {
			points = new Point2D.Double[0];
			ctrls = new Point2D.Double[0];
			tangents = new Line2D.Double[0];
			path.reset();
			repaint();
		}
		if (ac.equals("SHOW LINES")) {
			showLines = ((AbstractButton) e.getSource()).isSelected();
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(Color.blue);
		g2.draw(path);
		if (showLines) {
			g2.setPaint(Color.pink);
			for (int j = 0; j < points.length - 1; j++) {
				g2.draw(new Line2D.Double(points[j].x, points[j].y,
						points[j + 1].x, points[j + 1].y));
			}
			g2.setPaint(Color.orange);
			for (int j = 0; j < tangents.length; j++) {
				g2.draw(tangents[j]);
			}
		}
		g2.setPaint(Color.red);
		for (int j = 0; j < points.length; j++) {
			mark(points[j], g2);
		}
		g2.setPaint(Color.green.darker());
		for (int j = 0; j < ctrls.length; j++) {
			mark(ctrls[j], g2);
		}
	}

	private void fitCurve() {
		if (points.length < 3)
			return;
		int n = points.length - 1;
		// Initialize arrays.
		tangents = new Line2D.Double[n - 1];
		ctrls = new Point2D.Double[n];
		for (int j = 0; j < tangents.length; j++) {
			tangents[j] = new Line2D.Double();
		}
		for (int j = 0; j < ctrls.length; j++) {
			ctrls[j] = new Point2D.Double();
		}
		path.reset();
		path.moveTo(points[0].x, points[0].y);
		// Set tangent lines.
		for (int j = 1, k = 0; j < n; j++, k = j - 1) {
			double theta = getAngularDifference(j);
			theta *= skew;
			tangents[j - 1] = getTangentLine(j, theta);
		}
		// Set control points and advance path to next point.
		for (int j = 0; j < n; j++) {
			Point2D.Double ctrl = getCtrlPoint(j);
			ctrls[j].setLocation(ctrl.x, ctrl.y);
			path.quadTo(ctrl.x, ctrl.y, points[j + 1].x, points[j + 1].y);
		}
		repaint();
	}

	private double getAngularDifference(int n) {
		double dy = points[n + 1].y - points[n].y;
		double dx = points[n + 1].x - points[n].x;
		double theta2 = Math.atan2(dy, dx);
		dy = points[n].y - points[n - 1].y;
		dx = points[n].x - points[n - 1].x;
		double theta1 = Math.atan2(dy, dx);
		return theta2 - theta1;
	}

	private Line2D.Double getTangentLine(int n, double relTheta) {
		double d = 200;
		double dy = points[n].y - points[n - 1].y;
		double dx = points[n].x - points[n - 1].x;
		double theta1 = Math.atan2(dy, dx);
		double theta = theta1 + relTheta;
		double x1 = points[n].x + d * Math.cos(theta - Math.PI);
		double y1 = points[n].y + d * Math.sin(theta - Math.PI);
		double x2 = points[n].x + d * Math.cos(theta);
		double y2 = points[n].y + d * Math.sin(theta);
		return new Line2D.Double(x1, y1, x2, y2);
	}

	private Point2D.Double getCtrlPoint(int n) {
		if (n == 0 || n == points.length - 2) {
			double d = 200;
			int index = (n == 0) ? 0 : n - 1;
			double tangentTheta = getTheta(tangents[index]);
			double lineTheta = getTheta(points[n], points[n + 1]);
			// Find center of line between points[n] and points[n+1].
			double dist = points[n].distance(points[n + 1]) / 2;
			double cx = points[n].x + dist * Math.cos(lineTheta);
			double cy = points[n].y + dist * Math.sin(lineTheta);
			// Construct perpendicular from cx, cy to intersect tangents[index].
			double phi = lineTheta - Math.PI / 2;
			double x = cx + d * Math.cos(phi);
			double y = cy + d * Math.sin(phi);
			Line2D.Double line = new Line2D.Double(cx, cy, x, y);
			return getIntersection(tangents[index], line);
		} else {
			return getIntersection(tangents[n - 1], tangents[n]);
		}
	}

	private double getTheta(Line2D.Double line) {
		Point2D.Double p1 = new Point2D.Double(line.x1, line.y1);
		Point2D.Double p2 = new Point2D.Double(line.x2, line.y2);
		return getTheta(p1, p2);
	}

	private double getTheta(Point2D.Double p1, Point2D.Double p2) {
		double dy = p2.y - p1.y;
		double dx = p2.x - p1.x;
		return Math.atan2(dy, dx);
	}

	/**
	 * http://mathworld.wolfram.com/Line-LineIntersection.html
	 */
	private Point2D.Double getIntersection(Line2D.Double line1,
	                                       Line2D.Double line2) {
		double x1 = line1.x1, y1 = line1.y1;
		double x2 = line1.x2, y2 = line1.y2;
		double x3 = line2.x1, y3 = line2.y1;
		double x4 = line2.x2, y4 = line2.y2;

		Point2D.Double p = new Point2D.Double();
		double determinant = getDeterminant(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
		double d1 = getDeterminant(x1, y1, x2, y2);
		double d2 = getDeterminant(x3, y3, x4, y4);
		p.x = getDeterminant(d1, x1 - x2, d2, x3 - x4) / determinant;
		d1 = getDeterminant(x1, y1, x2, y2);
		d2 = getDeterminant(x3, y3, x4, y4);
		p.y = getDeterminant(d1, y1 - y2, d2, y3 - y4) / determinant;
		return p;
	}

	private double getDeterminant(double a, double b, double c, double d) {
		return a * d - b * c;
	}

	private void mark(Point2D.Double p, Graphics2D g2) {
		g2.fill(new Ellipse2D.Double(p.x - 2, p.y - 2, 4, 4));
	}

	private void addPoint(Point p) {
		int n = points.length;
		Point2D.Double[] temp = new Point2D.Double[n + 1];
		System.arraycopy(points, 0, temp, 0, n);
		temp[n] = new Point2D.Double(p.x, p.y);
		points = temp;
	}

	private JPanel getControls() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(1, 1, 1, 1);
		gbc.weightx = 1.0;
		String[] ids = {"add points", "fit curve", "reset", "show lines"};
		for (int j = 0; j < ids.length; j++) {
			AbstractButton button = switch(j) {
				case 0 -> new JToggleButton(ids[j]);
				case 3 -> new JCheckBox(ids[j], showLines);
				default -> new JButton(ids[j]);
			};
			button.setActionCommand(ids[j].toUpperCase(Locale.ENGLISH));
			button.addActionListener(this);
			panel.add(button, gbc);
		}
		gbc.gridy = 1;
		gbc.gridwidth = ids.length;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(getSlider(), gbc);
		return panel;
	}

	private JSlider getSlider() {
		int value = (int) (skew * 100);
		final JSlider slider = new JSlider(40, 60, value);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(2);
		slider.setPaintTicks(true);
		slider.setLabelTable(getLabelTable(40, 60, 2));
		slider.setPaintLabels(true);
		slider.addChangeListener(e -> {
			int value1 = slider.getValue();
			skew = value1 / 100.0;
			fitCurve();
		});
		slider.setBorder(BorderFactory.createTitledBorder("skew"));
		return slider;
	}

	private Hashtable getLabelTable(int min, int max, int step) {
		Hashtable<Integer, JLabel> ht = new Hashtable<Integer, JLabel>();
		for (int j = min; j <= max; j += step) {
			String s = String.format("%.2f", j / 100.0);
			ht.put(Integer.valueOf(j), new JLabel(s));
		}
		return ht;
	}
}
