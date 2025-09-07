package org.schema.game.common.controller.bpmarket;

import api.common.GameCommon;
import org.json.JSONObject;
import org.schema.common.JsonSerializable;
import org.schema.game.client.view.gui.shop.shopnew.AddBlueprintDialog;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;

import java.util.UUID;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class BlueprintMarketData implements JsonSerializable {

	private static final byte VERSION = 0;
	public static final byte SHIP = 0;
	public static final byte STATION = 1;

	private String dataUUID = "";
	private String catalogEntryUID = "";
	private String sellerName = "";
	private int factionId;
	private String name = "";
	private String description = "";
	private String image = "";
	private long price;
	private boolean filled;
	private boolean admin;
	private BlueprintClassification type = BlueprintClassification.NONE;
	private double mass;

	public BlueprintMarketData(PlayerState playerState, int mode) {
		dataUUID = UUID.randomUUID().toString();
		catalogEntryUID = "";
		sellerName = playerState.getName();
		factionId = playerState.getFactionId();
		price = -1;
		filled = mode == AddBlueprintDialog.ADMIN_MODE;
		admin = mode == AddBlueprintDialog.ADMIN_MODE;
	}

	public BlueprintMarketData(String catalogEntryUID, String sellerName, int factionId, String name, String description, String image, long price) {
		this.catalogEntryUID = catalogEntryUID;
		this.sellerName = sellerName;
		this.factionId = factionId;
		this.name = name;
		this.description = description;
		this.image = image;
		this.price = price;
		dataUUID = UUID.randomUUID().toString();
	}

	public BlueprintMarketData(String catalogEntryUID, String sellerName, int factionId, String name, String description, long price) {
		this(catalogEntryUID, sellerName, factionId, name, description, "", price);
	}

	public BlueprintMarketData(JSONObject json) {
		fromJson(json);
	}
	
	@Override
	public int hashCode() {
		return dataUUID.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof BlueprintMarketData data) return dataUUID.equals(data.dataUUID);
		return false;
	}
	
	@Override
	public String toString() {
		return toJson().toString();
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("version", VERSION);
		json.put("dataUUID", dataUUID);
		json.put("catalogEntryUID", catalogEntryUID);
		json.put("sellerName", sellerName);
		json.put("factionId", factionId);
		json.put("name", name);
		json.put("description", description);
		json.put("image", image);
		json.put("price", price);
		json.put("filled", filled);
		json.put("admin", admin);
		json.put("type", type.toString());
		json.put("mass", mass);
		return json;
	}

	@Override
	public void fromJson(JSONObject json) {
		byte version = (byte) json.getInt("version");
		dataUUID = json.getString("dataUUID");
		catalogEntryUID = json.getString("catalogEntryUID");
		sellerName = json.getString("sellerName");
		factionId = json.getInt("factionId");
		name = json.getString("name");
		description = json.getString("description");
		image = json.getString("image");
		price = json.getLong("price");
		filled = json.getBoolean("filled");
		admin = json.getBoolean("admin");
		type = BlueprintClassification.valueOf(json.getString("type"));
		mass = json.getDouble("mass");
	}

	public String getDataUUID() {
		return dataUUID;
	}

	public String getCatalogEntryUID() {
		return catalogEntryUID;
	}

	public void setCatalogEntryUID(String catalogEntryUID) {
		this.catalogEntryUID = catalogEntryUID;
	}

	public String getSellerName() {
		return sellerName;
	}

	public int getFactionId() {
		return factionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image.trim();
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public int calculateByteLength() {
		return 1 + dataUUID.length() + catalogEntryUID.length() + sellerName.length() + name.length() + description.length() + image.length() + 24;
	}

	public void setFromCatalogEntry(CatalogPermission permission) {
		catalogEntryUID = permission.getUid();
		name = permission.getUid();
		description = permission.description;
		price = permission.price;
		mass = permission.mass;
	}

	public boolean isFilled() {
		return filled;
	}

	public void setFilled(boolean filled) {
		this.filled = filled;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public BlueprintClassification getType() {
		return type;
	}

	public void setType(BlueprintClassification type) {
		this.type = type;
	}

	public String getFactionName() {
		return GameCommon.getGameState().getFactionManager().getFactionName(factionId);
	}

	public double getMass() {
		return mass;
	}
}
