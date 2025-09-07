package org.schema.game.common.data.player.faction;

import java.util.Locale;

public class PersonalEnemyMod {
	public final String initiator;
	public final String enemyPlayerName;
	public final int fid;
	public final boolean add;

	public PersonalEnemyMod(String initiator, String enemyPlayerName, int fid, boolean add) {
		super();
		this.initiator = initiator;
		this.enemyPlayerName = enemyPlayerName.toLowerCase(Locale.ENGLISH);
		this.fid = fid;
		this.add = add;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (add ? 1231 : 1237);
		result = prime * result
				+ ((enemyPlayerName == null) ? 0 : enemyPlayerName.hashCode());
		result = prime * result + fid;
		result = prime * result
				+ ((initiator == null) ? 0 : initiator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersonalEnemyMod)) {
			return false;
		}
		PersonalEnemyMod other = (PersonalEnemyMod) obj;
		if (add != other.add) {
			return false;
		}
		if (enemyPlayerName == null) {
			if (other.enemyPlayerName != null) {
				return false;
			}
		} else if (!enemyPlayerName.equals(other.enemyPlayerName)) {
			return false;
		}
		if (fid != other.fid) {
			return false;
		}
		if (initiator == null) {
			if (other.initiator != null) {
				return false;
			}
		} else if (!initiator.equals(other.initiator)) {
			return false;
		}
		return true;
	}
	
	
}
