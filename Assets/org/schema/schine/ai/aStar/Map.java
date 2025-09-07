package org.schema.schine.ai.aStar;

public interface Map {

	int FS = 24;

	public boolean checkField(int x, int y);

	public Field getField(int startX, int startY);

	public int getHeight();

	public ANode[][] getNodes();

	public Field[] getRectAroundField(int x, int y, int size);

	public int getWidth();

	public boolean isWalkable(int x, int z);

}
