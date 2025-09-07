package org.schema.game.common.facedit;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;

public class TexturePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public int selectedIndex = 0;
	private ElementInformation info;
	private boolean selectable;
	private List<TitledBorder> other = new ArrayList<TitledBorder>();
	private List<JPanel> otherLab = new ArrayList<JPanel>();
	/**
	 * Create the panel.
	 */
	public TexturePanel(ElementInformation info, boolean selectable) {
		this.info = info;
		this.selectable = selectable;
		init();

	}

	private JPanel getImage(int id, final int index, String name) {
		JPanel p = new JPanel();
		final TitledBorder titledBorder = new TitledBorder(name);
		other.add(titledBorder);
		otherLab.add(p);
		p.setBorder(titledBorder);
		final JLabel l = new JLabel(EditorTextureManager.getImage(id));
		p.add(l);

		if (selectable) {
			if (index == selectedIndex) {
				titledBorder.setTitleColor(Color.RED);
			}
			p.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					selectedIndex = index;
					for (int i = 0; i < other.size(); i++) {

						if (other.get(i) == titledBorder) {
							other.get(i).setTitleColor(Color.RED);
						} else {
							other.get(i).setTitleColor(Color.BLACK);
						}
					}
					TexturePanel.this.doLayout();
					TexturePanel.this.revalidate();
					TexturePanel.this.repaint();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}
			});
		}
		return p;
	}

	private void init() {
		System.err.println("INIT SELECTED TEXTURE ROW: " + Arrays.toString(info.getTextureIds()));
		other.clear();
		otherLab.clear();
		if (info.getIndividualSides() >= 6) {
			add(getImage(info.getTextureId(0), 0, "front"));
			add(getImage(info.getTextureId(1), 1, "back"));
			add(getImage(info.getTextureId(2), 2, "top"));
			add(getImage(info.getTextureId(3), 3, "bottom"));
			add(getImage(info.getTextureId(4), 4, "left"));
			add(getImage(info.getTextureId(5), 5, "right"));
		} else if (info.getIndividualSides() == 3) {
			add(getImage(info.getTextureId(Element.TOP), Element.TOP, "top"));
			add(getImage(info.getTextureId(Element.BOTTOM), Element.BOTTOM, "bottom"));
			add(getImage(info.getTextureId(0), 0, "sides"));
		} else {
			add(getImage(info.getTextureId(0), 0, "sides"));
		}
	}

	public void update() {
		for (int i = 0; i < otherLab.size(); i++) {
			this.remove(otherLab.get(i));
		}
		init();
		validate();
	}

	public void update(int id, int individualSides) {
		for (int i = 0; i < otherLab.size(); i++) {
			this.remove(otherLab.get(i));
		}
		
		if(id < 0){
			id = info.getTextureId(Element.FRONT);
		}
		info.setTextureId(selectedIndex, (short) id);
		info.setIndividualSides(individualSides);

		if (individualSides >= 6) {
			//nothing to do
		} else if (individualSides == 3) {
			if (selectedIndex == 0) {
				//everything except top and bottom is the same on 3 sided
				info.setTextureId(Element.BACK, (short) id);
				info.setTextureId(Element.FRONT, (short) id);
				info.setTextureId(Element.LEFT, (short) id);
				info.setTextureId(Element.RIGHT, (short) id);
			}

		} else {
			//everything is the same on top bottom
			for (int i = 0; i < 6; i++) {
				info.setTextureId(i, (short) id);
			}
		}
		init();
		doLayout();
		revalidate();
		validate();
		repaint();
	}
}
