package org.schema.game.common.data.element;

public class BlockLevel {

	private final short id;
	private final int level;

	public BlockLevel(short id, int level) {
		super();
		this.id = id;
		this.level = level;
	}

	/**
	 * @return the id
	 */
	public short getIdBase() {
		return id;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Level: " + level + "; Base: " + ElementKeyMap.getInfo(id);
	}

}
