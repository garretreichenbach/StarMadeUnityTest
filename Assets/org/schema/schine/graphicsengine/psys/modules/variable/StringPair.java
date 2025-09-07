package org.schema.schine.graphicsengine.psys.modules.variable;

public class StringPair {

	public String string;
	public int val;

	public StringPair(String string, int val) {
		super();
		this.string = string;
		this.val = val;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return string;
	}

}
