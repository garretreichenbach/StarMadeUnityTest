package org.schema.game.common.data.element;

import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerCollectionManager;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerElementManager;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerUnit;

public interface ScannerManagerInterface {

	public ManagerModuleCollection<LongRangeScannerUnit, LongRangeScannerCollectionManager, LongRangeScannerElementManager> getLongRangeScanner();

}
