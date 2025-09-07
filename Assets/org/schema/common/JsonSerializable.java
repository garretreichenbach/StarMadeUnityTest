package org.schema.common;

import org.json.JSONObject;

public interface JsonSerializable {

	JSONObject toJson();

	void fromJson(JSONObject json);
}
