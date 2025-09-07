package org.schema.game.network.objects.valueUpdate;

import java.io.DataOutputStream;
import java.io.IOException;

public interface ValueUpdateInterface {

	void serializeValueUpdate(DataOutputStream buffer) throws IOException;

}
