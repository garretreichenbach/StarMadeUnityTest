package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.chat.AllChannel;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.common.data.chat.ChannelRouter.ChannelType;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.common.data.chat.FactionChannel;
import org.schema.game.common.data.chat.PublicChannel;
import org.schema.schine.network.StateInterface;

public class ChatChannelModification implements SerializationInterface {

	public ChannelModType type;

	public int[] user;

	public String[] mods;
	public String[] banned;
	public String[] muted;
	public String[] ignored;

	public String changePasswd = "";

	public String channel;
	public ChannelType createChannelType;
	public String joinPw = "";
	public String createPublicChannelPassword;
	public boolean createPublicChannelAsPermanent;
	public int sender = -1;
	private ChatChannel toCreateChannel;
	private int createFactionId;

	public ChatChannelModification(ChannelModType type, ChatChannel toCreateChannel, int... user) {
		super();
		this.type = type;
		this.user = user;
		assert (toCreateChannel != null);
		this.channel = toCreateChannel.getUniqueChannelName();
		assert (channel != null);
		if (type == ChannelModType.CREATE) {
			this.toCreateChannel = toCreateChannel;
			this.createPublicChannelAsPermanent = toCreateChannel.isPermanent();

		}
	}

	public ChatChannelModification() {
		super();
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeByte(type.ordinal());
		b.writeShort(user.length);
		for (int i = 0; i < user.length; i++) {
			b.writeInt(user[i]);
		}
		b.writeUTF(channel);

		if (type == ChannelModType.CREATE) {
			b.writeByte(toCreateChannel.getType().ordinal());
			if (toCreateChannel.getType() == ChannelType.FACTION) {
				b.writeInt(((FactionChannel) toCreateChannel).getFactionId());
			} else if (toCreateChannel.getType() == ChannelType.PUBLIC) {
				if (isOnServer) {
					//do not send literal password. just a notice that there is one
					b.writeUTF(toCreateChannel.getPassword().length() > 0 ? "#" : "");
				} else {
					//client has set password before creating
					b.writeUTF(toCreateChannel.getPassword());

				}
				b.writeBoolean(((PublicChannel) toCreateChannel).isPublic());
				b.writeBoolean(createPublicChannelAsPermanent);

				if (mods != null) {
					b.writeShort(mods.length);
					for (int i = 0; i < mods.length; i++) {
						b.writeUTF(mods[i]);
					}
				} else {
					b.writeShort(0);
				}

				if (banned != null) {
					b.writeShort(banned.length);
					for (int i = 0; i < banned.length; i++) {
						b.writeUTF(banned[i]);
					}
				} else {
					b.writeShort(0);
				}

				if (muted != null) {
					b.writeShort(muted.length);
					for (int i = 0; i < muted.length; i++) {
						b.writeUTF(muted[i]);
					}
				} else {
					b.writeShort(0);
				}

				if (ignored != null) {
					b.writeShort(ignored.length);
					for (int i = 0; i < ignored.length; i++) {
						b.writeUTF(ignored[i]);
					}
				} else {
					b.writeShort(0);
				}

			} else {

			}
		} else if (type == ChannelModType.JOINED) {
			b.writeUTF(joinPw);
		} else if (type == ChannelModType.PASSWD_CHANGE) {
			b.writeUTF(changePasswd);
		} else if (type == ChannelModType.MOD_ADDED || type == ChannelModType.MOD_REMOVED) {
			if (mods != null) {
				b.writeShort(mods.length);
				for (int i = 0; i < mods.length; i++) {
					b.writeUTF(mods[i]);
				}
			} else {
				b.writeShort(0);
			}
		} else if (type == ChannelModType.BANNED || type == ChannelModType.UNBANNED) {
			if (banned != null) {
				b.writeShort(banned.length);
				for (int i = 0; i < banned.length; i++) {
					b.writeUTF(banned[i]);
				}
			} else {
				b.writeShort(0);
			}
		} else if (type == ChannelModType.MUTED || type == ChannelModType.UNMUTED) {
			if (muted != null) {
				b.writeShort(muted.length);
				for (int i = 0; i < muted.length; i++) {
					b.writeUTF(muted[i]);
				}
			} else {
				b.writeShort(0);
			}
		} else if (type == ChannelModType.IGNORED || type == ChannelModType.UNIGNORED) {
			if (ignored != null) {
				b.writeShort(ignored.length);
				for (int i = 0; i < ignored.length; i++) {
					b.writeUTF(ignored[i]);
				}
			} else {
				b.writeShort(0);
			}
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {
		sender = updateSenderStateId;
		type = ChannelModType.values()[b.readByte()];
		user = new int[b.readShort()];
		for (int i = 0; i < user.length; i++) {
			user[i] = b.readInt();
		}
		channel = b.readUTF();
		if (type == ChannelModType.CREATE) {
			createChannelType = ChannelType.values()[b.readByte()];
			if (createChannelType == ChannelType.FACTION) {
				createFactionId = b.readInt();
			} else if (createChannelType == ChannelType.PUBLIC) {
				createPublicChannelPassword = b.readUTF();
				b.readBoolean();
				createPublicChannelAsPermanent = b.readBoolean();
				int modsSize = b.readShort();
				mods = new String[modsSize];
				for (int i = 0; i < mods.length; i++) {
					mods[i] = b.readUTF();
				}

				int bannedSize = b.readShort();
				banned = new String[bannedSize];
				for (int i = 0; i < banned.length; i++) {
					banned[i] = b.readUTF();
				}

				int mutedSize = b.readShort();
				muted = new String[mutedSize];
				for (int i = 0; i < muted.length; i++) {
					muted[i] = b.readUTF();
				}

				int ignoredSize = b.readShort();
				ignored = new String[ignoredSize];
				for (int i = 0; i < ignored.length; i++) {
					ignored[i] = b.readUTF();
				}
			}
		} else if (type == ChannelModType.JOINED) {
			joinPw = b.readUTF();
		} else if (type == ChannelModType.PASSWD_CHANGE) {
			changePasswd = b.readUTF();
		} else if (type == ChannelModType.MOD_ADDED || type == ChannelModType.MOD_REMOVED) {
			int modsSize = b.readShort();
			mods = new String[modsSize];
			for (int i = 0; i < mods.length; i++) {
				mods[i] = b.readUTF();
			}
		} else if (type == ChannelModType.BANNED || type == ChannelModType.UNBANNED) {
			int bannedSize = b.readShort();
			banned = new String[bannedSize];
			for (int i = 0; i < banned.length; i++) {
				banned[i] = b.readUTF();
			}
		} else if (type == ChannelModType.MUTED || type == ChannelModType.UNMUTED) {
			int mutedSize = b.readShort();
			muted = new String[mutedSize];
			for (int i = 0; i < muted.length; i++) {
				muted[i] = b.readUTF();
			}
		} else if (type == ChannelModType.IGNORED || type == ChannelModType.UNIGNORED) {
			int ignoredSize = b.readShort();
			ignored = new String[ignoredSize];
			for (int i = 0; i < ignored.length; i++) {
				ignored[i] = b.readUTF();
			}
		}
	}

	public ChatChannel createNewChannel(StateInterface state) {
		if (type == ChannelModType.CREATE) {
			switch (createChannelType) {
				case ALL:
					return new AllChannel(state, ++ChannelRouter.idGen);
				case DIRECT:
					assert (false) : "TODO";
					return null;
				case FACTION:
					return new FactionChannel(state, ++ChannelRouter.idGen, createFactionId);
				case PARTY:
					assert (false) : "TODO";
					return null;
				case PUBLIC:
//				if(state instanceof ClientStateInterface){
//					System.err.println("CREATING CHANNEL "+channel+" WITH PASSWD::::: "+joinPw);
//				}
					return new PublicChannel(state, ++ChannelRouter.idGen, channel, createPublicChannelPassword, createPublicChannelAsPermanent, mods);
				default:
					assert (false) : "TODO " + createChannelType.name();
					return null;

			}
		}
		return null;
	}

	public enum ChannelModType {
		UPDATE,
		JOINED,
		LEFT,
		KICKED,
		BANNED,
		UNBANNED,
		INVITED,
		CREATE,
		REMOVED_ON_NOT_ALIVE,
		MOD_ADDED,
		MOD_REMOVED,
		PASSWD_CHANGE,
		DELETE,
		MUTED,
		UNMUTED,
		IGNORED,
		UNIGNORED,
	}

}
