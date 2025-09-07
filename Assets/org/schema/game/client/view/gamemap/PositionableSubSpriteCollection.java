package org.schema.game.client.view.gamemap;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;

public class PositionableSubSpriteCollection implements List<PositionableSubColorSprite> {
	public static final int DATASIZE = 8;
	public final float values[];
	public final int size;

	public int posMult = 1;
	public float spriteScale = 1;
	private StarPosition s = new StarPosition();
	private StarPosition s1 = new StarPosition();
	public PositionableSubSpriteCollection(float[] values) {
		size = values.length / DATASIZE;
		this.values = values;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		assert (false);
		return false;
	}

	@Override
	public Iterator<PositionableSubColorSprite> iterator() {
		assert (false);
		return null;
	}

	@Override
	public Object[] toArray() {
		assert (false);
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		assert (false);
		return null;
	}

	@Override
	public boolean add(PositionableSubColorSprite e) {
		assert (false);
		return false;
	}

	@Override
	public boolean remove(Object o) {
		assert (false);
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		assert (false);
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends PositionableSubColorSprite> c) {
		assert (false);
		return false;
	}

	@Override
	public boolean addAll(int index,
	                      Collection<? extends PositionableSubColorSprite> c) {
		assert (false);
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		assert (false);
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		assert (false);
		return false;
	}

	@Override
	public void clear() {
		assert (false);
	}

	@Override
	public PositionableSubColorSprite get(int index) {
		int a = index * DATASIZE;
		s.pos.set(values[a + 0] * posMult, values[a + 1] * posMult, values[a + 2] * posMult);
		s.color.set(values[a + 3], values[a + 4], values[a + 5], values[a + 6]);
		s.starSubSprite = (int) values[a + 7];
		s.scale = this.spriteScale;

		return s;
	}

	@Override
	public PositionableSubColorSprite set(int index,
	                                      PositionableSubColorSprite e) {
		int a = index * DATASIZE;
		assert (posMult == 1);
		values[a + 0] = e.getPos().x;
		values[a + 1] = e.getPos().y;
		values[a + 2] = e.getPos().z;
		values[a + 3] = e.getColor().x;
		values[a + 4] = e.getColor().y;
		values[a + 5] = e.getColor().z;
		values[a + 6] = e.getColor().w;
		values[a + 7] = e.getSubSprite(null);

		s1.pos.set(values[a + 0] * posMult, values[a + 1] * posMult, values[a + 2] * posMult);
		s1.color.set(values[a + 3], values[a + 4], values[a + 5], values[a + 6]);
		s1.starSubSprite = (int) values[a + 7];
		s1.scale = this.spriteScale;

		return s1;
	}

	@Override
	public void add(int index, PositionableSubColorSprite element) {
				assert (false);
	}

	@Override
	public PositionableSubColorSprite remove(int index) {
		assert (false);
		return null;
	}

	@Override
	public int indexOf(Object o) {
		assert (false);
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		assert (false);
		return 0;
	}

	@Override
	public ListIterator<PositionableSubColorSprite> listIterator() {
		assert (false);
		return null;
	}

	@Override
	public ListIterator<PositionableSubColorSprite> listIterator(int index) {
		assert (false);
		return null;
	}

	@Override
	public List<PositionableSubColorSprite> subList(int fromIndex, int toIndex) {
		assert (false);
		return null;
	}

}
