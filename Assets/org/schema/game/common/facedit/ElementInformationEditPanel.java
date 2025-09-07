package org.schema.game.common.facedit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementInformation.EIC;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.annotation.ElemType;
import org.schema.game.common.data.element.annotation.Element;
import org.schema.game.common.data.element.annotation.NodeDependency;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ElementInformationEditPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private TexturePanel texture;
	SortedMap<ElemContainer, ElementInformationOption> list;
	private Object2ObjectOpenHashMap<EIC, SortedMap<ElemContainer, ElementInformationOption>> opop;
	
	private class ElemContainer implements Comparable<ElemContainer>{
		public Element elem;
		public int order;
		public ElemContainer(Element elem) {
			super();
			this.elem = elem;
			this.order = elem.order();
		}
		@Override
		public int compareTo(ElemContainer o) {
			return order - o.order;
		}
		
	}
	/**
	 * Create the panel.
	 */
	public ElementInformationEditPanel(final JFrame frame, short type) {

		Field[] declaredFields = ElementInformation.class.getDeclaredFields();

		opop = new Object2ObjectOpenHashMap<EIC, SortedMap<ElemContainer, ElementInformationOption>>(EIC.values().length);
		for (int i = 0; i < declaredFields.length; i++) {
			final Element annotation = declaredFields[i].getAnnotation(Element.class);
			if (annotation != null) {
				list = opop.get(annotation.cat());
				if(list == null) {
					list = new Object2ObjectAVLTreeMap<ElemContainer, ElementInformationOption>();
					opop.put(annotation.cat(), list);
				}
				declaredFields[i].setAccessible(true);
				
				
				list.put(new ElemContainer(annotation), new ElementInformationOption(frame, declaredFields[i], type, i, this));
			}
			
			
		}
		
//		for (SortedMap<ElemType, ElementInformationOption> e : opop.values()) {
//			Collections.sort(e);
//		}
		
		GridBagLayout def = new GridBagLayout();
		def.columnWidths = new int[]{200};

		def.rowHeights = new int[opop.size() + 2];

		for (int i = 0; i < def.rowHeights.length; i++) {
			def.rowHeights[i] = 33;
		}
		def.columnWeights = new double[]{0.0};
		def.rowWeights = new double[]{0.0};
		setLayout(def);
		
		
		JXLabel lblName = new JXLabel(ElementKeyMap.getInfo(type).getName());
		lblName.setFont(new Font("Arial", Font.PLAIN, 19));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);
		
		texture = new TexturePanel(ElementKeyMap.getInfo(type), false);
		GridBagConstraints gbc_texture = new GridBagConstraints();
		gbc_texture.anchor = GridBagConstraints.NORTHWEST;
		gbc_texture.fill = GridBagConstraints.BOTH;
		gbc_texture.gridx = 0;
		gbc_texture.gridy = 1;
		add(texture, gbc_texture);
		
		
		
		
		int c = 0;
		for (int j = 0; j < EIC.values().length; j++) {
			final EIC key = EIC.values()[j];
			SortedMap<ElemContainer, ElementInformationOption> options = opop.get(key);
			
			for(Entry<ElemContainer, ElementInformationOption> e : options.entrySet()) {
				if(e.getKey().elem.parser().fac instanceof NodeDependency) {
					((NodeDependency)e.getKey().elem.parser().fac).onSwitch(e.getValue(), e.getValue().info, e.getKey().elem);
				}
			}
			
			JXPanel p = new JXPanel();
			p.setLayout(new BorderLayout());
			
			
			final JXCollapsiblePane tk = new JXCollapsiblePane();
			
			
			tk.setCollapsed(key.collapsed);
			
	
			JXPanel content = new JXPanel();
			
			GridBagLayout cont = new GridBagLayout();
			cont.columnWidths = new int[]{400};
	
			cont.rowHeights = new int[options.size()];
	
			for (int i = 0; i < cont.rowHeights.length; i++) {
				cont.rowHeights[i] = 33;
			}
			cont.columnWeights = new double[]{0.0};
			cont.rowWeights = new double[]{0.0};
			content.setLayout(cont);
	
			int i = 0;
			for (ElementInformationOption elementInformationOption : options.values()) {
				elementInformationOption.setLocalOrder(i);
				
				GridBagConstraints valConst = new GridBagConstraints();
				valConst.anchor = GridBagConstraints.WEST;
				valConst.fill = GridBagConstraints.BOTH;
				valConst.gridx = 0;
				valConst.gridy = i;
				content.add(elementInformationOption, valConst);
				i++;
			}
			tk.setContentPane(content);
			
			GridBagConstraints gbc_cont = new GridBagConstraints();
			gbc_cont.anchor = GridBagConstraints.NORTHWEST;
			gbc_cont.fill = GridBagConstraints.BOTH;
			gbc_cont.weightx = 1.0;
			gbc_cont.weighty = 1.0;
			gbc_cont.gridx = 0;
			gbc_cont.gridy = c+2;
			
			tk.addPropertyChangeListener(JXCollapsiblePane.ANIMATION_STATE_KEY, e -> key.collapsed = tk.isCollapsed());
			
			JButton toggle = new JButton(tk.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
			toggle.setText(key.getName());
			p.add(toggle, BorderLayout.NORTH);
			p.add(tk, BorderLayout.SOUTH);
			
			
			
			add(p, gbc_cont);
			c++;
		}
	}
	public void onSwitchActivationAnimationStyle(int lodActivationAnimationStyle) {
		for (int j = 0; j < EIC.values().length; j++) {
			final EIC key = EIC.values()[j];
			SortedMap<ElemContainer, ElementInformationOption> options = opop.get(key);
			
			for(Entry<ElemContainer, ElementInformationOption> e : options.entrySet()) {
				switch (lodActivationAnimationStyle) {
				case 1:
						if(e.getKey().elem.parser() == ElemType.LOD_SHAPE_ACTIVE) {
							setEnabledComp(e.getValue().editComponent, true);
						}
					break;
		
				default:
						if(e.getKey().elem.parser() == ElemType.LOD_SHAPE_ACTIVE) {
							setEnabledComp(e.getValue().editComponent, false);
						}
					break;
				}
			}
		}
		return ;
	}
	private void setEnabledComp(Component c, boolean b) {
		c.setEnabled(b);
		if(c instanceof JComponent) {
			JComponent j = (JComponent)c;
			for(int i = 0; i < j.getComponentCount(); i++) {
				setEnabledComp(j.getComponents()[i], b);
			}
		}
	}
	public void updateTextures() {
		texture.update();
	}

	public int getScroll() {
				return 0;
	}

	public void setScroll(int scrl) {
		
	}
}
