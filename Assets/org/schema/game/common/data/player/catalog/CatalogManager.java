package org.schema.game.common.data.player.catalog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.schema.common.LogUtil;
import org.schema.game.client.controller.CatalogChangeListener;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.io.SegmentSerializationBuffersGZIP;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Universe;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.remote.RemoteCatalogEntry;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.CatalogEntryNotFoundException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.controller.ImportFailedException;
import org.schema.game.server.controller.MayImportCallback;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprint.BluePrintWriteQueueElement;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.objects.remote.RemoteByteArrayDyn;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class CatalogManager implements DiskWritable {
	public static final String CALAOG_FILE_NAME = "CATALOG";
	public static final String CALAOG_FILE_EXTENSION = "cat";
	public static final String CALAOG_FILE_NAME_FULL = CALAOG_FILE_NAME + "." + CALAOG_FILE_EXTENSION;
	private final SendableGameState gameState;
	private final Object2ObjectOpenHashMap<String, CatalogRawEntry> rawBlueprints = new Object2ObjectOpenHashMap<String, CatalogRawEntry>();
	private final Object2ObjectOpenHashMap<String, CatalogPermission> serverCatalog = new Object2ObjectOpenHashMap<String, CatalogPermission>();
	private final Object2ObjectOpenHashMap<String, Object2IntArrayMap<String>> serverCatalogRatings = new Object2ObjectOpenHashMap<String, Object2IntArrayMap<String>>();
	private final List<CatalogRating> catalogRatingsToAddClient = new ObjectArrayList<CatalogRating>();
	private final List<CatalogPermission> catalogToAddClient = new ObjectArrayList<CatalogPermission>();
	private final List<CatalogPermission> catalogToRemoveClient = new ObjectArrayList<CatalogPermission>();
	private final List<CatalogPermission> catalogChangeRequests = new ObjectArrayList<CatalogPermission>();
	private final List<CatalogPermission> catalogDeleteRequests = new ObjectArrayList<CatalogPermission>();
	private final ObjectOpenHashSet<CatalogPermission> clientCatalogPermissions = new ObjectOpenHashSet<CatalogPermission>();
	private boolean flagServerCatalogChanged;
	private boolean hasAnyEnemySpawnable;
	private long lastSendEnemySpawnMsg;
	private boolean onServer;
	private boolean flagServerCatalogAddedOrRemoved;

	public final List<CatalogChangeListener> listeners = new ObjectArrayList<CatalogChangeListener>();
	
	public CatalogManager(SendableGameState gameState) {
		super();
		this.gameState = gameState;
		onServer = gameState.isOnServer();
		if (gameState.isOnServer()) {

			refreshServerRawBlueprints();

			try {
				Tag readEntity = ((GameServerState) gameState.getState())
						.getController().readEntity(CALAOG_FILE_NAME, CALAOG_FILE_EXTENSION);
				this.fromTagStructure(readEntity);

				// add default permission for catalog entries without permission
				initializeNewDefautCatalog(false);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (EntityNotFountException e) {
				System.err.println("[SERVER] NO DB CATALOG FOUND ON DISK: "
						+ e.getMessage());
				// add default permission for all catalog entries
				initializeNewDefautCatalog(true);
			}
		}

		lastSendEnemySpawnMsg = System.currentTimeMillis();
	}

	public static boolean isValidCatalogType(org.schema.game.server.data.blueprintnw.BlueprintType type) {
		return type == org.schema.game.server.data.blueprintnw.BlueprintType.SHIP || type == org.schema.game.server.data.blueprintnw.BlueprintType.SPACE_STATION;
	}


	private void addRawCatalogEntry(BlueprintEntry e) {
		rawBlueprints.put(
				e.getName(),
				new CatalogRawEntry(e.getName(), e.getPrice(), e.getScore(), e
						.getEntityType(), e.getClassification(), (float)e.getMass()));
	}

	public void addServerEntry(BlueprintEntry e, String ownerName, boolean privatePerm) {
		CatalogPermission p = new CatalogPermission();
		p.setUid(e.getName());
		p.ownerUID = ownerName;
		p.type = e.getEntityType();
		p.setClassification(e.getClassification());
		p.score = e.getScore();
		if(p.date == 0){
			p.date = System.currentTimeMillis();
		}
		System.err
				.println("[SERVER][CATALOG] ADDING ENTRY FROM RAW BLUEPRINT: "
						+ e.getName() + " for " + ownerName + "; price: "
						+ e.getPrice());
		p.price = e.getPrice();
		p.mass = (float)e.getMass();
		if (privatePerm) {
			p.permission &= ~CatalogPermission.P_BUY_FACTION;
			p.permission &= ~CatalogPermission.P_BUY_OTHERS;
			p.permission &= ~CatalogPermission.P_BUY_HOME_ONLY;
		}
		p.description = Lng.str("no description given");
		p.permission &= ~CatalogPermission.P_LOCKED;

		addRawCatalogEntry(e);

		serverCatalog.put(p.getUid(), p);

		if (isValidCatalogType(e.getEntityType())) {
			gameState.getNetworkObject().catalogBuffer
					.add(new RemoteCatalogEntry(p, gameState.getNetworkObject()));
		}
		flagServerCatalogChanged = true;
		flagServerCatalogAddedOrRemoved = true;
		lastModified = System.currentTimeMillis();
	}

	public boolean canEdit(CatalogPermission receivedCat) {

		String initiator = receivedCat.ownerUID;

		CatalogPermission perm = serverCatalog.get(receivedCat.getUid());

		boolean isAdminAndForced = receivedCat.changeFlagForced;

		if (perm == null) {
			((GameServerState) gameState.getState()).getController()
					.sendPlayerMessage(initiator,
							Lng.astr("ERROR\nCatalog entry does not exist!"),
							ServerMessage.MESSAGE_TYPE_ERROR);
			return false;
		}

		if (!perm.ownerUID.toLowerCase(Locale.ENGLISH).equals(initiator.toLowerCase(Locale.ENGLISH)) && !isAdminAndForced) {
			((GameServerState) gameState.getState()).getController()
					.sendPlayerMessage(initiator,
							Lng.astr("ERROR\nYou do not own this entry!"),
							ServerMessage.MESSAGE_TYPE_ERROR);
			return false;
		}
		System.err.println("[CATALOG] CHANGING REQUEST by " + initiator + ": "
				+ receivedCat + "; ADMIN: " + isAdminAndForced);
		return true;
	}

	public void clientRate(String initiator, String catalogEntry, int rating) {
		rating = Math.max(0, Math.min(CatalogPermission.MAX_RATING, rating));

		RemoteStringArray a = new RemoteStringArray(3,
				gameState.getNetworkObject());
		a.set(0, initiator);
		a.set(1, catalogEntry);
		a.set(2, String.valueOf(rating));

		gameState.getNetworkObject().catalogRatingBuffer.add(a);
	}

	public void clientRequestCatalogEdit(CatalogPermission p) {
		
		
		gameState.getNetworkObject().catalogChangeRequestBuffer
				.add(new RemoteCatalogEntry(p, gameState.getNetworkObject()));
	}

	public void clientRequestCatalogRemove(CatalogPermission p) {
		gameState.getNetworkObject().catalogDeleteRequestBuffer
				.add(new RemoteCatalogEntry(p, gameState.getNetworkObject()));
	}

	@Override
	public void fromTagStructure(Tag tag) {
		if ("cv0".equals(tag.getName())) {
			Tag[] subs = (Tag[]) tag.getValue();

			Tag[] perms = (Tag[]) subs[0].getValue();

			for (int i = 0; i < perms.length - 1; i++) {
				CatalogPermission p = new CatalogPermission();
				p.fromTagStructure(perms[i]);

				if (rawBlueprints.containsKey(p.getUid())) {
					if (p.mass == 0 || p.type == null) {
						try {
							BlueprintEntry blueprint = BluePrintController.active.getBlueprint( p.getUid());

							if (p.mass == 0) {
								p.mass = (float)blueprint.getMass();
							}
							if (p.type == null) {
								p.type = blueprint.getEntityType();
							}

						} catch (EntityNotFountException e) {
							e.printStackTrace();
							System.err.println("[Exception] catched: NOT ADDING ENTRY: " + p.getUid() + " to catalog (name not found)");
							continue;
						}
					}
					BlueprintEntry blueprint;
					try {
						blueprint = BluePrintController.active.getBlueprint(p.getUid());
						p.score = blueprint.getScore();
					} catch (EntityNotFountException e) {
						e.printStackTrace();
					}
					serverCatalog.put(p.getUid(), p);

					if (p.enemyUsable() && p.type.enemySpawnable()) {
						hasAnyEnemySpawnable = true;
					}
				} else {
					System.err
							.println("[CATALOG][ERROR] not found in raw Catalog: "
									+ p.getUid());
				}
			}
			Tag[] ratings = (Tag[]) subs[1].getValue();
			for (int i = 0; i < ratings.length - 1; i++) {
				ratingEntryFromTag(ratings[i]);
			}
			recalcAllRatings();

		}
	}

	@Override
	public Tag toTagStructure() {
		Tag[] t = new Tag[serverCatalog.size() + 1];
		t[serverCatalog.size()] = FinishTag.INST;
		int i = 0;
		for (CatalogPermission p : serverCatalog.values()) {

			t[i] = p.toTagStructure();
			i++;
		}
		Tag permission = new Tag(Type.STRUCT, "pv0", t);

		Tag[] ratingsTag = new Tag[serverCatalogRatings.size() + 1];
		ratingsTag[serverCatalogRatings.size()] = new Tag(Type.FINISH, null,
				null);
		i = 0;
		for (Entry<String, Object2IntArrayMap<String>> entry : serverCatalogRatings
				.entrySet()) {
			ratingsTag[i] = ratingEntryToTag(entry.getKey(), entry.getValue());
			i++;
		}
		Tag ratings = new Tag(Type.STRUCT, "r0", ratingsTag);

		return new Tag(Type.STRUCT, "cv0", new Tag[]{permission, ratings,
				FinishTag.INST});
	}

	/**
	 * @return the catalog
	 */
	public Collection<CatalogPermission> getCatalog() {
		if (gameState.isOnServer()) {
			return serverCatalog.values();
		} else {
			return clientCatalogPermissions;
		}
	}

	@Override
	public String getUniqueIdentifier() {
		return "CATALOG";
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	public void importEntry(File file, final String playerName) throws ImportFailedException, IOException {

		List<BlueprintEntry> importFile = BluePrintController.active
				.importFile(file, new MayImportCallback() {

					@Override
					public void callbackOnImportDenied(BlueprintEntry e) {
						((GameServerState) gameState.getState())
								.getController()
								.sendPlayerMessage(
										playerName,
										Lng.astr("Upload failed!\nEntry already exists!"),
										ServerMessage.MESSAGE_TYPE_ERROR);

						LogUtil.log().fine("[BLUEPRINT] " + playerName + ": IMPORT OF BLUEPRINT DENIED: " + e.getName());
					}

					@Override
					public boolean mayImport(BlueprintEntry e) {
						return !serverCatalog.containsKey(e.getName());
					}

					@Override
					public void onImport(BlueprintEntry e) {
						addServerEntry(e, playerName, ServerConfig.BLUEPRINT_DEFAULT_PRIVATE.isOn());

						LogUtil.log().fine("[BLUEPRINT] " + playerName + ": IMPORT OF BLUEPRINT DONE: " + e.getName());
					}
				});

	}

	public void initFromNetworkObject(NetworkGameState o) {
		// updateFromNetworkObject(o);
		// normal update update is autoperformed after init
	}

	private void initializeNewDefautCatalog(boolean newCatalog) {
		int i = 0;
		assert (gameState.isOnServer());

		ArrayList<CatalogRawEntry> init = new ArrayList<CatalogRawEntry>(
				rawBlueprints.size());
		init.addAll(rawBlueprints.values());

		Collections.sort(init, (o1, o2) -> {
			//sicne these are longs, do NOT use (int)(a - b)
			//Also Long.compareTo does not work on some
			//java dists

			if (o1.price == o2.price) {
				return 0;
			} else if (o1.price > o2.price) {
				return 1;
			} else {
				return -1;
			}
		});
		for (CatalogRawEntry e : init) {
			if (!serverCatalog.containsKey(e.name)) {

				//entry is not yet in the catalog

				CatalogPermission p = new CatalogPermission();
				p.permission |= CatalogPermission.getDefaultCatalogPermission();
				p.type = e.entityType;
				p.setClassification(e.classification);
				p.ownerUID = "(unknown)";
				p.setUid(e.name);
				p.score = e.score;
				p.price = e.price;
				p.mass = e.mass;
				p.date = System.currentTimeMillis();
				p.description = "no description given";

				// System.err.println("[CATALOGMANAGER][SERVER] ADDING TO SERVER CATALOG: "+p);
				serverCatalog.put(p.getUid(), p);

				i++;
			}
		}
		BluePrintController.active.setImportedByDefault(false);
		flagServerCatalogChanged = true;
	}

	private void ratingEntryFromTag(Tag mapTag) {

		String entry = mapTag.getName();

		Object2IntArrayMap<String> object2IntArrayMap = new Object2IntArrayMap<String>();
		serverCatalogRatings.put(entry, object2IntArrayMap);

		Tag[] subs = (Tag[]) mapTag.getValue();

		for (int i = 0; i < subs.length - 1; i++) {

			Tag[] en = (Tag[]) subs[i].getValue();
			object2IntArrayMap.put((String) en[0].getValue(),
					((Byte) en[1].getValue()).intValue());
		}

	}

	private Tag ratingEntryToTag(String entry, Object2IntArrayMap<String> map) {
		Tag[] a = new Tag[map.size() + 1];
		a[map.size()] = FinishTag.INST;
		int i = 0;
		for (Entry<String, Integer> s : map.entrySet()) {
			a[i] = new Tag(Type.STRUCT, null, new Tag[]{
					new Tag(Type.STRING, null, s.getKey()),
					new Tag(Type.BYTE, null, s.getValue().byteValue()),
					FinishTag.INST});
			i++;
		}

		return new Tag(Type.STRUCT, entry, a);
	}

	public void recalcAllRatings() {
		for (Entry<String, Object2IntArrayMap<String>> p : serverCatalogRatings
				.entrySet()) {
			CatalogPermission catalogPermission = serverCatalog.get(p.getKey());
			if (catalogPermission != null) {
				catalogPermission.recalculateRating(p.getValue());
			}
		}
	}

	public void refreshServerRawBlueprints() {
		long timestamp = System.currentTimeMillis();
		List<BlueprintEntry> readBluePrints = BluePrintController.active
				.readBluePrints();
		rawBlueprints.clear();
		System.err.println("[SERVER][CATALOG] READ RAW BLUEPRINTS: " + readBluePrints.size());
		for (BlueprintEntry e : readBluePrints) {
			// System.err.println("READ RAW BLUEPRINT: "+e);
			// e.buyable, e.privacy, e.ownderId
			addRawCatalogEntry(e);

		}
	}

	/**
	 * @return the catalogDeleteRequests
	 */
	public boolean serverDeletEntry(String catUID) {
		if(serverCatalog.containsKey(catUID)) {
			CatalogPermission p = new CatalogPermission();
			p.setUid(catUID);
			p.ownerUID = "ADMIN";
			p.changeFlagForced = true; // admin force
			synchronized (catalogDeleteRequests) {
				catalogDeleteRequests.add(p);
			}
			return true;
		}else {
			return false;
		}

	}
	public String serverGetInfo(String catUID) {
		CatalogPermission p = serverCatalog.get(catUID);
		if(p != null) {
			
			BlueprintEntry blueprint;
			try {
				blueprint = BluePrintController.active.getBlueprint(p.getUid());
			
				StringBuffer b = new StringBuffer();
				b.append("UID: "+p.getUid());b.append("\n");
				b.append("Owner: "+p.ownerUID);b.append("\n");
				b.append("DateMS: "+p.date);b.append("\n");
				b.append("DateReadable: "+(new Date(p.date)).toString());b.append("\n");
				b.append("Description: "+p.description);b.append("\n");
				b.append("Mass: "+p.mass);b.append("\n");
				b.append("SpawnCount: "+p.timesSpawned);b.append("\n");
				b.append("Price: "+p.price);b.append("\n");
				b.append("Rating: "+p.rating);b.append("\n");
				b.append("Blocks: "+blueprint.getElementMap().getTotalAmount());b.append("\n");
				b.append("BlocksInclChilds: "+blueprint.getElementCountMapWithChilds().getTotalAmount());b.append("\n");
				b.append("DockCountOnMother: "+blueprint.getChilds().size());b.append("\n");
				BoundingBox total = new BoundingBox();
				blueprint.calculateTotalBb(total);
				b.append("DimensionInclChilds: "+total);b.append("\n");
				b.append("PermissionMask: "+p.permission);b.append("\n");
				b.append("PermissionFaction: "+p.faction());b.append("\n");
				b.append("PermissionHomeOnly: "+p.homeOnly());b.append("\n");
				b.append("PermissionOthers: "+p.others());b.append("\n");
				b.append("PermissionEnemyUsable: "+p.enemyUsable());b.append("\n");
				b.append("PermissionLocked: "+p.locked());b.append("\n");
				return b.toString();
			} catch (EntityNotFountException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	public boolean serverChangeOwner(String catUID, String owner) {
		if(serverCatalog.containsKey(catUID)) {
			CatalogPermission p = new CatalogPermission(serverCatalog.get(catUID));
			p.setUid(catUID);
			p.ownerUID = owner;
			p.changeFlagForced = true; // admin force
			synchronized (catalogChangeRequests) {
				catalogChangeRequests.add(p);
			}
			return true;
		}else {
			return false;
		}
	}
	public void updateFromNetworkObject(NetworkGameState networkObject) {
		for (RemoteStringArray s : networkObject.catalogRatingBuffer
				.getReceiveBuffer()) {
			assert (gameState.isOnServer());
			String initiator = s.get(0).get();
			String catalogEntry = s.get(1).get();
			int rating = Integer.parseInt(s.get(2).get());
			CatalogRating r = new CatalogRating(initiator, catalogEntry, rating);
			synchronized (catalogRatingsToAddClient) {
				catalogRatingsToAddClient.add(r);
			}

		}
		for (RemoteByteArrayDyn ed : networkObject.catalogBufferDeflated.getReceiveBuffer()){
			
			SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
			try {
				assert (!gameState.isOnServer());
				byte[] rec = ed.get();
				
				byte[] buffer = bm.SEGMENT_BUFFER;
				Inflater inflater = bm.inflater;
			
				DataInputStream inputStream = new DataInputStream(new FastByteArrayInputStream(rec));
				int fullSize = inputStream.readInt();
				int zipSize = inputStream.readInt();
				
				System.err.println("[CLIENT][CATALOG] Received compressed catalog: Compressed "+zipSize/1024+" kb; uncompressed: "+fullSize/1024+"kb");
				
				byte[] byteArrayStream = bm.getStaticArray(fullSize);
				
				int read = inputStream.read(buffer, 0, zipSize);

				assert (read == zipSize) : read + "/" + zipSize;
				inflater.reset();

				inflater.setInput(buffer, 0, zipSize);

				int inflate;
				
				inflate = inflater.inflate(byteArrayStream, 0, fullSize);

				assert (inflate == fullSize) : inflate + " / " + fullSize;

				DataInputStream ds = new DataInputStream(new FastByteArrayInputStream(byteArrayStream, 0, fullSize));
				
				int sizeCatalog = ds.readInt();
				
				synchronized (catalogToAddClient) {
					for(int i = 0; i < sizeCatalog; i++){
						CatalogPermission catalogEntry = new CatalogPermission();
						catalogEntry.deserialize(ds, 0, onServer);
						catalogToAddClient.add(catalogEntry);
					}
				}
				
				ds.close();
				if (inflate == 0) {
					System.err.println("[PRICES] WARNING: INFLATED BYTES 0: " + inflater.needsInput() + " " + inflater.needsDictionary());
				}
	
					
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}finally {
				SegmentSerializationBuffersGZIP.free(bm);
			}
		}
		for (RemoteCatalogEntry s : networkObject.catalogBuffer
				.getReceiveBuffer()) {
			assert (!gameState.isOnServer());
			CatalogPermission catalogEntry = s.get();
			assert(catalogEntry.getUid() != null);
			synchronized (catalogToAddClient) {
				catalogToAddClient.add(catalogEntry);
			}
		}

		for (RemoteCatalogEntry s : networkObject.catalogDeleteBuffer
				.getReceiveBuffer()) {
			assert (!gameState.isOnServer());
			CatalogPermission catalogEntry = s.get();

			synchronized (catalogToRemoveClient) {
				catalogToRemoveClient.add(catalogEntry);
			}
		}

		for (RemoteCatalogEntry s : networkObject.catalogChangeRequestBuffer
				.getReceiveBuffer()) {
			assert (gameState.isOnServer());
			CatalogPermission catalogEntry = s.get();
			System.err.println("[SERVER] received catalog change request: "
					+ catalogEntry);
			synchronized (catalogChangeRequests) {
				catalogChangeRequests.add(catalogEntry);
			}
		}
		for (RemoteCatalogEntry s : networkObject.catalogDeleteRequestBuffer
				.getReceiveBuffer()) {
			assert (gameState.isOnServer());
			CatalogPermission catalogEntry = s.get();
			System.err.println("[SERVER] received catalog delete request: "
					+ catalogEntry);
			synchronized (catalogDeleteRequests) {
				catalogDeleteRequests.add(catalogEntry);
			}
		}
	}

	public void updateLocal() {
		boolean changed = false;
		if (!gameState.isOnServer()) {

			if (!catalogToAddClient.isEmpty()) {
				synchronized (catalogToAddClient) {
					while (!catalogToAddClient.isEmpty()) {
						CatalogPermission cat = catalogToAddClient.remove(0);
//						System.err.println("REMOVING AND READDING: "+cat);
						CatalogPermission catalogPermission = clientCatalogPermissions.get(cat);
						if (catalogPermission != null) {
							catalogPermission.apply(cat);
						} else {
							clientCatalogPermissions.add(cat);
						}
						changed = true;
					}
				}
			}
			if (!catalogToRemoveClient.isEmpty()) {
				synchronized (catalogToRemoveClient) {
					while (!catalogToRemoveClient.isEmpty()) {
						CatalogPermission cat = catalogToRemoveClient.remove(0);
						clientCatalogPermissions.remove(cat);
						changed = true;
					}
				}
			}

		} else {

			if (!hasAnyEnemySpawnable && System.currentTimeMillis() - lastSendEnemySpawnMsg > (1000 * 60) * 5) {
				lastSendEnemySpawnMsg = System.currentTimeMillis() + (1000 * 60) * 60 * 2; //one after 5 min. then every 5 hours
				((GameServerState) gameState.getState()).getController().broadcastMessageAdmin(Lng.astr("Admin Message:\nThere are currently\nno blueprints selected\nto spawn as NPC!\n(change in catalog->admin panel)"), ServerMessage.MESSAGE_TYPE_ERROR);
			}

			if (!catalogRatingsToAddClient.isEmpty()) {
				synchronized (catalogRatingsToAddClient) {
					while (!catalogRatingsToAddClient.isEmpty()) {
						CatalogRating rating = catalogRatingsToAddClient
								.remove(0);

						CatalogPermission catalogPermission = serverCatalog
								.get(rating.catalogEntry);
						if (catalogPermission != null) {
							Object2IntArrayMap<String> object2IntArrayMap = serverCatalogRatings
									.get(rating.catalogEntry);
							if (object2IntArrayMap == null) {
								object2IntArrayMap = new Object2IntArrayMap();
								serverCatalogRatings.put(rating.catalogEntry,
										object2IntArrayMap);
							}

							int previous = object2IntArrayMap.put(
									rating.initiator, rating.rating);

							catalogPermission
									.recalculateRating(object2IntArrayMap);

							gameState.getNetworkObject().catalogBuffer
									.add(new RemoteCatalogEntry(
											catalogPermission, gameState
											.getNetworkObject()));

							((GameServerState) gameState.getState())
									.getController().sendPlayerMessage(
									rating.initiator,
									Lng.astr("Rated %s:\nYour Rating: %s\nYour Rating before:", catalogPermission.getUid(),  rating.rating,  previous),
									ServerMessage.MESSAGE_TYPE_INFO);
							changed = true;
						} else {
							System.err
									.println("[SERVER][CATALOG][ERROR] cannot rate: "
											+ rating.catalogEntry
											+ ": entry not found");
						}

					}
				}
			}
			if (!catalogChangeRequests.isEmpty()) {
				synchronized (catalogChangeRequests) {
					while (!catalogChangeRequests.isEmpty()) {
						CatalogPermission receivedCat = catalogChangeRequests
								.remove(0);

						if (!canEdit(receivedCat)) {
							continue;
						}
						CatalogPermission current = serverCatalog
								.get(receivedCat.getUid());
						receivedCat.rating = current.rating;

						System.err
								.println("[SERVER][CATLOG] permission granted to change: "
										+ receivedCat);

						serverCatalog.put(receivedCat.getUid(), receivedCat);

						// distribute to clients
						gameState.getNetworkObject().catalogBuffer
								.add(new RemoteCatalogEntry(receivedCat,
										gameState.getNetworkObject()));
						changed = true;
					}
				}
			}
			if (!catalogDeleteRequests.isEmpty()) {
				synchronized (catalogDeleteRequests) {
					while (!catalogDeleteRequests.isEmpty()) {
						CatalogPermission receivedCat = catalogDeleteRequests
								.remove(0);

						System.err
								.println("[SERVER][CATALOG] handling delete request: "
										+ receivedCat);

						if (!canEdit(receivedCat)) {
							continue;
						}
						CatalogPermission current = serverCatalog
								.get(receivedCat.getUid());
						receivedCat.rating = current.rating;

						System.err
								.println("[SERVER][CATLOG] permission granted to delete: "
										+ receivedCat);
						serverCatalog.remove(receivedCat.getUid());
						serverCatalogRatings.remove(receivedCat.getUid());
						lastModified = System.currentTimeMillis();
						flagServerCatalogAddedOrRemoved = true;
						BlueprintEntry e = new BlueprintEntry(
								receivedCat.getUid());

						try {
							BluePrintController.active.export(e.getName());
							System.err
									.println("[SERVER] PHYSICALLY DELETING BLUEPRINT ENTRY (backup export): "
											+ e.getName());
							BluePrintController.active.removeBluePrint(e);
						} catch (IOException e1) {
							((GameServerState) gameState.getState())
									.getController()
									.sendPlayerMessage(
											receivedCat.ownerUID,
											Lng.astr("ERROR\nThere was an IO error,\ndeleting the entry...\nPlease report!"),
											ServerMessage.MESSAGE_TYPE_ERROR);
							e1.printStackTrace();
						} catch (CatalogEntryNotFoundException e1) {
							((GameServerState) gameState.getState())
									.getController().sendPlayerMessage(
									receivedCat.ownerUID,
									Lng.astr("ERROR\nEntry not found!\nplease report!"),
									ServerMessage.MESSAGE_TYPE_ERROR);
							e1.printStackTrace();
						}

						// distribute to clients
						gameState.getNetworkObject().catalogDeleteBuffer
								.add(new RemoteCatalogEntry(receivedCat,
										gameState.getNetworkObject()));
						changed = true;
					}
				}
			}

			if (flagServerCatalogChanged) {
				changed = true;
				flagServerCatalogChanged = false;
			}
		}
		if (changed) {
			hasAnyEnemySpawnable = false;
			for (CatalogPermission p : serverCatalog.values()) {
				if (p.enemyUsable()) {
					hasAnyEnemySpawnable = true;
					break;
				}
			}
			for(CatalogChangeListener s : listeners) {
				s.onCatalogChanged();
			}
		}
		if(flagServerCatalogAddedOrRemoved){
			if(onServer){
				try {
					System.err.println("[SERVER][CATALOGMANAGER]WRITING CATALOG TO DISK");
					writeToDisk();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			flagServerCatalogAddedOrRemoved = false;
		}
	}
	private static FastByteArrayOutputStream b = new FastByteArrayOutputStream(1024*1024);
	private static long lastModified;
	private static long lastCached = -1;
	private static byte[] currentCatalogDeflatedArray;
	public void updateToFullNetworkObject(NetworkGameState networkObject) {
		try {
			synchronized(b){
				if(lastModified != lastCached){
					lastCached = lastModified;
					b.reset();
					DataOutputStream d = new DataOutputStream(b);
					List<CatalogPermission> ml = new ObjectArrayList<CatalogPermission>();
					for (CatalogPermission p : serverCatalog.values()) {
						CatalogRawEntry raw = rawBlueprints.get(p.getUid());
						if (raw != null
								&& isValidCatalogType(raw.entityType)) {
							ml.add(p);
						}
					}
					d.writeInt(ml.size());
					for (CatalogPermission p : ml) {
						p.serialize(d, onServer);
					}
					final SegmentSerializationBuffersGZIP bb = SegmentSerializationBuffersGZIP.get();
					try {
						byte[] buffer = bb.SEGMENT_BUFFER;
						Deflater deflater = bb.deflater;
						int fullSize = (int) b.position();
						deflater.reset();
						deflater.setInput(b.array, 0, fullSize);
						deflater.finish();
						
						int zipSize = deflater.deflate(buffer);
						
						System.err.println("[SERVER][CATALOG] Deflated Catalog from "+fullSize+" kb to "+zipSize+" kb");
						b.reset();
						d.writeInt(fullSize);
						d.writeInt(zipSize);
						d.write(buffer, 0, zipSize);
						byte[] catalog = new byte[(int)b.position()];
						System.arraycopy(b.array, 0, catalog, 0, (int)b.position());
						currentCatalogDeflatedArray = catalog;
					}finally {
						SegmentSerializationBuffersGZIP.free(bb);
					}
				
				}
			}
			
			networkObject.catalogBufferDeflated.add(new RemoteByteArrayDyn(currentCatalogDeflatedArray, networkObject));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		for (CatalogPermission p : serverCatalog.values()) {
//			CatalogRawEntry raw = rawBlueprints.get(p.getUid());
//			
//			
//			if (raw != null
//					&& isValidCatalogType(raw.entityType)) {
//				networkObject.catalogBuffer.add(new RemoteCatalogEntry(p,
//						networkObject));
//			} 
//		}
	}

	public void updateToNetworkObject(NetworkGameState networkObject) {

	}

	public void writeEntryClient(BluePrintWriteQueueElement e, String pStateName) throws IOException {
		assert (!onServer);
		assert (e.local);
		e.segmentController.setAllTouched(true);
		e.segmentController.writeAllBufferedSegmentsToDatabase(true, true, true);
		BluePrintController.active.writeBluePrint(e.segmentController, e.name, e.local, e.classification);
	}

	public void writeEntryServer(BluePrintWriteQueueElement e, String pStateName)
			throws IOException {
		assert (onServer);
		e.segmentController.setAllTouched(true);
		if (ServerConfig.CATALOG_NAME_COLLISION_HANDLING.isOn()) {
			int i = 0;
			String n = e.name;

			while (existsLowerCase(n)) {
				n = e.name + String.valueOf(i);
				i++;
			}
			e.name = n;

			e.segmentController.writeAllBufferedSegmentsToDatabase(true, true, false);
			BluePrintController.active.writeBluePrint(e.segmentController, e.name, e.local, e.classification);
			List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();

			for (int j = 0; j < readBluePrints.size(); j++) {
				if (readBluePrints.get(j).getName().equals(e.name)) {
					addServerEntry(readBluePrints.get(j), pStateName, ServerConfig.BLUEPRINT_DEFAULT_PRIVATE.isOn());
					break;
				}
			}

		} else {
			if (existsLowerCase(e.name)) {
				for (Entry<String, CatalogPermission> name : serverCatalog.entrySet()) {
					if (name.getKey().toLowerCase(Locale.ENGLISH).equals(e.name.toLowerCase(Locale.ENGLISH))) {

						CatalogPermission p = name.getValue();
						if (!p.ownerUID.toLowerCase(Locale.ENGLISH).equals(pStateName.toLowerCase(Locale.ENGLISH))) {
							((GameServerState) gameState.getState()).getController()
									.sendPlayerMessage(pStateName,
											Lng.astr("Upload failed!\nEntry already exists\nand you don't own it!"),
											ServerMessage.MESSAGE_TYPE_ERROR);
							//no right to update
							return;
						}
					}
				}

				e.segmentController.writeAllBufferedSegmentsToDatabase(true, true, false);
				BluePrintController.active.writeBluePrint(e.segmentController, e.name, e.local, e.classification);
				List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();

				for (int j = 0; j < readBluePrints.size(); j++) {
					if (readBluePrints.get(j).getName().equals(e.name)) {
						updateServerEntry(e.name, readBluePrints.get(j), pStateName, ServerConfig.BLUEPRINT_DEFAULT_PRIVATE.isOn(), false);
						break;
					}
				}
			} else {
				e.segmentController.writeAllBufferedSegmentsToDatabase(true, true, false);
				BluePrintController.active.writeBluePrint(e.segmentController, e.name, e.local, e.classification);
				List<BlueprintEntry> readBluePrints = BluePrintController.active.readBluePrints();

				for (int j = 0; j < readBluePrints.size(); j++) {
					if (readBluePrints.get(j).getName().equals(e.name)) {
						addServerEntry(readBluePrints.get(j), pStateName, ServerConfig.BLUEPRINT_DEFAULT_PRIVATE.isOn());
						break;
					}
				}
			}
		}

	}

	public boolean isOnServer() {
		return onServer;
	}

	public boolean writeEntryAdmin(SegmentController e, String catName,
	                               String playerName, BlueprintClassification classification, boolean admin) throws IOException {

		e.setAllTouched(true);
		e.writeAllBufferedSegmentsToDatabase(true, false, false);
		BluePrintController.active.writeBluePrint(e, catName, false, classification);
		List<BlueprintEntry> readBluePrints = BluePrintController.active
				.readBluePrints();
		for (int i = 0; i < readBluePrints.size(); i++) {
			if (readBluePrints.get(i).getName().equals(catName)) {
				if (!existsLowerCase(catName)) {
					addServerEntry(readBluePrints.get(i), playerName, ServerConfig.BLUEPRINT_DEFAULT_PRIVATE.isOn());
					return true;
				} else {
					boolean updated = updateServerEntry(catName, readBluePrints.get(i), playerName, ServerConfig.BLUEPRINT_DEFAULT_PRIVATE.isOn(), admin);
					if (!updated) {
						try {

							PlayerState playerFromName = ((GameServerState) e.getState()).getPlayerFromName(playerName);
							playerFromName.sendServerMessage(new ServerMessage(Lng.astr("Cannot save,\nname already exists!"), ServerMessage.MESSAGE_TYPE_ERROR, playerFromName.getId()));
						} catch (PlayerNotFountException e1) {
							e1.printStackTrace();
						}
					}
					return updated;
				}
			}
		}
		return false;
	}

	private boolean updateServerEntry(String catName,
	                                  BlueprintEntry e, String playerName, boolean privatePerm, boolean admin) {
		for (Entry<String, CatalogPermission> name : serverCatalog.entrySet()) {
			
			if (name.getKey().toLowerCase(Locale.ENGLISH).equals(catName.toLowerCase(Locale.ENGLISH))) {
				
				CatalogPermission p = name.getValue();
				if (!p.ownerUID.toLowerCase(Locale.ENGLISH).equals(playerName.toLowerCase(Locale.ENGLISH)) && !admin) {
					//no right to update
					return false;
				}
				assert (catName.toLowerCase(Locale.ENGLISH).equals(p.getUid().toLowerCase(Locale.ENGLISH)));
				System.err
						.println("[SERVER][CATALOG] UPDATING ENTRY FROM RAW BLUEPRINT: "
								+ catName + " for " + playerName + "; price: "
								+ e.getPrice());
				p.price = e.getPrice();
				p.mass = (float) e.getMass();
				p.score = e.getScore();
				if (privatePerm) {
					p.permission &= ~CatalogPermission.P_BUY_FACTION;
					p.permission &= ~CatalogPermission.P_BUY_OTHERS;
					p.permission &= ~CatalogPermission.P_BUY_HOME_ONLY;
				}
				p.description = Lng.str("no description given");
				p.permission &= ~CatalogPermission.P_LOCKED;

//				addRawCatalogEntry(e);
//				serverCatalog.put(p.catUID, p);

				if (isValidCatalogType(e.getEntityType())) {
					gameState.getNetworkObject().catalogBuffer
							.add(new RemoteCatalogEntry(p, gameState.getNetworkObject()));
				}
				flagServerCatalogChanged = true;
				lastModified = System.currentTimeMillis();
				return true;
			}
		}
		return false;
	}

	private boolean existsLowerCase(String catName) {
		for (String name : serverCatalog.keySet()) {
			if (name.toLowerCase(Locale.ENGLISH).equals(catName.toLowerCase(Locale.ENGLISH))) {
				return true;
			}
		}
		return false;
	}

	public void writeToDisk() throws IOException {
		Universe.write(this, "CATALOG.cat");		
	}

}
