package org.schema.game.common.data.cubatoms;

import java.util.Locale;
import java.util.Set;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;

public class Cubatom {
	private final CubatomFlavor[] flavors;
	private final int id;

	public Cubatom(CubatomFlavor[] states) {
		assert (states.length == 4);
		assert (states[0].getState() == CubatomState.MASS);
		assert (states[1].getState() == CubatomState.SPINNING);
		assert (states[2].getState() == CubatomState.THERMAL);
		assert (states[3].getState() == CubatomState.CONDUCTIVITY);

		id = (states[3].ordinal() % 4) + (states[2].ordinal() % 4) * 4 + (states[1].ordinal() % 4) * (4 * 4) + (states[0].ordinal() % 4) * (4 * 4 * 4);
		assert (id < 256);
		this.flavors = states;
	}

	public static void draw(CubatomFlavor[] flavors) {
		Sprite cs = Controller.getResLoader().getSprite("cubaton-sprites-4x4-gui-");
		for (int i = 0; i < flavors.length; i++) {
			cs.setSelectedMultiSprite(flavors[i].ordinal());
			cs.draw();
		}
	}

	public static void draw(CubatomFlavor[] flavors, Set<CubatomFlavor> filter) {
		Sprite cs = Controller.getResLoader().getSprite("cubaton-sprites-4x4-gui-");
		if (cs.getTint() == null) {
			cs.setTint(new Vector4f(1, 1, 1, 1));
		}
		for (int i = 0; i < flavors.length; i++) {
			if (filter.contains(flavors[i])) {
				cs.setSelectedMultiSprite(flavors[i].ordinal());
				cs.draw();
			} else {
				cs.getTint().w = 0.3f;
				cs.setSelectedMultiSprite(flavors[i].ordinal());
				cs.draw();
			}
			cs.getTint().w = 1.0f;
		}
	}

	public static String getNiceString(CubatomFlavor[] flavors) {
		StringBuffer s = new StringBuffer();
		boolean special = false;
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].ordinal() % 4 == 3) {
				special = true;
				break;
			}
		}
		if (special) {
			s.append("--SPECIAL--\n");
		} else {
			s.append("--NATURAL--\n");
		}
		for (int i = 0; i < flavors.length; i++) {
			s.append(flavors[i].getState().name().toLowerCase(Locale.ENGLISH));
			s.append("<");
			if (flavors[i].ordinal() % 4 == 3) {
				s.append("*");
			}
			s.append(flavors[i].name().toLowerCase(Locale.ENGLISH));
			if (flavors[i].ordinal() % 4 == 3) {
				s.append("*");
			}
			s.append(">");

			if (i < flavors.length - 1) {
				s.append("\n");
			}
		}
		return s.toString();
	}

	public void draw() {
		Sprite cs = Controller.getResLoader().getSprite("cubaton-sprites-4x4-gui-");
		for (int i = 0; i < flavors.length; i++) {
			cs.setSelectedMultiSprite(flavors[i].ordinal());
			cs.draw();
		}
	}

	/**
	 * @return the flavors
	 */
	public CubatomFlavor[] getFlavors() {
		return flavors;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((Cubatom) obj).id == id;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(flavors[0].name().substring(0, 3)).
				append(flavors[1].name().substring(0, 3)).
				append(flavors[2].name().substring(0, 3)).
				append(flavors[3].name().substring(0, 3));
		return b.toString();
	}

	public boolean isNatural() {
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].isSpecial()) {
				return false;
			}
		}
		return true;
	}

}
