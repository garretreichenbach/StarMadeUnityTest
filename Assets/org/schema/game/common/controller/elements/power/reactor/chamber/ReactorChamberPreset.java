package org.schema.game.common.controller.elements.power.reactor.chamber;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schema.common.JsonSerializable;
import org.schema.common.SerializationInterface;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class ReactorChamberPreset implements SerializationInterface, TagSerializable, JsonSerializable {
	
	private static final byte VERSION = 0;
	
	private String name;
	private boolean isDefault;
	private final ObjectArrayList<ReactorElement> elements = new ObjectArrayList<>();
	
	public ReactorChamberPreset(String name, ObjectArrayList<ReactorElement> elements) {
		this.name = name;
		this.elements.addAll(elements);
		isDefault = name.equals("Default");
	}
	
	public ReactorChamberPreset(Tag tag) {
		fromTagStructure(tag);
	}
	
	public ReactorChamberPreset(JSONObject json) {
		fromJson(json);
	}
	
	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] subTags = tag.getStruct();
		byte version = subTags[0].getByte();
		name = subTags[1].getString();
		isDefault = subTags[2].getBoolean();
		Tag.listFromTagStruct(elements, subTags[3]);
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Tag.Type.STRUCT, "ReactorChamberPreset", new Tag[] {
				new Tag(Tag.Type.BYTE, "Version", VERSION),
				new Tag(Tag.Type.STRING, "Name", name),
				new Tag(Tag.Type.BYTE, "Default", isDefault),
				Tag.listToTagStruct(elements, Tag.Type.STRUCT, "Elements"),
				FinishTag.INST
		});
	}
	
	
	
	public void applyTo(ReactorTree reactorTree) {
		ObjectArrayList<ReactorElement> current = new ObjectArrayList<>(reactorTree.getActiveOrUnspecifiedChambers());
		ObjectArrayList<ReactorElement> toSet = new ObjectArrayList<>();
		ObjectArrayList<ReactorElement> toUnset = new ObjectArrayList<>(reactorTree.getActiveOrUnspecifiedChambers());
		for(ReactorElement element : elements) {
			if(element.parent != null && !element.parent.isGeneral() && element.parent.type > 0 && !element.parent.getPossibleSpecifications().contains(element.type)) continue;
			boolean canSet = true;
			for(ReactorElement currentElement : current) {
				if(currentElement.getId() == element.getId() && currentElement.getInfo().chamberRoot != element.getInfo().chamberRoot && element.getInfo().chamberRoot > 0 && currentElement.getInfo().chamberRoot > 0) {
					canSet = false;
					break;
				}
			}
			if(canSet) {
				if(element.type <= 0) continue;
				toSet.add(element);
			} else element.resetBooted();
		}
		for(ReactorElement element : toUnset) {
			if(element.getInfo().chamberRoot > 0 && !toSet.contains(element)) element.convertToClientRequest((short) element.getInfo().chamberRoot);
		}
		for(ReactorElement element : toSet) {
			if(element.type <= 0 || toUnset.contains(element) || element.isGeneral()) continue;
			element.setBooted();
			element.root = ((ManagedUsableSegmentController<?>) reactorTree.pw.getSegmentController()).getManagerContainer().getPowerInterface().getActiveReactor();
			element.convertToClientRequest(element.type);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isDefault() {
		return isDefault;
	}
	
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	public ObjectArrayList<ReactorElement> getElements() {
		return elements;
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("version", VERSION);
		json.put("name", name);
		json.put("isDefault", isDefault);
		JSONArray elementsArray = new JSONArray();
		for(ReactorElement element : elements) {
			elementsArray.put(element.toJson());
		}
		json.put("elements", elementsArray);
		return json;
	}

	@Override
	public void fromJson(JSONObject json) {
		byte version = (byte) json.getInt("version");
		name = json.getString("name");
		isDefault = json.getBoolean("isDefault");
		JSONArray elementsArray = json.getJSONArray("elements");
		for(int i = 0; i < elementsArray.length(); i++) {
			JSONObject elementJson = elementsArray.getJSONObject(i);
			ReactorElement element = new ReactorElement(elementJson);
			elements.add(element);
		}
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(VERSION);
		b.writeUTF(name);
		b.writeBoolean(isDefault);
		b.writeInt(elements.size());
		for(ReactorElement element : elements) element.serialize(b, isOnServer);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		byte version = b.readByte();
		name = b.readUTF();
		isDefault = b.readBoolean();
		int size = b.readInt();
		for(int i = 0; i < size; i++) {
			ReactorElement element = new ReactorElement();
			element.deserialize(b, updateSenderStateId, isOnServer);
			elements.add(element);
		}
	}
}
