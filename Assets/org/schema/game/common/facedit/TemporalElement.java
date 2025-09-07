package org.schema.game.common.facedit;

public class TemporalElement {

	private short id;
	private int iconId;
	private short textureId[];
	private TemporalFactory factory;
	private String name;

	/**
	 * @return the factory
	 */
	public TemporalFactory getFactory() {
		return factory;
	}

	/**
	 * @param factory the factory to set
	 */
	public void setFactory(TemporalFactory factory) {
		this.factory = factory;
	}

	/**
	 * @return the iconId
	 */
	public int getIconId() {
		return iconId;
	}

	/**
	 * @param iconId the iconId to set
	 */
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	/**
	 * @return the id
	 */
	public short getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(short id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the textureId
	 */
	public short[] getTextureId() {
		return textureId;
	}

	/**
	 * @param textureId the textureId to set
	 */
	public void setTextureId(short[] textureId) {
		this.textureId = textureId;
	}
}
