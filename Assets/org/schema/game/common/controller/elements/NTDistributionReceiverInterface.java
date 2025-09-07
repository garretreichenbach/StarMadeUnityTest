package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.ReceivedDistribution;
import org.schema.schine.network.objects.NetworkEntity;

public interface NTDistributionReceiverInterface {

	public boolean receiveDistribution(ReceivedDistribution d, NetworkEntity networkObject);

}
