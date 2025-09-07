package org.schema.game.common.data.player.faction;

import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class FactionRelationOffer extends FactionRelation {

	boolean revoke;
	private String message;
	private String initiator;

	public static final long getOfferCode(int a, int b) {
		return (long) Math.abs(a) * (long) Integer.MAX_VALUE + Math.abs(b);
	}

	public static void main(String args[]) {
		int a = 10010;
		int b = 10011;
		LongOpenHashSet s = new LongOpenHashSet();
		for (int i = 0; i < 200; i++) {
			a = 10010;
			for (int v = 0; v < 200; v++) {
				if (a != b) {
					//					long code = getCode(a, b);
					long code = getOfferCode(a, b);
					//					long codeB = getOfferCode(b, a);
					System.err.println("CODE: " + a + "; " + b + " -> " + code);
					assert (!s.contains(code));
					s.add(code);
				}
				a++;
			}
			b++;
		}
		b = 10011;
		a = 10012;

	}

	/**
	 * @return the initiator
	 */
	public String getInitiator() {
		return initiator;
	}

	/**
	 * @param initiator the initiator to set
	 */
	public void setInitiator(String initiator) {
		this.initiator = initiator;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	public RemoteStringArray getRemoteArrayOffer(NetworkObject synch) {
		RemoteStringArray ar = new RemoteStringArray(6, synch);
		ar.set(0, String.valueOf(a));
		ar.set(1, String.valueOf(b));
		ar.set(2, String.valueOf(rel));
		ar.set(3, message);
		ar.set(4, initiator);
		ar.set(5, String.valueOf(revoke));
		return ar;
	}

	public void set(String initiator, int a, int b, byte relation, String message, boolean revoke) {
		this.a = a;
		this.b = b;
		this.rel = relation;
		this.message = message;
		this.initiator = initiator;
		this.revoke = revoke;
	}

	@Override
	public String toString() {
		return "RelOffer[a=" + a + ", b=" + b + ", rel=" + getRelation().name() + "]";
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] subs = (Tag[]) tag.getValue();

		a = (Integer) subs[0].getValue();
		b = (Integer) subs[1].getValue();
		rel = (Byte) subs[2].getValue();
		message = (String) subs[3].getValue();
		initiator = (String) subs[4].getValue();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.INT, null, a),
				new Tag(Type.INT, null, b),
				new Tag(Type.BYTE, null, rel),
				new Tag(Type.STRING, null, message),
				new Tag(Type.STRING, null, initiator),
				FinishTag.INST});
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.faction.FactionRelation#getCode()
	 */
	@Override
	public long getCode() {
		return getOfferCode(a, b);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.faction.FactionRelation#set(int, int)
	 */
	@Override
	public void set(int a, int b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((initiator == null) ? 0 : initiator.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (revoke ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof FactionRelationOffer)) {
			return false;
		}
		FactionRelationOffer other = (FactionRelationOffer) obj;
		if (initiator == null) {
			if (other.initiator != null) {
				return false;
			}
		} else if (!initiator.equals(other.initiator)) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (revoke != other.revoke) {
			return false;
		}
		return true;
	}
	
	
}
